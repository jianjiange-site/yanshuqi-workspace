# example-service

Stage 00-A skeleton service.

## Package

com.dating.example

## Ports

- REST: 8086
- gRPC (reserved): 9096

## Start

`ash
cd dating-server/example-service
mvn spring-boot:run
`

## Health

- GET http://localhost:8086/health
- GET http://localhost:8086/actuator/health
"@ | Set-Content -Path (Join-Path F:\WorkSpace\yanshuqi-workspace\dating-server\example-service "README.md") -Encoding UTF8

    @"
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY target/example-service-*.jar app.jar
EXPOSE 8086
ENTRYPOINT ["java", "-jar", "app.jar"]
