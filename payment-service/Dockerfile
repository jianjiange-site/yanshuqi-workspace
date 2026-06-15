# payment-service

Stage 00-A skeleton service.

## Package

com.dating.payment

## Ports

- REST: 8085
- gRPC (reserved): 9095

## Start

`ash
cd dating-server/payment-service
mvn spring-boot:run
`

## Health

- GET http://localhost:8085/health
- GET http://localhost:8085/actuator/health
"@ | Set-Content -Path (Join-Path F:\WorkSpace\yanshuqi-workspace\dating-server\payment-service "README.md") -Encoding UTF8

    @"
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY target/payment-service-*.jar app.jar
EXPOSE 8085
ENTRYPOINT ["java", "-jar", "app.jar"]
