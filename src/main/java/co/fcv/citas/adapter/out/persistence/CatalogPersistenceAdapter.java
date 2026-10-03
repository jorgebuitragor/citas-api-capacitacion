package co.fcv.citas.adapter.out.persistence;

import co.fcv.citas.application.catalog.CatalogPorts;
import co.fcv.citas.application.catalog.CatalogPorts.Eps;
import co.fcv.citas.application.catalog.CatalogPorts.InsuranceRegime;
import co.fcv.citas.application.catalog.CatalogPorts.PlanDetail;
import co.fcv.citas.application.catalog.CatalogPorts.SpecialtyAdmin;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;

@Component
public class CatalogPersistenceAdapter implements CatalogPorts.CatalogRepository {
    private final JdbcTemplate jdbc;

    public CatalogPersistenceAdapter(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Override public List<InsuranceRegime> findInsuranceRegimes() {
        return jdbc.query("SELECT id, code, name FROM insurance_regimes ORDER BY name",
                (rs, row) -> new InsuranceRegime(rs.getLong(1), rs.getString(2), rs.getString(3)));
    }
    @Override public boolean regimeExists(long regimeId) {
        Long count = jdbc.queryForObject("SELECT COUNT(*) FROM insurance_regimes WHERE id = ?", Long.class, regimeId);
        return count != null && count > 0;
    }

    @Override public List<Eps> findAllEps() {
        return jdbc.query("SELECT id, code, name, active FROM eps ORDER BY name",
                (rs, row) -> new Eps(rs.getLong(1), rs.getString(2), rs.getString(3), rs.getBoolean(4)));
    }
    @Override public Optional<Eps> findEpsById(long id) {
        return jdbc.query("SELECT id, code, name, active FROM eps WHERE id = ?",
                (rs, row) -> new Eps(rs.getLong(1), rs.getString(2), rs.getString(3), rs.getBoolean(4)), id).stream().findFirst();
    }
    @Override public boolean epsCodeExists(String code) {
        Long count = jdbc.queryForObject("SELECT COUNT(*) FROM eps WHERE code = ?", Long.class, code);
        return count != null && count > 0;
    }
    @Override public long createEps(String code, String name) {
        GeneratedKeyHolder keys = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO eps (code, name) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, code); statement.setString(2, name);
            return statement;
        }, keys);
        return keys.getKey().longValue();
    }
    @Override public void updateEps(long id, String name, Boolean active) {
        if (name != null) jdbc.update("UPDATE eps SET name = ? WHERE id = ?", name, id);
        if (active != null) jdbc.update("UPDATE eps SET active = ? WHERE id = ?", active, id);
    }

    @Override public List<PlanDetail> findPlansByEps(long epsId) {
        return jdbc.query(planSelect() + " WHERE p.eps_id = ? ORDER BY p.name", (rs, row) -> plan(rs), epsId);
    }
    @Override public Optional<PlanDetail> findPlanById(long epsId, long planId) {
        return jdbc.query(planSelect() + " WHERE p.eps_id = ? AND p.id = ?", (rs, row) -> plan(rs), epsId, planId).stream().findFirst();
    }
    @Override public boolean planCodeExists(long epsId, String code) {
        Long count = jdbc.queryForObject("SELECT COUNT(*) FROM eps_plans WHERE eps_id = ? AND code = ?", Long.class, epsId, code);
        return count != null && count > 0;
    }
    @Override public long createPlan(long epsId, long regimeId, String code, String name) {
        GeneratedKeyHolder keys = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO eps_plans (eps_id, regime_id, code, name) VALUES (?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            statement.setLong(1, epsId); statement.setLong(2, regimeId); statement.setString(3, code); statement.setString(4, name);
            return statement;
        }, keys);
        return keys.getKey().longValue();
    }
    @Override public void updatePlan(long planId, String name, Boolean active) {
        if (name != null) jdbc.update("UPDATE eps_plans SET name = ? WHERE id = ?", name, planId);
        if (active != null) jdbc.update("UPDATE eps_plans SET active = ? WHERE id = ?", active, planId);
    }
    private static String planSelect() {
        return """
                SELECT p.id, p.eps_id, p.code, p.name, p.active, r.id, r.code, r.name
                FROM eps_plans p JOIN insurance_regimes r ON r.id = p.regime_id
                """;
    }
    private static PlanDetail plan(java.sql.ResultSet rs) throws java.sql.SQLException {
        return new PlanDetail(rs.getLong(1), rs.getLong(2), rs.getString(3), rs.getString(4), rs.getBoolean(5),
                new InsuranceRegime(rs.getLong(6), rs.getString(7), rs.getString(8)));
    }

    @Override public List<SpecialtyAdmin> findAllSpecialties() {
        return jdbc.query("""
                SELECT id, code, name, appointment_duration_minutes, is_general, requires_admin_approval, active
                FROM specialties ORDER BY name
                """, (rs, row) -> specialty(rs));
    }
    @Override public Optional<SpecialtyAdmin> findSpecialtyById(long id) {
        return jdbc.query("""
                SELECT id, code, name, appointment_duration_minutes, is_general, requires_admin_approval, active
                FROM specialties WHERE id = ?
                """, (rs, row) -> specialty(rs), id).stream().findFirst();
    }
    @Override public boolean specialtyCodeExists(String code) {
        Long count = jdbc.queryForObject("SELECT COUNT(*) FROM specialties WHERE code = ?", Long.class, code);
        return count != null && count > 0;
    }
    @Override public long createSpecialty(String code, String name, int durationMinutes, boolean general, boolean requiresAdminApproval) {
        GeneratedKeyHolder keys = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement statement = connection.prepareStatement("""
                    INSERT INTO specialties (code, name, appointment_duration_minutes, is_general, requires_admin_approval)
                    VALUES (?, ?, ?, ?, ?)
                    """, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, code); statement.setString(2, name); statement.setInt(3, durationMinutes);
            statement.setBoolean(4, general); statement.setBoolean(5, requiresAdminApproval);
            return statement;
        }, keys);
        return keys.getKey().longValue();
    }
    @Override public void updateSpecialty(long id, String name, Boolean active) {
        if (name != null) jdbc.update("UPDATE specialties SET name = ? WHERE id = ?", name, id);
        if (active != null) jdbc.update("UPDATE specialties SET active = ? WHERE id = ?", active, id);
    }
    private static SpecialtyAdmin specialty(java.sql.ResultSet rs) throws java.sql.SQLException {
        return new SpecialtyAdmin(rs.getLong(1), rs.getString(2), rs.getString(3), rs.getInt(4), rs.getBoolean(5), rs.getBoolean(6), rs.getBoolean(7));
    }
}
