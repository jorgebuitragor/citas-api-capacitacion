package co.fcv.citas.adapter.out.persistence;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

interface UserJpaRepository extends JpaRepository<UserEntity, Long> {
    boolean existsByEmail(String email);
    boolean existsByDocumentTypeAndDocumentNumber(String documentType, String documentNumber);
    Optional<UserEntity> findByEmail(String email);
}
interface RoleJpaRepository extends JpaRepository<RoleEntity, Short> {
    Optional<RoleEntity> findByCode(String code);
}
interface RefreshSessionJpaRepository extends JpaRepository<RefreshSessionEntity, Long> {
    Optional<RefreshSessionEntity> findByTokenHash(String tokenHash);
}
