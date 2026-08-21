package com.pet.backend.common;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.core.convert.ConversionFailedException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<ApiResponse<Void>> handleBusiness(BusinessException e) {
		ErrorCode code = e.getErrorCode();
		return ResponseEntity.status(code.getStatus()).body(ApiResponse.fail(code.getCode(), e.getMessage()));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiResponse<Void>> handleValidate(MethodArgumentNotValidException e) {
		Map<String, String> details = e.getBindingResult().getFieldErrors().stream()
				.collect(Collectors.toMap(
						FieldError::getField,
						error -> error.getDefaultMessage() == null
								? CommonErrorCode.VALIDATION_ERROR.getDefaultMessage()
								: error.getDefaultMessage(),
						(first, ignored) -> first,
						LinkedHashMap::new
				));
		String message = details.entrySet().stream()
				.map(entry -> entry.getKey() + ": " + entry.getValue())
				.collect(Collectors.joining(", "));
		return ResponseEntity.status(CommonErrorCode.VALIDATION_ERROR.getStatus())
				.body(ApiResponse.fail(CommonErrorCode.VALIDATION_ERROR.getCode(), message, details));
	}

	@ExceptionHandler(HandlerMethodValidationException.class)
	public ResponseEntity<ApiResponse<Void>> handleMethodValidation(HandlerMethodValidationException e) {
		String message = e.getAllErrors().stream()
				.map(error -> error.getDefaultMessage())
				.filter(msg -> msg != null && !msg.isBlank())
				.collect(Collectors.joining(", "));
		return ResponseEntity.status(CommonErrorCode.VALIDATION_ERROR.getStatus())
				.body(ApiResponse.fail(CommonErrorCode.VALIDATION_ERROR.getCode(),
						message.isBlank() ? CommonErrorCode.VALIDATION_ERROR.getDefaultMessage() : message));
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(ConstraintViolationException e) {
		String message = e.getConstraintViolations().stream()
				.map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
				.collect(Collectors.joining(", "));
		return ResponseEntity.status(CommonErrorCode.VALIDATION_ERROR.getStatus())
				.body(ApiResponse.fail(CommonErrorCode.VALIDATION_ERROR.getCode(), message));
	}

	@ExceptionHandler(MissingServletRequestParameterException.class)
	public ResponseEntity<ApiResponse<Void>> handleMissingParam(MissingServletRequestParameterException e) {
		return ResponseEntity.status(CommonErrorCode.VALIDATION_ERROR.getStatus())
				.body(ApiResponse.fail(CommonErrorCode.VALIDATION_ERROR.getCode(), e.getParameterName() + "는 필수입니다."));
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
		Class<?> enumType = enumTargetType(e);
		String message;
		if (enumType != null) {
			String allowedValues = Arrays.stream(enumType.getEnumConstants())
					.map(Object::toString)
					.collect(Collectors.joining(", "));
			message = e.getName() + "는 " + allowedValues + " 중 하나여야 합니다.";
		} else {
			message = e.getName() + "의 형식이 올바르지 않습니다.";
		}

		return ResponseEntity.status(CommonErrorCode.VALIDATION_ERROR.getStatus())
				.body(ApiResponse.fail(CommonErrorCode.VALIDATION_ERROR.getCode(), message));
	}

	private Class<?> enumTargetType(MethodArgumentTypeMismatchException e) {
		Class<?> requiredType = e.getRequiredType();
		if (requiredType != null && requiredType.isEnum()) {
			return requiredType;
		}
		// [수정] cause = cause.getCause() 재할당으로 무한 루프 수정
		for (Throwable cause = e.getCause(); cause != null; cause = cause.getCause()) {
			if (cause instanceof ConversionFailedException cfe && cfe.getTargetType().getType().isEnum()) {
				return cfe.getTargetType().getType();
			}
		}
		return null;
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ApiResponse<Void>> handleUnreadable(HttpMessageNotReadableException e) {
		return ResponseEntity.status(CommonErrorCode.VALIDATION_ERROR.getStatus())
				.body(ApiResponse.fail(CommonErrorCode.VALIDATION_ERROR.getCode(), "요청 본문을 읽을 수 없습니다."));
	}

	@ExceptionHandler(NoResourceFoundException.class)
	public ResponseEntity<ApiResponse<Void>> handleNoResource(NoResourceFoundException e) {
		return ResponseEntity.status(CommonErrorCode.NOT_FOUND.getStatus()).body(
				ApiResponse.fail(CommonErrorCode.NOT_FOUND.getCode(), CommonErrorCode.NOT_FOUND.getDefaultMessage()));
	}

	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public ResponseEntity<ApiResponse<Void>> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
		return ResponseEntity.status(CommonErrorCode.METHOD_NOT_ALLOWED.getStatus()).body(
				ApiResponse.fail(CommonErrorCode.METHOD_NOT_ALLOWED.getCode(), CommonErrorCode.METHOD_NOT_ALLOWED.getDefaultMessage()));
	}

	@ExceptionHandler(MissingServletRequestPartException.class)
	public ResponseEntity<ApiResponse<Void>> handleMissingPart(MissingServletRequestPartException e) {
		return ResponseEntity.status(CommonErrorCode.VALIDATION_ERROR.getStatus()).body(
				ApiResponse.fail(CommonErrorCode.VALIDATION_ERROR.getCode(), e.getRequestPartName() + " 파일을 첨부해 주세요."));
	}

	@ExceptionHandler(MaxUploadSizeExceededException.class)
	public ResponseEntity<ApiResponse<Void>> handleMaxUploadSize(MaxUploadSizeExceededException e) {
		return ResponseEntity.status(CommonErrorCode.VALIDATION_ERROR.getStatus())
				.body(ApiResponse.fail(CommonErrorCode.VALIDATION_ERROR.getCode(), "파일 용량이 너무 큽니다."));
	}

	// [수정] 파라미터 타입 MultipartException으로 수정
	@ExceptionHandler(MultipartException.class)
	public ResponseEntity<ApiResponse<Void>> handleMultipart(MultipartException e) {
		return ResponseEntity.status(CommonErrorCode.VALIDATION_ERROR.getStatus())
				.body(ApiResponse.fail(CommonErrorCode.VALIDATION_ERROR.getCode(), "파일 업로드 요청이 올바르지 않습니다."));
	}

	@ExceptionHandler(ObjectOptimisticLockingFailureException.class)
	public ResponseEntity<ApiResponse<Void>> handleOptimisticLock(ObjectOptimisticLockingFailureException e) {
		return ResponseEntity.status(CommonErrorCode.CONCURRENT_UPDATE.getStatus()).body(ApiResponse.fail(
				CommonErrorCode.CONCURRENT_UPDATE.getCode(), CommonErrorCode.CONCURRENT_UPDATE.getDefaultMessage()));
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ApiResponse<Void>> handleDataIntegrity(DataIntegrityViolationException e) {
		log.error("DB 제약 위반 - 도메인이 흡수하지 못한 경로", e);
		return ResponseEntity.status(CommonErrorCode.CONCURRENT_UPDATE.getStatus()).body(ApiResponse.fail(
				CommonErrorCode.CONCURRENT_UPDATE.getCode(), CommonErrorCode.CONCURRENT_UPDATE.getDefaultMessage()));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<Void>> handleUnknown(Exception e) {
		log.error("처리되지 않은 예외", e);
		return ResponseEntity.status(CommonErrorCode.INTERNAL_ERROR.getStatus()).body(ApiResponse
				.fail(CommonErrorCode.INTERNAL_ERROR.getCode(), CommonErrorCode.INTERNAL_ERROR.getDefaultMessage()));
	}
}