#!/usr/bin/bash

docker compose down -v

cd ../rinha-de-backend-2025/payment-processor

docker compose down -v
sleep 1

docker compose up -d
sleep 2

cd ../../rinha-backend-2025-quarkus
docker compose up -d

sleep 5

cd ../rinha-de-backend-2025/rinha-test
k6 run rinha.js
