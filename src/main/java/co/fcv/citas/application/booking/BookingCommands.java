package co.fcv.citas.application.booking;

import java.time.LocalDate;
import java.time.LocalDateTime;

public final class BookingCommands {
    private BookingCommands() { }

    public record AvailabilityQuery(long locationId, long specialtyId, long professionalId, LocalDate date) { }
    public record CreateAppointment(String patientUserId, long locationId, long specialtyId, long professionalId, LocalDateTime startAt) { }
    public record Decision(String adminUserId, long appointmentId, DecisionType type, String reason) { }
    public enum DecisionType { APPROVE, REJECT }
}
