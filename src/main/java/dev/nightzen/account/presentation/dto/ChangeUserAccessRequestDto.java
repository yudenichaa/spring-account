package dev.nightzen.account.presentation.dto;

import dev.nightzen.account.constants.ChangeUserAccessOperation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangeUserAccessRequestDto {
    @NotBlank
    private String user;

    @NotNull
    private ChangeUserAccessOperation operation;
}
