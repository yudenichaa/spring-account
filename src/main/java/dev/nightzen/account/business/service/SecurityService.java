package dev.nightzen.account.business.service;

import dev.nightzen.account.business.entity.SecurityEvent;
import dev.nightzen.account.business.entity.User;
import dev.nightzen.account.constants.SecurityAction;
import dev.nightzen.account.constants.UserRole;
import dev.nightzen.account.persistance.SecurityEventRepository;
import dev.nightzen.account.persistance.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SecurityService {
    private static final int MAX_FAILED_ATTEMPTS = 5;

    @Autowired
    private SecurityEventRepository securityEventRepository;

    @Autowired
    private UserRepository userRepository;

    private final Map<String, Integer> loginAttempts = new ConcurrentHashMap<>();

    public void loginSucceeded(String email) {
        loginAttempts.put(email, 0);
    }

    @Transactional
    public void loginFailed(String email, HttpServletRequest request) {
        createSecurityEvent(
                SecurityAction.LOGIN_FAILED,
                email,
                request.getRequestURI(),
                request.getRequestURI());

        Optional<User> optionalUser = userRepository.findByEmailIgnoreCase(email);

        if (optionalUser.isEmpty()) {
            return;
        }

        User user = optionalUser.get();

        if (user.getLocked()) {
            return;
        }

        int attempts = loginAttempts.getOrDefault(user.getEmail(), 0) + 1;

        if (attempts == MAX_FAILED_ATTEMPTS) {
            loginAttempts.put(user.getEmail(), 0);
            createSecurityEvent(
                    SecurityAction.BRUTE_FORCE,
                    user.getEmail(),
                    request.getRequestURI(),
                    request.getRequestURI());

            if (user.getRoles().stream().noneMatch(UserRole.ROLE_ADMINISTRATOR::equals)) {
                user.setLocked(true);
                userRepository.save(user);
                createSecurityEvent(
                        SecurityAction.LOCK_USER,
                        user.getEmail(),
                        "Lock user " + user.getEmail(),
                        request.getRequestURI());
            }
        } else {
            loginAttempts.put(user.getEmail(), attempts);
        }
    }

    public void createSecurityEvent(SecurityAction action, String subject, String object, String path) {
        SecurityEvent securityEvent = new SecurityEvent();
        securityEvent.setDate(LocalDateTime.now());
        securityEvent.setAction(action);
        securityEvent.setSubject(subject);
        securityEvent.setObject(object);
        securityEvent.setPath(path);
        securityEventRepository.save(securityEvent);
    }

    public Iterable<SecurityEvent> getSecurityEvents() {
        return securityEventRepository.findByOrderByIdAsc();
    }
}
