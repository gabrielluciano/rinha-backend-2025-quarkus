#!/usr/bin/bash
mvn clean package -Dnative
docker build -f src/main/docker/Dockerfile.native -t gabrielluciano/rinha-2025-quarkus:1.0.0 .
