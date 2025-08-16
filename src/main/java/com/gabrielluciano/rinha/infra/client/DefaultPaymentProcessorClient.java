package com.gabrielluciano.rinha.infra.client;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient
public interface DefaultPaymentProcessorClient extends PaymentProcessorClient {
}
