package dev.nightzen.account.presentation.controller;

import dev.nightzen.account.business.entity.User;
import dev.nightzen.account.business.service.UserService;
import dev.nightzen.account.presentation.dto.*;
import dev.nightzen.account.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/")
public class AuthController {
    @Autowired
    private UserService userService;

    @PostMapping("auth/signup")
    public UserResponseDto signup(@RequestBody @Valid User user) {
        return userService.signup(user);
    }

    @PostMapping("auth/changepass")
    public ChangePasswordResponseDto signup(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                            @RequestBody @Valid ChangePasswordRequestDto changePasswordRequestDto) {
        return userService.changePassword(
                userDetails.getUser(),
                changePasswordRequestDto.getNewPassword());
    }

    @GetMapping("admin/user")
    public List<UserResponseDto> getUsers() {
        return userService.getUsers();
    }

    @DeleteMapping("admin/user/{email}")
    public DeleteUserResponseDto deleteUser(@PathVariable String email,
                                            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return userService.deleteUser(email, userDetails.getUser());
    }

    @PutMapping("admin/user/role")
    public UserResponseDto changeRole(@RequestBody @Valid ChangeUserRoleRequestDto changeUserRoleRequestDto,
                                      @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return userService.changeRole(changeUserRoleRequestDto, userDetails.getUser());
    }

    @PutMapping("admin/user/access")
    public StatusResponseDto changeAccess(@Valid @RequestBody ChangeUserAccessRequestDto changeUserAccessRequestDto,
                                          @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return userService.changeAccess(changeUserAccessRequestDto, userDetails.getUser());
    }
}
