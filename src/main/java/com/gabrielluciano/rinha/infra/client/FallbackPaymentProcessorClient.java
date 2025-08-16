package com.gabrielluciano.rinha.infra.client;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient
public interface FallbackPaymentProcessorClient extends PaymentProcessorClient {
}
