# Manual Schedule API Documentation

## 🎯 Overview

The Manual Schedule API allows developers to **manually create, edit, and delete** bus and train schedules that users will see in the application. All schedules are stored in a JSON file (`schedules-data.json`) and are loaded in **real-time** when users search for routes.

---

## 📁 Files Created

### Core API Files:
1. **[ScheduleDataManager.java](src/main/java/com/travelmanager/api/ScheduleDataManager.java)** - Handles reading/writing JSON file
2. **[ManualBusApiProvider.java](src/main/java/com/travelmanager/api/ManualBusApiProvider.java)** - Provides bus schedules from JSON
3. **[ManualTrainApiProvider.java](src/main/java/com/travelmanager/api/ManualTrainApiProvider.java)** - Provides train schedules from JSON
4. **[ManualScheduleService.java](src/main/java/com/travelmanager/api/ManualScheduleService.java)** - Service layer for CRUD operations
5. **[ManualScheduleApiExample.java](src/main/java/com/travelmanager/api/ManualScheduleApiExample.java)** - Usage examples

### Data Files:
- **[schedules-data.json](schedules-data.json)** - Stores all manual schedules (JSON format)

---

## ⚙️ Configuration

In **[api-config.properties](api-config.properties)**:

```properties
use.mock.data=false      # Disable random mock data
use.manual.data=true     # Enable manual schedule data
```

**Priority order:**
1. `use.manual.data=true` → Uses JSON file schedules
2. `use.mock.data=true` → Uses random generated schedules  
3. Both false → Real API providers (not yet implemented)

---

## 📝 How to Add/Edit Schedules

### Option 1: Programmatically (Java Code)

```java
ManualScheduleService service = ManualScheduleService.getInstance();

// Add a bus schedule
service.createBusSchedule(
    "BUS100",                               // Unique Schedule ID
    "Dhaka",                                // Origin
    "Chittagong",                           // Destination
    LocalDateTime.of(2025, 12, 27, 8, 0),   // Departure Time
    LocalDateTime.of(2025, 12, 27, 14, 0),  // Arrival Time
    850.0,                                  // Fare (BDT)
    35,                                     // Available Seats
    "Green Line",                           // Company Name
    "AC"                                    // Bus Type
);

// Add a train schedule
service.createTrainSchedule(
    "TRAIN100",                             // Unique Schedule ID
    "Dhaka",                                // Origin
    "Chittagong",                           // Destination
    LocalDateTime.of(2025, 12, 27, 6, 30),  // Departure Time
    LocalDateTime.of(2025, 12, 27, 12, 0),  // Arrival Time
    550.0,                                  // Fare (BDT)
    120,                                    // Available Seats
    "Subarna Express",                      // Train Name
    "AC"                                    // Train Class
);

// Update a schedule
service.updateBusSchedule(
    "BUS100",      // Schedule ID to update
    "Dhaka",       // New origin
    "Chittagong",  // New destination
    // ... other parameters
);

// Delete a schedule
service.deleteBusSchedule("BUS100");
service.deleteTrainSchedule("TRAIN100");
```

### Option 2: Directly Edit JSON File

Open **[schedules-data.json](schedules-data.json)** in any text editor:

```json
{
  "busSchedules": [
    {
      "scheduleId": "BUS001",
      "origin": "Dhaka",
      "destination": "Chittagong",
      "departureTime": "2025-12-27T08:00:00",
      "arrivalTime": "2025-12-27T14:00:00",
      "fare": 850.0,
      "availableSeats": 35,
      "company": "Green Line",
      "busType": "AC"
    }
  ],
  "trainSchedules": [
    {
      "scheduleId": "TRAIN001",
      "origin": "Dhaka",
      "destination": "Chittagong",
      "departureTime": "2025-12-27T06:30:00",
      "arrivalTime": "2025-12-27T12:00:00",
      "fare": 550.0,
      "availableSeats": 120,
      "trainName": "Subarna Express",
      "trainClass": "AC"
    }
  ]
}
```

**After editing the file directly**, call `service.reloadData()` to refresh in the app.

