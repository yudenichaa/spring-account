package dev.nightzen.account.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChangePasswordResponseDto {
    private String email;
    private String status;
}
