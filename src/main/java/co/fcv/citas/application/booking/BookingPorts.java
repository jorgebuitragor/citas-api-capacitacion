package co.fcv.citas.application.booking;

import co.fcv.citas.domain.appointment.AppointmentStatus;
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
        List<PendingAppointment> findPending();
    }

    public record Location(long id, String code, String name) { }
    public record Specialty(long id, String code, String name, int durationMinutes, boolean general, boolean requiresAdminApproval) { }
    public record Professional(long id, String name, String code) { }
    public record ReservationContext(Specialty specialty) { }
    public record Slot(long id, LocalDateTime startAt, LocalDateTime endAt, Long appointmentId) { }
    public record NewAppointment(String patientUserId, long professionalId, long locationId, long specialtyId,
                                 AppointmentStatus status, LocalDateTime startAt, LocalDateTime endAt) { }
    public record PendingAppointment(long id, String patientName, String professionalName, String locationName,
                                     String specialtyName, int durationMinutes, LocalDateTime startAt, LocalDateTime endAt,
                                     AppointmentStatus status) { }
}
