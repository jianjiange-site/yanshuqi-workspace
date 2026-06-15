# user-service

Stage 00-A skeleton service.

## Package

`com.dating.user`

## Ports

- REST: 8081
- gRPC (reserved): 9091

## Start

```bash
cd user-service
mvn spring-boot:run
```

## Health

- GET http://localhost:8081/health
- GET http://localhost:8081/actuator/health