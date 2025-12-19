package com.travelmanager.database;

import com.travelmanager.model.BusSchedule;
import com.travelmanager.model.Route;
import com.travelmanager.model.Schedule;
import com.travelmanager.model.TrainSchedule;
import com.travelmanager.model.User;

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
            // Create default developer account
            String insertUser = "INSERT INTO users (username, email, password_hash, password_salt, role, full_name, created_date, failed_login_attempts) VALUES (?, ?, ?, ?, ?, ?, ?, 0)";
            
            // Default Developer: username=developer, password=dev123
            String devSalt = generateSalt();
            PreparedStatement pstmt = conn.prepareStatement(insertUser);
            pstmt.setString(1, "developer");
            pstmt.setString(2, "developer@travelmanager.com");
            pstmt.setString(3, hashPasswordWithSalt("dev123", devSalt));
            pstmt.setString(4, devSalt);
            pstmt.setString(5, "DEVELOPER");
            pstmt.setString(6, "System Developer");
            pstmt.setString(7, LocalDateTime.now().toString());
            pstmt.executeUpdate();
            
            // Default User: username=user, password=user123
            String userSalt = generateSalt();
            pstmt.setString(1, "user");
            pstmt.setString(2, "user@travelmanager.com");
            pstmt.setString(3, hashPasswordWithSalt("user123", userSalt));
            pstmt.setString(4, userSalt);
            pstmt.setString(5, "USER");
            pstmt.setString(6, "Demo User");
            pstmt.setString(7, LocalDateTime.now().toString());
            pstmt.executeUpdate();
            
            System.out.println("Default users created successfully");
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
