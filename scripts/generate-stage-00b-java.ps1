# Stage 00-B Java infra generator. Run once to scaffold shared infra code.
$ErrorActionPreference = "Stop"
$workspaceRoot = Split-Path -Parent $PSScriptRoot
$utf8 = New-Object System.Text.UTF8Encoding $false

function Write-Utf8File([string]$Path, [string]$Content) {
    $dir = Split-Path -Parent $Path
    if ($dir -and -not (Test-Path $dir)) { New-Item -ItemType Directory -Path $dir -Force | Out-Null }
    [System.IO.File]::WriteAllText($Path, $Content, $utf8)
}

$services = @(
    @{ name = "mobile-gateway"; pkg = "gateway"; appClass = "GatewayServiceApplication"; port = 8080; schema = "gateway"; flyway = "flyway_history_gateway"; redisSuffix = "gateway"; hasDb = $true; grpc = $null; openIm = $false },
    @{ name = "user-service"; pkg = "user"; appClass = "UserServiceApplication"; port = 8081; schema = "user_center"; flyway = "flyway_history_user"; redisSuffix = "user"; hasDb = $true; grpc = 9091; openIm = $false },
    @{ name = "match-service"; pkg = "match"; appClass = "MatchServiceApplication"; port = 8082; schema = "match_center"; flyway = "flyway_history_match"; redisSuffix = "match"; hasDb = $true; grpc = 9092; openIm = $false },
    @{ name = "im-service"; pkg = "im"; appClass = "ImServiceApplication"; port = 8083; schema = "im_center"; flyway = "flyway_history_im"; redisSuffix = "im"; hasDb = $true; grpc = 9093; openIm = $true },
    @{ name = "post-service"; pkg = "post"; appClass = "PostServiceApplication"; port = 8084; schema = "post_center"; flyway = "flyway_history_post"; redisSuffix = "post"; hasDb = $true; grpc = 9094; openIm = $false },
    @{ name = "payment-service"; pkg = "payment"; appClass = "PaymentServiceApplication"; port = 8085; schema = "payment_center"; flyway = "flyway_history_payment"; redisSuffix = "payment"; hasDb = $true; grpc = 9095; openIm = $false },
    @{ name = "example-service"; pkg = "example"; appClass = "ExampleServiceApplication"; port = 8086; schema = $null; flyway = $null; redisSuffix = "example"; hasDb = $false; grpc = 9096; openIm = $false }
)

$infraJavaTemplate = @'
package com.dating.{PKG}.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private Cache cache = new Cache();
    private Service service = new Service();
    private Infra infra = new Infra();

    public Cache getCache() { return cache; }
    public void setCache(Cache cache) { this.cache = cache; }
    public Service getService() { return service; }
    public void setService(Service service) { this.service = service; }
    public Infra getInfra() { return infra; }
    public void setInfra(Infra infra) { this.infra = infra; }

    public static class Cache {
        private String keyPrefix = "yanshuqi";
        public String getKeyPrefix() { return keyPrefix; }
        public void setKeyPrefix(String keyPrefix) { this.keyPrefix = keyPrefix; }
    }

    public static class Service {
        private String name;
        private String redisKeySuffix;
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getRedisKeySuffix() { return redisKeySuffix; }
        public void setRedisKeySuffix(String redisKeySuffix) { this.redisKeySuffix = redisKeySuffix; }
    }

    public static class Infra {
        private String redisTestKey;
        private int redisTestTtlSeconds = 60;
        public String getRedisTestKey() { return redisTestKey; }
        public void setRedisTestKey(String redisTestKey) { this.redisTestKey = redisTestKey; }
        public int getRedisTestTtlSeconds() { return redisTestTtlSeconds; }
        public void setRedisTestTtlSeconds(int redisTestTtlSeconds) { this.redisTestTtlSeconds = redisTestTtlSeconds; }
    }
}
'@

$minioPropsTemplate = @'
package com.dating.{PKG}.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "minio")
public class MinioProperties {

    private String endpoint;
    private String accessKey;
    private String secretKey;
    private String bucket = "dating-yanshuqi";
    private String region = "us-east-1";
    private boolean pathStyleAccess = true;

