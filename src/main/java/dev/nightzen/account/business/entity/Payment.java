package dev.nightzen.account.business.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.time.LocalDate;

@Entity
@Table(indexes = {@Index(columnList = "period")})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    @Id
    @GeneratedValue
    private Long id;

    @Column
    @NotNull
    private LocalDate period;

    @Column
    @NotNull
    @Positive
    private Long salary;

    @OneToOne
    @JoinColumn(name = "email", nullable = false)
    private User user;
}
