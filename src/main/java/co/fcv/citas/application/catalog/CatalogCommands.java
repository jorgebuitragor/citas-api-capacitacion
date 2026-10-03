package co.fcv.citas.application.catalog;

public final class CatalogCommands {
    private CatalogCommands() { }

    public record CreateEps(String code, String name) { }
    public record UpdateEps(long id, String name, Boolean active) { }
    public record CreatePlan(long epsId, long regimeId, String code, String name) { }
    public record UpdatePlan(long epsId, long planId, String name, Boolean active) { }
    public record CreateSpecialty(String code, String name, int durationMinutes, boolean general, boolean requiresAdminApproval) { }
    public record UpdateSpecialty(long id, String name, Boolean active) { }
}
