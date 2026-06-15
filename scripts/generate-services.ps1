# Internal script used during Stage 00-A scaffolding. Not required for runtime.
$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot

$services = @(
    @{ name = "mobile-gateway"; pkg = "gateway"; appClass = "GatewayServiceApplication"; port = 8080; grpc = $null; schema = "gateway"; flyway = "flyway_history_gateway"; redisPrefix = "yanshuqi:gateway:"; hasDb = $true },
    @{ name = "user-service"; pkg = "user"; appClass = "UserServiceApplication"; port = 8081; grpc = 9091; schema = "user_center"; flyway = "flyway_history_user"; redisPrefix = "yanshuqi:user:"; hasDb = $true },
    @{ name = "match-service"; pkg = "match"; appClass = "MatchServiceApplication"; port = 8082; grpc = 9092; schema = "match_center"; flyway = "flyway_history_match"; redisPrefix = "yanshuqi:match:"; hasDb = $true },
    @{ name = "im-service"; pkg = "im"; appClass = "ImServiceApplication"; port = 8083; grpc = 9093; schema = "im_center"; flyway = "flyway_history_im"; redisPrefix = "yanshuqi:im:"; hasDb = $true; hasOpenIm = $true },
    @{ name = "post-service"; pkg = "post"; appClass = "PostServiceApplication"; port = 8084; grpc = 9094; schema = "post_center"; flyway = "flyway_history_post"; redisPrefix = "yanshuqi:post:"; hasDb = $true },
    @{ name = "payment-service"; pkg = "payment"; appClass = "PaymentServiceApplication"; port = 8085; grpc = 9095; schema = "payment_center"; flyway = "flyway_history_payment"; redisPrefix = "yanshuqi:payment:"; hasDb = $true },
    @{ name = "example-service"; pkg = "example"; appClass = "ExampleServiceApplication"; port = 8086; grpc = 9096; schema = $null; flyway = $null; redisPrefix = "yanshuqi:example:"; hasDb = $false }
)

$packages = @("controller", "grpc", "service/impl", "manager", "mapper", "entity", "dto", "vo", "client", "config", "constant", "exception")

function Ensure-Dir([string]$path) {
    if (-not (Test-Path $path)) { New-Item -ItemType Directory -Path $path -Force | Out-Null }
}

