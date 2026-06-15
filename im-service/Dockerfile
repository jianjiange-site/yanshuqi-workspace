# im-service

Stage 00-A skeleton service.

## Package

com.dating.im

## Ports

- REST: 8083
- gRPC (reserved): 9093

## Start

`ash
cd dating-server/im-service
mvn spring-boot:run
`

## Health

- GET http://localhost:8083/health
- GET http://localhost:8083/actuator/health
"@ | Set-Content -Path (Join-Path F:\WorkSpace\yanshuqi-workspace\dating-server\im-service "README.md") -Encoding UTF8

    @"
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY target/im-service-*.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]
