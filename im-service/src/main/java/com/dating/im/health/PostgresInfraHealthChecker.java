package com.dating.im.health;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
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