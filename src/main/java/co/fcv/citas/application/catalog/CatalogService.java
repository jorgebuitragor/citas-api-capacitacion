package co.fcv.citas.application.catalog;

import co.fcv.citas.application.catalog.CatalogCommands.CreateEps;
import co.fcv.citas.application.catalog.CatalogCommands.CreatePlan;
import co.fcv.citas.application.catalog.CatalogCommands.CreateSpecialty;
import co.fcv.citas.application.catalog.CatalogCommands.UpdateEps;
import co.fcv.citas.application.catalog.CatalogCommands.UpdatePlan;
import co.fcv.citas.application.catalog.CatalogCommands.UpdateSpecialty;
import co.fcv.citas.application.catalog.CatalogPorts.CatalogRepository;
import co.fcv.citas.application.catalog.CatalogPorts.Eps;
import co.fcv.citas.application.catalog.CatalogPorts.InsuranceRegime;
import co.fcv.citas.application.catalog.CatalogPorts.PlanDetail;
import co.fcv.citas.application.catalog.CatalogPorts.SpecialtyAdmin;
import java.util.List;
import java.util.Set;
import org.springframework.transaction.annotation.Transactional;

/**
 * HU-006/HU-007/HU-008 (DEC-009). Ningún método de este servicio borra físicamente un
 * catálogo; solo alterna {@code active}. {@code code} y, para especialidades,
 * {@code durationMinutes}/{@code general}/{@code requiresAdminApproval} son inmutables
 * tras la creación (decisión de ingeniería de DEC-009, no solicitada explícitamente por la HU).
 */
public class CatalogService {
    private static final Set<Integer> VALID_DURATIONS = Set.of(30, 60);
    private final CatalogRepository catalog;

    public CatalogService(CatalogRepository catalog) { this.catalog = catalog; }

    public List<InsuranceRegime> regimes() { return catalog.findInsuranceRegimes(); }

    public List<Eps> listEps() { return catalog.findAllEps(); }

    @Transactional
    public Eps createEps(CreateEps command) {
        String code = requireCode(command.code());
        String name = requireName(command.name());
        if (catalog.epsCodeExists(code)) throw duplicate("Ya existe una EPS con ese código.");
        long id = catalog.createEps(code, name);
        return catalog.findEpsById(id).orElseThrow();
    }

    @Transactional
    public Eps updateEps(UpdateEps command) {
        catalog.findEpsById(command.id()).orElseThrow(() -> notFound("La EPS no existe."));
        catalog.updateEps(command.id(), command.name() == null ? null : requireName(command.name()), command.active());
        return catalog.findEpsById(command.id()).orElseThrow();
    }

    public List<PlanDetail> listPlans(long epsId) {
        catalog.findEpsById(epsId).orElseThrow(() -> notFound("La EPS no existe."));
        return catalog.findPlansByEps(epsId);
    }

    @Transactional
    public PlanDetail createPlan(CreatePlan command) {
        catalog.findEpsById(command.epsId()).orElseThrow(() -> notFound("La EPS no existe."));
        if (!catalog.regimeExists(command.regimeId())) throw invalid("El régimen de afiliación no existe.");
        String code = requireCode(command.code());
        String name = requireName(command.name());
        if (catalog.planCodeExists(command.epsId(), code)) throw duplicate("Ya existe un plan con ese código para esta EPS.");
        long id = catalog.createPlan(command.epsId(), command.regimeId(), code, name);
        return catalog.findPlanById(command.epsId(), id).orElseThrow();
    }

    @Transactional
    public PlanDetail updatePlan(UpdatePlan command) {
        catalog.findEpsById(command.epsId()).orElseThrow(() -> notFound("La EPS no existe."));
        catalog.findPlanById(command.epsId(), command.planId()).orElseThrow(() -> notFound("El plan no existe para esta EPS."));
        catalog.updatePlan(command.planId(), command.name() == null ? null : requireName(command.name()), command.active());
        return catalog.findPlanById(command.epsId(), command.planId()).orElseThrow();
    }

    public List<SpecialtyAdmin> listSpecialties() { return catalog.findAllSpecialties(); }

    @Transactional
    public SpecialtyAdmin createSpecialty(CreateSpecialty command) {
        if (!VALID_DURATIONS.contains(command.durationMinutes())) throw invalid("La duración debe ser 30 o 60 minutos.");
        String code = requireCode(command.code());
        String name = requireName(command.name());
        if (catalog.specialtyCodeExists(code)) throw duplicate("Ya existe una especialidad con ese código.");
        long id = catalog.createSpecialty(code, name, command.durationMinutes(), command.general(), command.requiresAdminApproval());
        return catalog.findSpecialtyById(id).orElseThrow();
    }

    @Transactional
    public SpecialtyAdmin updateSpecialty(UpdateSpecialty command) {
        catalog.findSpecialtyById(command.id()).orElseThrow(() -> notFound("La especialidad no existe."));
        catalog.updateSpecialty(command.id(), command.name() == null ? null : requireName(command.name()), command.active());
        return catalog.findSpecialtyById(command.id()).orElseThrow();
    }

    private static String requireCode(String value) {
        if (value == null || value.trim().isEmpty()) throw invalid("El código es obligatorio.");
        return value.trim();
    }
    private static String requireName(String value) {
        if (value == null || value.trim().isEmpty()) throw invalid("El nombre es obligatorio.");
        return value.trim();
    }
    private static CatalogException invalid(String message) { return new CatalogException(CatalogException.Reason.INVALID_REQUEST, message); }
    private static CatalogException notFound(String message) { return new CatalogException(CatalogException.Reason.NOT_FOUND, message); }
    private static CatalogException duplicate(String message) { return new CatalogException(CatalogException.Reason.DUPLICATE_CODE, message); }
}
