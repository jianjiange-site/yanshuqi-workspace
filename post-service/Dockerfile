# post-service

Stage 00-A skeleton service.

## Package

com.dating.post

## Ports

- REST: 8084
- gRPC (reserved): 9094

## Start

`ash
cd dating-server/post-service
mvn spring-boot:run
`

## Health

- GET http://localhost:8084/health
- GET http://localhost:8084/actuator/health
"@ | Set-Content -Path (Join-Path F:\WorkSpace\yanshuqi-workspace\dating-server\post-service "README.md") -Encoding UTF8

    @"
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY target/post-service-*.jar app.jar
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "app.jar"]
