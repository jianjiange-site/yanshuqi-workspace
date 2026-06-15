# match-service

Stage 00-A skeleton service.

## Package

`com.dating.match`

## Ports

- REST: 8082
- gRPC (reserved): 9092

## Start

```bash
cd match-service
mvn spring-boot:run
```

## Health

- GET http://localhost:8082/health
- GET http://localhost:8082/actuator/health