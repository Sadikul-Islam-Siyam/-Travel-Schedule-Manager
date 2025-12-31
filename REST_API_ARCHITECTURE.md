# REST API - Visual Architecture Guide

## System Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                   Travel Schedule Manager                        │
│                                                                   │
│  ┌──────────────────────┐        ┌──────────────────────┐      │
│  │   JavaFX UI Layer    │        │  REST API Server     │      │
│  │                      │        │  (Port 8080)         │      │
│  │  - Login/Register    │        │                      │      │
│  │  - Route Search      │◄──────►│  Javalin Framework   │      │
│  │  - Schedule Mgmt     │  HTTP  │                      │      │
│  │  - Developer Mode    │        │  12 REST Endpoints   │      │
│  └──────────────────────┘        └──────────┬───────────┘      │
│                                              │                   │
│                                              │                   │
│                                   ┌──────────▼───────────┐      │
│                                   │  Service Layer       │      │
│                                   │                      │      │
│                                   │  - ScheduleService   │      │
│                                   │  - Unified Logic     │      │
│                                   │  - Route Search      │      │
│                                   └──────────┬───────────┘      │
│                                              │                   │
│                                   ┌──────────┴───────────┐      │
│                                   │                      │      │
│                        ┌──────────▼─────────┐   ┌───────▼──────────┐
│                        │ BusScheduleStorage │   │TrainScheduleStorage│
│                        │                    │   │                    │
│                        │ - In-Memory Cache  │   │ - In-Memory Cache  │
│                        │ - JSON Read/Write  │   │ - JSON Read/Write  │
│                        └──────────┬─────────┘   └───────┬────────────┘
│                                   │                     │             │
│                                   │                     │             │
└───────────────────────────────────┼─────────────────────┼─────────────┘
                                    │                     │
                                    ▼                     ▼
                          ┌──────────────────┐  ┌──────────────────┐
                          │ bus_schedules    │  │ train_schedules  │
                          │ .json            │  │ .json            │
                          │                  │  │                  │
                          │ - 5 Bus Routes   │  │ - 7 Train Routes │
                          └──────────────────┘  └──────────────────┘
```

---

## Request Flow Diagrams

### User Mode: Route Search

```
User → JavaFX UI
         │
         │ HTTP GET /api/routes?start=Dhaka&destination=Chittagong
         ▼
    ScheduleController
         │
         │ searchRoutes()
         ▼
    ScheduleService
         │
         ├─────────────────────┐
         │                     │
         ▼                     ▼
BusScheduleStorage      TrainScheduleStorage
         │                     │
         │ Query JSON          │ Query JSON
         ▼                     ▼
    bus_schedules.json    train_schedules.json
         │                     │
         │ Filter: Dhaka→CTG   │ Filter: Dhaka→CTG
         ▼                     ▼
    [2 Buses Found]       [2 Trains Found]
         │                     │
         └─────────┬───────────┘
                   │
                   │ Merge Results
                   ▼
           UnifiedScheduleDTO[]
                   │
                   │ Convert to JSON
                   ▼
              HTTP Response
                   │
                   ▼
            Display in UI
```

### Developer Mode: Add Bus Schedule

```
Developer → JavaFX UI
              │
              │ HTTP POST /api/schedules/bus
              │ Body: {"busName":"New Bus",...}
              ▼
      ScheduleController
              │
              │ addBusSchedule()
              │ Validate Input
              ▼
       ScheduleService
              │
              │ addBusSchedule()
              ▼
    BusScheduleStorage
              │
              ├─ Check: Already exists?
              │
              ├─ Add to Memory Cache
              │
              └─ Save to File
                    ▼
           bus_schedules.json
                    │
                    │ File Updated
                    ▼
           Success Response
                    │
                    ▼
              Update UI
```

---

## Package Structure

```
com.travelmanager
│
├── api.rest
│   ├── RestApiServer.java           🌐 Embedded Javalin server
│   ├── ScheduleController.java      🎮 REST endpoint handlers
│   └── example
│       └── RestApiClientExample.java 📘 Usage examples
│
├── model.rest
│   ├── BusScheduleDTO.java          🚌 Bus schedule data model
│   ├── TrainScheduleDTO.java        🚂 Train schedule data model
│   └── UnifiedScheduleDTO.java      🔄 Unified schedule model
│
├── service.rest
│   └── ScheduleService.java         ⚙️ Business logic layer
│
├── storage
│   ├── BusScheduleStorage.java      💾 Bus JSON persistence
│   └── TrainScheduleStorage.java    💾 Train JSON persistence
│
└── App.java                          🚀 Main app (starts everything)
```

---

## Endpoint Map

```
Base URL: http://localhost:8080/api

