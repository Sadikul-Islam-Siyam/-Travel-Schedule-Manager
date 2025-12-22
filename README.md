# Travel Schedule Manager 

A smart multi-modal travel schedule management application built with JavaFX.

## Features 

-  **API Integration** - Real-time schedule fetching via API providers (mock data included)
-  **Create Travel Plans** - Build multi-leg journeys with ease
-  **Smart Search** - Autocomplete for 14 Bangladesh cities
-  **Filter Options** - View All, Bus Only, or Train Only schedules
-  **Date-Based Search** - Get schedules for specific travel dates
-  **Save & Manage** - SQLite database for persistent storage
-  **Edit Plans** - Add or remove schedules from existing plans
-  **View Details** - See complete itinerary with fare breakdown
-  **Calculate Totals** - Automatic calculation of total fare and duration
-  **Plan Validation** - Checks for connection point consistency and timing
-  **Tight Connection Warnings** - Alerts for connections under 30 minutes
-  **Notes/Comments** - Add notes to saved plans
-  **Single-Window Navigation** - Clean UI with back buttons (no popups)

### Original Features (Legacy/To Be Integrated)
- ✅ API Integration - Real-time schedule fetching
- ✅ Create Travel Plans - Build multi-leg journeys
- ✅ Smart Search - Autocomplete for 14 Bangladesh cities
- ✅ Filter Options - View All, Bus Only, or Train Only
- ✅ Date-Based Search - Get schedules for specific dates
- ✅ Save & Manage - SQLite database persistence
- ✅ Edit Plans - Add/remove schedules from existing plans
- ✅ View Details - Complete itinerary with fare breakdown
- ✅ Calculate Totals - Automatic fare and duration calculation
- ✅ Plan Validation - Connection point and timing checks
- ✅ Tight Connection Warnings - Alerts for <30min connections
- ✅ Notes/Comments - Add notes to saved plans

---

##  Quick Start - EASIEST WAY TO RUN

###  Method 1: Double-Click START.bat (Windows)

1. Make sure Java 17+ is installed
2. Make sure Maven is installed
3. **Double-click START.bat** in the project root
4. Wait for the application to start
5. Done! 

### Method 2: Command Line

`ash
mvn javafx:run
`

---

##  Requirements

Before running the application, ensure you have:

