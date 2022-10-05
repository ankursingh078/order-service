package com.microservices.project.external.client;

import com.microservices.project.exception.CustomOrderException;
import com.microservices.project.external.request.PaymentRequest;
import com.microservices.project.external.response.PaymentResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "PAYMENT-SERVICE/payment")
@CircuitBreaker(name = "external", fallbackMethod = "fallback")
public interface PaymentService {

    @PostMapping
    ResponseEntity<Long> doPayment(@RequestBody PaymentRequest paymentRequest);

    /*@GetMapping("/order/{orderId}")
    ResponseEntity<PaymentResponse> getPaymentDetailsByOrderId(@PathVariable long orderId);*/

    default void fallback(Exception e) {
        throw new CustomOrderException("Payment Service is not available!",
                "UNAVAILABLE",
                500);
    }

}
