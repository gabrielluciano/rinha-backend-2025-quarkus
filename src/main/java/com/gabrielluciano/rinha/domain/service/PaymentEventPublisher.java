package com.gabrielluciano.rinha.domain.service;

import com.gabrielluciano.rinha.domain.model.PaymentEvent;
import io.smallrye.mutiny.Uni;

public interface PaymentEventPublisher {

    Uni<Void> publishPaymentEvent(PaymentEvent event);
}
