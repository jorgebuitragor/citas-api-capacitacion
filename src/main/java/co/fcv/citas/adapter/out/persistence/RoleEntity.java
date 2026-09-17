package co.fcv.citas.adapter.out.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity @Table(name = "roles")
class RoleEntity {
    @Id String id;
    String name;
}
