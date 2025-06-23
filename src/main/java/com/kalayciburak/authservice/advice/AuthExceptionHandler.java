package com.kalayciburak.authservice.advice;

import com.kalayciburak.authservice.advice.exception.*;
import com.kalayciburak.commonjpa.advice.JPAExceptionHandler;
import com.kalayciburak.commonpackage.core.constant.Codes;
import com.kalayciburak.commonpackage.core.constant.Messages;
import com.kalayciburak.commonpackage.core.constant.Types;
import com.kalayciburak.commonpackage.core.response.error.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class AuthExceptionHandler extends JPAExceptionHandler {
    /**
     * {@code TokenTypeMismatchException} istisnasını yakalar.
     * <p>
     * Bu istisna, beklenen token türü ile alınan token türü eşleşmediğinde fırlatılır.
     *
     * @param exception Yakalanacak istisna.
     * @return Token türü uyuşmazlığı hakkında bilgi içeren {@link ResponseEntity}.
     */
    @ExceptionHandler(TokenTypeMismatchException.class)
    public ResponseEntity<ErrorResponse<?>> handleTokenTypeMismatchException(TokenTypeMismatchException exception) {
        var error = new ErrorResponse<>(
                Types.Exception.TOKEN_TYPE_MISMATCH,
                Codes.Auth.TOKEN_TYPE_MISMATCH,
                Messages.Auth.TOKEN_TYPE_MISMATCH,
                HttpStatus.BAD_REQUEST,
                exception);

        return buildResponseEntity(error);
    }

    /**
     * {@code TokenBlacklistedException} istisnasını yakalar.
     * <p>
     * Bu istisna, kara listeye alınmış bir token kullanıldığında fırlatılır.
     *
     * @param exception Yakalanacak istisna.
     * @return Token kara listeye alındığına dair bilgi içeren {@link ResponseEntity}.
     */
    @ExceptionHandler(TokenBlacklistedException.class)
    public ResponseEntity<ErrorResponse<?>> handleTokenBlacklistedException(TokenBlacklistedException exception) {
        var error = new ErrorResponse<>(
                Types.Exception.TOKEN_BLACKLISTED,
                Codes.Auth.TOKEN_BLACKLISTED,
                Messages.Auth.TOKEN_BLACKLISTED,
                HttpStatus.BAD_REQUEST,
                exception);

        return buildResponseEntity(error);
    }

    /**
     * {@code OldPasswordMismatchException} istisnasını yakalar.
     * <p>
     * Bu istisna, kullanıcının eski şifresiyle doğrulama yapılırken eşleşme olmadığında fırlatılır.
     *
     * @param exception Yakalanacak istisna.
     * @return Şifre eşleşmediğine dair bilgi içeren {@link ResponseEntity}.
     */
    @ExceptionHandler(OldPasswordMismatchException.class)
    public ResponseEntity<ErrorResponse<?>> handleOldPasswordMismatchException(OldPasswordMismatchException exception) {
        var error = new ErrorResponse<>(
                Types.Exception.OLD_PASSWORD_NOT_MATCH,
                Codes.Auth.OLD_PASSWORD_NOT_MATCH,
                Messages.Auth.OLD_PASSWORD_NOT_MATCH,
                HttpStatus.BAD_REQUEST,
                exception);

        return buildResponseEntity(error);
    }

    /**
     * {@code InvalidRoleIdsException} istisnasını yakalar.
     * <p>
     * Geçersiz veya sistemde bulunmayan rol ID’leri kullanıldığında fırlatılır.
     *
     * @param exception Yakalanacak istisna.
     * @return Geçersiz rol ID’si hatasını içeren {@link ResponseEntity}.
     */
    @ExceptionHandler(InvalidRoleIdsException.class)
    public ResponseEntity<ErrorResponse<?>> handleInvalidRoleIdsException(InvalidRoleIdsException exception) {
        var error = new ErrorResponse<>(
                Types.Exception.INVALID_ROLE_IDs,
                Codes.Auth.INVALID_ROLE_IDS,
                Messages.Auth.INVALID_ROLE_IDS,
                HttpStatus.BAD_REQUEST,
                exception);

        return buildResponseEntity(error);
    }

    /**
     * {@code InvalidJwtException} istisnasını yakalar.
     * <p>
     * Geçersiz veya süresi dolmuş JWT token kullanıldığında fırlatılır.
     *
     * @param exception Yakalanacak istisna.
     * @return JWT geçersizliği hakkında bilgi içeren {@link ResponseEntity}.
     */
    @ExceptionHandler(InvalidJwtException.class)
    public ResponseEntity<ErrorResponse<?>> handleInvalidJwtException(InvalidJwtException exception) {
        var error = new ErrorResponse<>(
                Types.Exception.INVALID_JWT,
                Codes.Auth.INVALID_JWT,
                Messages.Auth.INVALID_JWT,
                HttpStatus.UNAUTHORIZED,
                exception);

        return buildResponseEntity(error);
    }

    /**
     * {@code BreachedPasswordException} istisnasını yakalar.
     * <p>
     * Eğer kullanıcı sızdırılmış bir şifre kullanmaya çalışıyorsa fırlatılır.
     *
     * @param exception Yakalanacak istisna.
     * @return Şifre güvenliği hakkında bilgi içeren {@link ResponseEntity}.
     */
    @ExceptionHandler(BreachedPasswordException.class)
    public ResponseEntity<ErrorResponse<?>> handleBreachedPasswordException(BreachedPasswordException exception) {
        var error = new ErrorResponse<>(
                Types.Exception.BREACHED_PASSWORD,
                Codes.Auth.BREACHED_PASSWORD,
                Messages.Auth.BREACHED_PASSWORD,
                HttpStatus.BAD_REQUEST,
                exception);

        return buildResponseEntity(error);
    }

    /**
     * {@code AdminCannotBeDeletedException} istisnasını yakalar.
     * <p>
     * Eğer bir admin kullanıcısı silinmeye çalışılıyorsa fırlatılır.
     *
     * @param exception Yakalanacak istisna.
     * @return Admin silme girişiminin reddedildiğini belirten {@link ResponseEntity}.
     */
    @ExceptionHandler(AdminCannotBeDeletedException.class)
    public ResponseEntity<ErrorResponse<?>> handleAdminCannotBeDeletedException(
            AdminCannotBeDeletedException exception) {
        var error = new ErrorResponse<>(
                Types.Exception.ADMIN_CANNOT_BE_DELETED,
                Codes.Auth.ADMIN_CANNOT_BE_DELETED,
                Messages.Auth.ADMIN_CANNOT_BE_DELETED,
                HttpStatus.FORBIDDEN,
                exception);

        return buildResponseEntity(error);
    }

    /**
     * {@code BadCredentialsException} istisnasını yakalar.
     * <p>
     * Kullanıcı adı veya şifre hatalı girildiğinde fırlatılır.
     *
     * @param exception Yakalanacak istisna.
     * @return Geçersiz kimlik bilgileri hakkında bilgi içeren {@link ResponseEntity}.
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse<?>> handleBadCredentialsException(BadCredentialsException exception) {
        var error = new ErrorResponse<>(
                Types.Exception.UNAUTHORIZED,
                Codes.Auth.UNAUTHORIZED,
                Messages.Auth.INVALID_CREDENTIALS,
                HttpStatus.UNAUTHORIZED,
                exception);

        return buildResponseEntity(error);
    }

    /**
     * {@code AuthorizationDeniedException} istisnasını yakalar.
     * <p>
     * Kullanıcının belirli bir kaynağa erişim yetkisi yoksa fırlatılır.
     *
     * @param exception Yakalanacak istisna.
     * @return Yetki reddi hakkında bilgi içeren {@link ResponseEntity}.
     */
    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ErrorResponse<?>> handleAuthorizationDeniedException(AuthorizationDeniedException exception) {
        var error = new ErrorResponse<>(
                Types.Exception.ACCESS_DENIED,
                Codes.Auth.ACCESS_DENIED,
                Messages.Auth.ACCESS_DENIED,
                HttpStatus.FORBIDDEN,
                exception);

        return buildResponseEntity(error);
    }

    /**
     * {@code EmailAlreadyExistsException} istisnasını yakalar.
     * <p>
     * Bu istisna, kayıt sırasında email adresi zaten kullanımdaysa fırlatılır.
     *
     * @param exception Yakalanacak istisna.
     * @return Email kullanımda hatasını içeren {@link ResponseEntity}.
     */
    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse<?>> handleEmailAlreadyExistsException(EmailAlreadyExistsException exception) {
        var error = new ErrorResponse<>(
                Types.Exception.ENTITY_EXISTS,
                Codes.Auth.CONFLICT,
                exception.getMessage(),
                HttpStatus.CONFLICT,
                exception);

        return buildResponseEntity(error);
    }

    /**
     * {@code EmailNotVerifiedException} istisnasını yakalar.
     * <p>
     * Bu istisna, kullanıcı email doğrulaması yapmadan giriş yapmaya çalıştığında fırlatılır.
     *
     * @param exception Yakalanacak istisna.
     * @return Email doğrulanmamış hatasını içeren {@link ResponseEntity}.
     */
    @ExceptionHandler(EmailNotVerifiedException.class)
    public ResponseEntity<ErrorResponse<?>> handleEmailNotVerifiedException(EmailNotVerifiedException exception) {
        var error = new ErrorResponse<>(
                Types.Exception.EMAIL_NOT_VERIFIED,
                Codes.Auth.EMAIL_NOT_VERIFIED,
                Messages.Auth.EMAIL_NOT_VERIFIED,
                HttpStatus.FORBIDDEN,
                exception);

        return buildResponseEntity(error);
    }

    /**
     * {@code InvalidVerificationTokenException} istisnasını yakalar.
     * <p>
     * Bu istisna, geçersiz veya süresi dolmuş doğrulama token'ı kullanıldığında fırlatılır.
     *
     * @param exception Yakalanacak istisna.
     * @return Geçersiz token hatasını içeren {@link ResponseEntity}.
     */
    @ExceptionHandler(InvalidVerificationTokenException.class)
    public ResponseEntity<ErrorResponse<?>> handleInvalidVerificationTokenException(
            InvalidVerificationTokenException exception) {
        var error = new ErrorResponse<>(
                Types.Exception.INVALID_VERIFICATION_TOKEN,
                Codes.Auth.INVALID_VERIFICATION_TOKEN,
                Messages.Auth.INVALID_VERIFICATION_TOKEN,
                HttpStatus.BAD_REQUEST,
                exception);

        return buildResponseEntity(error);
    }
}