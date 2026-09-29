package co.fcv.citas.application.booking;

import java.time.LocalDate;
import java.time.LocalDateTime;

public final class BookingCommands {
    private BookingCommands() { }

    public record AvailabilityQuery(long locationId, long specialtyId, long professionalId, LocalDate date) { }
    public record CreateAppointment(String patientUserId, long locationId, long specialtyId, long professionalId, LocalDateTime startAt) { }
    public record Decision(String adminUserId, long appointmentId, DecisionType type, String reason) { }
    public record CancelAppointment(String userId, long appointmentId) { }
    public record RequestReschedule(String userId, long appointmentId, long locationId, long specialtyId,
                                    long professionalId, LocalDateTime startAt) { }
    public record RescheduleDecision(String adminUserId, long rescheduleRequestId, DecisionType type, String reason) { }
    public record AgendaQuery(String professionalUserId, AgendaRange range, LocalDate date, Long locationId) { }
    public record CloseAppointment(String professionalUserId, long appointmentId, co.fcv.citas.domain.appointment.AppointmentStatus outcome) { }
    public enum DecisionType { APPROVE, REJECT }
    public enum AgendaRange { DAY, WEEK }
}
