#!/usr/bin/env bash

VERSION=${1:-1.0.0}

mvn clean package -Dnative
docker build -f src/main/docker/Dockerfile.native -t gabrielluciano/rinha-2025-quarkus:${VERSION} .
