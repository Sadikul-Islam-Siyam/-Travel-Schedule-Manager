# REST API Documentation - Travel Schedule Manager

## Overview

The Travel Schedule Manager includes an embedded REST API server that runs on `http://localhost:8080`. This API provides a unified interface for managing bus and train schedules while maintaining separate JSON storage for each type.

## Architecture

### Data Storage
- **Bus Schedules**: `data/bus_schedules.json`
- **Train Schedules**: `data/train_schedules.json`

### Key Components
- **Models** (`model.rest`): DTOs for REST operations
- **Storage** (`storage`): JSON persistence with in-memory caching
- **Service** (`service.rest`): Business logic layer
- **Controllers** (`api.rest`): REST endpoint handlers
- **Server** (`api.rest.RestApiServer`): Embedded Javalin server

### Unified Interface
The API abstracts the dual storage system:
- Users don't need to know if data comes from bus or train files
- Route search queries both JSON files automatically
- Unified endpoints merge results from both sources

---

## API Endpoints

### Base URL
```
http://localhost:8080/api
```

### Health Check
```http
GET /api/health
```
**Response:**
```json
{
  "status": "UP",
  "timestamp": "2026-01-01T10:00:00",
  "service": "Travel Schedule Manager API"
}
```

---

## Unified Endpoints

### 1. Get All Schedules (Bus + Train)
```http
GET /api/schedules
```

**Description**: Returns all schedules from both bus and train storage in a unified format.

**Response Example:**
```json
[
  {
    "type": "bus",
    "name": "Hanif Paribahan",
    "start": "Dhaka",
    "destination": "Chittagong",
    "startTime": "08:00",
    "arrivalTime": "14:00",
    "fare": 850.0,
    "duration": "6:00h",
    "offDay": null
  },
  {
    "type": "train",
    "name": "SUBARNA EXPRESS (702)",
    "start": "Dhaka",
    "destination": "Chittagong",
    "startTime": "14:50",
    "arrivalTime": "20:50",
    "fare": 420.0,
    "duration": "6:00h",
    "offDay": "No off day"
  }
]
```

### 2. Search Routes (Unified)
```http
GET /api/routes?start={start}&destination={destination}
```

**Description**: Searches for routes across both bus and train schedules.

**Query Parameters:**
- `start` (required): Origin location
- `destination` (required): Destination location

**Example:**
```http
GET /api/routes?start=Dhaka&destination=Chittagong
```

**Response Example:**
```json
[
  {
    "type": "bus",
    "name": "Hanif Paribahan",
    "start": "Dhaka",
    "destination": "Chittagong",
    "startTime": "08:00",
    "arrivalTime": "14:00",
    "fare": 850.0,
    "duration": "6:00h",
    "offDay": null
  },
  {
    "type": "train",
    "name": "SUBARNA EXPRESS (702)",
    "start": "Dhaka",
    "destination": "Chittagong",
    "startTime": "14:50",
    "arrivalTime": "20:50",
    "fare": 420.0,
    "duration": "6:00h",
    "offDay": "No off day"
  }
]
```

---

## Bus Schedule Endpoints

### 3. Get All Bus Schedules
```http
GET /api/schedules/bus
```

**Response Example:**
```json
[
  {
    "busName": "Hanif Paribahan",
    "start": "Dhaka",
    "destination": "Chittagong",
    "startTime": "08:00",
    "arrivalTime": "14:00",
    "fare": 850.0,
    "duration": "6:00h"
  }
]
```

### 4. Get Specific Bus Schedule
```http
GET /api/schedules/bus/{busName}
```

**Example:**
```http
GET /api/schedules/bus/Hanif%20Paribahan
```

### 5. Add New Bus Schedule (Developer)
```http
POST /api/schedules/bus
Content-Type: application/json
```

**Request Body:**
```json
{
  "busName": "Shohagh Paribahan",
  "start": "Dhaka",
  "destination": "Khulna",
  "startTime": "10:00",
  "arrivalTime": "18:00",
  "fare": 750.0,
  "duration": "8:00h"
}
```

