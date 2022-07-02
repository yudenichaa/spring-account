package dev.nightzen.account.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DeleteUserResponseDto {
    private String user;
    private String status;
}
