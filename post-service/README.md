# post-service

Stage 00-A skeleton service.

## Package

`com.dating.post`

## Ports

- REST: 8084
- gRPC (reserved): 9094

## Start

```bash
cd post-service
mvn spring-boot:run
```

## Health

- GET http://localhost:8084/health
- GET http://localhost:8084/actuator/health