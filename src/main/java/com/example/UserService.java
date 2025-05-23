package com.example;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class UserService {
    private static final Logger logger = Logger.getLogger(UserService.class.getName());
    private static HikariDataSource dataSource;

    static {
        initializeDataSource();
    }

    private static void initializeDataSource() {
        try {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(System.getenv("POSTGRES_URL"));
            config.setUsername(System.getenv("POSTGRES_USER"));
            config.setPassword(System.getenv("POSTGRES_PASSWORD"));
            config.setMaximumPoolSize(10);
            config.setConnectionTimeout(30000);
            config.setIdleTimeout(600000);
            config.setMaxLifetime(1800000);
            
            dataSource = new HikariDataSource(config);
            createTableIfNotExists();
        } catch (Exception e) {
            logger.severe("Failed to initialize database connection: " + e.getMessage());
        }
    }

    private static void createTableIfNotExists() {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS users (" +
                "id SERIAL PRIMARY KEY, " +
                "username VARCHAR(100) UNIQUE NOT NULL, " +
                "email VARCHAR(255) UNIQUE NOT NULL, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")";

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createTableSQL);
            logger.info("Users table created or already exists");
        } catch (SQLException e) {
            logger.severe("Error creating users table: " + e.getMessage());
        }
    }

    public User createUser(User user) throws SQLException {
        String sql = "INSERT INTO users (username, email) VALUES (?, ?) RETURNING id";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getEmail());
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                user.setId(rs.getLong("id"));
                logger.info("User created: " + user);
                return user;
            }
        }
        throw new SQLException("Failed to create user");
    }

    public User getUserById(Long id) throws SQLException {
        String sql = "SELECT id, username, email FROM users WHERE id = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return new User(
                    rs.getLong("id"),
                    rs.getString("username"),
                    rs.getString("email")
                );
            }
        }
        return null;
    }

    public List<User> getAllUsers() throws SQLException {
        String sql = "SELECT id, username, email FROM users ORDER BY id";
        List<User> users = new ArrayList<>();
        
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                users.add(new User(
                    rs.getLong("id"),
                    rs.getString("username"),
                    rs.getString("email")
                ));
            }
        }
        return users;
    }

    public User updateUser(Long id, User user) throws SQLException {
        String sql = "UPDATE users SET username = ?, email = ? WHERE id = ? RETURNING id, username, email";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getEmail());
            stmt.setLong(3, id);
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new User(
                    rs.getLong("id"),
                    rs.getString("username"),
                    rs.getString("email")
                );
            }
        }
        return null;
    }

    public boolean deleteUser(Long id) throws SQLException {
        String sql = "DELETE FROM users WHERE id = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            return stmt.executeUpdate() > 0;
        }
    }
}