    public String getEndpoint() { return endpoint; }
    public void setEndpoint(String endpoint) { this.endpoint = endpoint; }
    public String getAccessKey() { return accessKey; }
    public void setAccessKey(String accessKey) { this.accessKey = accessKey; }
    public String getSecretKey() { return secretKey; }
    public void setSecretKey(String secretKey) { this.secretKey = secretKey; }
    public String getBucket() { return bucket; }
    public void setBucket(String bucket) { this.bucket = bucket; }
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
    public boolean isPathStyleAccess() { return pathStyleAccess; }
    public void setPathStyleAccess(boolean pathStyleAccess) { this.pathStyleAccess = pathStyleAccess; }
}
'@

$minioConfigTemplate = @'
package com.dating.{PKG}.config;

import io.minio.MinioClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({AppProperties.class, MinioProperties.class})
public class InfraConfiguration {

    @Bean
    public MinioClient minioClient(MinioProperties minioProperties) {
        return MinioClient.builder()
                .endpoint(minioProperties.getEndpoint())
                .credentials(minioProperties.getAccessKey(), minioProperties.getSecretKey())
                .region(minioProperties.getRegion())
                .build();
    }
}
'@

$infraHealthServiceTemplate = @'
package com.dating.{PKG}.service;

import com.dating.{PKG}.health.MinioInfraHealthChecker;
import com.dating.{PKG}.health.NacosInfraHealthChecker;
import com.dating.{PKG}.health.PostgresInfraHealthChecker;
import com.dating.{PKG}.health.RedisInfraHealthChecker;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class InfraHealthService {

    private final PostgresInfraHealthChecker postgresInfraHealthChecker;
    private final RedisInfraHealthChecker redisInfraHealthChecker;
    private final NacosInfraHealthChecker nacosInfraHealthChecker;
    private final MinioInfraHealthChecker minioInfraHealthChecker;

    public InfraHealthService(PostgresInfraHealthChecker postgresInfraHealthChecker,
                              RedisInfraHealthChecker redisInfraHealthChecker,
                              NacosInfraHealthChecker nacosInfraHealthChecker,
                              MinioInfraHealthChecker minioInfraHealthChecker) {
        this.postgresInfraHealthChecker = postgresInfraHealthChecker;
        this.redisInfraHealthChecker = redisInfraHealthChecker;
        this.nacosInfraHealthChecker = nacosInfraHealthChecker;
        this.minioInfraHealthChecker = minioInfraHealthChecker;
    }

    public Map<String, Object> checkAll() {
        Map<String, Object> checks = new LinkedHashMap<>();
        checks.put("postgresql", postgresInfraHealthChecker.check());
        checks.put("redis", redisInfraHealthChecker.check());
        checks.put("nacos", nacosInfraHealthChecker.check());
        checks.put("minio", minioInfraHealthChecker.check());

        String overall = checks.values().stream()
                .filter(Map.class::isInstance)
                .map(Map.class::cast)
                .map(item -> String.valueOf(item.get("status")))
                .allMatch(status -> "UP".equals(status) || "SKIPPED".equals(status)) ? "UP" : "DOWN";

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", overall);
        body.put("service", "{SERVICE_NAME}");
        body.put("stage", "00-B");
        body.put("checks", checks);
        return body;
    }
}
'@

$postgresCheckerTemplate = @'
package com.dating.{PKG}.health;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
@ConditionalOnBean(DataSource.class)
public class PostgresInfraHealthChecker {

    private final DataSource dataSource;

    @Value("${spring.flyway.schemas:}")
    private String schema;

    @Autowired
    public PostgresInfraHealthChecker(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Map<String, Object> check() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("component", "postgresql");
        result.put("schema", schema);
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT current_database(), current_schema()")) {
            if (resultSet.next()) {
                result.put("database", resultSet.getString(1));
                result.put("currentSchema", resultSet.getString(2));
            }
            result.put("status", "UP");
        } catch (Exception ex) {
            result.put("status", "DOWN");
            result.put("error", ex.getClass().getSimpleName() + ": " + ex.getMessage());
        }
        return result;
    }
}
'@

$postgresCheckerNoDbTemplate = @'
package com.dating.{PKG}.health;

import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class PostgresInfraHealthChecker {

    public Map<String, Object> check() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("component", "postgresql");
        result.put("status", "SKIPPED");
        result.put("reason", "No dedicated schema for example-service in Stage 00-B");
        return result;
    }
}
'@

