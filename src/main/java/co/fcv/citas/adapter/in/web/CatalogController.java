package co.fcv.citas.adapter.in.web;

import co.fcv.citas.application.catalog.CatalogCommands;
import co.fcv.citas.application.catalog.CatalogPorts;
import co.fcv.citas.application.catalog.CatalogService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class CatalogController {
    private final CatalogService catalog;

    public CatalogController(CatalogService catalog) { this.catalog = catalog; }

    @GetMapping("/catalogs/insurance-regimes")
    public List<RegimeResponse> regimes() {
        return catalog.regimes().stream().map(CatalogController::regimeResponse).toList();
    }

    @GetMapping("/admin/catalogs/eps")
    public List<EpsResponse> listEps() { return catalog.listEps().stream().map(CatalogController::epsResponse).toList(); }

    @PostMapping("/admin/catalogs/eps")
    public ResponseEntity<EpsResponse> createEps(@Valid @RequestBody CreateEpsRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(epsResponse(catalog.createEps(new CatalogCommands.CreateEps(request.code(), request.name()))));
    }

    @PatchMapping("/admin/catalogs/eps/{id}")
    public EpsResponse updateEps(@PathVariable @Positive long id, @RequestBody UpdateCatalogRequest request) {
        return epsResponse(catalog.updateEps(new CatalogCommands.UpdateEps(id, request.name(), request.active())));
    }

    @GetMapping("/admin/catalogs/eps/{epsId}/plans")
    public List<PlanResponse> listPlans(@PathVariable @Positive long epsId) {
        return catalog.listPlans(epsId).stream().map(CatalogController::planResponse).toList();
    }

    @PostMapping("/admin/catalogs/eps/{epsId}/plans")
    public ResponseEntity<PlanResponse> createPlan(@PathVariable @Positive long epsId, @Valid @RequestBody CreatePlanRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(planResponse(catalog.createPlan(
                new CatalogCommands.CreatePlan(epsId, request.regimeId(), request.code(), request.name()))));
    }

    @PatchMapping("/admin/catalogs/eps/{epsId}/plans/{planId}")
    public PlanResponse updatePlan(@PathVariable @Positive long epsId, @PathVariable @Positive long planId, @RequestBody UpdateCatalogRequest request) {
        return planResponse(catalog.updatePlan(new CatalogCommands.UpdatePlan(epsId, planId, request.name(), request.active())));
    }

    @GetMapping("/admin/catalogs/specialties")
    public List<SpecialtyAdminResponse> listSpecialties() {
        return catalog.listSpecialties().stream().map(CatalogController::specialtyResponse).toList();
    }

    @PostMapping("/admin/catalogs/specialties")
    public ResponseEntity<SpecialtyAdminResponse> createSpecialty(@Valid @RequestBody CreateSpecialtyRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(specialtyResponse(catalog.createSpecialty(new CatalogCommands.CreateSpecialty(
                request.code(), request.name(), request.durationMinutes(), request.general(), request.requiresAdminApproval()))));
    }

    @PatchMapping("/admin/catalogs/specialties/{id}")
    public SpecialtyAdminResponse updateSpecialty(@PathVariable @Positive long id, @RequestBody UpdateCatalogRequest request) {
        return specialtyResponse(catalog.updateSpecialty(new CatalogCommands.UpdateSpecialty(id, request.name(), request.active())));
    }

    private static RegimeResponse regimeResponse(CatalogPorts.InsuranceRegime value) { return new RegimeResponse(id(value.id()), value.code(), value.name()); }
    private static EpsResponse epsResponse(CatalogPorts.Eps value) { return new EpsResponse(id(value.id()), value.code(), value.name(), value.active()); }
    private static PlanResponse planResponse(CatalogPorts.PlanDetail value) {
        return new PlanResponse(id(value.id()), id(value.epsId()), value.code(), value.name(), value.active(), regimeResponse(value.regime()));
    }
    private static SpecialtyAdminResponse specialtyResponse(CatalogPorts.SpecialtyAdmin value) {
        return new SpecialtyAdminResponse(id(value.id()), value.code(), value.name(), value.durationMinutes(), value.general(), value.requiresAdminApproval(), value.active());
    }
    private static String id(long value) { return Long.toString(value); }

    public record RegimeResponse(String id, String code, String name) { }
    public record EpsResponse(String id, String code, String name, boolean active) { }
    public record PlanResponse(String id, String epsId, String code, String name, boolean active, RegimeResponse regime) { }
    public record SpecialtyAdminResponse(String id, String code, String name, int durationMinutes, boolean general, boolean requiresAdminApproval, boolean active) { }

    public record CreateEpsRequest(@NotNull @Size(min = 1, max = 30) String code, @NotNull @Size(min = 1, max = 150) String name) { }
    public record CreatePlanRequest(@NotNull @Positive Long regimeId, @NotNull @Size(min = 1, max = 50) String code, @NotNull @Size(min = 1, max = 150) String name) { }
    public record CreateSpecialtyRequest(@NotNull @Size(min = 1, max = 50) String code, @NotNull @Size(min = 1, max = 150) String name,
                                         int durationMinutes, boolean general, boolean requiresAdminApproval) { }
    public record UpdateCatalogRequest(String name, Boolean active) { }
}
