package com.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

public class Function {
    private static final Logger logger = Logger.getLogger(Function.class.getName());
    private final UserService userService = new UserService();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @FunctionName("createUser")
    public HttpResponseMessage createUser(
            @HttpTrigger(name = "req", methods = {HttpMethod.POST}, route = "users") 
            HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        
        try {
            String body = request.getBody().orElse("");
            User user = objectMapper.readValue(body, User.class);
            
            if (user.getUsername() == null || user.getEmail() == null) {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body("{\"error\": \"Username and email are required\"}")
                    .header("Content-Type", "application/json")
                    .build();
            }
            
            User createdUser = userService.createUser(user);
            return request.createResponseBuilder(HttpStatus.CREATED)
                .body(objectMapper.writeValueAsString(createdUser))
                .header("Content-Type", "application/json")
                .build();
                
        } catch (Exception e) {
            logger.severe("Error creating user: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("{\"error\": \"Internal server error\"}")
                .header("Content-Type", "application/json")
                .build();
        }
    }

    @FunctionName("getUser")
    public HttpResponseMessage getUser(
            @HttpTrigger(name = "req", methods = {HttpMethod.GET}, route = "users/{id}")
            HttpRequestMessage<Optional<String>> request,
            @BindingName("id") String id,
            final ExecutionContext context) {
        
        try {
            Long userId = Long.parseLong(id);
            User user = userService.getUserById(userId);
            
            if (user == null) {
                return request.createResponseBuilder(HttpStatus.NOT_FOUND)
                    .body("{\"error\": \"User not found\"}")
                    .header("Content-Type", "application/json")
                    .build();
            }
            
            return request.createResponseBuilder(HttpStatus.OK)
                .body(objectMapper.writeValueAsString(user))
                .header("Content-Type", "application/json")
                .build();
                
        } catch (NumberFormatException e) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                .body("{\"error\": \"Invalid user ID\"}")
                .header("Content-Type", "application/json")
                .build();
        } catch (Exception e) {
            logger.severe("Error getting user: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("{\"error\": \"Internal server error\"}")
                .header("Content-Type", "application/json")
                .build();
        }
    }

    @FunctionName("getAllUsers")
    public HttpResponseMessage getAllUsers(
            @HttpTrigger(name = "req", methods = {HttpMethod.GET}, route = "users")
            HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        
        try {
            List<User> users = userService.getAllUsers();
            return request.createResponseBuilder(HttpStatus.OK)
                .body(objectMapper.writeValueAsString(users))
                .header("Content-Type", "application/json")
                .build();
                
        } catch (Exception e) {
            logger.severe("Error getting all users: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("{\"error\": \"Internal server error\"}")
                .header("Content-Type", "application/json")
                .build();
        }
    }

    @FunctionName("updateUser")
    public HttpResponseMessage updateUser(
            @HttpTrigger(name = "req", methods = {HttpMethod.PUT}, route = "users/{id}")
            HttpRequestMessage<Optional<String>> request,
            @BindingName("id") String id,
            final ExecutionContext context) {
        
        try {
            Long userId = Long.parseLong(id);
            String body = request.getBody().orElse("");
            User user = objectMapper.readValue(body, User.class);
            
            User updatedUser = userService.updateUser(userId, user);
            if (updatedUser == null) {
                return request.createResponseBuilder(HttpStatus.NOT_FOUND)
                    .body("{\"error\": \"User not found\"}")
                    .header("Content-Type", "application/json")
                    .build();
            }
            
            return request.createResponseBuilder(HttpStatus.OK)
                .body(objectMapper.writeValueAsString(updatedUser))
                .header("Content-Type", "application/json")
                .build();
                
        } catch (NumberFormatException e) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                .body("{\"error\": \"Invalid user ID\"}")
                .header("Content-Type", "application/json")
                .build();
        } catch (Exception e) {
            logger.severe("Error updating user: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("{\"error\": \"Internal server error\"}")
                .header("Content-Type", "application/json")
                .build();
        }
    }

    @FunctionName("deleteUser")
    public HttpResponseMessage deleteUser(
            @HttpTrigger(name = "req", methods = {HttpMethod.DELETE}, route = "users/{id}")
            HttpRequestMessage<Optional<String>> request,
            @BindingName("id") String id,
            final ExecutionContext context) {
        
        try {
            Long userId = Long.parseLong(id);
            boolean deleted = userService.deleteUser(userId);
            
            if (!deleted) {
                return request.createResponseBuilder(HttpStatus.NOT_FOUND)
                    .body("{\"error\": \"User not found\"}")
                    .header("Content-Type", "application/json")
                    .build();
            }
            
            return request.createResponseBuilder(HttpStatus.NO_CONTENT).build();
                
        } catch (NumberFormatException e) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                .body("{\"error\": \"Invalid user ID\"}")
                .header("Content-Type", "application/json")
                .build();
        } catch (Exception e) {
            logger.severe("Error deleting user: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("{\"error\": \"Internal server error\"}")
                .header("Content-Type", "application/json")
                .build();
        }
    }
}
