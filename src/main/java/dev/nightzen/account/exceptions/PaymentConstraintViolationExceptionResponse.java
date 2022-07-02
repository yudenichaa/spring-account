package dev.nightzen.account.exceptions;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
@AllArgsConstructor
public class PaymentConstraintViolationExceptionResponse {
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private ZonedDateTime timestamp;
    private int status;
    private String path;
    private String message;
    private String error;
}
