package dev.nightzen.account.business.service;

import dev.nightzen.account.business.entity.Payment;
import dev.nightzen.account.business.entity.User;
import dev.nightzen.account.persistance.PaymentRepository;
import dev.nightzen.account.persistance.UserRepository;
import dev.nightzen.account.presentation.dto.PaymentDto;
import dev.nightzen.account.presentation.dto.PaymentResponseDto;
import dev.nightzen.account.presentation.dto.StatusResponseDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PaymentService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Transactional
    public StatusResponseDto addPayments(List<PaymentDto> payments) {
        Map<String, String> uniquePayments = new HashMap<>();

        for (PaymentDto paymentDto : payments) {
            if (Objects.equals(uniquePayments.get(paymentDto.getPeriod()), paymentDto.getEmployee())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
            }

            savePayment(paymentDto, true);
            uniquePayments.put(paymentDto.getPeriod(), paymentDto.getEmployee());
        }

        return new StatusResponseDto("Added successfully!");
    }

    public StatusResponseDto changePayment(PaymentDto paymentDto) {
        savePayment(paymentDto, false);
        return new StatusResponseDto("Updated successfully!");
    }

    private void savePayment(PaymentDto paymentDto, boolean unique) {
        Optional<User> optionalUser = userRepository.findByEmailIgnoreCase(paymentDto.getEmployee());

        if (optionalUser.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        User user = optionalUser.get();
        LocalDate paymentDate = parsePaymentPeriod(paymentDto.getPeriod());
        Optional<Payment> optionalPayment = paymentRepository.findByUserEmailIgnoreCaseAndPeriod(
                user.getEmail(), paymentDate);
        Payment payment;

        if (optionalPayment.isEmpty()) {
            payment = new Payment();
            payment.setPeriod(paymentDate);
            payment.setSalary(paymentDto.getSalary());
            payment.setUser(user);
        } else {
            if (unique) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
            }

            payment = optionalPayment.get();
            payment.setSalary(paymentDto.getSalary());
        }

        paymentRepository.save(payment);
    }

    public List<PaymentResponseDto> getPayments(User user) {
        List<Payment> payments = paymentRepository.findByUserEmailIgnoreCaseOrderByPeriodDesc(user.getEmail());

        return payments.stream()
                .map(payment -> new PaymentResponseDto(
                        user.getName(),
                        user.getLastname(),
                        getFormattedPaymentPeriod(payment.getPeriod()),
                        getFormattedMoney(payment.getSalary())
                ))
                .collect(Collectors.toList());
    }

    public PaymentResponseDto getPayment(User user, String period) {
        LocalDate paymentDate = parsePaymentPeriod(period);
        Optional<Payment> optionalPayment = paymentRepository.findByUserEmailIgnoreCaseAndPeriod(
                user.getEmail(), paymentDate);

        if (optionalPayment.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        Payment payment = optionalPayment.get();
        return new PaymentResponseDto(
                user.getName(),
                user.getLastname(),
                getFormattedPaymentPeriod(payment.getPeriod()),
                getFormattedMoney(payment.getSalary())
        );
    }

    private String getFormattedMoney(long cents) {
        return String.format("%d dollar(s) %d cent(s)", cents / 100, cents % 100);
    }

    private String getFormattedPaymentPeriod(LocalDate period) {
        return period.format(DateTimeFormatter.ofPattern("MMMM-yyyy"));
    }

    private LocalDate parsePaymentPeriod(String period) {
        return YearMonth.parse(period, DateTimeFormatter.ofPattern("MM-yyyy")).atDay(1);
    }
}
