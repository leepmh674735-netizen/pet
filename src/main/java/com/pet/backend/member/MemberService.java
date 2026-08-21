package com.pet.backend.member;

import java.io.IOException;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.pet.backend.chat.ChatLeftReason;
import com.pet.backend.chat.ChatRoomMemberRepository;
import com.pet.backend.common.BusinessException;
import com.pet.backend.common.CommonErrorCode;
import com.pet.backend.common.ImageStorageClient;
import com.pet.backend.member.dto.KakaoLoginRequest;
import com.pet.backend.member.dto.LoginRequest;
import com.pet.backend.member.dto.LoginResponse;
import com.pet.backend.member.dto.MemberResponse;
import com.pet.backend.member.dto.NameUpdateRequest;
import com.pet.backend.member.dto.PasswordChangeRequest;
import com.pet.backend.member.dto.SessionResponse;
import com.pet.backend.member.dto.SignupRequest;
import com.pet.backend.member.dto.TokenResponse;
import com.pet.backend.member.dto.WithdrawRequest;
import com.pet.backend.security.JwtTokenProvider;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberService {

	static final String WITHDRAW_CONFIRM_PHRASE = "탈퇴합니다.";

	private final MemberRepository memberRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtTokenProvider jwtTokenProvider;
	private final RefreshTokenService refreshTokenService;
	private final KakaoOAuthClient kakaoOAuthClient;
	private final ImageStorageClient imageStorageClient;
	private final MemberProfileImageUpdater memberProfileImageUpdater;
	private final ChatRoomMemberRepository chatRoomMemberRepository;

	@Transactional
	public MemberResponse signup(SignupRequest request) {
		String email = normalizeEmail(request.email());
		if (memberRepository.existsActiveByNormalizedEmail(email)) {
			throw new BusinessException(MemberErrorCode.EMAIL_DUPLICATED);
		}
		Member member = Member.createLocalMember(email, passwordEncoder.encode(request.password()), request.name());
		try {
			memberRepository.save(member);
		} catch (DataIntegrityViolationException e) {
			throw new BusinessException(MemberErrorCode.EMAIL_DUPLICATED);
		}
		return MemberResponse.from(member);
	}

	@Transactional
	public LoginResult login(LoginRequest request, String priorRefreshToken, String deviceInfo) {
		Member member = memberRepository.findActiveByNormalizedEmail(normalizeEmail(request.email()))
				.orElseThrow(() -> new BusinessException(MemberErrorCode.INVALID_CREDENTIALS));

		if (member.getPassword() == null || !matchesSafely(request.password(), member.getPassword())) {
			throw new BusinessException(MemberErrorCode.INVALID_CREDENTIALS);
		}
		return issueLoginTokens(member, priorRefreshToken, deviceInfo);
	}

	@Transactional
	public LoginResult kakaoLogin(KakaoLoginRequest request, String priorRefreshToken, String deviceInfo) {
		KakaoOAuthClient.KakaoUserInfo userInfo = kakaoOAuthClient.fetchUser(request.code(), request.redirectUri());
		Member member = memberRepository.findByProviderAndProviderIdAndDeletedAtIsNull(Provider.KAKAO, userInfo.providerId())
				.orElseGet(() -> registerKakaoMember(userInfo));
		return issueLoginTokens(member, priorRefreshToken, deviceInfo);
	}

	private Member registerKakaoMember(KakaoOAuthClient.KakaoUserInfo userInfo) {
		String email = normalizeEmail(userInfo.email());

		if (email != null && memberRepository.existsActiveByNormalizedEmail(email)) {
			throw new BusinessException(MemberErrorCode.SOCIAL_EMAIL_CONFLICT);
		}
		String name = (userInfo.nickname() == null || userInfo.nickname().isBlank()) ? "카카오 회원"
				: userInfo.nickname().trim();
		if (name.length() > 50) {
			name = name.substring(0, 50);
		}
		try {
			return memberRepository.save(Member.createKakaoMember(email, name, userInfo.providerId()));
		} catch (DataIntegrityViolationException e) {
			return memberRepository.findByProviderAndProviderIdAndDeletedAtIsNull(Provider.KAKAO, userInfo.providerId())
					.orElseGet(() -> {
						if (email != null && memberRepository.existsActiveByNormalizedEmail(email)) {
							throw new BusinessException(MemberErrorCode.SOCIAL_EMAIL_CONFLICT);
						}
						throw e;
					});
		}
	}

	private LoginResult issueLoginTokens(Member member, String priorRefreshToken, String deviceInfo) {
		refreshTokenService.revokeReplaceByLogin(priorRefreshToken);
		String accessToken = jwtTokenProvider.createAccessToken(member.getId(), member.getRole());
		String refreshToken = refreshTokenService.issue(member.getId(), deviceInfo);
		return new LoginResult(LoginResponse.of(accessToken, jwtTokenProvider.expirationSeconds(), member),
				refreshToken);
	}

	@Transactional
	public RefreshResult refresh(String rawRefreshToken) {
		RefreshToken token = refreshTokenService.findUsableOrThrow(rawRefreshToken);

		Member member = memberRepository.findByIdForShare(token.getMemberId()).filter(m -> !m.isDeleted())
				.orElseThrow(() -> new BusinessException(MemberErrorCode.INVALID_REFRESH_TOKEN));

		if (member.isTokenInvalidated(token.getCreatedAt())) {
			throw new BusinessException(MemberErrorCode.INVALID_REFRESH_TOKEN);
		}

		String newRawToken = refreshTokenService.rotate(token);
		String accessToken = jwtTokenProvider.createAccessToken(member.getId(), member.getRole());
		return new RefreshResult(TokenResponse.of(accessToken, jwtTokenProvider.expirationSeconds()), newRawToken);
	}

	@Transactional
	public void logout(String rawRefreshToken) {
		refreshTokenService.revoke(rawRefreshToken);
	}

	@Transactional
	public String changePassword(Long memberId, PasswordChangeRequest request, String deviceInfo) {
		Member member = findActiveMemberOrThrow(memberId);
		if (member.getPassword() == null || !matchesSafely(request.currentPassword(), member.getPassword())) {
			throw new BusinessException(MemberErrorCode.INVALID_CREDENTIALS);
		}

		if (matchesSafely(request.newPassword(), member.getPassword())) {
			throw new BusinessException(MemberErrorCode.PASSWORD_UNCHANGED);
		}
		member.changePassword(passwordEncoder.encode(request.newPassword()));

		return refreshTokenService.reissueAfterPasswordChange(memberId, deviceInfo);
	}

	@Transactional(readOnly = true)
	public List<SessionResponse> getSessions(Long memberId, String rawRefreshToken) {
		findActiveMemberOrThrow(memberId);
		UUID currentSessionId = refreshTokenService.findSessionIdOf(rawRefreshToken);
		Map<UUID, List<RefreshToken>> chains = refreshTokenService.findActiveTokens(memberId).stream()
				.filter(token -> !token.isExpired())
				.collect(Collectors.groupingBy(RefreshToken::getSessionId));

		return chains.entrySet().stream()
				.map(entry -> SessionResponse.of(entry.getValue(), entry.getKey().equals(currentSessionId)))
				.sorted(Comparator.comparing(SessionResponse::current, Comparator.reverseOrder())
						.thenComparing(SessionResponse::lastUsedAt, Comparator.reverseOrder()))
				.toList();
	}

	@Transactional
	public void revokeSession(Long memberId, String rawSessionId, String rawRefreshToken) {
		findActiveMemberOrThrow(memberId);
		UUID sessionId;
		try {
			sessionId = UUID.fromString(rawSessionId);
		} catch (IllegalArgumentException e) {
			throw new BusinessException(MemberErrorCode.SESSION_NOT_FOUND);
		}
		if (sessionId.equals(refreshTokenService.findSessionIdOf(rawRefreshToken))) {
			throw new BusinessException(MemberErrorCode.SESSION_CURRENT);
		}

		if (refreshTokenService.revokeSession(memberId, sessionId) == 0) {
			throw new BusinessException(MemberErrorCode.SESSION_NOT_FOUND);
		}
	}

	@Transactional
	public void withdraw(Long memberId, WithdrawRequest request) {
		Member member = findActiveMemberOrThrow(memberId);
		verifyWithdrawIdentity(member, request);

		if (chatRoomMemberRepository.existsActiveOwnedRoom(memberId)) {
			throw new BusinessException(MemberErrorCode.WITHDRAW_CHAT_OWNER);
		}

		member.withdraw();
		chatRoomMemberRepository.leaveAllByMemberId(memberId, Instant.now(), ChatLeftReason.LEFT);
		refreshTokenService.revokeAllOnWithdraw(memberId);
	}

	private void verifyWithdrawIdentity(Member member, WithdrawRequest request) {
		if (member.getProvider() == Provider.LOCAL) {
			if (member.getPassword() == null || !matchesSafely(request.password(), member.getPassword())) {
				throw new BusinessException(MemberErrorCode.INVALID_CREDENTIALS);
			}
			return;
		}
		if (!WITHDRAW_CONFIRM_PHRASE.equals(request.confirmPhrase())) {
			throw new BusinessException(CommonErrorCode.VALIDATION_ERROR,
					"확인 문구가 일치하지 않습니다. \"" + WITHDRAW_CONFIRM_PHRASE + "\"를 입력해 주세요.");
		}
	}

	private boolean matchesSafely(String rawPassword, String encodePassword) {
		try {
			return passwordEncoder.matches(rawPassword, encodePassword);
		} catch (IllegalArgumentException e) {
			return false;
		}
	}

	public MemberResponse uploadProfileImage(Long memberId, MultipartFile file) {
		imageStorageClient.validateImage(file);

		findActiveMemberOrThrow(memberId);

		byte[] bytes;
		try {
			bytes = file.getBytes();
		} catch (IOException e) {
			throw new BusinessException(CommonErrorCode.IMAGE_UPLOAD_FAILED);
		}

		String url = imageStorageClient.upload("member-" + memberId, bytes, file.getContentType());
		Member member = memberProfileImageUpdater.apply(memberId, url + "?v=" + Instant.now().toEpochMilli());
		return MemberResponse.from(member);
	}

	private String normalizeEmail(String email) {
		if (email == null || email.isBlank()) {
			return null;
		}
		return email.trim().toLowerCase(Locale.ROOT);
	}

	private Member findActiveMemberOrThrow(Long memberId) {
		return memberRepository.findByIdAndDeletedAtIsNull(memberId)
				.orElseThrow(() -> new BusinessException(MemberErrorCode.NOT_FOUND));
	}

	@Transactional
	public MemberResponse updateName(Long memberId, NameUpdateRequest request) {
		Member member = findActiveMemberOrThrow(memberId);
		member.changeName(request.name().trim());
		return MemberResponse.from(member);
	}

	@Transactional(readOnly = true)
	public MemberResponse getMyInfo(Long memberId) {
		Member member = findActiveMemberOrThrow(memberId);
		return MemberResponse.from(member);
	}
}