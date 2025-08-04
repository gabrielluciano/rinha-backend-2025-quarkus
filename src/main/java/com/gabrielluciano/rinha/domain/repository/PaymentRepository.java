package com.gabrielluciano.rinha.domain.repository;

import com.gabrielluciano.rinha.domain.model.Payment;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.Instant;
import java.util.List;

@ApplicationScoped
public interface PaymentRepository {

    void savePayment(Payment payment);

    List<Payment> getPayments(Instant from, Instant to);
}
