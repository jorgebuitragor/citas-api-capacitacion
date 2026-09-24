package co.fcv.citas.application.booking;

public final class BookingException extends RuntimeException {
    public enum Reason { INVALID_REQUEST, SLOT_UNAVAILABLE, NOT_FOUND, INVALID_STATE }
    private final Reason reason;
    public BookingException(Reason reason, String message) { super(message); this.reason = reason; }
    public Reason reason() { return reason; }
}
