FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY target/match-service-*.jar app.jar
EXPOSE 8082 9092
ENTRYPOINT ["java", "-jar", "app.jar"]
