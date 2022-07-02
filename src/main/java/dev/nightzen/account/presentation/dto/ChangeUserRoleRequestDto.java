package dev.nightzen.account.presentation.dto;

import dev.nightzen.account.constants.ChangeUserRoleOperation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangeUserRoleRequestDto {
    @NotNull
    private String user;

    @NotNull
    private String role;

    @NotNull
    private ChangeUserRoleOperation operation;
}
