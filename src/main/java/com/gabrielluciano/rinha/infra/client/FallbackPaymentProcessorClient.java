package com.gabrielluciano.rinha.infra.client;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(baseUri = "http://localhost:8002")
public interface FallbackPaymentProcessorClient extends PaymentProcessorClient {
}
