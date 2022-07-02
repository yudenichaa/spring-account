package dev.nightzen.account.presentation.controller;

import dev.nightzen.account.business.entity.User;
import dev.nightzen.account.business.service.PaymentService;
import dev.nightzen.account.constants.Regexp;
import dev.nightzen.account.exceptions.PaymentConstraintViolationExceptionResponse;
import dev.nightzen.account.presentation.dto.PaymentDto;
import dev.nightzen.account.presentation.dto.StatusResponseDto;
import dev.nightzen.account.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;
import javax.validation.Valid;
import javax.validation.constraints.Pattern;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@Validated
@RequestMapping("/api/")
public class PaymentController {
    @Autowired
    private PaymentService paymentService;

    @PostMapping("acct/payments")
    public StatusResponseDto addPayments(@RequestBody List<@Valid PaymentDto> payments) {
        return paymentService.addPayments(payments);
    }

    @PutMapping("acct/payments")
    public StatusResponseDto changePayment(@Valid @RequestBody PaymentDto payment) {
        return paymentService.changePayment(payment);
    }

    @GetMapping("empl/payment")
    public Object getPayments(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                @RequestParam(required = false)
                                                @Pattern(regexp = Regexp.PAYMENT_PERIOD) String period) {
        User user = userDetails.getUser();

        if (period == null) {
            return paymentService.getPayments(user);
        } else {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-yyyy");
            return paymentService.getPayment(user, period);
        }
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<PaymentConstraintViolationExceptionResponse> handleConstraintViolationException(
            ConstraintViolationException ex,
            HttpServletRequest req) {
        return new ResponseEntity<>(
                new PaymentConstraintViolationExceptionResponse(
                        ZonedDateTime.now(),
                        HttpStatus.BAD_REQUEST.value(),
                        req.getRequestURI(),
                        ex.getMessage(),
                        "Bad Request"),
                HttpStatus.BAD_REQUEST);
    }
}
