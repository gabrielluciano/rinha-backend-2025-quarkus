package com.gabrielluciano.rinha.infra.redis;

import com.gabrielluciano.rinha.domain.model.Payment;
import com.gabrielluciano.rinha.domain.repository.PaymentRepository;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.hash.HashCommands;
import io.quarkus.redis.datasource.sortedset.ScoreRange;
import io.quarkus.redis.datasource.sortedset.SortedSetCommands;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@ApplicationScoped
public class RedisPaymentRepository implements PaymentRepository {

    private static final String HSET_KEY = "payment_index";
    private static final String SORTED_SET_KEY = "payments";
    private final RedisDataSource dataSource;
    private final HashCommands<String, String, String> idHash;
    private final SortedSetCommands<String, Payment> paymentHash;

    public RedisPaymentRepository(RedisDataSource dataSource) {
        this.dataSource = dataSource;
        idHash = dataSource.hash(String.class, String.class, String.class);
        paymentHash = dataSource.sortedSet(String.class, Payment.class);
    }

    @Override
    public void savePayment(Payment payment) {
        if (idHash.hexists(HSET_KEY, payment.getCorrelationId())) {
            return;
        }
        long timestamp = payment.getTimestamp().toEpochSecond(ZoneOffset.UTC);
        dataSource.withTransaction(tx -> {
            tx.hash(String.class, String.class, String.class).hset(HSET_KEY, payment.getCorrelationId(), "1");
            tx.sortedSet(String.class, Payment.class).zadd(SORTED_SET_KEY, timestamp, payment);
        });
    }

    @Override
    public List<Payment> getPayments(LocalDateTime from, LocalDateTime to) {
        long fromTimestamp = from.toEpochSecond(ZoneOffset.UTC);
        long toTimestamp = to.toEpochSecond(ZoneOffset.UTC);
        return paymentHash.zrangebyscore(SORTED_SET_KEY, ScoreRange.from(fromTimestamp, toTimestamp));
    }
}
