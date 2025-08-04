package com.gabrielluciano.rinha.infra.service;

import com.gabrielluciano.rinha.domain.model.PaymentProcessorType;
import com.gabrielluciano.rinha.infra.client.DefaultPaymentProcessorClient;
import com.gabrielluciano.rinha.infra.client.FallbackPaymentProcessorClient;
import com.gabrielluciano.rinha.infra.client.PaymentProcessorClient;
import com.gabrielluciano.rinha.infra.client.model.PaymentProcessorHealthResponse;
import com.gabrielluciano.rinha.infra.exception.PaymentProcessorException;
import com.gabrielluciano.rinha.infra.redis.RedisProcessorRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.RestResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

@ApplicationScoped
public class PaymentProcessorDecisionService {

    private static final Logger log = LoggerFactory.getLogger(PaymentProcessorDecisionService.class);

    private final RedisProcessorRepository processorRepository;

    @Inject
    @RestClient
    private DefaultPaymentProcessorClient defaultPaymentProcessorClient;

    @Inject
    @RestClient
    private FallbackPaymentProcessorClient fallbackPaymentProcessorClient;

    public PaymentProcessorDecisionService(RedisProcessorRepository processorRepository) {
        this.processorRepository = processorRepository;
    }

    public PaymentProcessorType getActiveProcessor() {
        return Optional.ofNullable(processorRepository.getActiveProcessor())
                .orElse(PaymentProcessorType.DEFAULT);
    }

    public void updateActiveProcessor() {
        // TODO: Implement distributed locking to avoid call processor health check concurrently
        var defaultHealth = getProcessorHealth(defaultPaymentProcessorClient);
        var fallbackHealth = getProcessorHealth(fallbackPaymentProcessorClient);
        var processorType = determineActiveProcessor(defaultHealth, fallbackHealth);
        processorRepository.saveProcessor(processorType);
    }

    private PaymentProcessorType determineActiveProcessor(
            PaymentProcessorHealthResponse defaultHealth,
            PaymentProcessorHealthResponse fallbackHealth) {
        boolean defaultHealthy = !defaultHealth.failing();
        boolean fallbackHealthy = !fallbackHealth.failing();
        if (defaultHealthy && fallbackHealthy) {
            log.debug("Both payment processors are healthy, calculating active based on response times.");
            // TODO: Implement logic to choose based on response times, for now we default to default processor
            return PaymentProcessorType.DEFAULT;
        } else if (defaultHealthy) {
            log.debug("Default payment processor is healthy, using it.");
            return PaymentProcessorType.DEFAULT;
        } else if (fallbackHealthy) {
            log.debug("Fallback payment processor is healthy, using it.");
            return PaymentProcessorType.FALLBACK;
        } else {
            log.debug("Both payment processors are unhealthy, defaulting to fallback.");
            return PaymentProcessorType.FALLBACK;
        }
    }

    private PaymentProcessorHealthResponse getProcessorHealth(PaymentProcessorClient client) {
        try (RestResponse<PaymentProcessorHealthResponse> response = client.healthCheck()) {
            if (response.getStatus() == 200)
                return response.getEntity();
            throw new PaymentProcessorException("Health check failed with status: " + response.getStatus());
        } catch (Exception e) {
            log.error("Error from payment processor of type '{}':", client.getClass().getSimpleName(), e);
        }
        return new PaymentProcessorHealthResponse(true, 0);
    }
}
