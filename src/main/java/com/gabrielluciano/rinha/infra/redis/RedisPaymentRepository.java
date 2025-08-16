package com.gabrielluciano.rinha.infra.redis;

import com.alibaba.fastjson2.JSON;
import com.gabrielluciano.rinha.domain.model.Payment;
import com.gabrielluciano.rinha.domain.repository.PaymentRepository;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.sortedset.ScoreRange;
import io.quarkus.redis.datasource.sortedset.SortedSetCommands;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.Instant;
import java.util.List;

@ApplicationScoped
public class RedisPaymentRepository implements PaymentRepository {

    private static final String SORTED_SET_KEY = "payments";

    private final SortedSetCommands<String, String> paymentHash;

    public RedisPaymentRepository(RedisDataSource dataSource) {
        paymentHash = dataSource.sortedSet(String.class, String.class);
    }

    @Override
    public void savePayment(Payment payment) {
        long timestamp = payment.getTimestamp().toEpochMilli();
        paymentHash.zadd(SORTED_SET_KEY, timestamp, JSON.toJSONString(payment));
    }

    @Override
    public List<Payment> getPayments(Instant from, Instant to) {
        long fromTimestamp = from.toEpochMilli();
        long toTimestamp = to.toEpochMilli();
        return paymentHash.zrangebyscore(SORTED_SET_KEY, ScoreRange.from(fromTimestamp, toTimestamp))
                .stream().map(json -> JSON.parseObject(json, Payment.class))
                .toList();
    }
}
