package com.example.mobelite.first_spring_boot_rest_api.utils;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Configuration
public class MigrationHelper {

    @Autowired
    private Environment env;

    @Autowired
    private ApplicationContext context;

    @Bean
    @ConditionalOnProperty(name = "app.migration.create-empty", havingValue = "true")
    public CommandLineRunner createEmptyMigration() {
        return args -> {
            try {
                String activeProfile = env.getProperty("spring.profiles.active", "dev");
                String description = env.getProperty("app.migration.description", "manual_migration");

                // Create migration directories if they don't exist
                Path commonMigrationPath = Paths.get("src/main/resources/db/migration");
                Path envMigrationPath = Paths.get("src/main/resources/db/migration/" + activeProfile);

                if (!Files.exists(commonMigrationPath)) {
                    Files.createDirectories(commonMigrationPath);
                }

                if (!Files.exists(envMigrationPath)) {
                    Files.createDirectories(envMigrationPath);
                }

                // Generate version based on timestamp
                String version = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

                // Create empty migration file
                Path migrationFile = envMigrationPath.resolve("V" + version + "__" + description.toLowerCase().replace(' ', '_') + ".sql");
                Files.write(migrationFile,
                        ("-- Migration: " + description + "\n" +
                                "-- Environment: " + activeProfile + "\n" +
                                "-- Created: " + LocalDateTime.now() + "\n\n").getBytes());

                System.out.println("Created empty migration file: " + migrationFile);

                // Shutdown application after creating the file
                ((ConfigurableApplicationContext) context).close();
            } catch (Exception e) {
                System.err.println("Error creating migration file: " + e.getMessage());
                e.printStackTrace();
            }
        };
    }
}