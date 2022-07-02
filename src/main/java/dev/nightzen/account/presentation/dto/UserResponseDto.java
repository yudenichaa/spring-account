package dev.nightzen.account.presentation.dto;

import dev.nightzen.account.constants.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Set;

@Data
@AllArgsConstructor
public class UserResponseDto {
    private Long id;
    private String name;
    private String lastname;
    private String email;
    Set<UserRole> roles;
}
