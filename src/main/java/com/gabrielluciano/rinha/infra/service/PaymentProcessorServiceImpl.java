package com.gabrielluciano.rinha.infra.service;

import com.gabrielluciano.rinha.domain.model.Payment;
import com.gabrielluciano.rinha.domain.service.PaymentProcessorService;
import com.gabrielluciano.rinha.infra.client.DefaultPaymentProcessorClient;
import com.gabrielluciano.rinha.infra.client.FallbackPaymentProcessorClient;
import com.gabrielluciano.rinha.infra.client.PaymentProcessorClient;
import com.gabrielluciano.rinha.infra.client.model.PaymentProcessorRequest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.RestResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class PaymentProcessorServiceImpl implements PaymentProcessorService {

    private static final Logger log = LoggerFactory.getLogger(PaymentProcessorServiceImpl.class);

    private final PaymentProcessorDecisionService paymentProcessorDecisionService;

    public PaymentProcessorServiceImpl(PaymentProcessorDecisionService paymentProcessorDecisionService) {
        this.paymentProcessorDecisionService = paymentProcessorDecisionService;
    }

    @Inject
    @RestClient
    private DefaultPaymentProcessorClient defaultPaymentProcessorClient;

    @Inject
    @RestClient
    private FallbackPaymentProcessorClient fallbackPaymentProcessorClient;

    @Override
    public boolean processPayment(Payment payment) {
        var processor = paymentProcessorDecisionService.getActiveProcessor();
        return switch (processor) {
            case DEFAULT -> processWithDefaultProcessor(payment);
            case FALLBACK -> processWithFallbackProcessor(payment);
        };
    }

    private boolean processWithDefaultProcessor(Payment payment) {
        return processPaymentWithProcessor(payment, defaultPaymentProcessorClient);
    }

    private boolean processWithFallbackProcessor(Payment payment) {
        return processPaymentWithProcessor(payment, fallbackPaymentProcessorClient);
    }

    private boolean processPaymentWithProcessor(Payment payment, PaymentProcessorClient client) {
        try (RestResponse<Void> response = client.processPayment(PaymentProcessorRequest.fromPayment(payment))) {
            return response.getStatus() == 200;
        } catch (Exception e) {
            log.error("Error from payment processor of type '{}':", client.getClass().getSimpleName(), e);
        }
        return false;
    }
}
