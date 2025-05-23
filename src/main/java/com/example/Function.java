package com.example;

import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

import java.sql.*;
import java.util.*;
import java.util.logging.Logger;

public class Function {
    private static final Logger logger = Logger.getLogger(Function.class.getName());

    @FunctionName("PostgresExample")
    public HttpResponseMessage run(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.GET},
                authLevel = AuthorizationLevel.ANONYMOUS)
                HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        String jdbcUrl = System.getenv("POSTGRES_JDBC_URL");
        String username = System.getenv("POSTGRES_USERNAME");
        String password = System.getenv("POSTGRES_PASSWORD");

        try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password);
             Statement statement = connection.createStatement()) {
            
            ResultSet resultSet = statement.executeQuery("SELECT version()");
            if (resultSet.next()) {
                String version = resultSet.getString(1);
                return request.createResponseBuilder(HttpStatus.OK)
                        .body("PostgreSQL version: " + version)
                        .build();
            }
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("No results found")
                    .build();
            
        } catch (SQLException e) {
            logger.severe("SQL Exception: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Database connection failed: " + e.getMessage())
                    .build();
        }
    }
}
