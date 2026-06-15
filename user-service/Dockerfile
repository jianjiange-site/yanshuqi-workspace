# user-service

Stage 00-A skeleton service.

## Package

com.dating.user

## Ports

- REST: 8081
- gRPC (reserved): 9091

## Start

`ash
cd dating-server/user-service
mvn spring-boot:run
`

## Health

- GET http://localhost:8081/health
- GET http://localhost:8081/actuator/health
"@ | Set-Content -Path (Join-Path F:\WorkSpace\yanshuqi-workspace\dating-server\user-service "README.md") -Encoding UTF8

    @"
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY target/user-service-*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
