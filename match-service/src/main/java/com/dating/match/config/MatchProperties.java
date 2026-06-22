package com.dating.match.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Match 域配置。
 */
@ConfigurationProperties(prefix = "app.match")
public class MatchProperties {

    private long dhDelayedMatchMinMs = 15000L;
    private long dhDelayedMatchMaxMs = 120000L;
    private int superHiCoinPrice = 100;
    private FeedProperties feed = new FeedProperties();
    private D1Properties d1 = new D1Properties();
    private ScoreProperties score = new ScoreProperties();
    private ExternalProperties external = new ExternalProperties();
    private GrpcTargetProperties grpc = new GrpcTargetProperties();

    public long getDhDelayedMatchMinMs() {
        return dhDelayedMatchMinMs;
    }

    public void setDhDelayedMatchMinMs(long dhDelayedMatchMinMs) {
        this.dhDelayedMatchMinMs = dhDelayedMatchMinMs;
    }

    public long getDhDelayedMatchMaxMs() {
        return dhDelayedMatchMaxMs;
    }

    public void setDhDelayedMatchMaxMs(long dhDelayedMatchMaxMs) {
        this.dhDelayedMatchMaxMs = dhDelayedMatchMaxMs;
    }

    public int getSuperHiCoinPrice() {
        return superHiCoinPrice;
    }

    public void setSuperHiCoinPrice(int superHiCoinPrice) {
        this.superHiCoinPrice = superHiCoinPrice;
    }

    public FeedProperties getFeed() {
        return feed;
    }

    public void setFeed(FeedProperties feed) {
        this.feed = feed;
    }

    public D1Properties getD1() {
        return d1;
    }

    public void setD1(D1Properties d1) {
        this.d1 = d1;
    }

    public ScoreProperties getScore() {
        return score;
    }

    public void setScore(ScoreProperties score) {
        this.score = score;
    }

    public ExternalProperties getExternal() {
        return external;
    }

    public void setExternal(ExternalProperties external) {
        this.external = external;
    }

    public GrpcTargetProperties getGrpc() {
        return grpc;
    }

    public void setGrpc(GrpcTargetProperties grpc) {
        this.grpc = grpc;
    }

    public static class FeedProperties {

        private int queueSize = 240;
        private double coldStartBhRatio = 0.20D;
        private int queueTtlDays = 7;

        public int getQueueSize() {
            return queueSize;
        }

        public void setQueueSize(int queueSize) {
            this.queueSize = queueSize;
        }

        public double getColdStartBhRatio() {
            return coldStartBhRatio;
        }

        public void setColdStartBhRatio(double coldStartBhRatio) {
            this.coldStartBhRatio = coldStartBhRatio;
        }

        public int getQueueTtlDays() {
            return queueTtlDays;
        }

        public void setQueueTtlDays(int queueTtlDays) {
            this.queueTtlDays = queueTtlDays;
        }
    }

    public static class D1Properties {

        private double bhRatio = 0.40D;
        private boolean preferenceEnabled = true;
        private double preferenceOffset = 0.20D;
        private int minPreferenceSamples = 10;
        private int preferenceWindowDays = 30;

        public double getBhRatio() {
            return bhRatio;
        }

        public void setBhRatio(double bhRatio) {
            this.bhRatio = bhRatio;
        }

        public boolean isPreferenceEnabled() {
            return preferenceEnabled;
        }

        public void setPreferenceEnabled(boolean preferenceEnabled) {
            this.preferenceEnabled = preferenceEnabled;
        }

        public double getPreferenceOffset() {
            return preferenceOffset;
        }

        public void setPreferenceOffset(double preferenceOffset) {
            this.preferenceOffset = preferenceOffset;
        }

        public int getMinPreferenceSamples() {
            return minPreferenceSamples;
        }

        public void setMinPreferenceSamples(int minPreferenceSamples) {
            this.minPreferenceSamples = minPreferenceSamples;
        }

        public int getPreferenceWindowDays() {
            return preferenceWindowDays;
        }

        public void setPreferenceWindowDays(int preferenceWindowDays) {
            this.preferenceWindowDays = preferenceWindowDays;
        }
    }

    public static class ScoreProperties {

        private double mutualLikeBonus = 0.20D;
        private double newBhBonus = 0.20D;
        private int newBhWindowDays = 3;

        public double getMutualLikeBonus() {
            return mutualLikeBonus;
        }

        public void setMutualLikeBonus(double mutualLikeBonus) {
            this.mutualLikeBonus = mutualLikeBonus;
        }

        public double getNewBhBonus() {
            return newBhBonus;
        }

        public void setNewBhBonus(double newBhBonus) {
            this.newBhBonus = newBhBonus;
        }

        public int getNewBhWindowDays() {
            return newBhWindowDays;
        }

        public void setNewBhWindowDays(int newBhWindowDays) {
            this.newBhWindowDays = newBhWindowDays;
        }
    }

    /**
     * 外部服务 client 模式：mock（默认）或 grpc。
     */
    public static class ExternalProperties {

        private String userClientMode = "mock";
        private String paymentClientMode = "mock";
        private String imClientMode = "mock";

        public String getUserClientMode() {
            return userClientMode;
        }

        public void setUserClientMode(String userClientMode) {
            this.userClientMode = userClientMode;
        }

        public String getPaymentClientMode() {
            return paymentClientMode;
        }

        public void setPaymentClientMode(String paymentClientMode) {
            this.paymentClientMode = paymentClientMode;
        }

        public String getImClientMode() {
            return imClientMode;
        }

        public void setImClientMode(String imClientMode) {
            this.imClientMode = imClientMode;
        }
    }

    /**
     * gRPC 目标地址文档化配置；实际连接由 grpc.client.* 控制。
     */
    public static class GrpcTargetProperties {

        private String userServiceTarget = "static://127.0.0.1:9091";
        private String paymentServiceTarget = "static://127.0.0.1:9095";
        private String imServiceTarget = "static://127.0.0.1:9093";

        public String getUserServiceTarget() {
            return userServiceTarget;
        }

        public void setUserServiceTarget(String userServiceTarget) {
            this.userServiceTarget = userServiceTarget;
        }

        public String getPaymentServiceTarget() {
            return paymentServiceTarget;
        }

        public void setPaymentServiceTarget(String paymentServiceTarget) {
            this.paymentServiceTarget = paymentServiceTarget;
        }

        public String getImServiceTarget() {
            return imServiceTarget;
        }

        public void setImServiceTarget(String imServiceTarget) {
            this.imServiceTarget = imServiceTarget;
        }
    }
}
