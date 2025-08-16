package com.gabrielluciano.rinha.application.dto;

import com.alibaba.fastjson2.annotation.JSONCompiled;
import io.quarkus.runtime.annotations.RegisterForReflection;

import java.io.Serializable;

@JSONCompiled
@RegisterForReflection
public class ProcessorPaymentSummaryResponse implements Serializable {

    private long totalRequests;
    private double totalAmount;

    public ProcessorPaymentSummaryResponse() {
    }

    public ProcessorPaymentSummaryResponse(long totalRequests, double totalAmount) {
        this.totalRequests = totalRequests;
        this.totalAmount = totalAmount;
    }

    public long getTotalRequests() {
        return totalRequests;
    }

    public void setTotalRequests(long totalRequests) {
        this.totalRequests = totalRequests;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }
}
