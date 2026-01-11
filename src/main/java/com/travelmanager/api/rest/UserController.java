package com.travelmanager.api.rest;

import com.travelmanager.database.DatabaseManager;
import com.travelmanager.database.DatabaseManager.PendingUser;
import com.travelmanager.model.User;
import io.javalin.http.Context;

import java.sql.SQLException;
import java.util.*;

/**
 * REST Controller for user authentication and account management
 * Handles login, registration, and account approval for Android app integration
 */
public class UserController {
    private final DatabaseManager databaseManager;

    public UserController() {
        this.databaseManager = DatabaseManager.getInstance();
    }

    // ============= AUTHENTICATION ENDPOINTS =============

    /**
     * POST /api/auth/login
     * Authenticate user and return session token
     * Body: { "username": "...", "password": "..." }
     */
    public void login(Context ctx) {
        try {
            LoginRequest request = ctx.bodyAsClass(LoginRequest.class);
            
            if (request.username == null || request.username.trim().isEmpty() ||
                request.password == null || request.password.trim().isEmpty()) {
                ctx.status(400).json(Map.of(
                    "success", false,
                    "error", "Username and password are required"
                ));
                return;
            }

            User user = databaseManager.authenticateUser(request.username.trim(), request.password);
            
            if (user != null) {
                // Generate a simple session token (in production, use JWT)
                String token = generateSessionToken(user);
                
                ctx.json(Map.of(
                    "success", true,
                    "message", "Login successful",
                    "user", Map.of(
                        "id", user.getId(),
                        "username", user.getUsername(),
                        "email", user.getEmail(),
                        "fullName", user.getFullName(),
                        "role", user.getRole().toString()
                    ),
                    "token", token
                ));
            } else {
                ctx.status(401).json(Map.of(
                    "success", false,
                    "error", "Invalid username or password"
                ));
            }
        } catch (Exception e) {
            ctx.status(500).json(Map.of(
                "success", false,
                "error", "Login failed",
                "message", e.getMessage()
            ));
        }
    }

    /**
     * POST /api/auth/register
     * Register a new user account (pending approval)
     * Body: { "username": "...", "email": "...", "password": "...", "fullName": "...", "role": "USER|DEVELOPER" }
     */
    public void register(Context ctx) {
        try {
            RegisterRequest request = ctx.bodyAsClass(RegisterRequest.class);
            
            // Validate required fields
            List<String> errors = validateRegistration(request);
            if (!errors.isEmpty()) {
                ctx.status(400).json(Map.of(
                    "success", false,
                    "errors", errors
                ));
                return;
            }

            // Check if username already exists
            if (databaseManager.userExists(request.username.trim())) {
                ctx.status(409).json(Map.of(
                    "success", false,
                    "error", "Username already exists"
                ));
                return;
            }

            // Check if username is pending approval
            if (databaseManager.isPendingUser(request.username.trim())) {
                ctx.status(409).json(Map.of(
                    "success", false,
                    "error", "Username is pending approval"
                ));
                return;
            }

            // Create pending user
            User.Role role = User.Role.valueOf(request.role != null ? request.role.toUpperCase() : "USER");
            boolean success = databaseManager.createPendingUser(
                request.username.trim(),
                request.email.trim(),
                request.password,
                role,
                request.fullName.trim()
            );

            if (success) {
                ctx.status(201).json(Map.of(
                    "success", true,
                    "message", "Account created successfully. Awaiting master approval.",
                    "status", "PENDING"
                ));
            } else {
                ctx.status(500).json(Map.of(
                    "success", false,
                    "error", "Failed to create account"
                ));
            }
        } catch (IllegalArgumentException e) {
            ctx.status(400).json(Map.of(
                "success", false,
                "error", "Invalid role. Must be USER or DEVELOPER"
            ));
        } catch (Exception e) {
            ctx.status(500).json(Map.of(
                "success", false,
                "error", "Registration failed",
                "message", e.getMessage()
            ));
        }
    }

