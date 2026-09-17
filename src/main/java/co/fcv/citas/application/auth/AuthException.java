package co.fcv.citas.application.auth;

public class AuthException extends RuntimeException {
    public enum Reason { EMAIL_EXISTS, DOCUMENT_EXISTS, INVALID_CREDENTIALS, INVALID_REFRESH, INACTIVE_USER }
    private final Reason reason;
    public AuthException(Reason reason) { super(reason.name()); this.reason = reason; }
    public Reason reason() { return reason; }
}
