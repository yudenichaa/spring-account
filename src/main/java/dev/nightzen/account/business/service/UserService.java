package dev.nightzen.account.business.service;

import dev.nightzen.account.business.entity.User;
import dev.nightzen.account.constants.ChangeUserAccessOperation;
import dev.nightzen.account.constants.ChangeUserRoleOperation;
import dev.nightzen.account.constants.SecurityAction;
import dev.nightzen.account.constants.UserRole;
import dev.nightzen.account.persistance.UserRepository;
import dev.nightzen.account.presentation.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {
    private static final int MIN_PASSWORD_LENGTH = 12;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private SecurityService securityService;

    private static final Set<String> breachedPasswords = Set.of(
            "PasswordForJanuary", "PasswordForFebruary", "PasswordForMarch", "PasswordForApril",
            "PasswordForMay", "PasswordForJune", "PasswordForJuly", "PasswordForAugust",
            "PasswordForSeptember", "PasswordForOctober", "PasswordForNovember", "PasswordForDecember");

    @Transactional
    public UserResponseDto signup(User signupUser) {
        checkPassword(signupUser.getPassword());

        if (userRepository.findByEmailIgnoreCase(signupUser.getEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User exist!");
        }

        signupUser.setEmail(signupUser.getEmail().toLowerCase(Locale.ROOT));
        signupUser.setPassword(passwordEncoder.encode(signupUser.getPassword()));
        UserRole userRole = userRepository.count() == 0 ? UserRole.ROLE_ADMINISTRATOR : UserRole.ROLE_USER;
        signupUser.addRole(userRole);
        User user = userRepository.save(signupUser);
        securityService.createSecurityEvent(
                SecurityAction.CREATE_USER,
                "Anonymous",
                user.getEmail(),
                "/api/auth/signup");

        return new UserResponseDto(
                user.getId(),
                user.getName(),
                user.getLastname(),
                user.getEmail(),
                getSortedUserRoles(user.getRoles()));
    }

    @Transactional
    public ChangePasswordResponseDto changePassword(User user, String newPassword) {
        checkPassword(newPassword);

        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The passwords must be different!");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        securityService.createSecurityEvent(
                SecurityAction.CHANGE_PASSWORD,
                user.getEmail(),
                user.getEmail(),
                "/api/auth/changepass");

        return new ChangePasswordResponseDto(user.getEmail(), "The password has been updated successfully");
    }

    public List<UserResponseDto> getUsers() {
        return userRepository.findByOrderByIdAsc().stream()
                .map(user -> new UserResponseDto(
                        user.getId(),
                        user.getName(),
                        user.getLastname(),
                        user.getEmail(),
                        getSortedUserRoles(user.getRoles())))
                .collect(Collectors.toList());
    }

    @Transactional
    public DeleteUserResponseDto deleteUser(String email, User currentUser) {
        if (currentUser.getEmail().equalsIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can't remove ADMINISTRATOR role!");
        }

        Optional<User> optionalUser = userRepository.findByEmailIgnoreCase(email);

        if (optionalUser.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found!");
        }

        User user = optionalUser.get();
        userRepository.delete(user);
        securityService.createSecurityEvent(
                SecurityAction.DELETE_USER,
                currentUser.getEmail(),
                user.getEmail(),
                "/api/admin/user");

        return new DeleteUserResponseDto(user.getEmail(), "Deleted successfully!");
    }

    @Transactional
    public UserResponseDto changeRole(ChangeUserRoleRequestDto changeUserRoleRequestDto, User currentUser) {
        UserRole requestUserRole;

        try {
            requestUserRole = UserRole.valueOf("ROLE_" + changeUserRoleRequestDto.getRole());
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found!");
        }

        Optional<User> optionalUser = userRepository.findByEmailIgnoreCase(changeUserRoleRequestDto.getUser());

        if (optionalUser.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found!");
        }

        User user = optionalUser.get();
        SecurityAction securityAction = null;

        if (changeUserRoleRequestDto.getOperation() == ChangeUserRoleOperation.REMOVE) {

            if (user.getRoles().stream().noneMatch(role -> role.equals(requestUserRole))) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The user does not have a role!");
            }

            if (requestUserRole == UserRole.ROLE_ADMINISTRATOR) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can't remove ADMINISTRATOR role!");
            }

            if (user.getRoles().size() == 1) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The user must have at least one role!");
            }

            user.getRoles().remove(requestUserRole);
            securityAction = SecurityAction.REMOVE_ROLE;
        } else {
            if (user.getRoles().stream().anyMatch(UserRole.ROLE_ADMINISTRATOR::equals)) {
                if (requestUserRole != UserRole.ROLE_ADMINISTRATOR) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "The user cannot combine administrative and business roles!");
                }
            } else {
                if (requestUserRole == UserRole.ROLE_ADMINISTRATOR) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "The user cannot combine administrative and business roles!");
                }

                user.addRole(requestUserRole);
                securityAction = SecurityAction.GRANT_ROLE;
            }
        }

        userRepository.save(user);
        securityService.createSecurityEvent(
                securityAction,
                currentUser.getEmail(),
                (securityAction == SecurityAction.GRANT_ROLE ? "Grant role " : "Remove role ") +
                        changeUserRoleRequestDto.getRole() +
                        (securityAction == SecurityAction.GRANT_ROLE ? " to " : " from ") + user.getEmail(),
                "/api/admin/user/role");

        return new UserResponseDto(
                user.getId(),
                user.getName(),
                user.getLastname(),
                user.getEmail(),
                getSortedUserRoles(user.getRoles()));
    }

    @Transactional
    public StatusResponseDto changeAccess(ChangeUserAccessRequestDto changeUserAccessRequestDto, User currentUser) {
        Optional<User> optionalUser = userRepository.findByEmailIgnoreCase(changeUserAccessRequestDto.getUser());

        if (optionalUser.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found!");
        }

        User user = optionalUser.get();
        SecurityAction securityAction = null;

        if (changeUserAccessRequestDto.getOperation() == ChangeUserAccessOperation.LOCK) {
            if (user.getRoles().stream().anyMatch(UserRole.ROLE_ADMINISTRATOR::equals)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can't lock the ADMINISTRATOR!");
            }

            user.setLocked(true);
            securityAction = SecurityAction.LOCK_USER;
        } else {
            user.setLocked(false);
            securityAction = SecurityAction.UNLOCK_USER;
        }

        userRepository.save(user);
        securityService.createSecurityEvent(
                securityAction,
                currentUser.getEmail(),
                (securityAction == SecurityAction.LOCK_USER ? "Lock" : "Unlock") + " user " + user.getEmail(),
                "/api/admin/user/role");

        return new StatusResponseDto("User " + user.getEmail() + " " + (user.getLocked() ? "locked!" : "unlocked!"));
    }

    private void checkPassword(String password) throws ResponseStatusException {
        if (password.length() < MIN_PASSWORD_LENGTH) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password length must be 12 chars minimum!");
        }

        if (breachedPasswords.contains(password)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The password is in the hacker's database!");
        }
    }

    private Set<UserRole> getSortedUserRoles(Set<UserRole> roles) {
        return roles.stream()
                .sorted(Comparator.comparing(Enum::toString))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
