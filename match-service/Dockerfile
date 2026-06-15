# match-service

Stage 00-A skeleton service.

## Package

com.dating.match

## Ports

- REST: 8082
- gRPC (reserved): 9092

## Start

`ash
cd dating-server/match-service
mvn spring-boot:run
`

## Health

- GET http://localhost:8082/health
- GET http://localhost:8082/actuator/health
"@ | Set-Content -Path (Join-Path F:\WorkSpace\yanshuqi-workspace\dating-server\match-service "README.md") -Encoding UTF8

    @"
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY target/match-service-*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
