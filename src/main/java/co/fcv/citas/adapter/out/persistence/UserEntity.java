package co.fcv.citas.adapter.out.persistence;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity @Table(name = "users")
class UserEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) Long id;
    @Column(name = "first_name") String firstName;
    @Column(name = "last_name") String lastName;
    @Column(name = "document_type") String documentType;
    @Column(name = "document_number") String documentNumber;
    String email;
    String phone;
    @Column(name = "password_hash") String passwordHash;
    boolean active;
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
    Set<RoleEntity> roles = new HashSet<>();
}
