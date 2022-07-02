package dev.nightzen.account.exceptions;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AccessDeniedExceptionResponse {
    private String timestamp;
    private int status;
    private String path;
    private String message;
    private String error;
}
