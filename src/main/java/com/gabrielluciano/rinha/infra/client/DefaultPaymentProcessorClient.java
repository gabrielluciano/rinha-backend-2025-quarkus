package com.gabrielluciano.rinha.infra.client;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(baseUri = "http://localhost:8001")
public interface DefaultPaymentProcessorClient extends PaymentProcessorClient {
}
