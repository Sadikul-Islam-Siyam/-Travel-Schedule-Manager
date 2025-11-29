package com.travelmanager.database;

import com.travelmanager.model.BusSchedule;
import com.travelmanager.model.Route;
import com.travelmanager.model.Schedule;
import com.travelmanager.model.TrainSchedule;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
            e.printStackTrace();
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
