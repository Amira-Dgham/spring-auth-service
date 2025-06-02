package com.example.mobelite.first_spring_boot_rest_api.config;


import com.example.mobelite.first_spring_boot_rest_api.model.ERole;
import com.example.mobelite.first_spring_boot_rest_api.model.Role;
import com.example.mobelite.first_spring_boot_rest_api.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    public DataInitializer(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (roleRepository.count() == 0) {
            Role userRole = new Role();
            userRole.setName(String.valueOf(ERole.ROLE_USER));
            roleRepository.save(userRole);

            Role adminRole = new Role();
            adminRole.setName(String.valueOf(ERole.ROLE_ADMIN));
            roleRepository.save(adminRole);

            System.out.println("Roles have been initialized");
        }
    }
}