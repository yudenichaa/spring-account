package dev.nightzen.account.business.entity;

import dev.nightzen.account.constants.SecurityAction;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SecurityEvent {

    @Id
    @GeneratedValue
    private Long id;

    @Column
    private LocalDateTime date;

    @Column
    private SecurityAction action;

    @Column
    private String subject;

    @Column
    private String object;

    @Column
    private String path;
}
