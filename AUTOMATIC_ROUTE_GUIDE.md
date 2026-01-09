# Automatic Route Generator - Implementation Guide

## Overview
The Automatic Route Generator uses graph-based pathfinding to find optimal multi-leg journeys when no direct routes are available between two cities.

## Features

### 1. **Multi-Leg Journey Planning**
- Finds direct routes (1-leg)
- Finds routes with 1 transfer (2-leg)
- Finds routes with 2 transfers (3-leg)
- Configurable maximum number of legs

### 2. **Smart Transfer Validation**
- Minimum 30-minute transfer buffer between connections
- Validates transfer feasibility at intermediate cities
- Shows wait time at each transfer point

### 3. **Off-Day Filtering**
- Automatically filters out trains that don't run on the selected travel date
- Considers each train's off-day schedule
- Buses are always available (no off-day restrictions)

### 4. **Journey Optimization**
- Routes sorted by total travel time (fastest first)
- Calculates total fare across all legs
- Displays comprehensive journey information

## How to Use

### Step 1: Navigate to Automatic Route Generator
1. Launch the application
2. From the home screen, click on "Generate Plan" section
3. Select "Automatic Route" option

### Step 2: Enter Journey Details
- **From**: Starting city name (e.g., "Dhaka")
- **To**: Destination city name (e.g., "Cox's Bazar")
- **Date**: Select travel date from date picker
- **Max Legs**: Choose maximum number of connections:
  - 1 (Direct only) - Find only direct routes
  - 2 (1 transfer) - Allow one connection point
  - 3 (2 transfers) - Allow two connection points

### Step 3: Generate Routes
Click the "🔍 Generate Routes" button to search for available journeys.

### Step 4: Review Results
The system will display all found routes with:
- **Route Option Number**: Multiple alternatives ranked by speed
- **Number of Legs**: How many segments in the journey
- **Total Time**: Complete journey duration including transfers
- **Total Fare**: Sum of all leg fares

For each leg, you'll see:
- Transport type (🚆 Train or 🚌 Bus)
- Route/Company name
- Departure city and time
- Arrival city and time
- Leg duration and fare

Transfer points are highlighted with:
- 🔄 Transfer icon
- Transfer city name
- Wait time between connections

## Technical Implementation

### Core Components

#### 1. **RouteGraph.java** (`com.travelmanager.util`)
- Graph-based pathfinding algorithm
- Builds adjacency list from all schedules
- Implements BFS (Breadth-First Search) for multi-leg discovery

Key Methods:
```java
public List<JourneyPlan> findRoutes(String origin, String destination, int maxLegs)
private List<JourneyPlan> find2LegRoutes(String origin, String destination)
private List<JourneyPlan> find3LegRoutes(String origin, String destination)
private boolean isValidConnection(Schedule current, Schedule next)
```

#### 2. **AutomaticRouteController.java** (`com.travelmanager.controller`)
- JavaFX controller for UI interactions
- Handles user input and validation
- Displays search results with rich formatting

Key Methods:
```java
@FXML private void handleGenerateRoutes()
private void displayRoutes(List<JourneyPlan> routes, ...)
@FXML private void handleClear()
```

#### 3. **automatic-route.fxml** (`src/main/resources/fxml`)
- User interface layout
- Search form with input fields
- Scrollable results container

### Data Flow

1. **User Input** → Controller validates fields
2. **API Call** → RestScheduleService.getAllSchedules()
3. **Graph Building** → RouteGraph filters by date and builds adjacency list
4. **Pathfinding** → findRoutes() searches for 1, 2, and 3-leg journeys
5. **Result Display** → UI shows formatted journey cards

### Algorithm Details

#### Transfer Validation
```java
private boolean isValidConnection(Schedule current, Schedule next) {
    LocalDateTime arrivalAtTransfer = current.getArrivalTime();
    LocalDateTime departureFromTransfer = next.getDepartureTime();
    
    long bufferMinutes = Duration.between(arrivalAtTransfer, departureFromTransfer).toMinutes();
    
    return bufferMinutes >= 30; // Minimum 30 minutes for transfer
}
```

#### Off-Day Filtering (for Trains)
```java
if (schedule instanceof TrainSchedule) {
    TrainSchedule train = (TrainSchedule) schedule;
    if (!train.isAvailableOnDate(travelDate)) {
        continue; // Skip this train
    }
}
```

#### Journey Sorting
Routes are automatically sorted by total travel time:
```java
journeyPlans.sort(Comparator.comparingLong(JourneyPlan::getTotalTravelTime));
```

## Example Scenarios

### Scenario 1: Direct Route Available
**Input:**
- From: Dhaka
- To: Chittagong
- Date: 2024-01-15
- Max Legs: 3

**Result:**
- Option 1: Direct train (1 leg) - Sonar Bangla Express
- Option 2: Direct bus (1 leg) - Green Line
- Option 3: Via Comilla (2 legs) - If transfer time allows

### Scenario 2: No Direct Route
**Input:**
- From: Dhaka
- To: Cox's Bazar
- Date: 2024-01-15
- Max Legs: 3

