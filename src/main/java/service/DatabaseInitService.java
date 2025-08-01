package main.java.service;

import main.java.logger.LoggerFacade;
import main.java.util.DBConnection;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

@Service
public class DatabaseInitService {

    private static DatabaseInitService instance;

    private DatabaseInitService() {}

    public static DatabaseInitService getInstance() {
        if (instance == null) {
            instance = new DatabaseInitService();
        }
        return instance;
    }

    public void initializeDatabase() {
        LoggerFacade.info("Initializing database schema...");
        try (Connection conn = DBConnection.getConnection()) {
            // Test connection
            if (conn != null && !conn.isClosed()) {
                LoggerFacade.info("Database connection successful");
                executeSchema(conn);
                LoggerFacade.info("Database schema initialized successfully");
            } else {
                LoggerFacade.fatal("Failed to connect to database");
                throw new RuntimeException("Database connection failed");
            }
        } catch (SQLException e) {
            LoggerFacade.fatal("Database connection error: " + e.getMessage());
            throw new RuntimeException("Database initialization failed", e);
        }
    }

    private void executeSchema(Connection conn) throws SQLException {
        String schema = loadSchemaFromResources();

        if (schema == null || schema.trim().isEmpty()) {
            LoggerFacade.error("Schema is empty or could not be loaded");
            return;
        }

        try (Statement stmt = conn.createStatement()) {
            String[] statements = schema.split(";");

            for (String statement : statements) {
                String trimmedStatement = statement.trim();
                if (!trimmedStatement.isEmpty()) {
                    LoggerFacade.debug("Executing SQL: " + trimmedStatement);
                    stmt.execute(trimmedStatement);
                }
            }
        }
    }

    private String loadSchemaFromResources() {
        StringBuilder schema = new StringBuilder();

        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("resources/schema.sql")) {
            if (inputStream == null) {
                LoggerFacade.error("Schema file 'schema.sql' not found in resources");
                return null;
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    schema.append(line).append("\n");
                }
            }

        } catch (IOException e) {
            LoggerFacade.error("Error reading schema file: " + e.getMessage());
            return null;
        }

        return schema.toString();
    }

    public boolean testConnection() {
        try (Connection conn = DBConnection.getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            LoggerFacade.error("Error testing database connection: " + e.getMessage());
            return false;
        }
    }
}
