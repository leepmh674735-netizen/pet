package com.pet.backend.member;

import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.pet.backend.common.ApiResponse;
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

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class MemberController {

	private final MemberService memberService;
	private final RefreshTokenCookie refreshTokenCookie;

	@PostMapping("/api/members/signup")
	@ResponseStatus(HttpStatus.CREATED)
	public ApiResponse<MemberResponse> signup(@Valid @RequestBody SignupRequest request) {
		return ApiResponse.ok(memberService.signup(request));
	}

	@PostMapping("/api/members/login")
	public ResponseEntity<ApiResponse<LoginResponse>> login(
			@Valid @RequestBody LoginRequest request,
			@CookieValue(name = RefreshTokenCookie.NAME, required = false) String priorRefreshToken,
			@RequestHeader(name = HttpHeaders.USER_AGENT, required = false) String userAgent) {
		LoginResult result = memberService.login(request, priorRefreshToken, DeviceInfoParser.parse(userAgent));
		return ResponseEntity.ok()
				.header(HttpHeaders.SET_COOKIE, refreshTokenCookie.create(result.refreshToken()).toString())
				.body(ApiResponse.ok(result.response()));
	}

	@PostMapping("/api/members/login/kakao")
	public ResponseEntity<ApiResponse<LoginResponse>> kakaoLogin(
			@Valid @RequestBody KakaoLoginRequest request,
			@CookieValue(name = RefreshTokenCookie.NAME, required = false) String priorRefreshToken,
			@RequestHeader(name = HttpHeaders.USER_AGENT, required = false) String userAgent) {
		LoginResult result = memberService.kakaoLogin(request, priorRefreshToken, DeviceInfoParser.parse(userAgent));
		return ResponseEntity.ok()
				.header(HttpHeaders.SET_COOKIE, refreshTokenCookie.create(result.refreshToken()).toString())
				.body(ApiResponse.ok(result.response()));
	}

	@PostMapping("/api/members/refresh")
	public ResponseEntity<ApiResponse<TokenResponse>> refresh(
			@CookieValue(name = RefreshTokenCookie.NAME, required = false) String refreshToken) {
		RefreshResult result = memberService.refresh(refreshToken);
		return ResponseEntity.ok()
				.header(HttpHeaders.SET_COOKIE, refreshTokenCookie.create(result.refreshToken()).toString())
				.body(ApiResponse.ok(result.response()));
	}

	@PostMapping("/api/members/logout")
	public ResponseEntity<ApiResponse<Void>> logout(
			@CookieValue(name = RefreshTokenCookie.NAME, required = false) String refreshToken) {
		memberService.logout(refreshToken);
		return ResponseEntity.ok()
				.header(HttpHeaders.SET_COOKIE, refreshTokenCookie.expire().toString())
				.body(ApiResponse.ok());
	}

	@GetMapping("/api/members/me")
	public ApiResponse<MemberResponse> getMyInfo(@AuthenticationPrincipal Long memberId) {
		return ApiResponse.ok(memberService.getMyInfo(memberId));
	}

	@PatchMapping("/api/members/me")
	public ApiResponse<MemberResponse> updateName(
			@AuthenticationPrincipal Long memberId,
			@Valid @RequestBody NameUpdateRequest request) {
		return ApiResponse.ok(memberService.updateName(memberId, request));
	}

	@PostMapping("/api/members/me/image")
	public ApiResponse<MemberResponse> uploadProfileImage(
			@AuthenticationPrincipal Long memberId,
			@RequestPart("file") MultipartFile file) {
		return ApiResponse.ok(memberService.uploadProfileImage(memberId, file));
	}

	@PatchMapping("/api/members/me/password")
	public ResponseEntity<ApiResponse<Void>> changePassword(
			@AuthenticationPrincipal Long memberId,
			@Valid @RequestBody PasswordChangeRequest request,
			@RequestHeader(name = HttpHeaders.USER_AGENT, required = false) String userAgent) {
		String refreshToken = memberService.changePassword(memberId, request, DeviceInfoParser.parse(userAgent));
		return ResponseEntity.ok()
				.header(HttpHeaders.SET_COOKIE, refreshTokenCookie.create(refreshToken).toString())
				.body(ApiResponse.ok());
	}

	@PostMapping("/api/members/me/withdraw")
	public ResponseEntity<ApiResponse<Void>> withdraw(
			@AuthenticationPrincipal Long memberId,
			@RequestBody WithdrawRequest request) {
		memberService.withdraw(memberId, request);
		return ResponseEntity.ok()
				.header(HttpHeaders.SET_COOKIE, refreshTokenCookie.expire().toString())
				.body(ApiResponse.ok());
	}

	@GetMapping("/api/members/me/sessions")
	public ApiResponse<List<SessionResponse>> getSessions(
			@AuthenticationPrincipal Long memberId,
			@CookieValue(name = RefreshTokenCookie.NAME, required = false) String refreshToken) {
		return ApiResponse.ok(memberService.getSessions(memberId, refreshToken));
	}

	@DeleteMapping("/api/members/me/sessions/{sessionId}")
	public ApiResponse<Void> revokeSession(
			@AuthenticationPrincipal Long memberId,
			@PathVariable String sessionId,
			@CookieValue(name = RefreshTokenCookie.NAME, required = false) String refreshToken) {
		memberService.revokeSession(memberId, sessionId, refreshToken);
		return ApiResponse.ok();
	}
}