package co.fcv.citas.domain.appointment;

public enum RescheduleStatus {
    PENDING, APPROVED, REJECTED, CANCELLED;

    public boolean terminal() { return this != PENDING; }
}