    /**
     * GET /api/auth/status/{username}
     * Check the approval status of a pending account
     */
    public void checkAccountStatus(Context ctx) {
        String username = ctx.pathParam("username");
        
        try {
            // Check if user is already approved
            if (databaseManager.userExists(username)) {
                ctx.json(Map.of(
                    "username", username,
                    "status", "APPROVED",
                    "message", "Account is active. You can log in."
                ));
                return;
            }

            // Check if user is pending
            if (databaseManager.isPendingUser(username)) {
                ctx.json(Map.of(
                    "username", username,
                    "status", "PENDING",
                    "message", "Account is awaiting approval from master."
                ));
                return;
            }

            // User not found
            ctx.status(404).json(Map.of(
                "username", username,
                "status", "NOT_FOUND",
                "message", "No account found with this username."
            ));
        } catch (Exception e) {
            ctx.status(500).json(Map.of(
                "error", "Failed to check account status",
                "message", e.getMessage()
            ));
        }
    }

    // ============= ACCOUNT APPROVAL ENDPOINTS (Master/Developer only) =============

    /**
     * GET /api/admin/pending-users
     * Get all pending user registrations
     * Requires: Authorization header with valid token
     */
    public void getPendingUsers(Context ctx) {
        try {
            // Verify authorization (simplified - in production use proper JWT)
            String authHeader = ctx.header("Authorization");
            if (!isAuthorizedAdmin(authHeader)) {
                ctx.status(403).json(Map.of(
                    "success", false,
                    "error", "Access denied. Master or Developer role required."
                ));
                return;
            }

            List<PendingUser> pendingUsers = databaseManager.getPendingUsers();
            List<Map<String, Object>> userList = new ArrayList<>();
            
            for (PendingUser user : pendingUsers) {
                userList.add(Map.of(
                    "id", user.getId(),
                    "username", user.getUsername(),
                    "email", user.getEmail(),
                    "fullName", user.getFullName(),
                    "role", user.getRole().toString(),
                    "createdDate", user.getCreatedDate().toString(),
                    "status", "PENDING"
                ));
            }

            ctx.json(Map.of(
                "success", true,
                "count", userList.size(),
                "pendingUsers", userList
            ));
        } catch (SQLException e) {
            ctx.status(500).json(Map.of(
                "success", false,
                "error", "Failed to retrieve pending users",
                "message", e.getMessage()
            ));
        }
    }

    /**
     * POST /api/admin/approve/{userId}
     * Approve a pending user registration
     * Requires: Authorization header with valid token
     */
    public void approveUser(Context ctx) {
        try {
            int userId = Integer.parseInt(ctx.pathParam("userId"));
            
            // Verify authorization
            String authHeader = ctx.header("Authorization");
            if (!isAuthorizedAdmin(authHeader)) {
                ctx.status(403).json(Map.of(
                    "success", false,
                    "error", "Access denied. Master or Developer role required."
                ));
                return;
            }

            boolean success = databaseManager.approvePendingUser(userId);
            
            if (success) {
                ctx.json(Map.of(
                    "success", true,
                    "message", "User approved successfully"
                ));
            } else {
                ctx.status(404).json(Map.of(
                    "success", false,
                    "error", "Pending user not found"
                ));
            }
        } catch (NumberFormatException e) {
            ctx.status(400).json(Map.of(
                "success", false,
                "error", "Invalid user ID"
            ));
        } catch (SQLException e) {
            ctx.status(500).json(Map.of(
                "success", false,
                "error", "Failed to approve user",
                "message", e.getMessage()
            ));
        }
    }

