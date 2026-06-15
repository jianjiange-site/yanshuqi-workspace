package com.dating.post.config;

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