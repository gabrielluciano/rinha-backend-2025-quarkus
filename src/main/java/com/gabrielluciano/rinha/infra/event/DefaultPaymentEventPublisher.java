package com.gabrielluciano.rinha.infra.event;

import com.gabrielluciano.rinha.domain.model.PaymentEvent;
import com.gabrielluciano.rinha.domain.service.PaymentEventPublisher;
import io.smallrye.mutiny.Uni;
import io.vertx.core.eventbus.EventBus;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class DefaultPaymentEventPublisher implements PaymentEventPublisher {

    private final EventBus bus;

    public DefaultPaymentEventPublisher(EventBus bus) {
        this.bus = bus;
    }

    @Override
    public Uni<Void> publishPaymentEvent(PaymentEvent event) {
        return Uni.createFrom().voidItem().onItem().invoke(() -> bus.publish("payment.event", event));
    }
}