    /**
     * POST /api/admin/reject/{userId}
     * Reject a pending user registration
     * Body (optional): { "reason": "..." }
     * Requires: Authorization header with valid token
     */
    public void rejectUser(Context ctx) {
        try {
            int userId = Integer.parseInt(ctx.pathParam("userId"));
            
            // Verify authorization
            String authHeader = ctx.header("Authorization");
            if (!isAuthorizedAdmin(authHeader)) {
                ctx.status(403).json(Map.of(
                    "success", false,
                    "error", "Access denied. Master or Developer role required."
                ));
                return;
            }

            boolean success = databaseManager.rejectPendingUser(userId);
            
            if (success) {
                ctx.json(Map.of(
                    "success", true,
                    "message", "User rejected successfully"
                ));
            } else {
                ctx.status(404).json(Map.of(
                    "success", false,
                    "error", "Pending user not found"
                ));
            }
        } catch (NumberFormatException e) {
            ctx.status(400).json(Map.of(
                "success", false,
                "error", "Invalid user ID"
            ));
        } catch (SQLException e) {
            ctx.status(500).json(Map.of(
                "success", false,
                "error", "Failed to reject user",
                "message", e.getMessage()
            ));
        }
    }

    /**
     * GET /api/users/profile
     * Get current user's profile
     * Requires: Authorization header with valid token
     */
    public void getProfile(Context ctx) {
        try {
            String authHeader = ctx.header("Authorization");
            User user = getUserFromToken(authHeader);
            
            if (user == null) {
                ctx.status(401).json(Map.of(
                    "success", false,
                    "error", "Invalid or expired token"
                ));
                return;
            }

            ctx.json(Map.of(
                "success", true,
                "user", Map.of(
                    "id", user.getId(),
                    "username", user.getUsername(),
                    "email", user.getEmail(),
                    "fullName", user.getFullName(),
                    "role", user.getRole().toString()
                )
            ));
        } catch (Exception e) {
            ctx.status(500).json(Map.of(
                "success", false,
                "error", "Failed to get profile",
                "message", e.getMessage()
            ));
        }
    }

    // ============= HELPER METHODS =============

    private List<String> validateRegistration(RegisterRequest request) {
        List<String> errors = new ArrayList<>();
        
        if (request.username == null || request.username.trim().isEmpty()) {
            errors.add("Username is required");
        } else if (request.username.trim().length() < 3) {
            errors.add("Username must be at least 3 characters");
        }
        
        if (request.email == null || request.email.trim().isEmpty()) {
            errors.add("Email is required");
        } else if (!request.email.contains("@") || !request.email.contains(".")) {
            errors.add("Invalid email format");
        }
        
        if (request.password == null || request.password.isEmpty()) {
            errors.add("Password is required");
        } else if (request.password.length() < 6) {
            errors.add("Password must be at least 6 characters");
        }
        
        if (request.fullName == null || request.fullName.trim().isEmpty()) {
            errors.add("Full name is required");
        }
        
        return errors;
    }

    private String generateSessionToken(User user) {
        // Simple token format: base64(userId:username:role:timestamp)
        // In production, use proper JWT with secret key
        String tokenData = user.getId() + ":" + user.getUsername() + ":" + 
                          user.getRole().toString() + ":" + System.currentTimeMillis();
        return Base64.getEncoder().encodeToString(tokenData.getBytes());
    }

    private boolean isAuthorizedAdmin(String authHeader) {
        User user = getUserFromToken(authHeader);
        return user != null && (user.isMaster() || user.isDeveloper());
    }

    private User getUserFromToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        
        try {
            String token = authHeader.substring(7);
            String decoded = new String(Base64.getDecoder().decode(token));
            String[] parts = decoded.split(":");
            
            if (parts.length >= 2) {
                String username = parts[1];
                return databaseManager.getUserByUsername(username);
            }
        } catch (Exception e) {
            // Invalid token
        }
        return null;
    }

    // ============= REQUEST/RESPONSE DTOs =============

    public static class LoginRequest {
        public String username;
        public String password;
    }

    public static class RegisterRequest {
        public String username;
        public String email;
        public String password;
        public String fullName;
        public String role; // USER or DEVELOPER
    }
}
