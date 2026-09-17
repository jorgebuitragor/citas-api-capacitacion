package co.fcv.citas.domain.auth;

import java.util.Set;

public record UserAccount(
        String id, String firstName, String lastName, String documentType, String documentNumber,
        String email, String phone, String passwordHash, boolean active, Set<Role> roles) {
}
