# Quick Start Guide - REST API

## 🚀 Getting Started

### 1. Build the Project
```bash
mvn clean compile
```

### 2. Run the Application
```bash
mvn javafx:run
```

Or use the provided batch file:
```bash
START.bat
```

### 3. Verify API is Running

The REST API will start automatically when you launch the JavaFX application. You should see output like:

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

### 4. Test the API

#### In Your Browser
Open these URLs:
- Health check: http://localhost:8080/api/health
- All schedules: http://localhost:8080/api/schedules
- Search routes: http://localhost:8080/api/routes?start=Dhaka&destination=Chittagong

#### Using cURL
```bash
# Get all schedules
curl http://localhost:8080/api/schedules

# Search for routes
curl "http://localhost:8080/api/routes?start=Dhaka&destination=Chittagong"

# Add a bus schedule (Developer mode)
curl -X POST http://localhost:8080/api/schedules/bus \
  -H "Content-Type: application/json" \
  -d '{
    "busName": "Test Bus",
    "start": "Dhaka",
    "destination": "Sylhet",
    "startTime": "10:00",
    "arrivalTime": "16:00",
    "fare": 700.0,
    "duration": "6:00h"
  }'

# Update a bus schedule
curl -X PUT "http://localhost:8080/api/schedules/bus/Test%20Bus" \
  -H "Content-Type: application/json" \
  -d '{
    "busName": "Test Bus",
    "start": "Dhaka",
    "destination": "Sylhet",
    "startTime": "11:00",
    "arrivalTime": "17:00",
    "fare": 750.0,
    "duration": "6:00h"
  }'

# Delete a bus schedule
curl -X DELETE "http://localhost:8080/api/schedules/bus/Test%20Bus"
```

---

## 📦 What's Included

### Sample Data
The application comes with pre-populated data:
- **5 bus schedules** in `data/bus_schedules.json`
- **7 train schedules** in `data/train_schedules.json`

### Available Routes
Try searching these routes:
- Dhaka → Chittagong (2 buses + 2 trains)
- Dhaka → Sylhet (1 bus + 2 trains)
- Dhaka → Rajshahi (1 bus + 1 train)
- Dhaka → Rangpur (1 bus + 1 train)

---

## 🧪 Testing Scenarios

### As a User (Read-Only)
1. **Search for routes**:
   ```
   GET /api/routes?start=Dhaka&destination=Chittagong
   ```

2. **View all schedules**:
   ```
   GET /api/schedules
   ```

3. **View bus schedules only**:
   ```
   GET /api/schedules/bus
   ```

4. **View train schedules only**:
   ```
   GET /api/schedules/train
   ```

### As a Developer (Full Access)
1. **Add a new bus**:
   ```bash
   curl -X POST http://localhost:8080/api/schedules/bus \
     -H "Content-Type: application/json" \
     -d '{"busName":"New Bus","start":"Dhaka","destination":"Barisal","startTime":"09:00","arrivalTime":"15:00","fare":650.0,"duration":"6:00h"}'
   ```

2. **Update an existing train**:
   ```bash
   curl -X PUT "http://localhost:8080/api/schedules/train/SUBARNA%20EXPRESS%20(702)" \
     -H "Content-Type: application/json" \
     -d '{"trainName":"SUBARNA EXPRESS (702)","start":"Dhaka","destination":"Chittagong","startTime":"15:00","arrivalTime":"21:00","fare":450.0,"duration":"6:00h","offDay":"No off day"}'
   ```

3. **Delete a schedule**:
   ```bash
   curl -X DELETE "http://localhost:8080/api/schedules/bus/New%20Bus"
   ```

---

## 🔍 Troubleshooting

### API Not Starting
- **Check console output** for error messages
- **Verify port 8080** is not in use by another application
- **Check dependencies** are downloaded: `mvn dependency:resolve`

### Cannot Connect to API
- Ensure the JavaFX app is running
- Try: http://localhost:8080/api/health
- Check firewall settings

### Data Not Persisting
- Ensure `data/` directory exists
- Check file permissions
- Verify JSON files are valid

### Compilation Errors
- Run `mvn clean compile`
- Check Java version: `java -version` (should be 17+)
- Verify Maven installation: `mvn --version`

---

## 📚 Documentation

For detailed API documentation, see:
- **REST_API_DOCUMENTATION.md** - Complete API reference
- **REST_API_IMPLEMENTATION_SUMMARY.md** - Architecture overview

---

## 💻 Using the API in Code

### Java Example
```java
import com.travelmanager.api.rest.example.RestApiClientExample;
import com.travelmanager.model.rest.*;

RestApiClientExample client = new RestApiClientExample();

// Search routes
List<UnifiedScheduleDTO> routes = client.searchRoutes("Dhaka", "Chittagong");
routes.forEach(route -> 
    System.out.println(route.getName() + " - Fare: " + route.getFare())
);

// Add bus schedule
BusScheduleDTO bus = new BusScheduleDTO(
    "My Bus", "Dhaka", "Sylhet",
    "10:00", "16:00", 700.0, "6:00h"
);
boolean added = client.addBusSchedule(bus);
```

### JavaScript Example
```javascript
// Search routes
fetch('http://localhost:8080/api/routes?start=Dhaka&destination=Chittagong')
  .then(response => response.json())
  .then(routes => console.log(routes));

// Add bus schedule
fetch('http://localhost:8080/api/schedules/bus', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    busName: "My Bus",
    start: "Dhaka",
    destination: "Sylhet",
    startTime: "10:00",
    arrivalTime: "16:00",
    fare: 700.0,
    duration: "6:00h"
  })
}).then(response => response.json())
  .then(result => console.log(result));
```

---

## ✅ Quick Checklist

- [ ] Maven build successful
- [ ] JavaFX app launches
- [ ] Console shows "REST API Server started successfully!"
- [ ] Health check responds: http://localhost:8080/api/health
- [ ] Can view all schedules: http://localhost:8080/api/schedules
- [ ] Can search routes: http://localhost:8080/api/routes?start=Dhaka&destination=Chittagong
- [ ] Can add a bus schedule (POST)
- [ ] Can update a schedule (PUT)
- [ ] Can delete a schedule (DELETE)
- [ ] Changes persist to JSON files

---

## 🎯 Next Steps

1. **Integrate with JavaFX UI**: Update controllers to use REST API instead of direct file access
2. **Add Authentication**: Implement security for Developer endpoints
3. **Enhance Search**: Add filters for fare, duration, departure time
4. **Add Pagination**: Handle large datasets efficiently
5. **Real-time Updates**: Use WebSockets for live schedule changes

---

## 🆘 Need Help?

If you encounter issues:
1. Check the console output for error messages
2. Review REST_API_DOCUMENTATION.md
3. Examine RestApiClientExample.java for usage patterns
4. Verify JSON files are valid
5. Test with simple GET requests first

---

**The REST API is ready to use! Start building amazing features!** 🚀
