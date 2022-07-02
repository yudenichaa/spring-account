package dev.nightzen.account.business.entity;

import dev.nightzen.account.constants.Regexp;
import dev.nightzen.account.constants.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "auth_user", indexes = {@Index(columnList = "email")})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue
    private Long id;

    @Column
    @NotBlank
    private String name;

    @Column
    @NotBlank
    private String lastname;

    @Column(unique = true)
    @NotBlank
    @Pattern(regexp = Regexp.EMPLOYEE_EMAIL)
    private String email;

    @Column
    @NotBlank
    private String password;

    @Column
    @ElementCollection(fetch = FetchType.EAGER)
    Set<UserRole> roles = new HashSet<>();

    @Column
    Boolean locked = false;

    public void addRole(UserRole role) {
        roles.add(role);
    }
}
