package com.gabrielluciano.rinha.infra.service;

import com.gabrielluciano.rinha.domain.model.Payment;
import com.gabrielluciano.rinha.domain.service.PaymentProcessorService;
import com.gabrielluciano.rinha.infra.client.DefaultPaymentProcessorClient;
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

    @Inject
    @RestClient
    private DefaultPaymentProcessorClient defaultPaymentProcessorClient;

    @Override
    public boolean processPayment(Payment payment) {
        try (RestResponse<Void> response = defaultPaymentProcessorClient.processPayment(PaymentProcessorRequest.fromPayment(payment))) {
            return response.getStatus() == 200;
        } catch (Exception e) {
            log.error("Error from payment processor", e);
        }
        return false;
    }
}
