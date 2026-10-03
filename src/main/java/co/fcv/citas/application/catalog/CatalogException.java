package co.fcv.citas.application.catalog;

public final class CatalogException extends RuntimeException {
    public enum Reason { INVALID_REQUEST, NOT_FOUND, DUPLICATE_CODE }
    private final Reason reason;
    public CatalogException(Reason reason, String message) { super(message); this.reason = reason; }
    public Reason reason() { return reason; }
}
