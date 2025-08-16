package com.gabrielluciano.rinha.infra.service;

import com.gabrielluciano.rinha.domain.model.Payment;
import com.gabrielluciano.rinha.domain.model.PaymentProcessorType;
import com.gabrielluciano.rinha.domain.repository.PaymentRepository;
import com.gabrielluciano.rinha.domain.service.PaymentProcessorService;
import com.gabrielluciano.rinha.infra.client.DefaultPaymentProcessorClient;
import com.gabrielluciano.rinha.infra.client.FallbackPaymentProcessorClient;
import com.gabrielluciano.rinha.infra.client.PaymentProcessorClient;
import com.gabrielluciano.rinha.infra.client.model.PaymentProcessorRequest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.ClientWebApplicationException;
import org.jboss.resteasy.reactive.RestResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class PaymentProcessorServiceImpl implements PaymentProcessorService {

    private static final Logger log = LoggerFactory.getLogger(PaymentProcessorServiceImpl.class);

    private final PaymentProcessorDecisionService paymentProcessorDecisionService;
    private final PaymentRepository repository;

    public PaymentProcessorServiceImpl(PaymentProcessorDecisionService paymentProcessorDecisionService, PaymentRepository repository) {
        this.paymentProcessorDecisionService = paymentProcessorDecisionService;
        this.repository = repository;
    }

    @Inject
    @RestClient
    private DefaultPaymentProcessorClient defaultPaymentProcessorClient;

    @Inject
    @RestClient
    private FallbackPaymentProcessorClient fallbackPaymentProcessorClient;

    @Override
    public void processPayment(Payment payment, PaymentProcessorType processor) {
        if (processor == PaymentProcessorType.NONE) {
            log.warn("No active payment processor available, skipping processing");
            throw new IllegalStateException("No active payment processor available");
        }
        payment.setProcessor(processor);
        boolean processed = switch (processor) {
            case DEFAULT -> processWithDefaultProcessor(payment);
            case FALLBACK -> processWithFallbackProcessor(payment);
            default -> throw new IllegalStateException("Unknown payment processor type: " + processor);
        };

        if (processed) {
            repository.savePayment(payment);
        }
    }

    private boolean processWithDefaultProcessor(Payment payment) {
        return processPaymentWithProcessor(payment, defaultPaymentProcessorClient);
    }

    private boolean processWithFallbackProcessor(Payment payment) {
        return processPaymentWithProcessor(payment, fallbackPaymentProcessorClient);
    }

    private boolean processPaymentWithProcessor(Payment payment, PaymentProcessorClient client) {
        try (RestResponse<Void> response = client.processPayment(PaymentProcessorRequest.fromPayment(payment))) {
            return true;
        } catch (ClientWebApplicationException e) {
            if (e.getResponse().getStatus() == 422) {
                log.warn("Payment already processed, ignoring: {}", payment.getCorrelationId());
                return false;
            }

            if (client instanceof DefaultPaymentProcessorClient) {
                paymentProcessorDecisionService.setProcessorHealth(PaymentProcessorType.DEFAULT, false);
            } else {
                paymentProcessorDecisionService.setProcessorHealth(PaymentProcessorType.FALLBACK, false);
            }

            throw e; // Re-throw other client exceptions
        } catch (Exception e) {
            log.error("Error from payment processor of type '{}':", client.getClass().getSimpleName(), e);
            throw e;
        }
    }
}