---

## 🔄 Real-Time Updates

All changes are saved **immediately** to `schedules-data.json`:
- ✅ Add schedule → Saved to JSON instantly
- ✅ Edit schedule → Updated in JSON instantly  
- ✅ Delete schedule → Removed from JSON instantly

Users will see these changes the next time they search for a route!

---

## 🧪 Testing the API

Run the example class to test:

```bash
java com.travelmanager.api.ManualScheduleApiExample
```

This will:
1. Show current schedule counts
2. Add new bus and train schedules
3. Read and display a schedule
4. Update a schedule
5. Delete schedules
6. Show final counts

---

## 🎨 Creating a UI for Schedule Management

To create a developer panel in your JavaFX app:

1. **Create a new FXML file**: `manage-schedules.fxml`
2. **Create a controller**: `ManageSchedulesController.java`
3. **Add UI elements**:
   - TableView to display schedules
   - Forms to add/edit schedules
   - Delete buttons
   - Filter by route

Example controller methods:

```java
public class ManageSchedulesController {
    private ManualScheduleService service = ManualScheduleService.getInstance();
    
    @FXML
    private void addBusSchedule() {
        // Get data from form fields
        service.createBusSchedule(
            scheduleIdField.getText(),
            originField.getText(),
            // ... other fields
        );
        refreshTable();
    }
    
    @FXML
    private void deleteBusSchedule() {
        String selectedId = tableView.getSelectionModel().getSelectedItem().getScheduleId();
        service.deleteBusSchedule(selectedId);
        refreshTable();
    }
}
```

---

## 📊 Data Format Reference

### Bus Schedule Fields:
- `scheduleId` (String) - Unique identifier (e.g., "BUS001")
- `origin` (String) - Starting city
- `destination` (String) - Ending city
- `departureTime` (ISO DateTime) - Format: "2025-12-27T08:00:00"
- `arrivalTime` (ISO DateTime) - Format: "2025-12-27T14:00:00"
- `fare` (Number) - Price in BDT
- `availableSeats` (Number) - Number of seats
- `company` (String) - Bus company name
- `busType` (String) - Type: "AC", "Non-AC", "Sleeper", "Deluxe"

### Train Schedule Fields:
- `scheduleId` (String) - Unique identifier (e.g., "TRAIN001")
- `origin` (String) - Starting station
- `destination` (String) - Ending station
- `departureTime` (ISO DateTime) - Format: "2025-12-27T06:30:00"
- `arrivalTime` (ISO DateTime) - Format: "2025-12-27T12:00:00"
- `fare` (Number) - Price in BDT
- `availableSeats` (Number) - Number of seats
- `trainName` (String) - Train name
- `trainClass` (String) - Class: "AC", "First Class", "Snigdha", "S_Chair", "Shovan"

---

## ✅ Benefits

✓ **Developer Control** - Manually manage exact schedules  
✓ **Real-Time Updates** - Changes saved immediately  
✓ **Easy Editing** - Edit JSON file directly or use API  
✓ **Version Control** - Commit schedule changes to Git  
✓ **Portable** - Simple JSON file, no database needed  
✓ **User-Friendly** - Users see real, curated schedules instead of random data

---

## 🚀 Next Steps

1. **Create UI Panel** - Build JavaFX interface for schedule management
2. **Add Validation** - Ensure schedule IDs are unique, times are valid
3. **Add Search/Filter** - Filter schedules by route, company, etc.
4. **Add Import/Export** - Bulk import schedules from CSV/Excel
5. **Add API Endpoints** - Expose REST API for external management

---

## 💡 Tips

- Keep schedule IDs unique (e.g., BUS001, BUS002, TRAIN001)
- Use ISO date format: `YYYY-MM-DDTHH:MM:SS`
- Back up `schedules-data.json` before bulk edits
- Use version control (Git) to track schedule changes
- Test schedule additions with the example class first

---

**Questions?** Check [ManualScheduleApiExample.java](src/main/java/com/travelmanager/api/ManualScheduleApiExample.java) for usage examples!
