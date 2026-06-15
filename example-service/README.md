# example-service

Stage 00-A skeleton service.

## Package

`com.dating.example`

## Ports

- REST: 8086
- gRPC (reserved): 9096

## Start

```bash
cd example-service
mvn spring-boot:run
```

## Health

- GET http://localhost:8086/health
- GET http://localhost:8086/actuator/health