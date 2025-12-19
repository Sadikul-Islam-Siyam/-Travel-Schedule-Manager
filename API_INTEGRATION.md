# API Integration Guide

## Overview
The Travel Schedule Manager now integrates with external APIs to fetch real-time bus and train schedules. The system is designed with flexibility, allowing easy switching between mock data (for development/testing) and real API endpoints.

## Architecture

### Core Components

1. **ApiClient** (`com.travelmanager.api.ApiClient`)
   - Generic HTTP client for making API requests
   - Handles GET requests with custom headers
   - Built-in timeout handling (10 seconds)
   - UTF-8 encoding support

2. **ScheduleApiProvider** (`com.travelmanager.api.ScheduleApiProvider`)
   - Interface defining the contract for all schedule providers
   - Methods:
     - `fetchSchedules(origin, destination, date)` - Fetch schedules
     - `getProviderName()` - Get provider name
     - `isAvailable()` - Check if provider is available

3. **ScheduleApiManager** (`com.travelmanager.api.ScheduleApiManager`)
   - Singleton manager coordinating all schedule providers
   - Handles provider initialization and selection
   - Supports fetching all schedules or filtering by type (bus/train)

4. **ApiConfig** (`com.travelmanager.api.ApiConfig`)
   - Configuration management using properties file
   - Auto-creates `api-config.properties` with defaults
   - Methods for checking API availability and settings

### Mock Providers (Development)

1. **MockBusApiProvider** (`com.travelmanager.api.MockBusApiProvider`)
   - Generates 5-10 random bus schedules per search
   - Realistic bus companies: Green Line, Shyamoli Paribahan, Hanif Enterprise, etc.
   - Bus types: AC, Non-AC, Sleeper, Deluxe
   - Journey durations: 4-12 hours
   - Fares: 400-1500 BDT
   - Seats: 10-40

2. **MockTrainApiProvider** (`com.travelmanager.api.MockTrainApiProvider`)
   - Generates 4-8 random train schedules per search
   - Realistic train names: Subarna Express, Turna Nishitha, Silk City Express, etc.
   - Train classes: AC, First Class, Snigdha, S_Chair, Shovan
   - Journey durations: 5-14 hours
   - Fares: 300-1200 BDT
   - Seats: 20-120

## Configuration

### api-config.properties

Located at: `src/main/resources/api-config.properties`

```properties
# Mock data settings (set to false when using real APIs)
use.mock.data=true

# Bus API Configuration
bus.api.enabled=true
bus.api.endpoint=https://api.example.com/bus/schedules
bus.api.key=YOUR_BUS_API_KEY_HERE

# Train API Configuration
train.api.enabled=true
train.api.endpoint=https://api.example.com/train/schedules
train.api.key=YOUR_TRAIN_API_KEY_HERE

# API timeout settings (in milliseconds)
api.timeout.connect=10000
api.timeout.read=10000
```

### Switching Between Mock and Real APIs

**To use mock data (default):**
```properties
use.mock.data=true
```

**To use real APIs:**
1. Set `use.mock.data=false`
2. Configure API endpoints and keys
3. Implement real API provider classes (see below)

## Integration with ScheduleService

The `ScheduleService` has been updated to use the API manager:

```java
// Search all transport types
List<Schedule> results = scheduleService.searchSchedules(origin, destination, date);

// Search only buses
List<Schedule> buses = scheduleService.searchBusSchedules(origin, destination, date);

// Search only trains
List<Schedule> trains = scheduleService.searchTrainSchedules(origin, destination, date);
```

Features:
- Automatic caching for offline use
- Fallback to cached data on API errors
- Legacy method support (without date parameter)

## Implementing Real API Providers

To add a real API provider:

1. Create a new class implementing `ScheduleApiProvider`:

```java
public class RealBusApiProvider implements ScheduleApiProvider {
    
    @Override
    public List<Schedule> fetchSchedules(String origin, String destination, LocalDate date) {
        String endpoint = ApiConfig.getBusApiEndpoint();
        String apiKey = ApiConfig.getBusApiKey();
        
        // Build URL with parameters
        String url = endpoint + "?from=" + origin + "&to=" + destination + 
                    "&date=" + date + "&key=" + apiKey;
        
        try {
            String response = ApiClient.get(url);
            // Parse JSON response and convert to Schedule objects
            return parseJsonResponse(response);
        } catch (Exception e) {
            System.err.println("Error fetching bus schedules: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    @Override
    public String getProviderName() {
        return "Real Bus API Provider";
    }
    
    @Override
    public boolean isAvailable() {
        return ApiConfig.isBusApiEnabled();
    }
    
    private List<Schedule> parseJsonResponse(String json) {
        // Implement JSON parsing logic
        // Convert API response to BusSchedule objects
        return new ArrayList<>();
    }
}
```

2. Register the provider in `ScheduleApiManager.initializeProviders()`:

```java
if (ApiConfig.isBusApiEnabled() && !ApiConfig.useMockData()) {
    providers.add(new RealBusApiProvider());
}
```

## Testing

### With Mock Data

1. Ensure `use.mock.data=true` in config
2. Run the application
3. Search for any route (e.g., Dhaka to Chittagong)
4. You'll see randomly generated schedules

### With Real APIs

1. Set `use.mock.data=false`
2. Configure API endpoints and keys
3. Implement real provider classes
4. Test with actual routes

## Error Handling

The system handles errors gracefully:
- **API unavailable**: Falls back to cached schedules
- **Timeout**: Returns empty list after 10 seconds
- **Invalid response**: Logs error and returns empty list
- **No results**: Shows "No schedules found" message to user

## Future Enhancements

1. **Aggregator Providers**: Combine multiple APIs
2. **Caching Strategy**: Persistent cache for frequently searched routes
3. **Rate Limiting**: Prevent API quota exhaustion
4. **Retry Logic**: Auto-retry failed requests
5. **Response Caching**: Cache API responses for specific time periods
6. **Async Loading**: Non-blocking UI during API calls

## Current Status

✅ API infrastructure complete
✅ Mock providers implemented
✅ ScheduleService integrated
✅ CreatePlanController updated
✅ Configuration system in place

⏳ Real API providers (pending API access)
⏳ JSON parsing implementation
⏳ EditPlanController update (if needed)

## Notes

- Mock providers use `ThreadLocalRandom` for realistic variations
- All schedules use the requested date (not hard-coded times)
- Departure times are randomized throughout the day (6 AM - 10 PM)
- Journey durations are realistic for Bangladesh routes
- The system automatically handles date changes in schedule generation
