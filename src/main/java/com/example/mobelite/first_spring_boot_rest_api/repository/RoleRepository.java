package com.example.mobelite.first_spring_boot_rest_api.repository;

import com.example.mobelite.first_spring_boot_rest_api.model.ERole;
import com.example.mobelite.first_spring_boot_rest_api.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByName(String name);
}