package com.gabrielluciano.rinha.domain.service;

import com.gabrielluciano.rinha.domain.model.PaymentEvent;
import io.vertx.core.Future;

public interface PaymentEventPublisher {

    void publishPaymentEvent(PaymentEvent event);
}
