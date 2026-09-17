package co.fcv.citas.adapter.out.persistence;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

interface UserJpaRepository extends JpaRepository<UserEntity, String> {
    boolean existsByEmail(String email);
    boolean existsByDocumentTypeAndDocumentNumber(String documentType, String documentNumber);
    Optional<UserEntity> findByEmail(String email);
}
interface RoleJpaRepository extends JpaRepository<RoleEntity, String> {
    Optional<RoleEntity> findByName(String name);
}
interface RefreshSessionJpaRepository extends JpaRepository<RefreshSessionEntity, String> { }
