package com.travelmanager.database;

import com.travelmanager.model.BusSchedule;
import com.travelmanager.model.Route;
import com.travelmanager.model.Schedule;
import com.travelmanager.model.TrainSchedule;
import com.travelmanager.model.User;
import com.travelmanager.storage.BusScheduleStorage;
import com.travelmanager.storage.TrainScheduleStorage;
import com.travelmanager.model.rest.BusScheduleDTO;
import com.travelmanager.model.rest.TrainScheduleDTO;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Database manager for storing and retrieving saved travel plans
 */
public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:data/travel_plans.db";
    private static DatabaseManager instance;

    private DatabaseManager() {
        initializeDatabase();
    }

    public static DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    private void initializeDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            
            // Create plans table
            String createPlansTable = """
                CREATE TABLE IF NOT EXISTS plans (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL UNIQUE,
                    total_fare REAL NOT NULL,
                    created_date TEXT NOT NULL,
                    notes TEXT
                )
                """;
            stmt.execute(createPlansTable);
            
            // Add notes column if it doesn't exist (for existing databases)
            try {
                stmt.execute("ALTER TABLE plans ADD COLUMN notes TEXT");
            } catch (SQLException e) {
                // Column already exists, ignore
            }

            // Create schedules table
            String createSchedulesTable = """
                CREATE TABLE IF NOT EXISTS schedules (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    plan_id INTEGER NOT NULL,
                    schedule_id TEXT NOT NULL,
                    type TEXT NOT NULL,
                    origin TEXT NOT NULL,
                    destination TEXT NOT NULL,
                    departure_time TEXT NOT NULL,
                    arrival_time TEXT NOT NULL,
                    fare REAL NOT NULL,
                    available_seats INTEGER NOT NULL,
                    company_name TEXT,
                    bus_type TEXT,
                    train_name TEXT,
                    train_number TEXT,
                    seat_class TEXT,
                    leg_order INTEGER NOT NULL,
                    FOREIGN KEY (plan_id) REFERENCES plans(id) ON DELETE CASCADE
                )
                """;
            stmt.execute(createSchedulesTable);

            // Create users table
            String createUsersTable = """
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT NOT NULL UNIQUE,
                    email TEXT NOT NULL UNIQUE,
                    password_hash TEXT NOT NULL,
                    password_salt TEXT NOT NULL,
                    role TEXT NOT NULL,
                    full_name TEXT NOT NULL,
                    created_date TEXT NOT NULL,
                    failed_login_attempts INTEGER DEFAULT 0,
                    account_locked_until TEXT,
                    last_login TEXT
                )
                """;
            stmt.execute(createUsersTable);
            
            // Add new security columns if they don't exist (for existing databases)
            try {
                stmt.execute("ALTER TABLE users ADD COLUMN password_salt TEXT");
            } catch (SQLException e) {
                // Column already exists, ignore
            }
            try {
                stmt.execute("ALTER TABLE users ADD COLUMN failed_login_attempts INTEGER DEFAULT 0");
            } catch (SQLException e) {
                // Column already exists, ignore
            }
            try {
                stmt.execute("ALTER TABLE users ADD COLUMN account_locked_until TEXT");
            } catch (SQLException e) {
                // Column already exists, ignore
            }
            try {
                stmt.execute("ALTER TABLE users ADD COLUMN last_login TEXT");
            } catch (SQLException e) {
                // Column already exists, ignore
            }
            
            // Create pending users table
            String createPendingUsersTable = """
                CREATE TABLE IF NOT EXISTS pending_users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT NOT NULL UNIQUE,
                    email TEXT NOT NULL UNIQUE,
                    password_hash TEXT NOT NULL,
                    password_salt TEXT NOT NULL,
                    role TEXT NOT NULL,
                    full_name TEXT NOT NULL,
                    created_date TEXT NOT NULL,
                    status TEXT DEFAULT 'PENDING'
                )
                """;
            stmt.execute(createPendingUsersTable);
            
            // Create routes table for API management
            String createRoutesTable = """
                CREATE TABLE IF NOT EXISTS routes (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    route_name TEXT NOT NULL,
                    origin TEXT NOT NULL,
                    destination TEXT NOT NULL,
                    transport_type TEXT NOT NULL,
                    status TEXT DEFAULT 'ACTIVE',
                    duration_minutes INTEGER,
                    price REAL,
                    schedule_time TEXT,
                    metadata TEXT,
                    created_date TEXT NOT NULL,
                    modified_date TEXT
                )
                """;
            stmt.execute(createRoutesTable);
            
            // Create pending routes table for API change approval
            String createPendingRoutesTable = """
                CREATE TABLE IF NOT EXISTS pending_routes (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    route_name TEXT NOT NULL,
                    origin TEXT NOT NULL,
                    destination TEXT NOT NULL,
                    transport_type TEXT NOT NULL,
                    duration_minutes INTEGER,
                    price REAL,
                    schedule_time TEXT,
                    metadata TEXT,
                    status TEXT DEFAULT 'PENDING',
                    change_type TEXT NOT NULL,
                    original_route_id INTEGER,
                    submitted_by TEXT NOT NULL,
                    submitted_date TEXT NOT NULL,
                    notes TEXT,
                    feedback TEXT,
                    reviewed_by TEXT,
                    reviewed_date TEXT
                )
                """;
            stmt.execute(createPendingRoutesTable);
            
            // Create route history table for tracking all API changes
            String createRouteHistoryTable = """
                CREATE TABLE IF NOT EXISTS route_history (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    route_name TEXT NOT NULL,
                    origin TEXT,
                    destination TEXT,
                    transport_type TEXT,
                    duration_minutes INTEGER,
                    price REAL,
                    schedule_time TEXT,
                    metadata TEXT,
                    change_type TEXT NOT NULL,
                    original_route_id INTEGER,
                    submitted_by TEXT NOT NULL,
                    submitted_date TEXT NOT NULL,
                    reviewed_by TEXT,
                    reviewed_date TEXT NOT NULL,
                    status TEXT NOT NULL,
                    notes TEXT,
                    feedback TEXT
                )
                """;
            stmt.execute(createRouteHistoryTable);
            
            // Create activity log table for tracking user actions
            String createActivityLogTable = """
                CREATE TABLE IF NOT EXISTS activity_log (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT NOT NULL,
                    action_type TEXT NOT NULL,
                    action_details TEXT,
                    ip_address TEXT,
                    timestamp TEXT NOT NULL,
                    success INTEGER DEFAULT 1
                )
                """;
            stmt.execute(createActivityLogTable);
            
            // Create default users if table is empty
            createDefaultUsers(conn);

        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void createDefaultUsers(Connection conn) throws SQLException {
        String checkQuery = "SELECT COUNT(*) FROM users";
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(checkQuery);
        
        if (rs.next() && rs.getInt(1) == 0) {
            String insertUser = "INSERT INTO users (username, email, password_hash, password_salt, role, full_name, created_date, failed_login_attempts) VALUES (?, ?, ?, ?, ?, ?, ?, 0)";
            
            // Master Account: username=master, password=11111111
            String masterSalt = generateSalt();
            PreparedStatement pstmt = conn.prepareStatement(insertUser);
            pstmt.setString(1, "master");
            pstmt.setString(2, "master@travelmanager.com");
            pstmt.setString(3, hashPasswordWithSalt("11111111", masterSalt));
            pstmt.setString(4, masterSalt);
            pstmt.setString(5, "MASTER");
            pstmt.setString(6, "Master Account");
            pstmt.setString(7, LocalDateTime.now().toString());
            pstmt.executeUpdate();
            
            System.out.println("Master account created successfully");
        }
    }
    
    // Generate random salt for password hashing
    private String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }
    
    // Enhanced password hashing using PBKDF2 with salt
    private String hashPasswordWithSalt(String password, String salt) {
        try {
            byte[] saltBytes = Base64.getDecoder().decode(salt);
            KeySpec spec = new PBEKeySpec(password.toCharArray(), saltBytes, 65536, 128);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            byte[] hash = factory.generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Error hashing password with salt", e);
        }
    }
    
    // Legacy SHA-256 hashing (kept for backward compatibility during migration)
    @Deprecated
    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }
    
    // User authentication with account lockout protection
    public User authenticateUser(String usernameOrEmail, String password) throws SQLException {
        String query = "SELECT * FROM users WHERE username = ? OR email = ?";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, usernameOrEmail);
            pstmt.setString(2, usernameOrEmail);
            
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                int userId = rs.getInt("id");
                
                // Check if account is locked
                String lockedUntil = rs.getString("account_locked_until");
                if (lockedUntil != null) {
                    LocalDateTime lockTime = LocalDateTime.parse(lockedUntil);
                    if (LocalDateTime.now().isBefore(lockTime)) {
                        throw new SQLException("Account is locked due to multiple failed login attempts. Try again later.");
                    } else {
                        // Unlock account and reset failed attempts
                        unlockAccount(conn, userId);
                    }
                }
                
                String storedHash = rs.getString("password_hash");
                String storedSalt = rs.getString("password_salt");
                String inputHash;
                
                // Support both new (with salt) and legacy (without salt) authentication
                if (storedSalt != null && !storedSalt.isEmpty()) {
                    inputHash = hashPasswordWithSalt(password, storedSalt);
                } else {
                    // Fallback to legacy SHA-256 for old accounts
                    inputHash = hashPassword(password);
                }
                
                if (storedHash.equals(inputHash)) {
                    // Successful login - reset failed attempts and update last login
                    resetFailedLoginAttempts(conn, userId);
                    updateLastLogin(conn, userId);
                    
                    return new User(
                        userId,
                        rs.getString("username"),
                        rs.getString("email"),
                        storedHash,
                        User.Role.valueOf(rs.getString("role")),
                        rs.getString("full_name")
                    );
                } else {
                    // Failed login - increment failed attempts
                    incrementFailedLoginAttempts(conn, userId);
                }
            }
        }
        
        return null; // Authentication failed
    }
    
    // Increment failed login attempts and lock account if threshold exceeded
    private void incrementFailedLoginAttempts(Connection conn, int userId) throws SQLException {
        String query = "UPDATE users SET failed_login_attempts = failed_login_attempts + 1 WHERE id = ?";
        PreparedStatement pstmt = conn.prepareStatement(query);
        pstmt.setInt(1, userId);
        pstmt.executeUpdate();
        
        // Check if account should be locked (5 failed attempts)
        String checkQuery = "SELECT failed_login_attempts FROM users WHERE id = ?";
        pstmt = conn.prepareStatement(checkQuery);
        pstmt.setInt(1, userId);
        ResultSet rs = pstmt.executeQuery();
        
        if (rs.next() && rs.getInt("failed_login_attempts") >= 5) {
            // Lock account for 15 minutes
            LocalDateTime lockUntil = LocalDateTime.now().plusMinutes(15);
            String lockQuery = "UPDATE users SET account_locked_until = ? WHERE id = ?";
            pstmt = conn.prepareStatement(lockQuery);
            pstmt.setString(1, lockUntil.toString());
            pstmt.setInt(2, userId);
            pstmt.executeUpdate();
        }
    }
    
    // Reset failed login attempts after successful login
    private void resetFailedLoginAttempts(Connection conn, int userId) throws SQLException {
        String query = "UPDATE users SET failed_login_attempts = 0, account_locked_until = NULL WHERE id = ?";
        PreparedStatement pstmt = conn.prepareStatement(query);
        pstmt.setInt(1, userId);
        pstmt.executeUpdate();
    }
    
    // Unlock account after lockout period expires
    private void unlockAccount(Connection conn, int userId) throws SQLException {
        String query = "UPDATE users SET failed_login_attempts = 0, account_locked_until = NULL WHERE id = ?";
        PreparedStatement pstmt = conn.prepareStatement(query);
        pstmt.setInt(1, userId);
        pstmt.executeUpdate();
    }
    
    // Update last login timestamp
    private void updateLastLogin(Connection conn, int userId) throws SQLException {
        String query = "UPDATE users SET last_login = ? WHERE id = ?";
        PreparedStatement pstmt = conn.prepareStatement(query);
        pstmt.setString(1, LocalDateTime.now().toString());
        pstmt.setInt(2, userId);
        pstmt.executeUpdate();
    }
    
    // Create new user with enhanced security
    public boolean createUser(String username, String email, String password, User.Role role, String fullName) throws SQLException {
        String salt = generateSalt();
        String query = "INSERT INTO users (username, email, password_hash, password_salt, role, full_name, created_date, failed_login_attempts) VALUES (?, ?, ?, ?, ?, ?, ?, 0)";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, username);
            pstmt.setString(2, email);
            pstmt.setString(3, hashPasswordWithSalt(password, salt));
            pstmt.setString(4, salt);
            pstmt.setString(5, role.name());
            pstmt.setString(6, fullName);
            pstmt.setString(7, LocalDateTime.now().toString());
            
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            if (e.getMessage().contains("UNIQUE constraint failed")) {
                return false; // Username or email already exists
            }
            throw e;
        }
    }
    
    // Create pending user account (requires master approval)
    public boolean createPendingUser(String username, String email, String password, User.Role role, String fullName) throws SQLException {
        String salt = generateSalt();
        String query = "INSERT INTO pending_users (username, email, password_hash, password_salt, role, full_name, created_date, status) VALUES (?, ?, ?, ?, ?, ?, ?, 'PENDING')";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, username);
            pstmt.setString(2, email);
            pstmt.setString(3, hashPasswordWithSalt(password, salt));
            pstmt.setString(4, salt);
            pstmt.setString(5, role.name());
            pstmt.setString(6, fullName);
            pstmt.setString(7, LocalDateTime.now().toString());
            
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            if (e.getMessage().contains("UNIQUE constraint failed")) {
                return false; // Username or email already exists
            }
            throw e;
        }
    }
    
    // Get all pending users for master approval
    public List<PendingUser> getPendingUsers() throws SQLException {
        String query = "SELECT * FROM pending_users WHERE status = 'PENDING' ORDER BY created_date DESC";
        List<PendingUser> pendingUsers = new ArrayList<>();
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                PendingUser pu = new PendingUser(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("email"),
                    rs.getString("password_hash"),
                    rs.getString("password_salt"),
                    User.Role.valueOf(rs.getString("role")),
                    rs.getString("full_name"),
                    rs.getString("created_date")
                );
                pendingUsers.add(pu);
            }
        }
        
        return pendingUsers;
    }
    
    // Approve pending user account
    public boolean approvePendingUser(int pendingUserId) throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            conn.setAutoCommit(false);
            
            try {
                // Get pending user data
                String selectQuery = "SELECT * FROM pending_users WHERE id = ?";
                PreparedStatement selectStmt = conn.prepareStatement(selectQuery);
                selectStmt.setInt(1, pendingUserId);
                ResultSet rs = selectStmt.executeQuery();
                
                if (rs.next()) {
                    // Insert into users table
                    String insertQuery = "INSERT INTO users (username, email, password_hash, password_salt, role, full_name, created_date, failed_login_attempts) VALUES (?, ?, ?, ?, ?, ?, ?, 0)";
                    PreparedStatement insertStmt = conn.prepareStatement(insertQuery);
                    insertStmt.setString(1, rs.getString("username"));
                    insertStmt.setString(2, rs.getString("email"));
                    insertStmt.setString(3, rs.getString("password_hash"));
                    insertStmt.setString(4, rs.getString("password_salt"));
                    insertStmt.setString(5, rs.getString("role"));
                    insertStmt.setString(6, rs.getString("full_name"));
                    insertStmt.setString(7, LocalDateTime.now().toString());
                    insertStmt.executeUpdate();
                    
                    // Delete from pending_users
                    String deleteQuery = "DELETE FROM pending_users WHERE id = ?";
                    PreparedStatement deleteStmt = conn.prepareStatement(deleteQuery);
                    deleteStmt.setInt(1, pendingUserId);
                    deleteStmt.executeUpdate();
                    
                    conn.commit();
                    return true;
                }
                
                conn.rollback();
                return false;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }
    
    // Reject pending user account
    public boolean rejectPendingUser(int pendingUserId) throws SQLException {
        String query = "DELETE FROM pending_users WHERE id = ?";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, pendingUserId);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        }
    }
    
    // Check if a username exists in pending_users table
    public boolean isPendingUser(String username) {
        String query = "SELECT COUNT(*) FROM pending_users WHERE username = ?";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // Check if a username exists in users table
    public boolean userExists(String username) {
        String query = "SELECT COUNT(*) FROM users WHERE username = ?";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // Change user role (Master only)
    public boolean changeUserRole(String username, String newRole) throws SQLException {
        String query = "UPDATE users SET role = ? WHERE username = ?";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, newRole);
            pstmt.setString(2, username);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        }
    }
    
    // Get all users for management
    public List<User> getAllUsers() throws SQLException {
        String query = "SELECT id, username, email, password_hash, role, full_name FROM users ORDER BY created_date DESC";
        List<User> users = new ArrayList<>();
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                User.Role role = User.Role.valueOf(rs.getString("role"));
                User user = new User(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("email"),
                    rs.getString("password_hash"),
                    role,
                    rs.getString("full_name")
                );
                users.add(user);
            }
        }
        
        return users;
    }
    
    // ==================== ROUTES MANAGEMENT (Master API) ====================
    
    // Create a new route (Master only - goes directly to live)
    public boolean createRoute(String routeName, String origin, String destination, 
                               String transportType, int durationMinutes, double price, 
                               String scheduleTime, String metadata) {
        String query = "INSERT INTO routes (route_name, origin, destination, transport_type, " +
                      "duration_minutes, price, schedule_time, metadata, status, created_date) " +
                      "VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'ACTIVE', ?)";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, routeName);
            pstmt.setString(2, origin);
            pstmt.setString(3, destination);
            pstmt.setString(4, transportType);
            pstmt.setInt(5, durationMinutes);
            pstmt.setDouble(6, price);
            pstmt.setString(7, scheduleTime);
            pstmt.setString(8, metadata);
            pstmt.setString(9, LocalDateTime.now().toString());
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // Get all routes
    public List<RouteData> getAllRoutes() throws SQLException {
        String query = "SELECT * FROM routes ORDER BY created_date DESC";
        List<RouteData> routes = new ArrayList<>();
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                routes.add(new RouteData(
                    rs.getInt("id"),
                    rs.getString("route_name"),
                    rs.getString("origin"),
                    rs.getString("destination"),
                    rs.getString("transport_type"),
                    rs.getString("status"),
                    rs.getInt("duration_minutes"),
                    rs.getDouble("price"),
                    rs.getString("schedule_time"),
                    rs.getString("metadata"),
                    rs.getString("created_date"),
                    rs.getString("modified_date")
                ));
            }
        }
        return routes;
    }
    
    // Update a route
    public boolean updateRoute(int routeId, String routeName, String origin, String destination,
                              String transportType, int durationMinutes, double price,
                              String scheduleTime, String metadata, String status) {
        String query = "UPDATE routes SET route_name = ?, origin = ?, destination = ?, " +
                      "transport_type = ?, duration_minutes = ?, price = ?, schedule_time = ?, " +
                      "metadata = ?, status = ?, modified_date = ? WHERE id = ?";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, routeName);
            pstmt.setString(2, origin);
            pstmt.setString(3, destination);
            pstmt.setString(4, transportType);
            pstmt.setInt(5, durationMinutes);
            pstmt.setDouble(6, price);
            pstmt.setString(7, scheduleTime);
            pstmt.setString(8, metadata);
            pstmt.setString(9, status);
            pstmt.setString(10, LocalDateTime.now().toString());
            pstmt.setInt(11, routeId);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // Delete a route
    public boolean deleteRoute(int routeId) {
        String query = "DELETE FROM routes WHERE id = ?";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, routeId);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // ==================== PENDING ROUTES (Developer Submissions) ====================
    
    // Submit a new route change request (Developer)
    public boolean submitPendingRoute(String routeName, String origin, String destination,
                                     String transportType, int durationMinutes, double price,
                                     String scheduleTime, String metadata, String changeType,
                                     Integer originalRouteId, String submittedBy, String notes) {
        String query = "INSERT INTO pending_routes (route_name, origin, destination, transport_type, " +
                      "duration_minutes, price, schedule_time, metadata, status, change_type, " +
                      "original_route_id, submitted_by, submitted_date, notes) " +
                      "VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'PENDING', ?, ?, ?, ?, ?)";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, routeName);
            pstmt.setString(2, origin);
            pstmt.setString(3, destination);
            pstmt.setString(4, transportType);
            pstmt.setInt(5, durationMinutes);
            pstmt.setDouble(6, price);
            pstmt.setString(7, scheduleTime);
            pstmt.setString(8, metadata);
            pstmt.setString(9, changeType);
            if (originalRouteId != null) {
                pstmt.setInt(10, originalRouteId);
            } else {
                pstmt.setNull(10, java.sql.Types.INTEGER);
            }
            pstmt.setString(11, submittedBy);
            pstmt.setString(12, LocalDateTime.now().toString());
            pstmt.setString(13, notes);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // Get all pending route changes
    public List<PendingRouteData> getPendingRoutes() throws SQLException {
        String query = "SELECT * FROM pending_routes WHERE status = 'PENDING' ORDER BY submitted_date DESC";
        List<PendingRouteData> routes = new ArrayList<>();
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                routes.add(new PendingRouteData(
                    rs.getInt("id"),
                    rs.getString("route_name"),
                    rs.getString("origin"),
                    rs.getString("destination"),
                    rs.getString("transport_type"),
                    rs.getInt("duration_minutes"),
                    rs.getDouble("price"),
                    rs.getString("schedule_time"),
                    rs.getString("metadata"),
                    rs.getString("status"),
                    rs.getString("change_type"),
                    rs.getObject("original_route_id") != null ? rs.getInt("original_route_id") : null,
                    rs.getString("submitted_by"),
                    rs.getString("submitted_date"),
                    rs.getString("notes")
                ));
            }
        }
        return routes;
    }
    
    // Approve pending route change (Master)
    public boolean approvePendingRoute(int pendingRouteId, String reviewedBy) throws SQLException {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(DB_URL);
            conn.setAutoCommit(false);
            
            // Get pending route data
            String selectQuery = "SELECT * FROM pending_routes WHERE id = ?";
            PreparedStatement selectStmt = conn.prepareStatement(selectQuery);
            selectStmt.setInt(1, pendingRouteId);
            ResultSet rs = selectStmt.executeQuery();
            
            if (rs.next()) {
                String changeType = rs.getString("change_type");
                String routeName = rs.getString("route_name");
                String origin = rs.getString("origin");
                String destination = rs.getString("destination");
                String transportType = rs.getString("transport_type");
                int durationMinutes = rs.getInt("duration_minutes");
                double price = rs.getDouble("price");
                String scheduleTime = rs.getString("schedule_time");
                String metadata = rs.getString("metadata");
                
                // Apply changes to database
                if ("CREATE".equals(changeType) || "ADD".equals(changeType)) {
                    // Insert new route to live routes table
                    String insertQuery = "INSERT INTO routes (route_name, origin, destination, transport_type, " +
                                        "duration_minutes, price, schedule_time, metadata, status, created_date) " +
                                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'ACTIVE', ?)";
                    PreparedStatement insertStmt = conn.prepareStatement(insertQuery);
                    insertStmt.setString(1, routeName);
                    insertStmt.setString(2, origin);
                    insertStmt.setString(3, destination);
                    insertStmt.setString(4, transportType);
                    insertStmt.setInt(5, durationMinutes);
                    insertStmt.setDouble(6, price);
                    insertStmt.setString(7, scheduleTime);
                    insertStmt.setString(8, metadata);
                    insertStmt.setString(9, LocalDateTime.now().toString());
                    insertStmt.executeUpdate();
                    
                    // Write to JSON file for REST API
                    writeToJsonFile(routeName, origin, destination, transportType, durationMinutes, 
                                   price, scheduleTime, metadata, "CREATE");
                    
                } else if ("UPDATE".equals(changeType)) {
                    // Update existing route
                    int originalRouteId = rs.getInt("original_route_id");
                    String updateQuery = "UPDATE routes SET route_name = ?, origin = ?, destination = ?, " +
                                        "transport_type = ?, duration_minutes = ?, price = ?, schedule_time = ?, " +
                                        "metadata = ?, modified_date = ? WHERE id = ?";
                    PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
                    updateStmt.setString(1, routeName);
                    updateStmt.setString(2, origin);
                    updateStmt.setString(3, destination);
                    updateStmt.setString(4, transportType);
                    updateStmt.setInt(5, durationMinutes);
                    updateStmt.setDouble(6, price);
                    updateStmt.setString(7, scheduleTime);
                    updateStmt.setString(8, metadata);
                    updateStmt.setString(9, LocalDateTime.now().toString());
                    updateStmt.setInt(10, originalRouteId);
                    updateStmt.executeUpdate();
                    
                    // Update JSON file for REST API
                    writeToJsonFile(routeName, origin, destination, transportType, durationMinutes, 
                                   price, scheduleTime, metadata, "UPDATE");
                    
                } else if ("DELETE".equals(changeType)) {
                    // Delete route from live table
                    int originalRouteId = rs.getInt("original_route_id");
                    String deleteQuery = "DELETE FROM routes WHERE id = ?";
                    PreparedStatement deleteStmt = conn.prepareStatement(deleteQuery);
                    deleteStmt.setInt(1, originalRouteId);
                    deleteStmt.executeUpdate();
                    
                    // Delete from JSON file for REST API
                    deleteFromJsonFile(routeName, transportType);
                }
                
                // Archive to history
                String insertHistory = "INSERT INTO route_history (route_name, origin, destination, transport_type, " +
                                      "duration_minutes, price, schedule_time, metadata, change_type, original_route_id, " +
                                      "submitted_by, submitted_date, reviewed_by, reviewed_date, status, notes, feedback) " +
                                      "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'APPROVED', ?, NULL)";
                PreparedStatement historyStmt = conn.prepareStatement(insertHistory);
                historyStmt.setString(1, routeName);
                historyStmt.setString(2, origin);
                historyStmt.setString(3, destination);
                historyStmt.setString(4, transportType);
                historyStmt.setInt(5, durationMinutes);
                historyStmt.setDouble(6, price);
                historyStmt.setString(7, scheduleTime);
                historyStmt.setString(8, metadata);
                historyStmt.setString(9, changeType);
                if (rs.getObject("original_route_id") != null) {
                    historyStmt.setInt(10, rs.getInt("original_route_id"));
                } else {
                    historyStmt.setNull(10, java.sql.Types.INTEGER);
                }
                historyStmt.setString(11, rs.getString("submitted_by"));
                historyStmt.setString(12, rs.getString("submitted_date"));
                historyStmt.setString(13, reviewedBy);
                historyStmt.setString(14, LocalDateTime.now().toString());
                historyStmt.setString(15, rs.getString("notes"));
                historyStmt.executeUpdate();
                
                // Delete from pending routes
                String deleteQuery = "DELETE FROM pending_routes WHERE id = ?";
                PreparedStatement deleteStmt = conn.prepareStatement(deleteQuery);
                deleteStmt.setInt(1, pendingRouteId);
                deleteStmt.executeUpdate();
                
                conn.commit();
                
                System.out.println("✓ Route " + changeType + " approved and written to JSON file: " + routeName);
                return true;
            }
            
            conn.rollback();
            return false;
            
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }
    
    /**
     * Write approved route to JSON file for REST API consumption
     */
    private void writeToJsonFile(String routeName, String origin, String destination, 
                                  String transportType, int durationMinutes, double price,
                                  String scheduleTime, String metadata, String operationType) {
        try {
            // Parse schedule time and calculate arrival time
            String startTime = scheduleTime != null && !scheduleTime.isEmpty() ? scheduleTime : "08:00";
            String arrivalTime = calculateArrivalTime(startTime, durationMinutes);
            String duration = formatDuration(durationMinutes);
            
            if ("BUS".equalsIgnoreCase(transportType)) {
                BusScheduleStorage busStorage = BusScheduleStorage.getInstance();
                BusScheduleDTO busDTO = new BusScheduleDTO();
                busDTO.setBusName(routeName);
                busDTO.setStart(origin);
                busDTO.setDestination(destination);
                busDTO.setStartTime(startTime);
                busDTO.setArrivalTime(arrivalTime);
                busDTO.setFare(price);
                busDTO.setDuration(duration);
                
                if ("CREATE".equals(operationType)) {
                    busStorage.addSchedule(busDTO);
                } else if ("UPDATE".equals(operationType)) {
                    busStorage.updateSchedule(routeName, busDTO);
                }
                System.out.println("✓ Bus schedule written to JSON: " + routeName);
                
            } else if ("TRAIN".equalsIgnoreCase(transportType)) {
                TrainScheduleStorage trainStorage = TrainScheduleStorage.getInstance();
                TrainScheduleDTO trainDTO = new TrainScheduleDTO();
                trainDTO.setTrainName(routeName);
                trainDTO.setStart(origin);
                trainDTO.setDestination(destination);
                trainDTO.setStartTime(startTime);
                trainDTO.setArrivalTime(arrivalTime);
                trainDTO.setFare(price);
                trainDTO.setDuration(duration);
                
                // Parse metadata for stops and offDay
                String offDay = "No off day";
                if (metadata != null && !metadata.isEmpty()) {
                    // Extract offDay
                    if (metadata.contains("offDay:")) {
                        String[] parts = metadata.split(";");
                        for (String part : parts) {
                            if (part.trim().startsWith("offDay:")) {
                                offDay = part.substring(part.indexOf(":") + 1).trim();
                            }
                        }
                    }
                    
                    // Extract and parse stops JSON
                    if (metadata.contains("stops:")) {
                        try {
                            String stopsJsonStr = metadata.substring(metadata.indexOf("stops:") + 6);
                            if (stopsJsonStr.contains(";")) {
                                stopsJsonStr = stopsJsonStr.substring(0, stopsJsonStr.indexOf(";"));
                            }
                            
                            // Parse stops JSON manually (simple JSON array parsing)
                            List<TrainScheduleDTO.TrainStop> stops = new ArrayList<>();
                            stopsJsonStr = stopsJsonStr.trim();
                            if (stopsJsonStr.startsWith("[") && stopsJsonStr.endsWith("]")) {
                                stopsJsonStr = stopsJsonStr.substring(1, stopsJsonStr.length() - 1);
                                String[] stopObjects = stopsJsonStr.split("\\},\\{");
                                
                                for (String stopObj : stopObjects) {
                                    stopObj = stopObj.replace("{", "").replace("}", "");
                                    String station = "";
                                    String arr = "";
                                    String dep = "";
                                    double fare = 0;
                                    
                                    String[] fields = stopObj.split(",");
                                    for (String field : fields) {
                                        field = field.trim();
                                        if (field.contains("\"station\":")) {
                                            station = field.substring(field.indexOf(":") + 1).replace("\"", "").trim();
                                        } else if (field.contains("\"arrivalTime\":")) {
                                            arr = field.substring(field.indexOf(":") + 1).replace("\"", "").trim();
                                        } else if (field.contains("\"departureTime\":")) {
                                            dep = field.substring(field.indexOf(":") + 1).replace("\"", "").trim();
                                        } else if (field.contains("\"cumulativeFare\":")) {
                                            String fareStr = field.substring(field.indexOf(":") + 1).trim();
                                            fare = Double.parseDouble(fareStr);
                                        }
                                    }
                                    
                                    TrainScheduleDTO.TrainStop stop = new TrainScheduleDTO.TrainStop();
                                    stop.setStation(station);
                                    stop.setArrivalTime(arr);
                                    stop.setDepartureTime(dep);
                                    stop.setCumulativeFare(fare);
                                    stops.add(stop);
                                }
                            }
                            
                            if (!stops.isEmpty()) {
                                trainDTO.setStops(stops);
                            }
                        } catch (Exception e) {
                            System.err.println("⚠ Warning: Could not parse stops from metadata: " + e.getMessage());
                        }
                    }
                }
                
                trainDTO.setOffDay(offDay);
                
                if ("CREATE".equals(operationType)) {
                    trainStorage.addSchedule(trainDTO);
                } else if ("UPDATE".equals(operationType)) {
                    trainStorage.updateSchedule(routeName, trainDTO);
                }
                System.out.println("✓ Train schedule written to JSON: " + routeName);
            }
        } catch (Exception e) {
            System.err.println("✗ Error writing to JSON file: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Delete route from JSON file for REST API
     */
    private void deleteFromJsonFile(String routeName, String transportType) {
        try {
            if ("BUS".equalsIgnoreCase(transportType)) {
                BusScheduleStorage busStorage = BusScheduleStorage.getInstance();
                busStorage.deleteSchedule(routeName);
                System.out.println("✓ Bus schedule deleted from JSON: " + routeName);
            } else if ("TRAIN".equalsIgnoreCase(transportType)) {
                TrainScheduleStorage trainStorage = TrainScheduleStorage.getInstance();
                trainStorage.deleteSchedule(routeName);
                System.out.println("✓ Train schedule deleted from JSON: " + routeName);
            }
        } catch (Exception e) {
            System.err.println("✗ Error deleting from JSON file: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Calculate arrival time based on departure time and duration
     */
    private String calculateArrivalTime(String departureTime, int durationMinutes) {
        try {
            String[] parts = departureTime.split(":");
            int hours = Integer.parseInt(parts[0]);
            int minutes = Integer.parseInt(parts[1]);
            
            minutes += durationMinutes;
            hours += minutes / 60;
            minutes = minutes % 60;
            hours = hours % 24;
            
            return String.format("%02d:%02d", hours, minutes);
        } catch (Exception e) {
            return "10:00"; // Default fallback
        }
    }
    
    /**
     * Format duration in minutes to "H:MMh" format (e.g., 390 minutes -> "6:30h")
     */
    private String formatDuration(int durationMinutes) {
        int hours = durationMinutes / 60;
        int minutes = durationMinutes % 60;
        return String.format("%d:%02dh", hours, minutes);
    }
    
    // Backward compatibility
    public boolean approvePendingRoute(int pendingRouteId) throws SQLException {
        return approvePendingRoute(pendingRouteId, "SYSTEM");
    }
    
    // Reject pending route change (Master) with feedback
    public boolean rejectPendingRoute(int pendingRouteId, String feedback, String reviewedBy) throws SQLException {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(DB_URL);
            conn.setAutoCommit(false);
            
            // Get pending route data first
            String selectQuery = "SELECT * FROM pending_routes WHERE id = ?";
            PreparedStatement selectStmt = conn.prepareStatement(selectQuery);
            selectStmt.setInt(1, pendingRouteId);
            ResultSet rs = selectStmt.executeQuery();
            
            if (rs.next()) {
                // Archive to history
                String insertHistory = "INSERT INTO route_history (route_name, origin, destination, transport_type, " +
                                      "duration_minutes, price, schedule_time, metadata, change_type, original_route_id, " +
                                      "submitted_by, submitted_date, reviewed_by, reviewed_date, status, notes, feedback) " +
                                      "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'REJECTED', ?, ?)";
                PreparedStatement historyStmt = conn.prepareStatement(insertHistory);
                historyStmt.setString(1, rs.getString("route_name"));
                historyStmt.setString(2, rs.getString("origin"));
                historyStmt.setString(3, rs.getString("destination"));
                historyStmt.setString(4, rs.getString("transport_type"));
                historyStmt.setInt(5, rs.getInt("duration_minutes"));
                historyStmt.setDouble(6, rs.getDouble("price"));
                historyStmt.setString(7, rs.getString("schedule_time"));
                historyStmt.setString(8, rs.getString("metadata"));
                historyStmt.setString(9, rs.getString("change_type"));
                if (rs.getObject("original_route_id") != null) {
                    historyStmt.setInt(10, rs.getInt("original_route_id"));
                } else {
                    historyStmt.setNull(10, java.sql.Types.INTEGER);
                }
                historyStmt.setString(11, rs.getString("submitted_by"));
                historyStmt.setString(12, rs.getString("submitted_date"));
                historyStmt.setString(13, reviewedBy);
                historyStmt.setString(14, LocalDateTime.now().toString());
                historyStmt.setString(15, rs.getString("notes"));
                historyStmt.setString(16, feedback);
                historyStmt.executeUpdate();
            }
            
            // Delete from pending
            String deleteQuery = "DELETE FROM pending_routes WHERE id = ?";
            PreparedStatement deleteStmt = conn.prepareStatement(deleteQuery);
            deleteStmt.setInt(1, pendingRouteId);
            int rowsAffected = deleteStmt.executeUpdate();
            
            conn.commit();
            return rowsAffected > 0;
        } catch (SQLException e) {
            if (conn != null) conn.rollback();
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }
    
    // Backward compatibility - reject without feedback
    public boolean rejectPendingRoute(int pendingRouteId) throws SQLException {
        return rejectPendingRoute(pendingRouteId, null, "SYSTEM");
    }
    
    // Withdraw a pending request (Developer)
    public boolean withdrawPendingRoute(int pendingRouteId, String username) throws SQLException {
        String query = "DELETE FROM pending_routes WHERE id = ? AND submitted_by = ? AND status = 'PENDING'";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, pendingRouteId);
            pstmt.setString(2, username);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        }
    }
    
    // Update a pending request (Developer)
    public boolean updatePendingRoute(int pendingRouteId, String username, String routeName, 
                                     String origin, String destination, String transportType, 
                                     int durationMinutes, double price, String scheduleTime, 
                                     String metadata, String notes) throws SQLException {
        String query = "UPDATE pending_routes SET route_name = ?, origin = ?, destination = ?, " +
                      "transport_type = ?, duration_minutes = ?, price = ?, schedule_time = ?, " +
                      "metadata = ?, notes = ? WHERE id = ? AND submitted_by = ? AND status = 'PENDING'";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, routeName);
            pstmt.setString(2, origin);
            pstmt.setString(3, destination);
            pstmt.setString(4, transportType);
            pstmt.setInt(5, durationMinutes);
            pstmt.setDouble(6, price);
            pstmt.setString(7, scheduleTime);
            pstmt.setString(8, metadata);
            pstmt.setString(9, notes);
            pstmt.setInt(10, pendingRouteId);
            pstmt.setString(11, username);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        }
    }
    
    // Get route history for a specific user
    public List<RouteHistoryData> getRouteHistory(String username) throws SQLException {
        String query = "SELECT * FROM route_history WHERE submitted_by = ? ORDER BY reviewed_date DESC";
        List<RouteHistoryData> history = new ArrayList<>();
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                history.add(new RouteHistoryData(
                    rs.getInt("id"),
                    rs.getString("route_name"),
                    rs.getString("origin"),
                    rs.getString("destination"),
                    rs.getString("transport_type"),
                    rs.getInt("duration_minutes"),
                    rs.getDouble("price"),
                    rs.getString("schedule_time"),
                    rs.getString("metadata"),
                    rs.getString("change_type"),
                    rs.getObject("original_route_id") != null ? rs.getInt("original_route_id") : null,
                    rs.getString("submitted_by"),
                    rs.getString("submitted_date"),
                    rs.getString("reviewed_by"),
                    rs.getString("reviewed_date"),
                    rs.getString("status"),
                    rs.getString("notes"),
                    rs.getString("feedback")
                ));
            }
        }
        return history;
    }
    
    // Get count of unread notifications (rejected requests with feedback)
    public int getUnreadNotificationsCount(String username) throws SQLException {
        String query = "SELECT COUNT(*) FROM route_history WHERE submitted_by = ? AND status = 'REJECTED' " +
                      "AND feedback IS NOT NULL AND reviewed_date > datetime('now', '-7 days')";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }
    
    // Get a specific pending route by ID (for editing)
    public PendingRouteData getPendingRouteById(int id, String username) throws SQLException {
        String query = "SELECT * FROM pending_routes WHERE id = ? AND submitted_by = ? AND status = 'PENDING'";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, id);
            pstmt.setString(2, username);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return new PendingRouteData(
                    rs.getInt("id"),
                    rs.getString("route_name"),
                    rs.getString("origin"),
                    rs.getString("destination"),
                    rs.getString("transport_type"),
                    rs.getInt("duration_minutes"),
                    rs.getDouble("price"),
                    rs.getString("schedule_time"),
                    rs.getString("metadata"),
                    rs.getString("status"),
                    rs.getString("change_type"),
                    rs.getObject("original_route_id") != null ? rs.getInt("original_route_id") : null,
                    rs.getString("submitted_by"),
                    rs.getString("submitted_date"),
                    rs.getString("notes")
                );
            }
        }
        return null;
    }
    
    // Helper class for pending route data
    public static class PendingRouteData {
        private int id;
        private String routeName;
        private String origin;
        private String destination;
        private String transportType;
        private int durationMinutes;
        private double price;
        private String scheduleTime;
        private String metadata;
        private String status;
        private String changeType;
        private Integer originalRouteId;
        private String submittedBy;
        private String submittedDate;
        private String notes;
        
        public PendingRouteData(int id, String routeName, String origin, String destination,
                               String transportType, int durationMinutes, double price,
                               String scheduleTime, String metadata, String status, String changeType,
                               Integer originalRouteId, String submittedBy, String submittedDate, String notes) {
            this.id = id;
            this.routeName = routeName;
            this.origin = origin;
            this.destination = destination;
            this.transportType = transportType;
            this.durationMinutes = durationMinutes;
            this.price = price;
            this.scheduleTime = scheduleTime;
            this.metadata = metadata;
            this.status = status;
            this.changeType = changeType;
            this.originalRouteId = originalRouteId;
            this.submittedBy = submittedBy;
            this.submittedDate = submittedDate;
            this.notes = notes;
        }
        
        public int getId() { return id; }
        public String getRouteName() { return routeName; }
        public String getOrigin() { return origin; }
        public String getDestination() { return destination; }
        public String getTransportType() { return transportType; }
        public int getDurationMinutes() { return durationMinutes; }
        public double getPrice() { return price; }
        public String getScheduleTime() { return scheduleTime; }
        public String getMetadata() { return metadata; }
        public String getStatus() { return status; }
        public String getChangeType() { return changeType; }
        public Integer getOriginalRouteId() { return originalRouteId; }
        public String getSubmittedBy() { return submittedBy; }
        public String getSubmittedDate() { return submittedDate; }
        public String getNotes() { return notes; }
    }
    
    // Helper class for route history data
    public static class RouteHistoryData {
        private int id;
        private String routeName;
        private String origin;
        private String destination;
        private String transportType;
        private int durationMinutes;
        private double price;
        private String scheduleTime;
        private String metadata;
        private String changeType;
        private Integer originalRouteId;
        private String submittedBy;
        private String submittedDate;
        private String reviewedBy;
        private String reviewedDate;
        private String status;
        private String notes;
        private String feedback;
        
        public RouteHistoryData(int id, String routeName, String origin, String destination,
                               String transportType, int durationMinutes, double price,
                               String scheduleTime, String metadata, String changeType,
                               Integer originalRouteId, String submittedBy, String submittedDate,
                               String reviewedBy, String reviewedDate, String status, 
                               String notes, String feedback) {
            this.id = id;
            this.routeName = routeName;
            this.origin = origin;
            this.destination = destination;
            this.transportType = transportType;
            this.durationMinutes = durationMinutes;
            this.price = price;
            this.scheduleTime = scheduleTime;
            this.metadata = metadata;
            this.changeType = changeType;
            this.originalRouteId = originalRouteId;
            this.submittedBy = submittedBy;
            this.submittedDate = submittedDate;
            this.reviewedBy = reviewedBy;
            this.reviewedDate = reviewedDate;
            this.status = status;
            this.notes = notes;
            this.feedback = feedback;
        }
        
        public int getId() { return id; }
        public String getRouteName() { return routeName; }
        public String getOrigin() { return origin; }
        public String getDestination() { return destination; }
        public String getTransportType() { return transportType; }
        public int getDurationMinutes() { return durationMinutes; }
        public double getPrice() { return price; }
        public String getScheduleTime() { return scheduleTime; }
        public String getMetadata() { return metadata; }
        public String getChangeType() { return changeType; }
        public Integer getOriginalRouteId() { return originalRouteId; }
        public String getSubmittedBy() { return submittedBy; }
        public String getSubmittedDate() { return submittedDate; }
        public String getReviewedBy() { return reviewedBy; }
        public String getReviewedDate() { return reviewedDate; }
        public String getStatus() { return status; }
        public String getNotes() { return notes; }
        public String getFeedback() { return feedback; }
    }
    
    // Helper class for route data
    public static class RouteData {
        private int id;
        private String routeName;
        private String origin;
        private String destination;
        private String transportType;
        private String status;
        private int durationMinutes;
        private double price;
        private String scheduleTime;
        private String metadata;
        private String createdDate;
        private String modifiedDate;
        
        public RouteData(int id, String routeName, String origin, String destination,
                        String transportType, String status, int durationMinutes, double price,
                        String scheduleTime, String metadata, String createdDate, String modifiedDate) {
            this.id = id;
            this.routeName = routeName;
            this.origin = origin;
            this.destination = destination;
            this.transportType = transportType;
            this.status = status;
            this.durationMinutes = durationMinutes;
            this.price = price;
            this.scheduleTime = scheduleTime;
            this.metadata = metadata;
            this.createdDate = createdDate;
            this.modifiedDate = modifiedDate;
        }
        
        public int getId() { return id; }
        public String getRouteName() { return routeName; }
        public String getOrigin() { return origin; }
        public String getDestination() { return destination; }
        public String getTransportType() { return transportType; }
        public String getStatus() { return status; }
        public int getDurationMinutes() { return durationMinutes; }
        public double getPrice() { return price; }
        public String getScheduleTime() { return scheduleTime; }
        public String getMetadata() { return metadata; }
        public String getCreatedDate() { return createdDate; }
        public String getModifiedDate() { return modifiedDate; }
    }
    
    // Helper class for pending users
    public static class PendingUser {
        private int id;
        private String username;
        private String email;
        private String passwordHash;
        private String passwordSalt;
        private User.Role role;
        private String fullName;
        private String createdDate;
        
        public PendingUser(int id, String username, String email, String passwordHash, 
                          String passwordSalt, User.Role role, String fullName, String createdDate) {
            this.id = id;
            this.username = username;
            this.email = email;
            this.passwordHash = passwordHash;
            this.passwordSalt = passwordSalt;
            this.role = role;
            this.fullName = fullName;
            this.createdDate = createdDate;
        }
        
        public int getId() { return id; }
        public String getUsername() { return username; }
        public String getEmail() { return email; }
        public String getPasswordHash() { return passwordHash; }
        public String getPasswordSalt() { return passwordSalt; }
        public User.Role getRole() { return role; }
        public String getFullName() { return fullName; }
        public String getCreatedDate() { return createdDate; }
    }

    public void savePlan(String planName, Route route, String notes) throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            conn.setAutoCommit(false);
            
            try {
                // Insert plan
                String insertPlan = "INSERT INTO plans (name, total_fare, created_date, notes) VALUES (?, ?, ?, ?)";
                PreparedStatement pstmt = conn.prepareStatement(insertPlan);
                pstmt.setString(1, planName);
                pstmt.setDouble(2, route.getTotalFare());
                pstmt.setString(3, LocalDateTime.now().toString());
                pstmt.setString(4, notes);
                pstmt.executeUpdate();

                // Get generated plan ID using SQLite's last_insert_rowid()
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT last_insert_rowid()");
                rs.next();
                int planId = rs.getInt(1);

                // Insert schedules
                String insertSchedule = """
                    INSERT INTO schedules (plan_id, schedule_id, type, origin, destination, 
                                         departure_time, arrival_time, fare, available_seats, 
                                         company_name, bus_type, train_name, train_number, 
                                         seat_class, leg_order) 
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """;
                
                PreparedStatement schedStmt = conn.prepareStatement(insertSchedule);
                List<Schedule> schedules = route.getSchedules();
                
                for (int i = 0; i < schedules.size(); i++) {
                    Schedule schedule = schedules.get(i);
                    schedStmt.setInt(1, planId);
                    schedStmt.setString(2, schedule.getId());
                    schedStmt.setString(3, schedule.getType());
                    schedStmt.setString(4, schedule.getOrigin());
                    schedStmt.setString(5, schedule.getDestination());
                    schedStmt.setString(6, schedule.getDepartureTime().toString());
                    schedStmt.setString(7, schedule.getArrivalTime().toString());
                    schedStmt.setDouble(8, schedule.getFare());
                    schedStmt.setInt(9, schedule.getAvailableSeats());
                    
                    if (schedule instanceof BusSchedule) {
                        BusSchedule bus = (BusSchedule) schedule;
                        schedStmt.setString(10, bus.getBusCompany());
                        schedStmt.setString(11, bus.getBusType());
                        schedStmt.setNull(12, Types.VARCHAR);
                        schedStmt.setNull(13, Types.VARCHAR);
                        schedStmt.setNull(14, Types.VARCHAR);
                    } else if (schedule instanceof TrainSchedule) {
                        TrainSchedule train = (TrainSchedule) schedule;
                        schedStmt.setNull(10, Types.VARCHAR);
                        schedStmt.setNull(11, Types.VARCHAR);
                        schedStmt.setString(12, train.getTrainName());
                        schedStmt.setString(13, train.getTrainNumber());
                        schedStmt.setString(14, train.getSeatClass());
                    }
                    
                    schedStmt.setInt(15, i);
                    schedStmt.executeUpdate();
                }
                
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public List<String> getAllPlanNames() {
        List<String> planNames = new ArrayList<>();
        String query = "SELECT name, created_date FROM plans ORDER BY created_date DESC";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                planNames.add(rs.getString("name"));
            }
        } catch (SQLException e) {
            System.err.println("Error getting plan names: " + e.getMessage());
        }
        
        return planNames;
    }

    public Route loadPlan(String planName) throws SQLException {
        String query = """
            SELECT s.* FROM schedules s
            JOIN plans p ON s.plan_id = p.id
            WHERE p.name = ?
            ORDER BY s.leg_order
            """;
        
        Route route = new Route();
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, planName);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                String type = rs.getString("type");
                Schedule schedule;
                
                LocalDateTime departure = LocalDateTime.parse(rs.getString("departure_time"));
                LocalDateTime arrival = LocalDateTime.parse(rs.getString("arrival_time"));
                
                if ("Bus".equals(type)) {
                    schedule = new BusSchedule(
                        rs.getString("schedule_id"),
                        rs.getString("origin"),
                        rs.getString("destination"),
                        departure,
                        arrival,
                        rs.getDouble("fare"),
                        rs.getInt("available_seats"),
                        rs.getString("company_name"),
                        rs.getString("bus_type")
                    );
                } else {
                    schedule = new TrainSchedule(
                        rs.getString("schedule_id"),
                        rs.getString("origin"),
                        rs.getString("destination"),
                        departure,
                        arrival,
                        rs.getDouble("fare"),
                        rs.getInt("available_seats"),
                        rs.getString("train_name"),
                        rs.getString("train_number"),
                        rs.getString("seat_class")
                    );
                }
                
                route.addSchedule(schedule);
            }
        }
        
        return route;
    }

    public void deletePlan(String planName) throws SQLException {
        String query = "DELETE FROM plans WHERE name = ?";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, planName);
            pstmt.executeUpdate();
        }
    }

    public PlanSummary getPlanSummary(String planName) throws SQLException {
        String query = """
            SELECT p.name, p.total_fare, p.created_date, p.notes, COUNT(s.id) as leg_count,
                   MIN(s.departure_time) as first_departure,
                   MAX(s.arrival_time) as last_arrival
            FROM plans p
            JOIN schedules s ON p.id = s.plan_id
            WHERE p.name = ?
            GROUP BY p.id
            """;
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, planName);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return new PlanSummary(
                    rs.getString("name"),
                    rs.getDouble("total_fare"),
                    rs.getString("created_date"),
                    rs.getString("notes"),
                    rs.getInt("leg_count"),
                    rs.getString("first_departure"),
                    rs.getString("last_arrival")
                );
            }
        }
        
        return null;
    }

    // Activity Logging Methods
    public void logActivity(String username, String actionType, String actionDetails, boolean success) {
        String query = "INSERT INTO activity_log (username, action_type, action_details, timestamp, success) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, username);
            pstmt.setString(2, actionType);
            pstmt.setString(3, actionDetails);
            pstmt.setString(4, LocalDateTime.now().toString());
            pstmt.setInt(5, success ? 1 : 0);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error logging activity: " + e.getMessage());
        }
    }
    
    public List<ActivityLog> getRecentActivity(int limit) throws SQLException {
        String query = "SELECT * FROM activity_log ORDER BY timestamp DESC LIMIT ?";
        List<ActivityLog> activities = new ArrayList<>();
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, limit);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                activities.add(new ActivityLog(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("action_type"),
                    rs.getString("action_details"),
                    rs.getString("timestamp"),
                    rs.getInt("success") == 1
                ));
            }
        }
        
        return activities;
    }
    
    public int getFailedLoginAttempts(String username, int minutes) throws SQLException {
        String query = """
            SELECT COUNT(*) FROM activity_log 
            WHERE username = ? AND action_type = 'LOGIN_FAILED' 
            AND datetime(timestamp) > datetime('now', '-' || ? || ' minutes')
            """;
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, username);
            pstmt.setInt(2, minutes);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        
        return 0;
    }
    
    // Statistics Methods
    public SystemStatistics getSystemStatistics() throws SQLException {
        SystemStatistics stats = new SystemStatistics();
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            
            // Total users
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users");
            if (rs.next()) stats.totalUsers = rs.getInt(1);
            
            // Pending accounts
            rs = stmt.executeQuery("SELECT COUNT(*) FROM pending_users WHERE status = 'PENDING'");
            if (rs.next()) stats.pendingAccounts = rs.getInt(1);
            
            // Recent approvals (last 7 days)
            rs = stmt.executeQuery("""
                SELECT COUNT(*) FROM activity_log 
                WHERE action_type = 'ACCOUNT_APPROVED' 
                AND datetime(timestamp) > datetime('now', '-7 days')
                """);
            if (rs.next()) stats.recentApprovals = rs.getInt(1);
            
            // Failed login attempts (last 24 hours)
            rs = stmt.executeQuery("""
                SELECT COUNT(*) FROM activity_log 
                WHERE action_type = 'LOGIN_FAILED' 
                AND datetime(timestamp) > datetime('now', '-1 day')
                """);
            if (rs.next()) stats.failedLogins = rs.getInt(1);
            
            // Total logins today
            rs = stmt.executeQuery("""
                SELECT COUNT(*) FROM activity_log 
                WHERE action_type = 'LOGIN_SUCCESS' 
                AND date(timestamp) = date('now')
                """);
            if (rs.next()) stats.todayLogins = rs.getInt(1);
        }
        
        return stats;
    }

    public static class ActivityLog {
        private final int id;
        private final String username;
        private final String actionType;
        private final String actionDetails;
        private final String timestamp;
        private final boolean success;
        
        public ActivityLog(int id, String username, String actionType, String actionDetails, 
                          String timestamp, boolean success) {
            this.id = id;
            this.username = username;
            this.actionType = actionType;
            this.actionDetails = actionDetails;
            this.timestamp = timestamp;
            this.success = success;
        }
        
        public int getId() { return id; }
        public String getUsername() { return username; }
        public String getActionType() { return actionType; }
        public String getActionDetails() { return actionDetails; }
        public String getTimestamp() { return timestamp; }
        public boolean isSuccess() { return success; }
    }
    
    public static class SystemStatistics {
        public int totalUsers;
        public int pendingAccounts;
        public int recentApprovals;
        public int failedLogins;
        public int todayLogins;
    }

    public static class PlanSummary {
        private final String name;
        private final double totalFare;
        private final String createdDate;
        private final String notes;
        private final int legCount;
        private final String firstDeparture;
        private final String lastArrival;

        public PlanSummary(String name, double totalFare, String createdDate, String notes,
                          int legCount, String firstDeparture, String lastArrival) {
            this.name = name;
            this.totalFare = totalFare;
            this.createdDate = createdDate;
            this.notes = notes;
            this.legCount = legCount;
            this.firstDeparture = firstDeparture;
            this.lastArrival = lastArrival;
        }

        public String getName() { return name; }
        public double getTotalFare() { return totalFare; }
        public String getCreatedDate() { return createdDate; }
        public String getNotes() { return notes; }
        public int getLegCount() { return legCount; }
        public String getFirstDeparture() { return firstDeparture; }
        public String getLastArrival() { return lastArrival; }
    }
}
