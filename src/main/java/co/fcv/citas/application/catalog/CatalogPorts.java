package co.fcv.citas.application.catalog;

import java.util.List;
import java.util.Optional;

public final class CatalogPorts {
    private CatalogPorts() { }

    public interface CatalogRepository {
        List<InsuranceRegime> findInsuranceRegimes();
        boolean regimeExists(long regimeId);

        List<Eps> findAllEps();
        Optional<Eps> findEpsById(long id);
        boolean epsCodeExists(String code);
        long createEps(String code, String name);
        void updateEps(long id, String name, Boolean active);

        List<PlanDetail> findPlansByEps(long epsId);
        Optional<PlanDetail> findPlanById(long epsId, long planId);
        boolean planCodeExists(long epsId, String code);
        long createPlan(long epsId, long regimeId, String code, String name);
        void updatePlan(long planId, String name, Boolean active);

        List<SpecialtyAdmin> findAllSpecialties();
        Optional<SpecialtyAdmin> findSpecialtyById(long id);
        boolean specialtyCodeExists(String code);
        long createSpecialty(String code, String name, int durationMinutes, boolean general, boolean requiresAdminApproval);
        void updateSpecialty(long id, String name, Boolean active);
    }

    public record InsuranceRegime(long id, String code, String name) { }
    public record Eps(long id, String code, String name, boolean active) { }
    public record PlanDetail(long id, long epsId, String code, String name, boolean active, InsuranceRegime regime) { }
    public record SpecialtyAdmin(long id, String code, String name, int durationMinutes, boolean general, boolean requiresAdminApproval, boolean active) { }
}
