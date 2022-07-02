package dev.nightzen.account.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PaymentResponseDto {
    private String name;
    private String lastname;
    private String period;
    private String salary;
}
