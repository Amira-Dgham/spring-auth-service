
package com.example.mobelite.first_spring_boot_rest_api.dto.payload;

import com.example.mobelite.first_spring_boot_rest_api.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProfileResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String username;
    private String email;
    private Set<Role> roles;
    private boolean enabled;
}