**Success Response (201 Created):**
```json
{
  "message": "Bus schedule added successfully",
  "busName": "Shohagh Paribahan"
}
```

**Error Response (409 Conflict):**
```json
{
  "error": "Bus schedule already exists",
  "busName": "Shohagh Paribahan"
}
```

### 6. Update Bus Schedule (Developer)
```http
PUT /api/schedules/bus/{busName}
Content-Type: application/json
```

**Request Body:**
```json
{
  "busName": "Hanif Paribahan",
  "start": "Dhaka",
  "destination": "Chittagong",
  "startTime": "08:30",
  "arrivalTime": "14:30",
  "fare": 900.0,
  "duration": "6:00h"
}
```

### 7. Delete Bus Schedule (Developer)
```http
DELETE /api/schedules/bus/{busName}
```

**Example:**
```http
DELETE /api/schedules/bus/Hanif%20Paribahan
```

**Success Response (200 OK):**
```json
{
  "message": "Bus schedule deleted successfully",
  "busName": "Hanif Paribahan"
}
```

---

## Train Schedule Endpoints

### 8. Get All Train Schedules
```http
GET /api/schedules/train
```

**Response Example:**
```json
[
  {
    "trainName": "SUBARNA EXPRESS (702)",
    "start": "Dhaka",
    "destination": "Chittagong",
    "startTime": "14:50",
    "arrivalTime": "20:50",
    "fare": 420.0,
    "duration": "6:00h",
    "offDay": "No off day"
  }
]
```

### 9. Get Specific Train Schedule
```http
GET /api/schedules/train/{trainName}
```

**Example:**
```http
GET /api/schedules/train/SUBARNA%20EXPRESS%20(702)
```

### 10. Add New Train Schedule (Developer)
```http
POST /api/schedules/train
Content-Type: application/json
```

**Request Body:**
```json
{
  "trainName": "MOHANAGAR GODHULI (722)",
  "start": "Dhaka",
  "destination": "Chittagong",
  "startTime": "17:00",
  "arrivalTime": "23:00",
  "fare": 400.0,
  "duration": "6:00h",
  "offDay": "No off day"
}
```

### 11. Update Train Schedule (Developer)
```http
PUT /api/schedules/train/{trainName}
Content-Type: application/json
```

**Request Body:**
```json
{
  "trainName": "SUBARNA EXPRESS (702)",
  "start": "Dhaka",
  "destination": "Chittagong",
  "startTime": "15:00",
  "arrivalTime": "21:00",
  "fare": 450.0,
  "duration": "6:00h",
  "offDay": "No off day"
}
```

### 12. Delete Train Schedule (Developer)
```http
DELETE /api/schedules/train/{trainName}
```

**Example:**
```http
DELETE /api/schedules/train/SUBARNA%20EXPRESS%20(702)
```

---

## Data Format Specifications

### Bus Schedule Format
```json
{
  "busName": "string (unique identifier)",
  "start": "string",
  "destination": "string",
  "startTime": "string (HH:MM format)",
  "arrivalTime": "string (HH:MM format)",
  "fare": "number (double)",
  "duration": "string (HH:MMh format)"
}
```

### Train Schedule Format
```json
{
  "trainName": "string (unique identifier, e.g., 'RANGPUR EXPRESS (772)')",
  "start": "string",
  "destination": "string",
  "startTime": "string (HH:MM format)",
  "arrivalTime": "string (HH:MM format)",
  "fare": "number (double)",
  "duration": "string (HH:MMh format)",
  "offDay": "string (e.g., 'No off day', 'Monday', etc.)"
}
```

---

## Usage Examples

### Using cURL

#### Search for routes:
```bash
curl "http://localhost:8080/api/routes?start=Dhaka&destination=Chittagong"
```

#### Add a bus schedule:
```bash
curl -X POST http://localhost:8080/api/schedules/bus \
  -H "Content-Type: application/json" \
  -d '{
    "busName": "Royal Coach",
    "start": "Dhaka",
    "destination": "Sylhet",
    "startTime": "09:00",
    "arrivalTime": "15:30",
    "fare": 700.0,
    "duration": "6:30h"
  }'
```

