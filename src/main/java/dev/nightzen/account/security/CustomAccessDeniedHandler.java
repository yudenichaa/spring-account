package dev.nightzen.account.security;

import dev.nightzen.account.business.service.SecurityService;
import dev.nightzen.account.constants.SecurityAction;
import dev.nightzen.account.exceptions.AccessDeniedExceptionResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {
    @Autowired
    private SecurityService securityService;

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException exception) throws IOException {
        String json = new ObjectMapper().writeValueAsString(new AccessDeniedExceptionResponse(
                LocalDateTime.now().toString(),
                HttpStatus.FORBIDDEN.value(),
                request.getRequestURI(),
                "Access Denied!",
                "Forbidden"));
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.getWriter().write(json);

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        securityService.createSecurityEvent(
                SecurityAction.ACCESS_DENIED,
                email,
                request.getRequestURI(),
                request.getRequestURI());
    }
}