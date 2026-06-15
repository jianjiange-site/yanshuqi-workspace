from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    model_config = SettingsConfigDict(env_file=".env", env_file_encoding="utf-8", extra="ignore")

    personal_name: str = "yanshuqi"
    app_env: str = "dev"
    service_name: str = "ai-chat"

    ai_chat_http_port: int = 8090
    ai_chat_grpc_port: int = 9190

    postgres_host: str = "localhost"
    postgres_port: int = 5432
    postgres_database: str = "dating_dev_yanshuqi"
    postgres_username: str = "your-postgres-username"
    postgres_password: str = "your-postgres-password"
    postgres_schema: str = "ai_chat"

    redis_host: str = "localhost"
    redis_port: int = 6379
    redis_password: str = "your-redis-password"
    redis_database: int = 1
    redis_key_prefix: str = "yanshuqi"
    redis_test_key: str = "yanshuqi:ai:infra:ping"
    redis_test_ttl_seconds: int = 60

    nacos_server_addr: str = "localhost:8848"
    nacos_namespace: str = "yanshuqi-dev"
    nacos_group: str = "DEFAULT_GROUP"
    nacos_username: str = "your-nacos-username"
    nacos_password: str = "your-nacos-password"

    minio_endpoint: str = "https://minio-api.example.com"
    minio_access_key: str = "your-minio-access-key"
    minio_secret_key: str = "your-minio-secret-key"
    minio_bucket: str = "dating-yanshuqi"
    minio_region: str = "us-east-1"
    minio_path_style_access: bool = True

    rocketmq_name_server: str = ""
    rocketmq_access_key: str = "your-rocketmq-access-key"
    rocketmq_secret_key: str = "your-rocketmq-secret-key"
    rocketmq_topic_prefix: str = "yanshuqi_dev"

    llm_provider: str = "disabled"
    llm_api_key: str = "your-llm-api-key"


settings = Settings()
