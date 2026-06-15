# im-service

Stage 00-A skeleton service.

## Package

`com.dating.im`

## Ports

- REST: 8083
- gRPC (reserved): 9093

## Start

```bash
cd im-service
mvn spring-boot:run
```

## Health

- GET http://localhost:8083/health
- GET http://localhost:8083/actuator/health