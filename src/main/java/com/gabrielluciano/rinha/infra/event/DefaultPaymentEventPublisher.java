package com.gabrielluciano.rinha.infra.event;

import com.gabrielluciano.rinha.domain.model.PaymentEvent;
import com.gabrielluciano.rinha.domain.service.PaymentEventPublisher;
import io.vertx.core.eventbus.EventBus;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class DefaultPaymentEventPublisher implements PaymentEventPublisher {

    private final EventBus bus;

    public DefaultPaymentEventPublisher(EventBus bus) {
        this.bus = bus;
    }

    @Override
    public void publishPaymentEvent(PaymentEvent event) {
        bus.publish("payment.event", event);
    }
}
