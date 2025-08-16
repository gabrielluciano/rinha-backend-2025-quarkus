package com.gabrielluciano.rinha.application.resource;

import com.alibaba.fastjson2.JSON;
import com.gabrielluciano.rinha.application.dto.PaymentRequest;
import com.gabrielluciano.rinha.application.dto.ProcessorPaymentSummaryResponse;
import com.gabrielluciano.rinha.domain.service.PaymentService;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.stream.Collectors;

@Path("/")
@ApplicationScoped
public class PaymentResource {

    private static final Logger log = LoggerFactory.getLogger(PaymentResource.class);
    private final PaymentService paymentService;

    public PaymentResource(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @POST
    @Path("payments")
    public Uni<Void> pay(byte[] request) {
        PaymentRequest payment = JSON.parseObject(request, PaymentRequest.class);
        log.info("Received payment request: {}", payment);
        return paymentService.processPayment(payment.toDomainModel());
    }

    @GET
    @Path("payments-summary")
    public byte[] summary(@QueryParam("from") Instant from, @QueryParam("to") Instant to) {
        if (to == null) {
            to = Instant.now();
        }

        var result = paymentService.getPaymentSummary(from, to).stream()
                .collect(Collectors.toMap(
                        summary -> summary.processor().getName(),
                        summary -> new ProcessorPaymentSummaryResponse(
                                summary.totalRequests(),
                                summary.totalAmount()
                        )));

        return JSON.toJSONBytes(result);
    }
}
