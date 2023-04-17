package com.example.task1jwt.repositories;

import com.example.task1jwt.domains.ERole;
import com.example.task1jwt.domains.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(ERole name);
}