1. **Java 17 or higher**
   - Download: [https://adoptium.net/](https://adoptium.net/)
   - Verify: java -version

2. **Apache Maven 3.6+**
   - Download: [https://maven.apache.org/download.cgi](https://maven.apache.org/download.cgi)
   - Verify: mvn -version

3. **Internet connection** (first run only - to download dependencies)

---

##  Project Structure

```
-Travel-Schedule-Manager/

 src/main/java/com/travelmanager/
    App.java                      # Main application
    api/                          # API Integration (NEW)
       ApiClient.java             # HTTP client
       ApiConfig.java             # Configuration manager
       ScheduleApiProvider.java   # Provider interface
       ScheduleApiManager.java    # Provider coordinator
       MockBusApiProvider.java    # Mock bus data
       MockTrainApiProvider.java  # Mock train data
    controller/                   # UI Controllers
       HomeController.java
       CreatePlanController.java
       SummarizePlanController.java
       EditPlanController.java
       SavedPlansController.java
       ViewPlanDetailsController.java
       HelpController.java
       AutomaticRouteController.java
    model/                        # Data Models
    service/                      # Business Logic
    database/                     # SQLite Database
    util/                        # Utilities (NavigationManager, etc.)

 src/main/resources/
    fxml/                        # UI Layouts (7 files)
    css/                         # Stylesheets
    api-config.properties        # API Configuration (NEW)

 START.bat                        # Windows Quick Launcher
 run.bat                          # Alternative launcher
 pom.xml                          # Maven configuration
 README.md                        # This file
 API_INTEGRATION.md               # API integration guide (NEW)
 API_SUMMARY.md                   # API implementation summary (NEW)
```

---

##  Database

- **Type**: SQLite (data/travel_plans.db)
- **Auto-created on first run**
- **Tables**:
  - `users` - Authentication (username, password_hash, role, account status, failed login attempts)
  - `plans` - Travel plan metadata (name, total_fare, created_date, notes, user_id)
  - `schedules` - Individual schedule legs (plan_id, origin, destination, type, etc.)
  - `routes` - API routes data (managed by DEVELOPER/MASTER roles)
  
### Security Features
- Password hashing with unique salt per user (PBKDF2 with HMAC SHA-256)
- Account lockout after 3 failed login attempts (30-minute timeout)
- Role-based authorization checks
- Session management with last login tracking

---

##  Building from Source

```bash
# Clone the repository
git clone <repository-url>
cd -Travel-Schedule-Manager

# Build the project
mvn clean compile

# Run tests
mvn test

# Package (creates JAR)
mvn clean package

# Run the application
mvn javafx:run
```

---

##  Troubleshooting

### Error: \"Module javafx.controls not found\"
**Solution**: Always use mvn javafx:run or START.bat. Do NOT run the JAR directly.

### Error: \"Java is not installed\"
**Solution**: 
1. Download Java 17+ from [Adoptium](https://adoptium.net/)
2. Install and add to PATH
3. Verify with: java -version

### Error: \"Maven not recognized\"
**Solution**:
1. Download Maven from [Apache Maven](https://maven.apache.org/download.cgi)
2. Extract and add in folder to PATH
3. Verify with: mvn -version

### Application won't start
**Solution**:
1. Ensure Java 17+ is installed: java -version
2. Ensure Maven is installed: mvn -version
3. Try rebuilding: mvn clean compile
4. Run: mvn javafx:run

---

##  How to Use

### First Time Setup
1. **Run the application** (using START.bat or `mvn javafx:run`)
2. **Register an account** - Choose role: USER, DEVELOPER, or MASTER
3. **Wait for approval** - PENDING accounts need MASTER approval (or manually set to APPROVED in database)
4. **Login** - Use your credentials to access the system

### Creating a Plan (USER/DEVELOPER/MASTER)
1. Click **\"Create Plan\"** on home screen
2. Enter start and destination (autocomplete available)
3. Select date and transport type
4. Click **\"Search Schedules\"**
5. Select schedule and click **\"Add to Plan\"**
6. Repeat for multi-leg journeys
7. Click **\"Summarize & Save Plan\"**
8. Enter plan name and save

### Editing a Plan (DEVELOPER/MASTER)
1. Go to **\"Saved Plans\"**
2. Select a plan from the list
3. Click **\"Edit Plan\"**
4. Remove unwanted schedules OR add new ones
5. Click **\"Save Changes\"**

### Managing Routes (DEVELOPER/MASTER)
1. Click **\"Edit API\"** on home screen
2. **View Routes** - See all bus/train routes
3. **Add Route** - Fill origin, destination, type, schedule details
4. **Edit Route** - Modify existing routes
5. **Delete Route** - Remove outdated routes

### Account Approval (MASTER Only)
1. Click **\"Pending Application\"** on home screen
2. **View Details** - Check pending user registrations
3. **Approve** - Grant access with optional welcome note
4. **Reject** - Deny access with reason

### Viewing Plans (All Roles)
1. Go to **\"Saved Plans\"**
2. Select a plan
3. Click **\"View Details\"**

---

##  API Configuration

The application uses API providers to fetch bus and train schedules.

### Mock Data (Default)
- Enabled by default for development and testing
- Generates realistic random schedules
- No API keys required - works offline

### Configuration File
Edit `src/main/resources/api-config.properties`:

```properties
# Toggle between mock and real data
use.mock.data=true

# Bus API Settings
bus.api.enabled=true
bus.api.url=http://api.example.com/bus
bus.api.key=your_api_key_here

# Train API Settings
train.api.enabled=true
train.api.url=http://api.example.com/train
train.api.key=your_api_key_here
```

### API Infrastructure
- **ApiClient** - Generic HTTP client with timeout handling
- **ScheduleApiProvider** - Interface for all providers
- **ScheduleApiManager** - Singleton coordinator for providers
- **ApiConfig** - Configuration manager (auto-creates properties file)
- **Mock Providers** - MockBusApiProvider, MockTrainApiProvider

To switch to real APIs: Set `use.mock.data=false` and configure endpoints/keys.

---

##  Key Features

### Authentication & Security
- 🔐 **Role-Based Access Control** - Three roles: USER, DEVELOPER, MASTER
- 🔒 **Secure Authentication** - Password hashing with salt, account lockout after failed attempts
- 👤 **Registration System** - New users register with PENDING status, MASTER approves/rejects
- 🛡️ **Session Management** - Secure login/logout with proper session tracking

### Role Capabilities
- **USER**: Create and view travel plans
- **DEVELOPER**: Full plan management (create, edit, delete) + API route editor
- **MASTER**: All developer permissions + account approval + API management

### API Integration
- ✅ Complete API infrastructure with mock providers
- ✅ Configuration-based provider management (`api-config.properties`)
- ✅ Date-based schedule searching with automatic caching
- ✅ Offline fallback support
- 🔄 **API Routes Management** (DEVELOPER/MASTER only) - Add/edit/delete bus and train routes

### Master Mode Dashboard
- 📋 **Account Approval** - Review pending registrations, approve/reject with notes
- 🛠️ **API Management** - Full CRUD operations on routes and API configurations

### UI/UX
- ✅ Single-window navigation (no popups)
- ✅ Enhanced back buttons throughout app
- ✅ Plan validation with warnings for connection issues
- ✅ Tight connection alerts (<30 min)
- ✅ Total journey duration display
- ✅ Notes/comments on saved plans

---

##  License

This project is licensed under the MIT License.

---

##  Technical Documentation

For detailed implementation guides, see:
- **API_SUMMARY.md** - Complete API implementation summary and architecture
- **SECURITY_DOCUMENTATION.md** - Complete authentication architecture, database schema, security best practices
- **MASTER_MODE_IMPLEMENTATION.md** - Master dashboard implementation, role hierarchy, account approval workflow
- **ROLE_BASED_FLOW_DIAGRAM.md** - Visual workflow diagrams for role-based access
- **REGISTRATION_IMPLEMENTATION_SUMMARY.md** - Registration system implementation details
- **REGISTRATION_SYSTEM_TESTING.md** - Testing procedures and test cases
- **PENDING_API_CHANGES_IMPLEMENTATION.md** - API change request workflow
- **IMPLEMENTATION_VERIFICATION.md** - Verification and testing documentation

---

##  Author

Developed as part of a smart travel management solution.

---

##  Enjoy Your Travel Planning!

For any issues or questions, please refer to the troubleshooting section above.