foreach ($svc in $services) {
    $svcRoot = Join-Path $root $svc.name
    $javaRoot = Join-Path $svcRoot "src/main/java/com/dating/$($svc.pkg)"
    $resRoot = Join-Path $svcRoot "src/main/resources"
    $testRoot = Join-Path $svcRoot "src/test/java/com/dating/$($svc.pkg)"
    $migrationRoot = Join-Path $resRoot "db/migration"

    foreach ($d in @($javaRoot, $resRoot, $testRoot, $migrationRoot)) { Ensure-Dir $d }
    foreach ($p in $packages) { Ensure-Dir (Join-Path $javaRoot $p) }

    $grpcBlock = ""
    if ($svc.grpc) {
        $grpcBlock = @"

app:
  grpc:
    port: $($svc.grpc)
"@
    }

    $openImBlock = ""
    if ($svc.ContainsKey("hasOpenIm") -and $svc.hasOpenIm) {
        $openImBlock = @"

# OpenIM placeholders (Stage 00-B, no calls in Stage 00-A)
openim:
  api-base-url: `${OPENIM_API_BASE_URL:}
  ws-url: `${OPENIM_WS_URL:}
  admin-user-id: `${OPENIM_ADMIN_USER_ID:}
  admin-secret: `${OPENIM_ADMIN_SECRET:}
  callback-secret: `${OPENIM_CALLBACK_SECRET:}
  user-id-prefix: `${OPENIM_USER_ID_PREFIX:yanshuqi_}
"@
    }

    $flywayBlock = ""
    if ($svc.hasDb) {
        $flywayBlock = @"
  flyway:
    enabled: false
    schemas: $($svc.schema)
    table: $($svc.flyway)
    baseline-on-migrate: true
"@
    }

    $excludeBlock = @"
  autoconfigure:
    exclude:
      - org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
      - org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration
"@
    if ($svc.hasDb) {
        $excludeBlock += @"

      - org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration
"@
    }

    $datasourceTemplate = ""
    if ($svc.hasDb) {
        $datasourceTemplate = @"

  # Stage 00-B: enable datasource and flyway after shared infra is ready
  # datasource:
  #   url: jdbc:postgresql://`${POSTGRES_HOST:`${POSTGRES_HOST}}:`${POSTGRES_PORT:`${POSTGRES_PORT}}/`${POSTGRES_DATABASE:dating_dev_yanshuqi}?currentSchema=$($svc.schema)&stringtype=unspecified
  #   username: `${POSTGRES_USERNAME}
  #   password: `${POSTGRES_PASSWORD}
  #   driver-class-name: org.postgresql.Driver
  # data:
  #   redis:
  #     host: `${REDIS_HOST}
  #     port: `${REDIS_PORT}
  #     password: `${REDIS_PASSWORD}
  #     database: `${REDIS_DATABASE:1}
"@
    }

    @"
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.12</version>
        <relativePath/>
    </parent>

    <groupId>com.dating</groupId>
    <artifactId>$($svc.name)</artifactId>
    <version>0.1.0-SNAPSHOT</version>
    <name>$($svc.name)</name>
    <description>$($svc.name) skeleton</description>

    <properties>
        <java.version>17</java.version>
        <spring-cloud.version>2023.0.3</spring-cloud.version>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-dependencies</artifactId>
                <version>`${spring-cloud.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-bootstrap</artifactId>
        </dependency>
        <dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
            <version>2023.0.1.2</version>
        </dependency>
        <dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId>
            <version>2023.0.1.2</version>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
"@ | Set-Content -Path (Join-Path $svcRoot "pom.xml") -Encoding utf8NoBOM

    @"
package com.dating.$($svc.pkg);

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class $($svc.appClass) {

    public static void main(String[] args) {
        SpringApplication.run($($svc.appClass).class, args);
    }
}
"@ | Set-Content -Path (Join-Path $javaRoot "$($svc.appClass).java") -Encoding UTF8

    @"
package com.dating.$($svc.pkg).controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class HealthController {

    private static final String SERVICE_NAME = "$($svc.name)";

    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", "UP");
        body.put("service", SERVICE_NAME);
        body.put("timestamp", Instant.now().toString());
        body.put("stage", "00-A");
        return body;
    }
}
"@ | Set-Content -Path (Join-Path $javaRoot "controller/HealthController.java") -Encoding UTF8

    @"
server:
  port: `${SERVER_PORT:$($svc.port)}

spring:
  application:
    name: $($svc.name)
$excludeBlock
$datasourceTemplate
$flywayBlock

app:
  cache:
    key-prefix: `${REDIS_KEY_PREFIX:yanshuqi}
  service:
    name: $($svc.name)
$grpcBlock
$openImBlock

management:
  endpoints:
    web:
      exposure:
        include: health,info
  endpoint:
    health:
      show-details: always

logging:
  level:
    root: INFO
"@ | Set-Content -Path (Join-Path $resRoot "application-dev.yml") -Encoding UTF8

    @"
spring:
  profiles:
    active: dev
"@ | Set-Content -Path (Join-Path $resRoot "application.yml") -Encoding UTF8

    @"
spring:
  application:
    name: `${APP_NAME:$($svc.name)}
  profiles:
    active: `${SPRING_PROFILES_ACTIVE:dev}
  cloud:
    nacos:
      discovery:
        enabled: false
        server-addr: `${NACOS_SERVER_ADDR:localhost:8848}
        namespace: `${NACOS_NAMESPACE:yanshuqi-dev}
        group: `${NACOS_GROUP:DEFAULT_GROUP}
        username: `${NACOS_USERNAME:}
        password: `${NACOS_PASSWORD:}
      config:
        enabled: false
        server-addr: `${NACOS_SERVER_ADDR:localhost:8848}
        namespace: `${NACOS_NAMESPACE:yanshuqi-dev}
        group: `${NACOS_GROUP:DEFAULT_GROUP}
        username: `${NACOS_USERNAME:}
        password: `${NACOS_PASSWORD:}
        file-extension: yaml
"@ | Set-Content -Path (Join-Path $resRoot "bootstrap.yml") -Encoding UTF8

    @"
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <springProperty scope="context" name="serviceName" source="spring.application.name" defaultValue="$($svc.name)"/>
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{yyyy-MM-dd'T'HH:mm:ss.SSS'Z',UTC} [%thread] %-5level ${serviceName} traceId=%X{traceId:-} - %msg%n</pattern>
        </encoder>
    </appender>
    <root level="INFO">
        <appender-ref ref="CONSOLE"/>
    </root>
</configuration>
"@ | Set-Content -Path (Join-Path $resRoot "logback-spring.xml") -Encoding UTF8

    if ($svc.hasDb) {
        @"
-- Stage 00-A baseline placeholder. No business tables.
-- Schema creation is handled in Stage 00-B deploy scripts.
SELECT 1;
"@ | Set-Content -Path (Join-Path $migrationRoot "V000__baseline.sql") -Encoding UTF8
    }

    @"
# $($svc.name)

Stage 00-A skeleton service.

## Package

`com.dating.$($svc.pkg)`

## Ports

- REST: $($svc.port)
$(if ($svc.grpc) { "- gRPC (reserved): $($svc.grpc)" })

## Start

```bash
cd $($svc.name)
mvn spring-boot:run
```

## Health

- `GET http://localhost:$($svc.port)/health`
- `GET http://localhost:$($svc.port)/actuator/health`
"@ | Set-Content -Path (Join-Path $svcRoot "README.md") -Encoding UTF8

    @"
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY target/$($svc.name)-*.jar app.jar
EXPOSE $($svc.port)
ENTRYPOINT ["java", "-jar", "app.jar"]
"@ | Set-Content -Path (Join-Path $svcRoot "Dockerfile") -Encoding UTF8

    @"
package com.dating.$($svc.pkg);

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class $($svc.appClass)Tests {

    @Test
    void contextLoads() {
    }
}
"@ | Set-Content -Path (Join-Path $testRoot "$($svc.appClass)Tests.java") -Encoding UTF8
}

Write-Host "Generated $($services.Count) Java services."
