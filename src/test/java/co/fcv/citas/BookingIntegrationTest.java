package co.fcv.citas;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BookingIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired JdbcTemplate jdbc;
    private final List<Long> createdAppointments = new ArrayList<>();

    @AfterEach void releaseFixtures() {
        for (Long id : createdAppointments) jdbc.update("DELETE FROM appointments WHERE id = ?", id);
    }

    @Test void rejectsSecondReservationWithoutCreatingAnotherAppointment() throws Exception {
        String userId = registerUser();
        Slot slot = slotFor("MEDICINA_GENERAL");
        long before = jdbc.queryForObject("SELECT COUNT(*) FROM appointments", Long.class);

        long appointmentId = appointmentId(reserve(userId, slot).andExpect(status().isCreated()).andReturn());
        createdAppointments.add(appointmentId);
        reserve(userId, slot).andExpect(status().isConflict());

        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM appointments", Long.class)).isEqualTo(before + 1);
        assertThat(statusOf(appointmentId)).isEqualTo("APPROVED");
        assertThat(slotCount(appointmentId)).isEqualTo(1);
        assertThat(historyCount(appointmentId, "APPROVED", "SYSTEM")).isEqualTo(1);
        assertThat(historyActor(appointmentId, "APPROVED")).isNull();
    }

    @Test void userCanSearchOnlyCompleteAvailabilityUsingAllFilters() throws Exception {
        String userId = registerUser();
        Slot slot = slotFor("MEDICINA_GENERAL");
        mvc.perform(get("/api/v1/catalogs/locations").header("Authorization", "Bearer " + accessToken(userId, "USER"))).andExpect(status().isOk());
        mvc.perform(get("/api/v1/availability")
                .header("Authorization", "Bearer " + accessToken(userId, "USER"))
                .param("locationId", Long.toString(slot.locationId())).param("specialtyId", Long.toString(slot.specialtyId()))
                .param("professionalId", Long.toString(slot.professionalId())).param("date", slot.startAt().substring(0, 10)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.items[0].startAt").isNotEmpty());
    }

    @Test void specializedRequestReservesTwoConsecutiveSlotsAndRejectsPartialDuration() throws Exception {
        String userId = registerUser();
        Slot full = slotFor("ORTOPEDIA_TRAUMATOLOGIA");
        long requested = appointmentId(reserve(userId, full).andExpect(status().isCreated()).andReturn());
        createdAppointments.add(requested);
        assertThat(statusOf(requested)).isEqualTo("REQUESTED");
        assertThat(slotCount(requested)).isEqualTo(2);
        assertThat(historyCount(requested, "REQUESTED", "USER")).isEqualTo(1);
        assertThat(historyActor(requested, "REQUESTED")).isEqualTo(Long.parseLong(userId));

        Slot finalHalfHour = jdbc.queryForObject("""
                SELECT ab.location_id, pspecialty.specialty_id, ab.professional_id,
                       DATE_FORMAT(ps.start_at, '%Y-%m-%dT%H:%i:%s')
                FROM professional_slots ps
                JOIN availability_blocks ab ON ab.id = ps.availability_block_id
                JOIN professional_specialties pspecialty ON pspecialty.professional_id = ab.professional_id
                JOIN specialties specialty ON specialty.id = pspecialty.specialty_id
                WHERE specialty.code = 'ORTOPEDIA_TRAUMATOLOGIA' AND specialty.active = TRUE
                  AND pspecialty.active = TRUE AND ab.active = TRUE
                  AND ps.appointment_id IS NULL AND ps.reschedule_request_id IS NULL AND ps.start_at > NOW()
                ORDER BY ps.start_at DESC
                LIMIT 1
                """, (rs, row) -> new Slot(rs.getLong(1), rs.getLong(2), rs.getLong(3), rs.getString(4)));
        long before = jdbc.queryForObject("SELECT COUNT(*) FROM appointments", Long.class);
        reserve(userId, finalHalfHour).andExpect(status().isConflict());
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM appointments", Long.class)).isEqualTo(before);
        assertThat(jdbc.queryForObject("""
                SELECT COUNT(*) FROM professional_slots ps JOIN availability_blocks ab ON ab.id = ps.availability_block_id
                WHERE ps.appointment_id IS NULL AND ps.start_at = ? AND ab.professional_id = ? AND ab.location_id = ?
                """, Long.class, java.time.LocalDateTime.parse(finalHalfHour.startAt()), finalHalfHour.professionalId(), finalHalfHour.locationId())).isEqualTo(1);
    }

    @Test void secondSpecializedReservationCannotUseHeldSlots() throws Exception {
        String firstUser = registerUser();
        String secondUser = registerUser();
        Slot slot = slotFor("ORTOPEDIA_TRAUMATOLOGIA");
        long before = jdbc.queryForObject("SELECT COUNT(*) FROM appointments", Long.class);

        long requested = appointmentId(reserve(firstUser, slot).andExpect(status().isCreated()).andReturn());
        createdAppointments.add(requested);
        reserve(secondUser, slot).andExpect(status().isConflict());

        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM appointments", Long.class)).isEqualTo(before + 1);
        assertThat(statusOf(requested)).isEqualTo("REQUESTED");
        assertThat(slotCount(requested)).isEqualTo(2);
    }

    @Test void adminApprovesOrRejectsRequestedAppointmentsAndKeepsAudit() throws Exception {
        String requester = registerUser();
        String admin = registerUser();
        long approved = appointmentId(reserve(requester, slotFor("CARDIOLOGIA_ADULTO")).andExpect(status().isCreated()).andReturn());
        createdAppointments.add(approved);
        decide(admin, approved, "APPROVE", null).andExpect(status().isOk());
        assertThat(statusOf(approved)).isEqualTo("APPROVED");
        assertThat(slotCount(approved)).isEqualTo(1);
        assertThat(historyCount(approved, "APPROVED", "ADMIN")).isEqualTo(1);
        assertThat(historyActor(approved, "APPROVED")).isEqualTo(Long.parseLong(admin));

        long rejected = appointmentId(reserve(requester, slotFor("CARDIOLOGIA_ADULTO")).andExpect(status().isCreated()).andReturn());
        createdAppointments.add(rejected);
        decide(admin, rejected, "REJECT", " ").andExpect(status().isBadRequest());
        assertThat(statusOf(rejected)).isEqualTo("REQUESTED");
        assertThat(slotCount(rejected)).isEqualTo(1);
        decide(admin, rejected, "REJECT", "Información clínica pendiente").andExpect(status().isOk());
        assertThat(statusOf(rejected)).isEqualTo("REJECTED");
        assertThat(slotCount(rejected)).isZero();
        assertThat(historyCount(rejected, "REJECTED", "ADMIN")).isEqualTo(1);
    }

    @Test void userAndProfessionalCannotDecideSpecializedRequests() throws Exception {
        String requester = registerUser();
        long requested = appointmentId(reserve(requester, slotFor("CARDIOLOGIA_ADULTO")).andExpect(status().isCreated()).andReturn());
        createdAppointments.add(requested);
        decide(requester, requested, "APPROVE", null, "USER").andExpect(status().isForbidden());
        decide(registerUser(), requested, "APPROVE", null, "PROFESSIONAL").andExpect(status().isForbidden());
        assertThat(statusOf(requested)).isEqualTo("REQUESTED");
    }

    @Test void userListsOnlyOwnAppointmentsAndCanFilterByStatusAndDate() throws Exception {
        String owner = registerUser();
        String anotherUser = registerUser();
        long approved = appointmentId(reserve(owner, slotFor("MEDICINA_GENERAL")).andExpect(status().isCreated()).andReturn());
        createdAppointments.add(approved);
        Slot rejectedSlot = slotFor("CARDIOLOGIA_ADULTO");
        long rejected = appointmentId(reserve(owner, rejectedSlot).andExpect(status().isCreated()).andReturn());
        createdAppointments.add(rejected);
        decide(anotherUser, rejected, "REJECT", "Agenda no disponible").andExpect(status().isOk());
        long foreign = appointmentId(reserve(anotherUser, slotFor("MEDICINA_GENERAL")).andExpect(status().isCreated()).andReturn());
        createdAppointments.add(foreign);

        String token = accessToken(owner, "USER");
        mvc.perform(get("/api/v1/appointments").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.items[?(@.status == 'APPROVED')].location.name").isNotEmpty())
                .andExpect(jsonPath("$.items[?(@.status == 'REJECTED')].professional.name").isNotEmpty())
                .andExpect(jsonPath("$.items[?(@.status == 'REJECTED')].specialty.name").isNotEmpty())
                .andExpect(jsonPath("$.items[?(@.status == 'REJECTED')].rejectionReason").value(org.hamcrest.Matchers.hasItem("Agenda no disponible")));
        mvc.perform(get("/api/v1/appointments").header("Authorization", "Bearer " + token).param("status", "APPROVED"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.items.length()").value(1)).andExpect(jsonPath("$.items[0].id").value(Long.toString(approved)));
        String date = rejectedSlot.startAt().substring(0, 10);
        mvc.perform(get("/api/v1/appointments").header("Authorization", "Bearer " + token).param("date", date))
                .andExpect(status().isOk()).andExpect(jsonPath("$.items[*].startAt").value(org.hamcrest.Matchers.everyItem(org.hamcrest.Matchers.startsWith(date))));
        mvc.perform(get("/api/v1/appointments").header("Authorization", "Bearer " + token).param("status", "CANCELLED"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.items").isEmpty());
    }

    @Test void userCancellationReleasesSlotsAddsHistoryAndIsNotRepeatable() throws Exception {
        String userId = registerUser();
        Slot slot = slotFor("MEDICINA_GENERAL");
        long appointmentId = appointmentId(reserve(userId, slot).andExpect(status().isCreated()).andReturn());
        createdAppointments.add(appointmentId);

        mvc.perform(post("/api/v1/appointments/{id}/cancellation", appointmentId)
                        .header("Authorization", "Bearer " + accessToken(userId, "USER")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("CANCELLED"))
                .andExpect(jsonPath("$.cancellationAllowed").value(false)).andExpect(jsonPath("$.id").value(Long.toString(appointmentId)));
        assertThat(statusOf(appointmentId)).isEqualTo("CANCELLED");
        assertThat(slotCount(appointmentId)).isZero();
        assertThat(historyCount(appointmentId, "CANCELLED", "USER")).isEqualTo(1);
        assertThat(historyActor(appointmentId, "CANCELLED")).isEqualTo(Long.parseLong(userId));

        mvc.perform(post("/api/v1/appointments/{id}/cancellation", appointmentId)
                        .header("Authorization", "Bearer " + accessToken(userId, "USER")))
                .andExpect(status().isConflict());
        assertThat(historyCount(appointmentId, "CANCELLED", "USER")).isEqualTo(1);
        assertThat(slotCount(appointmentId)).isZero();
    }

    @Test void cannotCancelAnotherUsersOrPastOrRejectedAppointment() throws Exception {
        String owner = registerUser();
        String other = registerUser();
        Slot ownedSlot = slotFor("MEDICINA_GENERAL");
        long owned = appointmentId(reserve(owner, ownedSlot).andExpect(status().isCreated()).andReturn());
        createdAppointments.add(owned);
        long ownedSlotsBefore = slotCount(owned);
        mvc.perform(post("/api/v1/appointments/{id}/cancellation", owned)
                        .header("Authorization", "Bearer " + accessToken(other, "USER")))
                .andExpect(status().isNotFound());
        assertThat(statusOf(owned)).isEqualTo("APPROVED");
        assertThat(slotCount(owned)).isEqualTo(ownedSlotsBefore);
        assertThat(historyCount(owned, "CANCELLED", "USER")).isZero();

        Slot pastSlot = slotFor("MEDICINA_GENERAL");
        long past = appointmentId(reserve(owner, pastSlot).andExpect(status().isCreated()).andReturn());
        createdAppointments.add(past);
        jdbc.update("UPDATE appointments SET scheduled_start_at = ?, scheduled_end_at = ? WHERE id = ?",
                java.time.LocalDateTime.now().minusDays(1), java.time.LocalDateTime.now().minusDays(1).plusMinutes(30), past);
        long pastSlotsBefore = slotCount(past);
        mvc.perform(post("/api/v1/appointments/{id}/cancellation", past)
                        .header("Authorization", "Bearer " + accessToken(owner, "USER")))
                .andExpect(status().isConflict());
        assertThat(statusOf(past)).isEqualTo("APPROVED");
        assertThat(slotCount(past)).isEqualTo(pastSlotsBefore);

        long rejected = appointmentId(reserve(owner, slotFor("CARDIOLOGIA_ADULTO")).andExpect(status().isCreated()).andReturn());
        createdAppointments.add(rejected);
        decide(other, rejected, "REJECT", "Prueba de estado terminal").andExpect(status().isOk());
        long rejectedHistoryBefore = jdbc.queryForObject("SELECT COUNT(*) FROM appointment_status_history WHERE appointment_id = ?", Long.class, rejected);
        mvc.perform(post("/api/v1/appointments/{id}/cancellation", rejected)
                        .header("Authorization", "Bearer " + accessToken(owner, "USER")))
                .andExpect(status().isConflict());
        assertThat(statusOf(rejected)).isEqualTo("REJECTED");
        assertThat(historyCount(rejected, "CANCELLED", "USER")).isZero();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM appointment_status_history WHERE appointment_id = ?", Long.class, rejected)).isEqualTo(rejectedHistoryBefore);
    }

    @Test void professionalSeesOnlyOwnApprovedAgendaFilteredByDayWeekAndLocation() throws Exception {
        String owner = registerUser();
        Slot slot = slotFor("MEDICINA_GENERAL");
        long appointmentId = appointmentId(reserve(owner, slot).andExpect(status().isCreated()).andReturn());
        createdAppointments.add(appointmentId);
        String date = slot.startAt().substring(0, 10);
        String generalProfessionalUserId = professionalUserId("prof.general@demo.invalid");
        String otherProfessionalUserId = professionalUserId("prof.especialista@demo.invalid");

        mvc.perform(get("/api/v1/professional/agenda").header("Authorization", "Bearer " + accessToken(generalProfessionalUserId, "PROFESSIONAL"))
                        .param("range", "day").param("date", date))
                .andExpect(status().isOk()).andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].id").value(Long.toString(appointmentId)))
                .andExpect(jsonPath("$.items[0].status").value("APPROVED"));
        mvc.perform(get("/api/v1/professional/agenda").header("Authorization", "Bearer " + accessToken(generalProfessionalUserId, "PROFESSIONAL"))
                        .param("range", "week").param("date", date))
                .andExpect(status().isOk()).andExpect(jsonPath("$.items[?(@.id == '" + appointmentId + "')]").isNotEmpty());
        mvc.perform(get("/api/v1/professional/agenda").header("Authorization", "Bearer " + accessToken(generalProfessionalUserId, "PROFESSIONAL"))
                        .param("range", "day").param("date", date).param("locationId", Long.toString(slot.locationId() + 1)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.items").isEmpty());
        mvc.perform(get("/api/v1/professional/agenda").header("Authorization", "Bearer " + accessToken(otherProfessionalUserId, "PROFESSIONAL"))
                        .param("range", "day").param("date", date))
                .andExpect(status().isOk()).andExpect(jsonPath("$.items").isEmpty());
    }

    @Test void professionalCanCloseEligiblePastApprovedAppointmentAndRecordsAudit() throws Exception {
        String owner = registerUser();
        Slot slot = slotFor("MEDICINA_GENERAL");
        long appointmentId = appointmentId(reserve(owner, slot).andExpect(status().isCreated()).andReturn());
        createdAppointments.add(appointmentId);
        jdbc.update("UPDATE appointments SET scheduled_start_at = ?, scheduled_end_at = ? WHERE id = ?",
                java.time.LocalDateTime.now().minusDays(1), java.time.LocalDateTime.now().minusDays(1).plusMinutes(30), appointmentId);
        String professionalUserId = professionalUserId("prof.general@demo.invalid");

        mvc.perform(post("/api/v1/appointments/{id}/closure", appointmentId)
                        .header("Authorization", "Bearer " + accessToken(professionalUserId, "PROFESSIONAL")).contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of("outcome", "COMPLETED"))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("COMPLETED")).andExpect(jsonPath("$.closureAllowed").value(false));
        assertThat(statusOf(appointmentId)).isEqualTo("COMPLETED");
        assertThat(historyCount(appointmentId, "COMPLETED", "USER")).isEqualTo(1);
        assertThat(historyActor(appointmentId, "COMPLETED")).isEqualTo(Long.parseLong(professionalUserId));

        mvc.perform(post("/api/v1/appointments/{id}/closure", appointmentId)
                        .header("Authorization", "Bearer " + accessToken(professionalUserId, "PROFESSIONAL")).contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of("outcome", "NO_SHOW"))))
                .andExpect(status().isConflict());
        assertThat(statusOf(appointmentId)).isEqualTo("COMPLETED");
        assertThat(historyCount(appointmentId, "NO_SHOW", "USER")).isZero();
    }

    @Test void cannotCloseFutureOrForeignAppointment() throws Exception {
        String owner = registerUser();
        Slot slot = slotFor("MEDICINA_GENERAL");
        long futureAppointment = appointmentId(reserve(owner, slot).andExpect(status().isCreated()).andReturn());
        createdAppointments.add(futureAppointment);
        String professionalUserId = professionalUserId("prof.general@demo.invalid");
        String otherProfessionalUserId = professionalUserId("prof.especialista@demo.invalid");

        mvc.perform(post("/api/v1/appointments/{id}/closure", futureAppointment)
                        .header("Authorization", "Bearer " + accessToken(professionalUserId, "PROFESSIONAL")).contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of("outcome", "NO_SHOW"))))
                .andExpect(status().isConflict());
        assertThat(statusOf(futureAppointment)).isEqualTo("APPROVED");
        assertThat(historyCount(futureAppointment, "NO_SHOW", "USER")).isZero();

        mvc.perform(post("/api/v1/appointments/{id}/closure", futureAppointment)
                        .header("Authorization", "Bearer " + accessToken(otherProfessionalUserId, "PROFESSIONAL")).contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of("outcome", "NO_SHOW"))))
                .andExpect(status().isNotFound());
        assertThat(statusOf(futureAppointment)).isEqualTo("APPROVED");
        assertThat(historyCount(futureAppointment, "NO_SHOW", "USER")).isZero();
    }

    @Test void agendaAndClosureRequireProfessionalRole() throws Exception {
        String userId = registerUser();
        mvc.perform(get("/api/v1/professional/agenda").header("Authorization", "Bearer " + accessToken(userId, "USER"))
                        .param("range", "day").param("date", "2026-10-01"))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/appointments/{id}/closure", 999999)
                        .header("Authorization", "Bearer " + accessToken(userId, "USER")).contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of("outcome", "COMPLETED"))))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/professional/agenda").param("range", "day").param("date", "2026-10-01")).andExpect(status().isUnauthorized());
    }

    @Test void myAppointmentsAndCancellationRequireUserRole() throws Exception {
        String userId = registerUser();
        mvc.perform(get("/api/v1/appointments").header("Authorization", "Bearer " + accessToken(userId, "PROFESSIONAL")))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/appointments/{id}/cancellation", 999999)
                        .header("Authorization", "Bearer " + accessToken(userId, "PROFESSIONAL")))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/appointments")).andExpect(status().isUnauthorized());
    }

    @Test void userRequestsReschedulePendingHoldsNewSlotAndKeepsOriginal() throws Exception {
        String userId = registerUser();
        List<Slot> slots = freeSlotsFor("MEDICINA_GENERAL", 2);
        Slot original = slots.get(0);
        Slot proposed = slots.get(1);
        long appointmentId = appointmentId(reserve(userId, original).andExpect(status().isCreated()).andReturn());
        createdAppointments.add(appointmentId);

        long requestId = rescheduleId(requestReschedule(userId, appointmentId, proposed).andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.previousStartAt").value(original.startAt()))
                .andExpect(jsonPath("$.requestedStartAt").value(proposed.startAt()))
                .andExpect(jsonPath("$.decisionReason").value(org.hamcrest.Matchers.nullValue())).andReturn());

        assertThat(rescheduleStatusOf(requestId)).isEqualTo("PENDING");
        assertThat(slotCount(appointmentId)).isEqualTo(1);
        assertThat(slotAppointmentAt(original)).isEqualTo(appointmentId);
        assertThat(heldSlotCount(requestId)).isEqualTo(1);
        assertThat(slotAppointmentAt(proposed)).isNull();
        assertThat(jdbc.queryForObject("SELECT DATE_FORMAT(scheduled_start_at, '%Y-%m-%dT%H:%i:%s') FROM appointments WHERE id = ?",
                String.class, appointmentId)).isEqualTo(original.startAt());
        assertThat(historyCount(appointmentId, "APPROVED", "USER")).isEqualTo(1);

        mvc.perform(get("/api/v1/appointments").header("Authorization", "Bearer " + accessToken(userId, "USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].startAt").value(original.startAt()))
                .andExpect(jsonPath("$.items[0].status").value("APPROVED"))
                .andExpect(jsonPath("$.items[0].rescheduleAllowed").value(false))
                .andExpect(jsonPath("$.items[0].rescheduleRequest.status").value("PENDING"))
                .andExpect(jsonPath("$.items[0].rescheduleRequest.requestedStartAt").value(proposed.startAt()));
    }

    @Test void heldRescheduleSlotIsNotOfferedNorReservable() throws Exception {
        String userId = registerUser();
        String otherUser = registerUser();
        List<Slot> slots = freeSlotsFor("MEDICINA_GENERAL", 2);
        Slot original = slots.get(0);
        Slot proposed = slots.get(1);
        long appointmentId = appointmentId(reserve(userId, original).andExpect(status().isCreated()).andReturn());
        createdAppointments.add(appointmentId);
        requestReschedule(userId, appointmentId, proposed).andExpect(status().isCreated());

        mvc.perform(get("/api/v1/availability").header("Authorization", "Bearer " + accessToken(userId, "USER"))
                        .param("locationId", Long.toString(proposed.locationId())).param("specialtyId", Long.toString(proposed.specialtyId()))
                        .param("professionalId", Long.toString(proposed.professionalId())).param("date", proposed.startAt().substring(0, 10)))
                .andExpect(status().isOk())
                // HU-014 CA-03: ni el slot reservado por la cita ni el retenido por la reprogramación se ofrecen.
                .andExpect(jsonPath("$.items[?(@.startAt == '" + original.startAt() + "')]").isEmpty())
                .andExpect(jsonPath("$.items[?(@.startAt == '" + proposed.startAt() + "')]").isEmpty());

        long before = jdbc.queryForObject("SELECT COUNT(*) FROM appointments", Long.class);
        reserve(otherUser, proposed).andExpect(status().isConflict());
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM appointments", Long.class)).isEqualTo(before);
        assertThat(slotAppointmentAt(proposed)).isNull();
    }

    @Test void rescheduleRequestRejectsForeignProfessionalChangeConflictAndIneligibleAppointment() throws Exception {
        String owner = registerUser();
        String intruder = registerUser();
        List<Slot> slots = freeSlotsFor("MEDICINA_GENERAL", 2);
        Slot original = slots.get(0);
        Slot proposed = slots.get(1);
        long appointmentId = appointmentId(reserve(owner, original).andExpect(status().isCreated()).andReturn());
        createdAppointments.add(appointmentId);

        requestReschedule(intruder, appointmentId, proposed).andExpect(status().isNotFound());
        requestReschedule(owner, appointmentId, proposed, proposed.professionalId() + 1, proposed.specialtyId())
                .andExpect(status().isBadRequest());
        requestReschedule(owner, appointmentId, original).andExpect(status().isConflict());
        assertThat(rescheduleCountFor(appointmentId)).isZero();
        assertThat(slotAppointmentAt(original)).isEqualTo(appointmentId);
        assertThat(slotAppointmentAt(proposed)).isNull();
        assertThat(heldSlotCountFor(proposed)).isNull();

        requestReschedule(owner, appointmentId, proposed).andExpect(status().isCreated());
        requestReschedule(owner, appointmentId, proposed).andExpect(status().isConflict());
        assertThat(rescheduleCountFor(appointmentId)).isEqualTo(1);

        List<Slot> specialized = freeSlotsFor("ORTOPEDIA_TRAUMATOLOGIA", 4);
        long requestedAppointment = appointmentId(reserve(owner, specialized.get(0)).andExpect(status().isCreated()).andReturn());
        createdAppointments.add(requestedAppointment);
        assertThat(statusOf(requestedAppointment)).isEqualTo("REQUESTED");
        requestReschedule(owner, requestedAppointment, specialized.get(2)).andExpect(status().isConflict());
        assertThat(rescheduleCountFor(requestedAppointment)).isZero();
    }

    @Test void adminApprovalSwapsSlotsAtomicallyAndKeepsAudit() throws Exception {
        String userId = registerUser();
        String adminUserId = registerUser();
        List<Slot> slots = freeSlotsFor("MEDICINA_GENERAL", 2);
        Slot original = slots.get(0);
        Slot proposed = slots.get(1);
        long appointmentId = appointmentId(reserve(userId, original).andExpect(status().isCreated()).andReturn());
        createdAppointments.add(appointmentId);
        long requestId = rescheduleId(requestReschedule(userId, appointmentId, proposed).andExpect(status().isCreated()).andReturn());

        mvc.perform(get("/api/v1/admin/reschedule-requests").header("Authorization", "Bearer " + accessToken(adminUserId, "ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[?(@.id == '" + requestId + "')]").isNotEmpty());

        decideReschedule(adminUserId, requestId, "APPROVE", null).andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.decisionReason").value(org.hamcrest.Matchers.nullValue()))
                .andExpect(jsonPath("$.decidedAt").isNotEmpty());

        assertThat(rescheduleStatusOf(requestId)).isEqualTo("APPROVED");
        assertThat(jdbc.queryForObject("SELECT decided_by_user_id FROM reschedule_requests WHERE id = ?", Long.class, requestId))
                .isEqualTo(Long.parseLong(adminUserId));
        assertThat(jdbc.queryForObject("SELECT DATE_FORMAT(scheduled_start_at, '%Y-%m-%dT%H:%i:%s') FROM appointments WHERE id = ?",
                String.class, appointmentId)).isEqualTo(proposed.startAt());
        assertThat(statusOf(appointmentId)).isEqualTo("APPROVED");
        assertThat(slotAppointmentAt(original)).isNull();
        assertThat(slotAppointmentAt(proposed)).isEqualTo(appointmentId);
        assertThat(heldSlotCount(requestId)).isZero();
        assertThat(slotCount(appointmentId)).isEqualTo(1);
        assertThat(historyCount(appointmentId, "APPROVED", "ADMIN")).isEqualTo(1);
        assertThat(historyActor(appointmentId, "APPROVED")).isEqualTo(Long.parseLong(adminUserId));

        decideReschedule(adminUserId, requestId, "REJECT", "Ya fue decidida.").andExpect(status().isConflict());
        assertThat(rescheduleStatusOf(requestId)).isEqualTo("APPROVED");
        assertThat(slotAppointmentAt(proposed)).isEqualTo(appointmentId);
    }

    @Test void adminRejectionRequiresReasonReleasesHoldAndKeepsOriginalAppointment() throws Exception {
        String userId = registerUser();
        String adminUserId = registerUser();
        List<Slot> slots = freeSlotsFor("MEDICINA_GENERAL", 2);
        Slot original = slots.get(0);
        Slot proposed = slots.get(1);
        long appointmentId = appointmentId(reserve(userId, original).andExpect(status().isCreated()).andReturn());
        createdAppointments.add(appointmentId);
        long requestId = rescheduleId(requestReschedule(userId, appointmentId, proposed).andExpect(status().isCreated()).andReturn());

        decideReschedule(adminUserId, requestId, "REJECT", null).andExpect(status().isBadRequest());
        assertThat(rescheduleStatusOf(requestId)).isEqualTo("PENDING");
        assertThat(heldSlotCount(requestId)).isEqualTo(1);
        assertThat(historyCount(appointmentId, "APPROVED", "ADMIN")).isZero();

        decideReschedule(adminUserId, requestId, "REJECT", "El profesional no estará disponible ese día.")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"))
                .andExpect(jsonPath("$.decisionReason").value("El profesional no estará disponible ese día."));

        assertThat(rescheduleStatusOf(requestId)).isEqualTo("REJECTED");
        assertThat(heldSlotCount(requestId)).isZero();
        assertThat(slotAppointmentAt(proposed)).isNull();
        assertThat(slotAppointmentAt(original)).isEqualTo(appointmentId);
        assertThat(statusOf(appointmentId)).isEqualTo("APPROVED");
        assertThat(jdbc.queryForObject("SELECT DATE_FORMAT(scheduled_start_at, '%Y-%m-%dT%H:%i:%s') FROM appointments WHERE id = ?",
                String.class, appointmentId)).isEqualTo(original.startAt());
        assertThat(historyCount(appointmentId, "APPROVED", "ADMIN")).isEqualTo(1);
        assertThat(historyActor(appointmentId, "APPROVED")).isEqualTo(Long.parseLong(adminUserId));

        mvc.perform(get("/api/v1/appointments").header("Authorization", "Bearer " + accessToken(userId, "USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].startAt").value(original.startAt()))
                .andExpect(jsonPath("$.items[0].rescheduleAllowed").value(true))
                .andExpect(jsonPath("$.items[0].rescheduleRequest.status").value("REJECTED"))
                .andExpect(jsonPath("$.items[0].rescheduleRequest.decisionReason").value("El profesional no estará disponible ese día."));
    }

    @Test void rescheduleInboxAndDecisionRequireAdminRoleAndRequestRequiresUserRole() throws Exception {
        String userId = registerUser();
        mvc.perform(get("/api/v1/admin/reschedule-requests").header("Authorization", "Bearer " + accessToken(userId, "USER")))
                .andExpect(status().isForbidden());
        decideReschedule(userId, 999999, "APPROVE", null, "USER").andExpect(status().isForbidden());
        decideReschedule(userId, 999999, "APPROVE", null, "PROFESSIONAL").andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/admin/reschedule-requests")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/v1/appointments/{id}/reschedule-requests", 999999)
                        .header("Authorization", "Bearer " + accessToken(userId, "ADMIN")).contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of("locationId", "1", "specialtyId", "1", "professionalId", "1",
                                "startAt", "2026-12-01T09:00:00"))))
                .andExpect(status().isForbidden());
    }

    private org.springframework.test.web.servlet.ResultActions requestReschedule(String userId, long appointmentId, Slot target) throws Exception {
        return requestReschedule(userId, appointmentId, target, target.professionalId(), target.specialtyId());
    }

    private org.springframework.test.web.servlet.ResultActions requestReschedule(String userId, long appointmentId, Slot target,
                                                                                 long professionalId, long specialtyId) throws Exception {
        return mvc.perform(post("/api/v1/appointments/{id}/reschedule-requests", appointmentId)
                .header("Authorization", "Bearer " + accessToken(userId, "USER")).contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(Map.of(
                        "locationId", Long.toString(target.locationId()),
                        "specialtyId", Long.toString(specialtyId),
                        "professionalId", Long.toString(professionalId),
                        "startAt", target.startAt()))));
    }

    private org.springframework.test.web.servlet.ResultActions decideReschedule(String userId, long requestId, String decision, String reason) throws Exception {
        return decideReschedule(userId, requestId, decision, reason, "ADMIN");
    }

    private org.springframework.test.web.servlet.ResultActions decideReschedule(String userId, long requestId, String decision,
                                                                               String reason, String role) throws Exception {
        return mvc.perform(post("/api/v1/admin/reschedule-requests/{id}/decision", requestId)
                .header("Authorization", "Bearer " + accessToken(userId, role)).contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(reason == null ? Map.of("decision", decision) : Map.of("decision", decision, "reason", reason))));
    }

    private List<Slot> freeSlotsFor(String specialtyCode, int limit) {
        Slot reference = slotFor(specialtyCode);
        return jdbc.query("""
                SELECT ab.location_id, ?, ab.professional_id, DATE_FORMAT(ps.start_at, '%Y-%m-%dT%H:%i:%s')
                FROM professional_slots ps JOIN availability_blocks ab ON ab.id = ps.availability_block_id
                WHERE ab.professional_id = ? AND ab.location_id = ? AND ab.active = TRUE
                  AND ps.appointment_id IS NULL AND ps.reschedule_request_id IS NULL AND ps.start_at > NOW()
                ORDER BY ps.start_at LIMIT ?
                """, (rs, row) -> new Slot(rs.getLong(1), rs.getLong(2), rs.getLong(3), rs.getString(4)),
                reference.specialtyId(), reference.professionalId(), reference.locationId(), limit);
    }

    private long rescheduleId(org.springframework.test.web.servlet.MvcResult result) throws Exception {
        return Long.parseLong(mapper.readTree(result.getResponse().getContentAsString()).get("id").asText());
    }
    private String rescheduleStatusOf(long requestId) {
        return jdbc.queryForObject("""
                SELECT status.code FROM reschedule_requests r JOIN reschedule_request_statuses status ON status.id = r.status_id
                WHERE r.id = ?
                """, String.class, requestId);
    }
    private long rescheduleCountFor(long appointmentId) {
        return jdbc.queryForObject("SELECT COUNT(*) FROM reschedule_requests WHERE appointment_id = ?", Long.class, appointmentId);
    }
    private long heldSlotCount(long requestId) {
        return jdbc.queryForObject("SELECT COUNT(*) FROM professional_slots WHERE reschedule_request_id = ?", Long.class, requestId);
    }
    private Long heldSlotCountFor(Slot slot) {
        return jdbc.queryForObject("""
                SELECT ps.reschedule_request_id FROM professional_slots ps JOIN availability_blocks ab ON ab.id = ps.availability_block_id
                WHERE ab.professional_id = ? AND ab.location_id = ? AND ps.start_at = ?
                """, Long.class, slot.professionalId(), slot.locationId(), java.time.LocalDateTime.parse(slot.startAt()));
    }
    private Long slotAppointmentAt(Slot slot) {
        return jdbc.queryForObject("""
                SELECT ps.appointment_id FROM professional_slots ps JOIN availability_blocks ab ON ab.id = ps.availability_block_id
                WHERE ab.professional_id = ? AND ab.location_id = ? AND ps.start_at = ?
                """, Long.class, slot.professionalId(), slot.locationId(), java.time.LocalDateTime.parse(slot.startAt()));
    }

    private String professionalUserId(String email) {
        return jdbc.queryForObject("SELECT id FROM users WHERE email = ?", Long.class, email).toString();
    }

    private String registerUser() throws Exception {
        String suffix = UUID.randomUUID().toString().replace("-", "");
        String body = mvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(Map.of(
                "firstName", "Reserva", "lastName", "Prueba", "documentType", "CC", "documentNumber", suffix.substring(0, 20),
                "email", "booking." + suffix + "@example.test", "phone", "3000000000", "password", "BookingTestOnly!"))))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return mapper.readTree(body).get("id").asText();
    }

    private org.springframework.test.web.servlet.ResultActions reserve(String userId, Slot slot) throws Exception {
        return mvc.perform(post("/api/v1/appointments")
                .header("Authorization", "Bearer " + accessToken(userId, "USER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(Map.of(
                        "locationId", Long.toString(slot.locationId()),
                        "specialtyId", Long.toString(slot.specialtyId()),
                        "professionalId", Long.toString(slot.professionalId()),
                        "startAt", slot.startAt()))));
    }

    private org.springframework.test.web.servlet.ResultActions decide(String adminUserId, long appointmentId, String decision, String reason) throws Exception {
        return decide(adminUserId, appointmentId, decision, reason, "ADMIN");
    }

    private org.springframework.test.web.servlet.ResultActions decide(String userId, long appointmentId, String decision, String reason, String role) throws Exception {
        return mvc.perform(post("/api/v1/admin/appointments/{id}/decision", appointmentId)
                .header("Authorization", "Bearer " + accessToken(userId, role)).contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(reason == null ? Map.of("decision", decision) : Map.of("decision", decision, "reason", reason))));
    }

    private Slot slotFor(String specialtyCode) {
        return jdbc.queryForObject("""
                SELECT ab.location_id, pspecialty.specialty_id, ab.professional_id,
                       DATE_FORMAT(ps.start_at, '%Y-%m-%dT%H:%i:%s')
                FROM professional_slots ps
                JOIN availability_blocks ab ON ab.id = ps.availability_block_id
                JOIN professional_specialties pspecialty ON pspecialty.professional_id = ab.professional_id
                JOIN specialties specialty ON specialty.id = pspecialty.specialty_id
                WHERE specialty.code = ? AND specialty.active = TRUE AND pspecialty.active = TRUE AND ab.active = TRUE
                  AND ps.appointment_id IS NULL AND ps.reschedule_request_id IS NULL AND ps.start_at > NOW()
                ORDER BY ps.start_at LIMIT 1
                """, (rs, row) -> new Slot(rs.getLong(1), rs.getLong(2), rs.getLong(3), rs.getString(4)), specialtyCode);
    }

    private long appointmentId(org.springframework.test.web.servlet.MvcResult result) throws Exception { return Long.parseLong(mapper.readTree(result.getResponse().getContentAsString()).get("id").asText()); }
    private String statusOf(long appointmentId) { return jdbc.queryForObject("SELECT status.code FROM appointments a JOIN appointment_statuses status ON status.id = a.status_id WHERE a.id = ?", String.class, appointmentId); }
    private long slotCount(long appointmentId) { return jdbc.queryForObject("SELECT COUNT(*) FROM professional_slots WHERE appointment_id = ?", Long.class, appointmentId); }
    private long historyCount(long appointmentId, String status, String source) { return jdbc.queryForObject("""
            SELECT COUNT(*) FROM appointment_status_history h JOIN appointment_statuses s ON s.id = h.status_id
            WHERE h.appointment_id = ? AND s.code = ? AND h.change_source = ?
            """, Long.class, appointmentId, status, source); }
    private Long historyActor(long appointmentId, String status) { return jdbc.queryForObject("""
            SELECT h.changed_by_user_id FROM appointment_status_history h JOIN appointment_statuses s ON s.id = h.status_id
            WHERE h.appointment_id = ? AND s.code = ? ORDER BY h.id DESC LIMIT 1
            """, Long.class, appointmentId, status); }

    private String accessToken(String subject, String role) {
        byte[] secret = "test-access-secret-must-be-at-least-32-bytes-long".getBytes(StandardCharsets.UTF_8);
        Date now = new Date();
        return Jwts.builder().subject(subject).claim("typ", "access").claim("roles", List.of(role))
                .issuedAt(now).expiration(new Date(now.getTime() + 300_000)).signWith(Keys.hmacShaKeyFor(secret)).compact();
    }

    private record Slot(long locationId, long specialtyId, long professionalId, String startAt) { }
}
