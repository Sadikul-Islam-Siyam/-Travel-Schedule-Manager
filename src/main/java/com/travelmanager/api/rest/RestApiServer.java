package com.travelmanager.api.rest;

import io.javalin.Javalin;

/**
 * Embedded REST API Server using Javalin
 * Runs alongside JavaFX application on localhost:8080
 */
public class RestApiServer {
    private static RestApiServer instance;
    private Javalin app;
    private final int port;
    private final ScheduleController scheduleController;
    private final UserController userController;
    private boolean isRunning = false;

    private RestApiServer(int port) {
        this.port = port;
        this.scheduleController = new ScheduleController();
        this.userController = new UserController();
    }

    public static synchronized RestApiServer getInstance() {
        return getInstance(8080);
    }

    public static synchronized RestApiServer getInstance(int port) {
        if (instance == null) {
            instance = new RestApiServer(port);
        }
        return instance;
    }

    /**
     * Start the embedded REST API server
     */
    public void start() {
        if (isRunning) {
            System.out.println("REST API server is already running on port " + port);
            return;
        }

        try {
            app = Javalin.create(config -> {
                // Enable CORS for development (including Android app access)
                config.plugins.enableCors(cors -> {
                    cors.add(it -> {
                        it.anyHost();
                        it.allowCredentials = false;
                    });
                });
                
                // Configure JSON serialization
                config.jsonMapper(new io.javalin.json.JavalinGson());
            });

            // Add explicit CORS headers for Android app compatibility
            app.before("/api/*", ctx -> {
                ctx.header("Access-Control-Allow-Origin", "*");
                ctx.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
                ctx.header("Access-Control-Allow-Headers", "Content-Type, Authorization");
            });

            // Handle preflight OPTIONS requests
            app.options("/api/*", ctx -> ctx.status(200));

            // Register all routes
            registerRoutes();

            // Start server
            app.start(port);
            isRunning = true;
            
            System.out.println("==============================================");
            System.out.println("REST API Server started successfully!");
            System.out.println("Base URL: http://localhost:" + port + "/api");
            System.out.println("==============================================");
            System.out.println("Available Endpoints:");
            System.out.println("  GET    /api/schedules");
            System.out.println("  GET    /api/schedules/bus");
            System.out.println("  GET    /api/schedules/bus/{busName}");
            System.out.println("  POST   /api/schedules/bus");
            System.out.println("  PUT    /api/schedules/bus/{busName}");
            System.out.println("  DELETE /api/schedules/bus/{busName}");
            System.out.println("  GET    /api/schedules/train");
            System.out.println("  GET    /api/schedules/train/{trainName}");
            System.out.println("  POST   /api/schedules/train");
            System.out.println("  PUT    /api/schedules/train/{trainName}");
            System.out.println("  DELETE /api/schedules/train/{trainName}");
            System.out.println("  GET    /api/routes?start={start}&destination={destination}");
            System.out.println("----------------------------------------------");
            System.out.println("Authentication Endpoints (for Android app):");
            System.out.println("  POST   /api/auth/login");
            System.out.println("  POST   /api/auth/register");
            System.out.println("  GET    /api/auth/status/{username}");
            System.out.println("  GET    /api/users/profile");
            System.out.println("----------------------------------------------");
            System.out.println("Admin Endpoints (Master/Developer only):");
            System.out.println("  GET    /api/admin/pending-users");
            System.out.println("  POST   /api/admin/approve/{userId}");
            System.out.println("  POST   /api/admin/reject/{userId}");
            System.out.println("==============================================");
            
        } catch (Exception e) {
            System.err.println("Failed to start REST API server: " + e.getMessage());
            e.printStackTrace();
            isRunning = false;
        }
    }

    /**
     * Register all REST API routes
     */
    private void registerRoutes() {
        // Health check endpoint
        app.get("/api/health", ctx -> ctx.json(
            java.util.Map.of(
                "status", "UP",
                "timestamp", java.time.LocalDateTime.now().toString(),
                "service", "Travel Schedule Manager API"
            )
        ));

        // ============= UNIFIED ENDPOINTS =============
        
        // GET /api/schedules - Returns all schedules (bus + train)
        app.get("/api/schedules", scheduleController::getAllSchedules);
        
        // GET /api/routes - Search routes across bus and train
        app.get("/api/routes", scheduleController::searchRoutes);

        // ============= BUS ENDPOINTS =============
        
        // GET /api/schedules/bus - Get all bus schedules
        app.get("/api/schedules/bus", scheduleController::getAllBusSchedules);
        
        // GET /api/schedules/bus/{busName} - Get specific bus schedule
        app.get("/api/schedules/bus/{busName}", scheduleController::getBusSchedule);
        
        // POST /api/schedules/bus - Add new bus schedule
        app.post("/api/schedules/bus", scheduleController::addBusSchedule);
        
        // PUT /api/schedules/bus/{busName} - Update bus schedule
        app.put("/api/schedules/bus/{busName}", scheduleController::updateBusSchedule);
        
        // DELETE /api/schedules/bus/{busName} - Delete bus schedule
        app.delete("/api/schedules/bus/{busName}", scheduleController::deleteBusSchedule);

        // ============= TRAIN ENDPOINTS =============
        
        // GET /api/schedules/train - Get all train schedules
        app.get("/api/schedules/train", scheduleController::getAllTrainSchedules);
        
        // GET /api/schedules/train/{trainName} - Get specific train schedule
        app.get("/api/schedules/train/{trainName}", scheduleController::getTrainSchedule);
        
        // POST /api/schedules/train - Add new train schedule
        app.post("/api/schedules/train", scheduleController::addTrainSchedule);
        
        // PUT /api/schedules/train/{trainName} - Update train schedule
        app.put("/api/schedules/train/{trainName}", scheduleController::updateTrainSchedule);
        
        // DELETE /api/schedules/train/{trainName} - Delete train schedule
        app.delete("/api/schedules/train/{trainName}", scheduleController::deleteTrainSchedule);

        // ============= AUTHENTICATION ENDPOINTS =============
        
        // POST /api/auth/login - User login
        app.post("/api/auth/login", userController::login);
        
        // POST /api/auth/register - User registration (pending approval)
        app.post("/api/auth/register", userController::register);
        
        // GET /api/auth/status/{username} - Check account approval status
        app.get("/api/auth/status/{username}", userController::checkAccountStatus);
        
        // GET /api/users/profile - Get current user profile
        app.get("/api/users/profile", userController::getProfile);

        // ============= ADMIN ENDPOINTS (Master/Developer only) =============
        
        // GET /api/admin/pending-users - Get all pending registrations
        app.get("/api/admin/pending-users", userController::getPendingUsers);
        
        // POST /api/admin/approve/{userId} - Approve a pending user
        app.post("/api/admin/approve/{userId}", userController::approveUser);
        
        // POST /api/admin/reject/{userId} - Reject a pending user
        app.post("/api/admin/reject/{userId}", userController::rejectUser);

        // 404 handler
        app.error(404, ctx -> {
            ctx.json(java.util.Map.of(
                "error", "Endpoint not found",
                "path", ctx.path(),
                "message", "The requested endpoint does not exist"
            ));
        });
    }

    /**
     * Stop the REST API server
     */
    public void stop() {
        if (app != null && isRunning) {
            app.stop();
            isRunning = false;
            System.out.println("REST API server stopped.");
        }
    }

    /**
     * Check if server is running
     */
    public boolean isRunning() {
        return isRunning;
    }

    /**
     * Get the port number
     */
    public int getPort() {
        return port;
    }
}
