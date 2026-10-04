package co.fcv.citas.application.automation;

public final class AutomationException extends RuntimeException {
    public enum Reason { INVALID_REQUEST, NOT_FOUND, DUPLICATE }
    private final Reason reason;
    public AutomationException(Reason reason, String message) { super(message); this.reason = reason; }
    public Reason reason() { return reason; }
}