**Result:**
- Option 1: Dhaka → Chittagong → Cox's Bazar (2 legs)
  - Leg 1: Train to Chittagong (Depart 7:00 AM, Arrive 1:00 PM)
  - Transfer at Chittagong (Wait: 45 min)
  - Leg 2: Bus to Cox's Bazar (Depart 1:45 PM, Arrive 6:00 PM)
  - Total Time: 11h 0m | Total Fare: ৳850

### Scenario 3: Complex Multi-Leg Journey
**Input:**
- From: Sylhet
- To: Khulna
- Date: 2024-01-15
- Max Legs: 3

**Result:**
- Option 1: Sylhet → Dhaka → Khulna (2 legs)
- Option 2: Sylhet → Dhaka → Jessore → Khulna (3 legs)

## Troubleshooting

### No Routes Found
**Possible Reasons:**
1. **Misspelled city names** - Check spelling exactly as in database
2. **No connecting routes** - Try increasing max legs
3. **Off-day restrictions** - Train may not run on selected date
4. **Insufficient transfer time** - No routes meet 30-min buffer requirement

**Solutions:**
- Verify city name spelling
- Increase "Max Legs" to 3
- Try a different travel date
- Check if schedules exist for both origin and destination

### Application Not Responding
**Cause:** Large route search (many schedules to process)

**Solution:** Wait for the search to complete (runs in background thread)

### Transfer Time Too Short
The system automatically filters out connections with less than 30 minutes transfer time. If you see fewer routes than expected, this is by design to ensure realistic journeys.

## Future Enhancements

### Planned Features
1. **Journey Selection** - Select a route and pass to Create Plan
2. **Advanced Filters**
   - Prefer trains over buses
   - Maximum total fare
   - Maximum total time
   - Minimum/maximum wait time at transfers
3. **Visual Journey Map** - Show route on map with transfer points
4. **Alternative Dates** - Find cheapest/fastest day in date range
5. **Favorite Routes** - Save frequently searched routes
6. **Share Journey** - Export journey details as PDF

### Performance Optimizations
1. **Caching** - Cache schedules for faster repeated searches
2. **Parallel Search** - Search multiple paths simultaneously
3. **Smart Pruning** - Eliminate unpromising paths early

## Data Synchronization

The automatic route generator uses data from:
- **REST API**: http://localhost:8080/api/schedules
- **JSON Files**: 
  - `data/train_schedules.json`
  - `data/bus_schedules.json`

### Manual Sync (if needed)
```powershell
.\sync_db_to_json.ps1
```

This syncs database routes to JSON files after bulk operations.

## Testing Checklist

- [ ] Direct route search works (1-leg)
- [ ] Transfer route search works (2-leg)
- [ ] Multi-transfer search works (3-leg)
- [ ] Off-day filtering for trains is accurate
- [ ] Transfer buffer validation (min 30 min) enforced
- [ ] Routes sorted by total travel time
- [ ] Total fare calculation correct
- [ ] Transfer points displayed correctly
- [ ] Wait time at transfers calculated accurately
- [ ] UI handles no results gracefully
- [ ] Clear button resets all fields
- [ ] Date picker validation (no past dates)
- [ ] Empty field validation works

## Architecture Diagram

```
┌─────────────────────────────────────────────┐
│     AutomaticRouteController (UI)           │
│  - handleGenerateRoutes()                   │
│  - displayRoutes()                          │
└────────────────┬────────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────────┐
│      RestScheduleService (API)              │
│  - getAllSchedules()                        │
└────────────────┬────────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────────┐
│         RouteGraph (Algorithm)              │
│  - buildGraph()                             │
│  - findRoutes()                             │
│  - find2LegRoutes()                         │
│  - find3LegRoutes()                         │
│  - isValidConnection()                      │
└────────────────┬────────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────────┐
│      JourneyPlan (Data Model)               │
│  - legs: List<RouteEdge>                    │
│  - getTotalTravelTime()                     │
│  - getTotalFare()                           │
│  - getTransferPoints()                      │
└─────────────────────────────────────────────┘
```

## Code Quality Standards

### Naming Conventions
- Classes: PascalCase (e.g., `RouteGraph`, `JourneyPlan`)
- Methods: camelCase (e.g., `findRoutes`, `isValidConnection`)
- Constants: UPPER_SNAKE_CASE (e.g., `MIN_TRANSFER_MINUTES`)

### Documentation
- All public classes have Javadoc comments
- Complex algorithms include inline comments
- Method purposes clearly stated

### Error Handling
- User input validation before processing
- Try-catch blocks for API calls
- Graceful error messages displayed to user
- Background thread for non-blocking search

## Dependencies

- **JavaFX 21.0.1** - UI framework
- **Javalin 5.6.3** - REST API server
- **Gson 2.10.1** - JSON parsing
- **SQLite JDBC** - Database connectivity
- **Java 17** - Language version

## Contact & Support

For issues or feature requests, please refer to the main README.md file.

---

**Last Updated:** January 9, 2026  
**Version:** 1.0  
**Status:** Production Ready ✅