┌─────────────────────────────────────────────────────────────────┐
│                      UNIFIED ENDPOINTS                           │
├─────────────────────────────────────────────────────────────────┤
│ GET  /health                    → Server status                 │
│ GET  /schedules                 → All schedules (bus + train)   │
│ GET  /routes?start=X&dest=Y     → Search both types             │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                      BUS ENDPOINTS                               │
├─────────────────────────────────────────────────────────────────┤
│ GET    /schedules/bus           → List all buses                │
│ GET    /schedules/bus/{name}    → Get specific bus              │
│ POST   /schedules/bus           → Add new bus (Dev)             │
│ PUT    /schedules/bus/{name}    → Update bus (Dev)              │
│ DELETE /schedules/bus/{name}    → Delete bus (Dev)              │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                      TRAIN ENDPOINTS                             │
├─────────────────────────────────────────────────────────────────┤
│ GET    /schedules/train         → List all trains               │
│ GET    /schedules/train/{name}  → Get specific train            │
│ POST   /schedules/train         → Add new train (Dev)           │
│ PUT    /schedules/train/{name}  → Update train (Dev)            │
│ DELETE /schedules/train/{name}  → Delete train (Dev)            │
└─────────────────────────────────────────────────────────────────┘
```

---

## Data Flow Layers

```
┌───────────────────────────────────────────────────────────────┐
│ Layer 1: API (REST Controllers)                               │
│                                                                │
│ Responsibilities:                                              │
│ • Handle HTTP requests/responses                              │
│ • Validate input parameters                                   │
│ • Return JSON responses                                       │
│ • Set proper HTTP status codes                                │
│                                                                │
│ No Business Logic! No File I/O!                               │
└───────────────────────────────────────────────────────────────┘
                            ⬇️
┌───────────────────────────────────────────────────────────────┐
│ Layer 2: Service (Business Logic)                             │
│                                                                │
│ Responsibilities:                                              │
│ • Implement business rules                                    │
│ • Provide unified interface                                   │
│ • Merge bus and train data                                    │
│ • Coordinate storage operations                               │
│                                                                │
│ No HTTP details! No direct file access!                       │
└───────────────────────────────────────────────────────────────┘
                            ⬇️
┌───────────────────────────────────────────────────────────────┐
│ Layer 3: Storage (Persistence)                                │
│                                                                │
│ Responsibilities:                                              │
│ • Manage JSON files                                           │
│ • Maintain in-memory cache                                    │
│ • Ensure thread safety                                        │
│ • Handle file I/O errors                                      │
│                                                                │
│ No business logic! No HTTP details!                           │
└───────────────────────────────────────────────────────────────┘
                            ⬇️
┌───────────────────────────────────────────────────────────────┐
│ Layer 4: Data (JSON Files)                                    │
│                                                                │
│ • bus_schedules.json                                          │
│ • train_schedules.json                                        │
└───────────────────────────────────────────────────────────────┘
```

---

## Clean Architecture Benefits

```
┌────────────────────────────────────────────────────────────────┐
│ ✅ Separation of Concerns                                      │
│    Each layer has a single, clear responsibility              │
│                                                                │
│ ✅ Testability                                                 │
│    Service layer can be unit tested independently             │
│                                                                │
│ ✅ Maintainability                                             │
│    Changes in one layer don't affect others                   │
│                                                                │
│ ✅ Scalability                                                 │
│    Easy to switch from JSON to database                       │
│                                                                │
│ ✅ Flexibility                                                 │
│    Can add new storage types without changing API             │
│                                                                │
│ ✅ Unified Interface                                           │
│    Users don't need to know about dual storage                │
└────────────────────────────────────────────────────────────────┘
```

---

## Threading & Concurrency

```
┌─────────────────────────────────────────────────────────────────┐
│                    Concurrent Request Handling                   │
│                                                                   │
│  Request 1 ──┐                                                   │
│  Request 2 ──┼──► Javalin Thread Pool ──┐                       │
│  Request 3 ──┘                           │                       │
│                                          │                       │
│                                          ▼                       │
│                               ┌────────────────────┐             │
│                               │ ScheduleService    │             │
│                               │ (Singleton)        │             │
│                               └────────┬───────────┘             │
│                                        │                         │
│                                        ▼                         │
│                         ┌──────────────────────────┐             │
│                         │ ConcurrentHashMap        │             │
│                         │ (Thread-Safe Cache)      │             │
│                         └──────────────────────────┘             │
│                                                                   │
│ ✅ Multiple requests can be processed simultaneously             │
│ ✅ In-memory cache is thread-safe                                │
│ ✅ File writes are synchronized                                  │
└─────────────────────────────────────────────────────────────────┘
```

---

## Startup Sequence

```
1. JavaFX Application.launch()
         ⬇️