$redisCheckerTemplate = @'
package com.dating.{PKG}.health;

import com.dating.{PKG}.config.AppProperties;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

@Component
public class RedisInfraHealthChecker {

    private final StringRedisTemplate stringRedisTemplate;
    private final AppProperties appProperties;

    public RedisInfraHealthChecker(StringRedisTemplate stringRedisTemplate, AppProperties appProperties) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.appProperties = appProperties;
    }

    public Map<String, Object> check() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("component", "redis");
        String testKey = appProperties.getInfra().getRedisTestKey();
        int ttlSeconds = appProperties.getInfra().getRedisTestTtlSeconds();
        result.put("testKey", testKey);
        try {
            stringRedisTemplate.opsForValue().set(testKey, "ping", Duration.ofSeconds(ttlSeconds));
            String value = stringRedisTemplate.opsForValue().get(testKey);
            if (!Objects.equals("ping", value)) {
                throw new IllegalStateException("Redis read/write mismatch");
            }
            stringRedisTemplate.delete(testKey);
            result.put("status", "UP");
        } catch (Exception ex) {
            result.put("status", "DOWN");
            result.put("error", ex.getClass().getSimpleName() + ": " + ex.getMessage());
        }
        return result;
    }
}
'@

$nacosCheckerTemplate = @'
package com.dating.{PKG}.health;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.naming.NamingService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

@Component
public class NacosInfraHealthChecker {

    @Value("${spring.cloud.nacos.discovery.server-addr}")
    private String serverAddr;

    @Value("${spring.cloud.nacos.discovery.namespace:yanshuqi-dev}")
    private String namespace;

    @Value("${spring.cloud.nacos.discovery.group:DEFAULT_GROUP}")
    private String group;

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${spring.cloud.nacos.discovery.username:}")
    private String username;

    @Value("${spring.cloud.nacos.discovery.password:}")
    private String password;

    public Map<String, Object> check() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("component", "nacos");
        result.put("namespace", namespace);
        result.put("group", group);
        result.put("dataId", applicationName + "-dev.yaml");
        try {
            Properties properties = new Properties();
            properties.put("serverAddr", serverAddr);
            properties.put("namespace", namespace);
            properties.put("username", username);
            properties.put("password", password);

            ConfigService configService = NacosFactory.createConfigService(properties);
            NamingService namingService = NacosFactory.createNamingService(properties);
            String configStatus = configService.getServerStatus();
            String namingStatus = namingService.getServerStatus();

            result.put("configStatus", configStatus);
            result.put("discoveryStatus", namingStatus);
            boolean up = "UP".equalsIgnoreCase(configStatus) && "UP".equalsIgnoreCase(namingStatus);
            result.put("status", up ? "UP" : "DOWN");
            if (!up) {
                result.put("error", "Nacos server not UP");
            }
        } catch (Exception ex) {
            result.put("status", "DOWN");
            result.put("error", ex.getClass().getSimpleName() + ": " + ex.getMessage());
        }
        return result;
    }
}
'@

$minioCheckerTemplate = @'
package com.dating.{PKG}.health;

import com.dating.{PKG}.config.MinioProperties;
import io.minio.MinioClient;
import io.minio.BucketExistsArgs;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class MinioInfraHealthChecker {

    private final MinioClient minioClient;
    private final MinioProperties minioProperties;

    public MinioInfraHealthChecker(MinioClient minioClient, MinioProperties minioProperties) {
        this.minioClient = minioClient;
        this.minioProperties = minioProperties;
    }

    public Map<String, Object> check() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("component", "minio");
        result.put("bucket", minioProperties.getBucket());
        result.put("pathStyleAccess", minioProperties.isPathStyleAccess());
        try {
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(minioProperties.getBucket()).build());
            result.put("bucketExists", exists);
            result.put("status", exists ? "UP" : "DOWN");
            if (!exists) {
                result.put("error", "Bucket not found: " + minioProperties.getBucket());
            }
        } catch (Exception ex) {
            result.put("status", "DOWN");
            result.put("error", ex.getClass().getSimpleName() + ": " + ex.getMessage());
        }
        return result;
    }
}
'@

$infraControllerTemplate = @'
package com.dating.{PKG}.controller;

