package co.fcv.citas.application.booking;

import co.fcv.citas.domain.appointment.AppointmentStatus;
import co.fcv.citas.domain.appointment.RescheduleStatus;
import co.fcv.citas.domain.appointment.StatusSource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public final class BookingPorts {
    private BookingPorts() { }

    public interface BookingRepository {
        List<Location> findActiveLocations();
        List<Specialty> findActiveSpecialties();
        List<Professional> findProfessionals(long locationId, long specialtyId);
        Optional<ReservationContext> findReservationContext(long locationId, long specialtyId, long professionalId);
        boolean isActiveUser(String userId);
        List<Slot> findSlots(long professionalId, long locationId, LocalDate date, boolean lock);
        long createAppointment(NewAppointment appointment);
        void assignSlots(long appointmentId, List<Long> slotIds);
        void addHistory(long appointmentId, AppointmentStatus status, String actorUserId, StatusSource source, String reason);
        Optional<PendingAppointment> lockRequestedAppointment(long appointmentId);
        void approve(long appointmentId, String adminUserId, LocalDateTime approvedAt);
        void reject(long appointmentId);
        void releaseSlots(long appointmentId);
        List<PendingAppointment> findPending(Long locationId, Long professionalId, Long specialtyId, LocalDate date);
        List<MyAppointment> findMyAppointments(String userId, AppointmentStatus status, LocalDate date);
        Optional<MyAppointment> lockAppointmentForUser(long appointmentId, String userId);
        void cancel(long appointmentId);
        Optional<Long> findProfessionalId(String userId);
        List<AgendaAppointment> findAgenda(long professionalId, LocalDateTime rangeStart, LocalDateTime rangeEndExclusive, Long locationId);
        Optional<AgendaAppointment> lockAppointmentForProfessional(long appointmentId, long professionalId);
        void close(long appointmentId, AppointmentStatus outcome);

        List<RescheduleRequest> findLatestRescheduleRequests(List<Long> appointmentIds);
        boolean hasPendingRescheduleRequest(long appointmentId);
        long createRescheduleRequest(NewRescheduleRequest request);
        void holdSlots(long rescheduleRequestId, List<Long> slotIds);
        void releaseHeldSlots(long rescheduleRequestId);
        void confirmHeldSlots(long rescheduleRequestId, long appointmentId);
        void moveAppointmentWindow(long appointmentId, LocalDateTime startAt, LocalDateTime endAt);
        void releaseSlotsInRange(long appointmentId, LocalDateTime fromInclusive, LocalDateTime toExclusive);
        List<RescheduleRequestDetail> findRescheduleRequests(RescheduleStatus status, Long locationId, Long professionalId,
                                                             Long specialtyId, LocalDate date);
        Optional<RescheduleRequestDetail> lockRescheduleRequest(long rescheduleRequestId);
        void decideRescheduleRequest(long rescheduleRequestId, RescheduleStatus status, String adminUserId,
                                    LocalDateTime decidedAt, String reason);
        Optional<RescheduleRequestDetail> findRescheduleRequestDetail(long rescheduleRequestId);

        /** HU-025. Ownership mínimo de una cita para autorizar la consulta de auditoría sin bloquear la fila. */
        Optional<AppointmentOwnership> findAppointmentOwnership(long appointmentId);
        List<StatusHistoryEvent> findStatusHistory(long appointmentId);
    }

    public record Location(long id, String code, String name) { }
    public record Specialty(long id, String code, String name, int durationMinutes, boolean general, boolean requiresAdminApproval) { }
    public record Professional(long id, String name, String code) { }
    public record ReservationContext(Specialty specialty) { }

    /** Un slot está libre solo cuando no tiene cita asignada ni retención de reprogramación. */
    public record Slot(long id, LocalDateTime startAt, LocalDateTime endAt, Long appointmentId, Long rescheduleRequestId) {
        public boolean free() { return appointmentId == null && rescheduleRequestId == null; }
    }

    public record NewAppointment(String patientUserId, long professionalId, long locationId, long specialtyId,
                                 AppointmentStatus status, LocalDateTime startAt, LocalDateTime endAt) { }
    public record PendingAppointment(long id, String patientName, String professionalName, String locationName,
                                     String specialtyName, int durationMinutes, LocalDateTime startAt, LocalDateTime endAt,
                                     AppointmentStatus status) { }
    public record MyAppointment(long id, long locationId, String locationName, long professionalId, String professionalName,
                                long specialtyId, String specialtyName, LocalDateTime startAt, LocalDateTime endAt,
                                int durationMinutes, AppointmentStatus status, boolean terminal, String rejectionReason) { }
    public record AgendaAppointment(long id, String patientName, long locationId, String locationName,
                                    long specialtyId, String specialtyName, LocalDateTime startAt, LocalDateTime endAt,
                                    int durationMinutes, AppointmentStatus status) { }

    public record NewRescheduleRequest(long appointmentId, String requestedByUserId, long requestedLocationId,
                                       LocalDateTime previousStartAt, LocalDateTime previousEndAt,
                                       LocalDateTime requestedStartAt, LocalDateTime requestedEndAt) { }
    public record RescheduleRequest(long id, long appointmentId, RescheduleStatus status,
                                    LocalDateTime requestedStartAt, LocalDateTime requestedEndAt,
                                    String decisionReason, LocalDateTime decidedAt) { }
    public record RescheduleRequestDetail(long id, long appointmentId, RescheduleStatus status, String patientName,
                                          String professionalName, String specialtyName, String locationName,
                                          long requestedLocationId, int durationMinutes,
                                          LocalDateTime previousStartAt, LocalDateTime previousEndAt,
                                          LocalDateTime requestedStartAt, LocalDateTime requestedEndAt,
                                          String decisionReason, LocalDateTime decidedAt) { }

    public record AppointmentOwnership(long id, String patientUserId, long professionalId) { }
    public record StatusHistoryEvent(long id, long appointmentId, AppointmentStatus status, String actorUserId,
                                     String actorName, StatusSource source, LocalDateTime changedAt, String reason) { }
}
