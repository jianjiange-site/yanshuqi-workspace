# mobile-gateway

Stage 00-A skeleton service.

## Package

com.dating.gateway

## Ports

- REST: 8080


## Start

`ash
cd dating-server/mobile-gateway
mvn spring-boot:run
`

## Health

- GET http://localhost:8080/health
- GET http://localhost:8080/actuator/health
"@ | Set-Content -Path (Join-Path F:\WorkSpace\yanshuqi-workspace\dating-server\mobile-gateway "README.md") -Encoding UTF8

    @"
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY target/mobile-gateway-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
