package com.gabrielluciano.rinha.application.resource;

import com.gabrielluciano.rinha.application.dto.PaymentRequest;
import com.gabrielluciano.rinha.application.dto.ProcessorPaymentSummaryResponse;
import com.gabrielluciano.rinha.domain.service.PaymentService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Map;
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
    public void pay(PaymentRequest payment) {
        log.info("Received payment request: {}", payment);
        paymentService.processPayment(payment.toDomainModel());
    }

    @GET
    @Path("payments-summary")
    public Map<String, ProcessorPaymentSummaryResponse> summary(@QueryParam("from") Instant from, @QueryParam("to") Instant to) {
        if (to == null) {
            to = Instant.now();
        }

        return paymentService.getPaymentSummary(from, to).stream()
                .collect(Collectors.toMap(
                        summary -> summary.processor().getName(),
                        summary -> new ProcessorPaymentSummaryResponse(
                                summary.totalRequests(),
                                summary.totalAmount()
                        )));
    }
}
