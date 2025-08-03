package com.gabrielluciano.rinha.infra.service;

import com.gabrielluciano.rinha.domain.model.PaymentProcessorType;
import com.gabrielluciano.rinha.infra.redis.RedisProcessorRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;

@ApplicationScoped
public class PaymentProcessorDecisionService {

    private final RedisProcessorRepository processorRepository;

    public PaymentProcessorDecisionService(RedisProcessorRepository processorRepository) {
        this.processorRepository = processorRepository;
    }

    public PaymentProcessorType getActiveProcessor() {
        return Optional.ofNullable(processorRepository.getActiveProcessor())
                .orElse(PaymentProcessorType.DEFAULT);
    }
}
