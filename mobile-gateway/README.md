# mobile-gateway

Stage 00-A skeleton service.

## Package

`com.dating.gateway`

## Ports

- REST: 8080


## Start

```bash
cd mobile-gateway
mvn spring-boot:run
```

## Health

- GET http://localhost:8080/health
- GET http://localhost:8080/actuator/health