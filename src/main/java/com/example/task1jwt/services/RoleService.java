package com.example.task1jwt.services;

import com.example.task1jwt.domains.ERole;
import com.example.task1jwt.domains.Role;
import com.example.task1jwt.repositories.RoleRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class RoleService {
    private final RoleRepository repository;

    public RoleService(RoleRepository repository) {
        this.repository = repository;
    }

    public Optional<Role> getRoleByName(ERole name) {
        return repository.findByName(name);
    }
}
