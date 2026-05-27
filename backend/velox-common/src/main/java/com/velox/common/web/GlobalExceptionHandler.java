package com.velox.common.web;

import com.velox.common.exception.ApiException;
import com.velox.common.exception.BusinessErrorCode;
import com.velox.common.exception.ClientErrorCode;
import com.velox.common.exception.InternalErrorCode;
import com.velox.common.result.Result;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ApiException.class)
    public Result<Void> handleApiException(ApiException exception, HttpServletRequest request) {
        log.warn("Business exception [{}] {}: {}", request.getMethod(), request.getRequestURI(), exception.getMessage());
        if (exception.getPayload() != null) {
            log.warn("Exception payload: {}", exception.getPayload());
        }
        return Result.fail(exception.getErrorCode(), exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleValidationException(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("Validation error: {}", message);
        return Result.fail(ClientErrorCode.VALIDATION_ERROR, message);
    }

    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleBindException(BindException exception) {
        String message = exception.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("Bind error: {}", message);
        return Result.fail(ClientErrorCode.VALIDATION_ERROR, message);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleMissingParamException(MissingServletRequestParameterException exception) {
        log.warn("Missing parameter: {}", exception.getParameterName());
        return Result.fail(ClientErrorCode.BAD_REQUEST, "Missing request parameter: " + exception.getParameterName());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public Result<Void> handleMethodNotAllowed(HttpRequestMethodNotSupportedException exception) {
        log.warn("Method not allowed: {} {}", exception.getMethod(), exception.getMessage());
        return Result.fail(ClientErrorCode.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result<Void> handleNotFoundException(NoHandlerFoundException exception) {
        log.warn("Not found: {} {}", exception.getHttpMethod(), exception.getRequestURL());
        return Result.fail(ClientErrorCode.NOT_FOUND);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result<Void> handleNoResourceFoundException(NoResourceFoundException exception,
                                                       HttpServletRequest request) {
        log.warn("Not found: {} {}", request.getMethod(), request.getRequestURI());
        return Result.fail(ClientErrorCode.NOT_FOUND);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleIllegalArgumentException(IllegalArgumentException exception) {
        log.warn("Illegal argument: {}", exception.getMessage());
        return Result.fail(ClientErrorCode.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(S3Exception.class)
    public Result<Void> handleS3Exception(S3Exception exception, HttpServletRequest request) {
        String errorCode = exception.awsErrorDetails() != null
                ? exception.awsErrorDetails().errorCode()
                : "Unknown";
        String friendlyMessage = resolveS3ErrorMessage(errorCode, exception.getMessage());
        HttpStatus status = HttpStatus.resolve(exception.statusCode());
        if (status == null) {
            status = HttpStatus.BAD_GATEWAY;
        }
        log.warn("S3 error [{}] {}: code={}, status={}, message={}",
                request.getMethod(), request.getRequestURI(), errorCode, exception.statusCode(), exception.getMessage());
        return Result.fail(BusinessErrorCode.FILE_STORAGE_ERROR, friendlyMessage);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleException(Exception exception, HttpServletRequest request) {
        log.error("Unexpected error [{} {}]: {}",
                request.getMethod(), request.getRequestURI(), exception.getMessage(), exception);
        return Result.fail(InternalErrorCode.INTERNAL_ERROR);
    }

    private String resolveS3ErrorMessage(String errorCode, String originalMessage) {
        return switch (errorCode) {
            case "AccessDenied" -> "S3 access denied. Check access key and bucket policy.";
            case "UserDisable" -> "S3 account is disabled.";
            case "InvalidAccessKeyId" -> "Invalid S3 access key.";
            case "SignatureDoesNotMatch" -> "S3 signature mismatch. Check secret key.";
            case "NoSuchBucket" -> "S3 bucket does not exist.";
            case "NoSuchKey" -> "S3 object does not exist.";
            case "BucketAlreadyExists" -> "S3 bucket already exists.";
            case "InvalidBucketName" -> "Invalid S3 bucket name.";
            case "RequestTimeTooSkewed" -> "S3 request time skew is too large.";
            case "ServiceUnavailable" -> "S3 service is temporarily unavailable.";
            case "SlowDown" -> "S3 request rate is too high.";
            case "InternalError" -> "S3 internal server error.";
            default -> "S3 error: " + (originalMessage != null ? originalMessage : errorCode);
        };
    }
}