#### Update a train schedule:
```bash
curl -X PUT "http://localhost:8080/api/schedules/train/RANGPUR%20EXPRESS%20(772)" \
  -H "Content-Type: application/json" \
  -d '{
    "trainName": "RANGPUR EXPRESS (772)",
    "start": "Dhaka",
    "destination": "Rangpur",
    "startTime": "10:00",
    "arrivalTime": "20:00",
    "fare": 500.0,
    "duration": "10:00h",
    "offDay": "Monday"
  }'
```

### Using JavaScript (Fetch API)

```javascript
// Search routes
async function searchRoutes(start, destination) {
  const response = await fetch(
    `http://localhost:8080/api/routes?start=${start}&destination=${destination}`
  );
  const routes = await response.json();
  console.log(routes);
}

// Add bus schedule
async function addBusSchedule(schedule) {
  const response = await fetch('http://localhost:8080/api/schedules/bus', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(schedule)
  });
  const result = await response.json();
  console.log(result);
}

// Get all schedules
async function getAllSchedules() {
  const response = await fetch('http://localhost:8080/api/schedules');
  const schedules = await response.json();
  console.log(schedules);
}
```

---

## Error Handling

### Standard Error Response Format
```json
{
  "error": "Error type",
  "message": "Detailed error message"
}
```

### HTTP Status Codes

| Status Code | Description |
|-------------|-------------|
| 200 OK | Request successful |
| 201 Created | Resource created successfully |
| 400 Bad Request | Invalid request body or parameters |
| 404 Not Found | Resource not found |
| 409 Conflict | Resource already exists |
| 500 Internal Server Error | Server error |

---

## Clean Architecture Implementation

### Separation of Concerns

1. **API Layer** (`api.rest`)
   - REST controllers handle HTTP requests
   - No business logic
   - Delegates to service layer

2. **Service Layer** (`service.rest`)
   - Contains business logic
   - Abstracts storage implementation
   - Provides unified interface

3. **Storage Layer** (`storage`)
   - Handles JSON file I/O
   - In-memory caching
   - Thread-safe operations

4. **Model Layer** (`model.rest`)
   - DTOs for REST operations
   - No business logic
   - Clean data structures

### Key Benefits

✅ **JavaFX controllers NEVER access JSON files directly**  
✅ **All file I/O is isolated in storage layer**  
✅ **Unified API abstracts dual storage system**  
✅ **Service layer can be tested independently**  
✅ **Easy to switch storage implementations**

---

## Starting the Server

The REST API server starts automatically when you launch the JavaFX application. You'll see output like:

```
==============================================
REST API Server started successfully!
Base URL: http://localhost:8080/api
==============================================
Available Endpoints:
  GET    /api/schedules
  GET    /api/schedules/bus
  ...
==============================================
```

The server stops automatically when you close the application.

---

## Testing the API

### Using Browser (GET requests only)
- Open: `http://localhost:8080/api/health`
- Open: `http://localhost:8080/api/schedules`
- Open: `http://localhost:8080/api/routes?start=Dhaka&destination=Chittagong`

### Using Postman or Insomnia
Import the base URL and test all endpoints with different HTTP methods.

### Using JavaFX Application
The JavaFX UI can now consume these REST endpoints instead of directly accessing files.

---

## Notes for Developers

1. **Developer Mode**: Use POST, PUT, DELETE endpoints to manage schedules
2. **User Mode**: Use GET endpoints only for searching and viewing
3. **Data Persistence**: All changes are immediately saved to JSON files
4. **Thread Safety**: Storage layer uses ConcurrentHashMap for thread-safe operations
5. **URL Encoding**: Remember to URL-encode special characters in path parameters (e.g., spaces as `%20`)

---

## Future Enhancements

Potential improvements:
- Add authentication/authorization
- Implement pagination for large datasets
- Add filtering and sorting options
- Support batch operations
- Add WebSocket support for real-time updates
- Implement caching strategies
- Add rate limiting
