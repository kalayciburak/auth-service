package com.kalayciburak.authservice.repository;

import com.kalayciburak.authservice.model.entity.Role;
import com.kalayciburak.authservice.model.enums.RoleType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(RoleType name);
}
