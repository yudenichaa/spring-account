package dev.nightzen.account.persistance;

import dev.nightzen.account.business.entity.Payment;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends CrudRepository<Payment, Long> {
    List<Payment> findByUserEmailIgnoreCaseOrderByPeriodDesc(String email);
    Optional<Payment> findByUserEmailIgnoreCaseAndPeriod(String email, LocalDate period);
}
