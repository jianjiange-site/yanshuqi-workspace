# payment-service

Stage 00-A skeleton service.

## Package

`com.dating.payment`

## Ports

- REST: 8085
- gRPC (reserved): 9095

## Start

```bash
cd payment-service
mvn spring-boot:run
```

## Health

- GET http://localhost:8085/health
- GET http://localhost:8085/actuator/health