import com.dating.{PKG}.service.InfraHealthService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class InfraHealthController {

    private final InfraHealthService infraHealthService;

    public InfraHealthController(InfraHealthService infraHealthService) {
        this.infraHealthService = infraHealthService;
    }

    @GetMapping("/health/infra")
    public Map<String, Object> infraHealth() {
        Map<String, Object> body = new LinkedHashMap<>(infraHealthService.checkAll());
        body.put("timestamp", Instant.now().toString());
        return body;
    }
}
'@

$pomExtraDeps = @'
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-jdbc</artifactId>
        </dependency>
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-core</artifactId>
        </dependency>
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-database-postgresql</artifactId>
        </dependency>
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>io.minio</groupId>
            <artifactId>minio</artifactId>
            <version>8.5.12</version>
        </dependency>
'@

foreach ($svc in $services) {
    $svcRoot = Join-Path $workspaceRoot $svc.name
    $javaRoot = Join-Path $svcRoot "src/main/java/com/dating/$($svc.pkg)"
    $resRoot = Join-Path $svcRoot "src/main/resources"

    Write-Utf8File (Join-Path $javaRoot "config/AppProperties.java") ($infraJavaTemplate -replace '\{PKG\}', $svc.pkg)
    Write-Utf8File (Join-Path $javaRoot "config/MinioProperties.java") ($minioPropsTemplate -replace '\{PKG\}', $svc.pkg)
    Write-Utf8File (Join-Path $javaRoot "config/InfraConfiguration.java") ($minioConfigTemplate -replace '\{PKG\}', $svc.pkg)
    Write-Utf8File (Join-Path $javaRoot "service/InfraHealthService.java") (($infraHealthServiceTemplate -replace '\{PKG\}', $svc.pkg) -replace '\{SERVICE_NAME\}', $svc.name)
    if ($svc.hasDb) {
        Write-Utf8File (Join-Path $javaRoot "health/PostgresInfraHealthChecker.java") ($postgresCheckerTemplate -replace '\{PKG\}', $svc.pkg)
    } else {
        Write-Utf8File (Join-Path $javaRoot "health/PostgresInfraHealthChecker.java") ($postgresCheckerNoDbTemplate -replace '\{PKG\}', $svc.pkg)
    }
    Write-Utf8File (Join-Path $javaRoot "health/RedisInfraHealthChecker.java") ($redisCheckerTemplate -replace '\{PKG\}', $svc.pkg)
    Write-Utf8File (Join-Path $javaRoot "health/NacosInfraHealthChecker.java") ($nacosCheckerTemplate -replace '\{PKG\}', $svc.pkg)
    Write-Utf8File (Join-Path $javaRoot "health/MinioInfraHealthChecker.java") ($minioCheckerTemplate -replace '\{PKG\}', $svc.pkg)
    Write-Utf8File (Join-Path $javaRoot "controller/InfraHealthController.java") ($infraControllerTemplate -replace '\{PKG\}', $svc.pkg)

    # Update HealthController stage
    $healthPath = Join-Path $javaRoot "controller/HealthController.java"
    if (Test-Path $healthPath) {
        $hc = [System.IO.File]::ReadAllText($healthPath)
        $hc = $hc -replace 'stage", "00-A"', 'stage", "00-B"'
        [System.IO.File]::WriteAllText($healthPath, $hc, $utf8)
    }

    # application-dev.yml
    $grpcBlock = ""
    if ($svc.grpc) { $grpcBlock = "`n  grpc:`n    port: $($svc.grpc)" }
    $openImBlock = ""
    if ($svc.openIm) {
        $openImBlock = @"

openim:
  api-base-url: `${OPENIM_API_BASE_URL:}
  ws-url: `${OPENIM_WS_URL:}
  admin-user-id: `${OPENIM_ADMIN_USER_ID:}
  admin-secret: `${OPENIM_ADMIN_SECRET:}
  callback-secret: `${OPENIM_CALLBACK_SECRET:}
  user-id-prefix: `${OPENIM_USER_ID_PREFIX:yanshuqi_}
"@
    }
    $dbBlock = ""
    if ($svc.hasDb) {
        $dbBlock = @"
  datasource:
    url: jdbc:postgresql://`${POSTGRES_HOST}:`${POSTGRES_PORT}/`${POSTGRES_DATABASE:dating_dev_yanshuqi}?currentSchema=$($svc.schema)&stringtype=unspecified
    username: `${POSTGRES_USERNAME}
    password: `${POSTGRES_PASSWORD}
    driver-class-name: org.postgresql.Driver
    hikari:
      maximum-pool-size: 8
      minimum-idle: 2
      connection-init-sql: SET TIME ZONE 'UTC'
  data:
    redis:
      host: `${REDIS_HOST}
      port: `${REDIS_PORT}
      password: `${REDIS_PASSWORD}
      database: `${REDIS_DATABASE:1}
      timeout: 3s
  flyway:
    enabled: true
    schemas: $($svc.schema)
    table: $($svc.flyway)
    baseline-on-migrate: true
"@
    } else {
        $dbBlock = @"
  data:
    redis:
      host: `${REDIS_HOST}
      port: `${REDIS_PORT}
      password: `${REDIS_PASSWORD}
      database: `${REDIS_DATABASE:1}
      timeout: 3s
"@
    }

    $appDev = @"
server:
  port: `${SERVER_PORT:$($svc.port)}

spring:
  application:
    name: $($svc.name)
$dbBlock

app:
  cache:
    key-prefix: `${REDIS_KEY_PREFIX:yanshuqi}
  service:
    name: $($svc.name)
    redis-key-suffix: $($svc.redisSuffix)$grpcBlock
  infra:
    redis-test-key: `${REDIS_KEY_PREFIX:yanshuqi}:$($svc.redisSuffix):infra:ping
    redis-test-ttl-seconds: 60

minio:
  endpoint: `${MINIO_ENDPOINT}
  access-key: `${MINIO_ACCESS_KEY}
  secret-key: `${MINIO_SECRET_KEY}
  bucket: `${MINIO_BUCKET:dating-yanshuqi}
  region: `${MINIO_REGION:us-east-1}
  path-style-access: `${MINIO_PATH_STYLE_ACCESS:true}

rocketmq:
  name-server: `${ROCKETMQ_NAME_SERVER:}
  access-key: `${ROCKETMQ_ACCESS_KEY:}
  secret-key: `${ROCKETMQ_SECRET_KEY:}
  topic-prefix: `${ROCKETMQ_TOPIC_PREFIX:yanshuqi_dev}
$openImBlock
management:
  endpoints:
    web:
      exposure:
        include: health,info
  endpoint:
    health:
      show-details: always
  health:
    db:
      enabled: $($svc.hasDb.ToString().ToLower())
    redis:
      enabled: true

logging:
  level:
    root: INFO
"@
    Write-Utf8File (Join-Path $resRoot "application-dev.yml") $appDev

    $bootstrap = @"
spring:
  application:
    name: `${APP_NAME:$($svc.name)}
  profiles:
    active: `${SPRING_PROFILES_ACTIVE:dev}
  cloud:
    nacos:
      discovery:
        enabled: true
        server-addr: `${NACOS_SERVER_ADDR}
        namespace: `${NACOS_NAMESPACE:yanshuqi-dev}
        group: `${NACOS_GROUP:DEFAULT_GROUP}
        username: `${NACOS_USERNAME}
        password: `${NACOS_PASSWORD}
      config:
        enabled: true
        server-addr: `${NACOS_SERVER_ADDR}
        namespace: `${NACOS_NAMESPACE:yanshuqi-dev}
        group: `${NACOS_GROUP:DEFAULT_GROUP}
        username: `${NACOS_USERNAME}
        password: `${NACOS_PASSWORD}
        file-extension: yaml
"@
    Write-Utf8File (Join-Path $resRoot "bootstrap.yml") $bootstrap

    # pom.xml - inject infra deps before spring-boot-starter-test
    $pomPath = Join-Path $svcRoot "pom.xml"
    $pom = [System.IO.File]::ReadAllText($pomPath)
    if ($pom -notmatch 'spring-boot-starter-data-redis') {
        $pom = $pom -replace '(<dependency>\s*<groupId>org.springframework.boot</groupId>\s*<artifactId>spring-boot-starter-test</artifactId>)', ($pomExtraDeps + "`r`n        `$1")
        [System.IO.File]::WriteAllText($pomPath, $pom, $utf8)
    }
}

Write-Host "Stage 00-B Java infra generated for $($services.Count) services."
