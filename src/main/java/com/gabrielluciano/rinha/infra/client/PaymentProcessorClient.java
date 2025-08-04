package com.gabrielluciano.rinha.infra.client;

import com.gabrielluciano.rinha.infra.client.model.PaymentProcessorHealthResponse;
import com.gabrielluciano.rinha.infra.client.model.PaymentProcessorRequest;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import org.jboss.resteasy.reactive.RestResponse;

@Path("/payments")
public interface PaymentProcessorClient {

    @POST
    RestResponse<Void> processPayment(PaymentProcessorRequest request);

    @GET
    @Path("service-health")
    RestResponse<PaymentProcessorHealthResponse> healthCheck();
}
