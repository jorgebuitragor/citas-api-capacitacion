package co.fcv.citas.adapter.out.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity @Table(name = "roles")
class RoleEntity {
    @Id Short id;
    @Column(name = "code") String code;
    String name;
}