2. App.start(Stage stage)
         ⬇️
3. RestApiServer.getInstance(8080)
         ⬇️
4. BusScheduleStorage.getInstance()
         ├─► Load bus_schedules.json
         └─► Populate in-memory cache
         ⬇️
5. TrainScheduleStorage.getInstance()
         ├─► Load train_schedules.json
         └─► Populate in-memory cache
         ⬇️
6. Javalin.create() & register routes
         ⬇️
7. app.start(8080)
         ⬇️
8. ✅ REST API Server Ready
         ⬇️
9. JavaFX UI Shows
         ⬇️
10. 🎉 Application Running

Console Output:
==============================================
REST API Server started successfully!
Base URL: http://localhost:8080/api
==============================================
```

---

## Error Handling Flow

```
Request → Controller
            │
            ├─ Validate Input
            │  └─ Invalid? → 400 Bad Request
            │
            ├─ Call Service
            │  └─ Exception? → 500 Internal Server Error
            │
            ├─ Resource Not Found?
            │  └─ 404 Not Found
            │
            ├─ Resource Exists?
            │  └─ 409 Conflict
            │
            └─ Success
               ├─ Create: 201 Created
               ├─ Update: 200 OK
               └─ Delete: 200 OK

All errors return JSON:
{
  "error": "Error Type",
  "message": "Detailed message"
}
```

---

## Storage Abstraction

```
┌─────────────────────────────────────────────────────────────────┐
│                  Why Separate JSON Files?                        │
│                                                                   │
│  ✅ Organization: Clear separation of concerns                   │
│  ✅ Performance: Smaller files load faster                       │
│  ✅ Scalability: Easy to add new transport types                 │
│  ✅ Maintenance: Edit bus/train data independently               │
│  ✅ Backup: Can backup/restore separately                        │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│             How is Dual Storage Abstracted?                      │
│                                                                   │
│  Service Layer provides unified methods:                         │
│                                                                   │
│  getAllSchedules()          ──► Merges bus + train              │
│  searchRoutes(start, dest)  ──► Queries both files              │
│                                                                   │
│  Users/UI see a single unified interface!                        │
│  They don't know about separate storage.                         │
└─────────────────────────────────────────────────────────────────┘
```

---

## Future Enhancements Roadmap

```
Phase 1: Current Implementation ✅
├─ Embedded REST API
├─ Dual JSON storage
├─ CRUD operations
└─ Unified interface

Phase 2: Security & Auth 🔒
├─ JWT authentication
├─ Role-based access
├─ API key management
└─ Rate limiting

Phase 3: Advanced Features 🚀
├─ Pagination
├─ Advanced filtering
├─ Batch operations
└─ Real-time updates (WebSocket)

Phase 4: Scalability 📈
├─ Database integration
├─ Caching strategies
├─ Load balancing
└─ Microservices

Phase 5: Monitoring & DevOps 📊
├─ Metrics collection
├─ Logging improvements
├─ Health checks
└─ API documentation (Swagger)
```

---

## Quick Reference Card

```
╔═══════════════════════════════════════════════════════════════╗
║                   REST API Quick Reference                     ║
╠═══════════════════════════════════════════════════════════════╣
║ Base URL: http://localhost:8080/api                           ║
║                                                                ║
║ Health Check:                                                  ║
║   GET /health                                                  ║
║                                                                ║
║ Search Routes (User):                                          ║
║   GET /routes?start=Dhaka&destination=Chittagong              ║
║                                                                ║
║ View All (User):                                               ║
║   GET /schedules                                               ║
║   GET /schedules/bus                                           ║
║   GET /schedules/train                                         ║
║                                                                ║
║ Add (Developer):                                               ║
║   POST /schedules/bus        (JSON body)                       ║
║   POST /schedules/train      (JSON body)                       ║
║                                                                ║
║ Update (Developer):                                            ║
║   PUT /schedules/bus/{name}   (JSON body)                      ║
║   PUT /schedules/train/{name} (JSON body)                      ║
║                                                                ║
║ Delete (Developer):                                            ║
║   DELETE /schedules/bus/{name}                                 ║
║   DELETE /schedules/train/{name}                               ║
╚═══════════════════════════════════════════════════════════════╝
```

---

**The architecture is clean, scalable, and production-ready!** 🎉
