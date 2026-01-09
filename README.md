# Travel Schedule Manager 🚆🚌

A comprehensive multi-modal travel schedule management application built with JavaFX, featuring REST API integration, role-based access control, and real-time schedule management for Bangladesh's bus and train services.

## ✨ Key Features

### Core Functionality
- 🔌 **API Integration** - Real-time schedule fetching via REST API (mock & manual data support)
- 📋 **Multi-Leg Travel Plans** - Create complex journeys with multiple connections (up to 3 legs)
- 🔍 **Smart Search** - Autocomplete for 19 Bangladesh cities with advanced filtering
- 📅 **Date-Based Search** - Schedule lookup for specific travel dates
- 💾 **Persistent Storage** - SQLite database with JSON file support
- ✏️ **Plan Management** - Edit, update, and delete saved travel plans
- 💰 **Fare Calculation** - Automatic total fare and duration computation
- ⚠️ **Connection Validation** - Smart checks for timing and connection points (30-min transfer buffer)
- 📝 **Notes & Comments** - Add custom notes to your travel plans
- 🚦 **Intelligent Routing** - Automatic route finding with pathfinding algorithm

### Authentication & Security
- 🔐 **Role-Based Access Control** - Three role levels (USER, DEVELOPER, MASTER)
- 🔒 **Secure Authentication** - PBKDF2 password hashing with unique salt per user
- 🛡️ **Account Protection** - Auto-lockout after 3 failed login attempts (30 min timeout)
- 👤 **Registration System** - Approval workflow for new user accounts
- 📊 **Session Management** - Track user sessions and login history

### Advanced Features (DEVELOPER/MASTER)
- 🛠️ **Schedule Management UI** - Add, edit, delete bus and train schedules
- 🌐 **REST API Server** - Embedded Javalin server on port 8080
- 📡 **API Testing Tool** - Built-in tool to test API endpoints
- 🗺️ **Route Management** - CRUD operations for routes with approval workflow
- 👥 **User Management** - Account approval and role administration (MASTER only)
- 📈 **Request History** - Track API change requests and approvals
- 🚦 **Multi-Leg Routing** - Advanced pathfinding algorithm with transfer validation

---

## 🚀 Quick Start

### Easiest Method: Double-Click START.bat (Windows)

1. Ensure **Java 17+** and **Maven** are installed
2. **Double-click START.bat** in the project root
3. Wait for compilation and startup
4. Login or register a new account

### Alternative: Command Line

```bash
mvn javafx:run
```

### First Time Setup

1. **Register an Account**
   - Choose your role: USER, DEVELOPER, or MASTER
   - Provide username, email, password, and full name
   
2. **Wait for Approval** (if applicable)
   - PENDING accounts require MASTER approval
   - Or manually update status to 'APPROVED' in database for testing
   
3. **Login**
   - Use your credentials to access the system
   - Failed attempts are tracked (3 strikes = 30 min lockout)

---

## 📋 Requirements

Before running the application, ensure you have:

1. **Java 17 or Higher**
   - Download: [https://adoptium.net/](https://adoptium.net/)
   - Verify: `java -version`

2. **Apache Maven 3.6+**
   - Download: [https://maven.apache.org/download.cgi](https://maven.apache.org/download.cgi)
   - Verify: `mvn -version`

3. **Internet Connection** (first run only - to download dependencies)

---

## 🎯 User Guide

### For All Users (USER/DEVELOPER/MASTER)

#### Creating a Travel Plan
1. Click **"Create Plan"** on the home screen
2. Enter origin and destination (autocomplete available for 14 cities)
3. Select travel date and transport type (All/Bus/Train)
4. Click **"Search Schedules"**
5. Browse results and click **"Add to Plan"** for desired schedule
6. Repeat for multi-leg journeys
7. Click **"Summarize & Save Plan"**
8. Enter plan name, add notes (optional), and save

#### Viewing Saved Plans
1. Click **"Saved Plans"** on the home screen
2. Select a plan from the list
3. Click **"View Details"** to see complete itinerary with fare breakdown

#### Automatic Route Finding (Multi-Leg Pathfinding)
1. Click **"Automatic Route"** on the home screen
2. Enter origin and destination (autocomplete available for 19 cities)
3. Select travel date and maximum number of legs (1-3)
4. Click **"Generate Routes"**
5. System finds optimal routes using smart pathfinding algorithm:
   - Finds direct routes and multi-leg journeys with transfers
   - Validates 30-minute minimum transfer buffer at connection points
   - Filters by off-days for train schedules (e.g., weekly holidays)
   - Optimizes routes by total travel time
6. Review generated journey options with:
   - Complete route details with departure/arrival times
   - Transfer point indicators between legs
   - Total fare and duration breakdown
7. Click **"💾 Save Plan"** on any journey card to save as a travel plan

### For DEVELOPER & MASTER Roles

#### Editing Plans
1. Go to **"Saved Plans"**
2. Select a plan from the list
3. Click **"Edit Plan"**
4. Remove schedules OR add new ones
5. Click **"Save Changes"**

#### Managing Schedules
1. Click **"Manage Schedules"** on the home screen
2. **View All** - Browse all bus and train schedules
3. **Filter** - Select ALL, BUS, or TRAIN
4. **Search** - Find schedules by ID or route
5. **Add New** - Fill form and click "Save Schedule"
6. **Edit** - Select schedule, modify details, click "Update Schedule"
7. **Delete** - Select schedule and confirm deletion
8. Click **🔄 Refresh** to reload data

#### Managing Routes (Route API Configuration)
1. Click **"Manage Routes"** on the home screen
2. View all configured API routes in the system
3. **Add New Route** (Developer/Master):
   - Click **"➕ Add Route"** button
   - Fill in route details:
     * Route Name (Bus/Train service name)
     * Origin and Destination cities
     * Transport Type (BUS or TRAIN)
     * Duration (in minutes), Price (in BDT)
     * Schedule Time, Departure/Arrival Time
     * Status (ACTIVE, INACTIVE, MAINTENANCE)
     * Notes for Master (explain why this change is needed)
   - Click **"Submit for Approval"**
   - Master must approve before changes are applied to database and JSON files
4. **Edit Existing Route**:
   - Select a route from the table
   - Click **"✏ Edit"** button
   - Modify necessary fields and add approval notes
   - Submit for Master approval
5. **Delete Route**:
   - Select a route from the table
   - Click **"🗑 Delete"** button
   - Confirm deletion request
   - Master reviews and approves/rejects deletion

**Approval Workflow:**
- All route changes (Add/Edit/Delete) go through approval workflow
- Developer submits change → stored in `pending_routes` table
- Master reviews in **"API Approval"** page
- Upon approval:
  - Changes applied to `routes` database table
  - JSON files updated (`data/bus_schedules.json` or `data/train_schedules.json`)
  - Change logged in `route_history` table
- Upon rejection: Developer receives feedback explaining why

#### Testing API
1. Click **"API Testing Tool"** on the home screen
2. Select endpoint from dropdown
3. Enter required parameters
4. Click **"Send Request"**
5. View response with syntax highlighting

### For MASTER Role Only

#### Approving User Accounts
1. Click **"Account Approval"** on the home screen
2. View list of pending registrations
3. Click on a user to see details
4. **Approve** - Grant access with optional welcome note
5. **Reject** - Deny access with reason
6. User receives notification upon next login

#### Managing Users
1. Click **"Manage Users"** on the home screen
2. View all registered users
3. Edit user roles and status
4. Lock/unlock accounts as needed

#### Request History
1. Click **"Request History"** to view all API change requests
2. Monitor developer activities
3. Track approval/rejection history

---

## 🌐 REST API Documentation

### Overview
The application includes an embedded REST API server that starts automatically with the JavaFX application.

**Base URL:** `http://localhost:8080/api`

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

### Core Endpoints

#### Get All Schedules
```http
GET /api/schedules
GET /api/schedules?type=bus
GET /api/schedules?type=train
```

#### Get Schedule by ID
```http
GET /api/schedules/{id}
```

#### Search Routes
```http
GET /api/routes?start={origin}&destination={dest}
GET /api/routes?start={origin}&destination={dest}&date={yyyy-MM-dd}
GET /api/routes?start={origin}&destination={dest}&type=bus
```

#### Create Schedule (POST)
```http
POST /api/schedules
Content-Type: application/json

{
  "scheduleId": "BUS100",
  "type": "BUS",
  "origin": "Dhaka",
  "destination": "Chittagong",
  "departureDate": "2026-01-15",
  "departureTime": "08:00",
  "arrivalDate": "2026-01-15",
  "arrivalTime": "14:00",
  "fare": 850.0,
  "availableSeats": 40,
  "companyName": "Green Line",
  "busType": "AC"
}
```

#### Update Schedule (PUT)
```http
PUT /api/schedules/{id}
Content-Type: application/json
```

#### Delete Schedule
```http
DELETE /api/schedules/{id}
```

### Testing the API

#### Browser
- Health: `http://localhost:8080/api/health`
- All schedules: `http://localhost:8080/api/schedules`
- Search: `http://localhost:8080/api/routes?start=Dhaka&destination=Chittagong`

#### cURL
```bash
# Get all schedules
curl http://localhost:8080/api/schedules

# Search routes
curl "http://localhost:8080/api/routes?start=Dhaka&destination=Chittagong"

# Create schedule
curl -X POST http://localhost:8080/api/schedules \
  -H "Content-Type: application/json" \
  -d '{"scheduleId":"BUS200","type":"BUS",...}'
```

#### Built-in Testing Tool
1. Login as DEVELOPER or MASTER
2. Click **"API Testing Tool"** on home screen
3. Select endpoint and fill parameters
4. Click **"Send Request"**
5. View formatted response

---

## ⚙️ Configuration

### API Configuration

Edit `src/main/resources/api-config.properties`:

```properties
# Data Source Configuration
use.mock.data=false          # Enable/disable random mock data
use.manual.data=true         # Enable/disable manual JSON schedules

# REST API Server
rest.api.enabled=true        # Enable/disable REST API server
rest.api.port=8080          # API server port

# Bus API Settings (for future real API integration)
bus.api.enabled=true
bus.api.url=http://api.example.com/bus
bus.api.key=your_api_key_here

# Train API Settings (for future real API integration)
train.api.enabled=true
train.api.url=http://api.example.com/train
train.api.key=your_api_key_here

# Connection Settings
api.timeout.seconds=10
```

### Data Source Priority
1. **Manual Data** (`use.manual.data=true`) - Uses `schedules-data.json`
2. **Mock Data** (`use.mock.data=true`) - Generates random realistic schedules
3. **Real APIs** (both false) - External API providers (when configured)

### Manual Schedule Management

Schedules are stored in `schedules-data.json`:

```json
{
  "busSchedules": [
    {
      "scheduleId": "BUS001",
      "origin": "Dhaka",
      "destination": "Chittagong",
      "departureDate": "2026-01-15",
      "departureTime": "08:00",
      "arrivalDate": "2026-01-15",
      "arrivalTime": "14:00",
      "fare": 850.0,
      "availableSeats": 40,
      "companyName": "Green Line",
      "busType": "AC"
    }
  ],
  "trainSchedules": [
    {
      "scheduleId": "TRAIN001",
      "origin": "Dhaka",
      "destination": "Sylhet",
      "departureDate": "2026-01-15",
      "departureTime": "07:30",
      "arrivalDate": "2026-01-15",
      "arrivalTime": "14:00",
      "fare": 450.0,
      "availableSeats": 120,
      "trainName": "Parabat Express",
      "trainClass": "AC_CHAIR"
    }
  ]
}
```

Manage schedules via:
- **UI**: "Manage Schedules" (DEVELOPER/MASTER)
- **REST API**: POST/PUT/DELETE endpoints
- **Direct Edit**: Modify JSON file (requires app restart)

---

## 🏗️ Architecture & Project Structure

### Technology Stack
- **Frontend**: JavaFX 17 (FXML + CSS)
- **Backend**: Java 17
- **Database**: SQLite 3.x
- **API Server**: Javalin 5.x (embedded)
- **Build Tool**: Maven 3.6+
- **Data Format**: JSON for schedules

### Project Structure

```
-Travel-Schedule-Manager/
├── src/main/java/com/travelmanager/
│   ├── App.java                          # Main application entry
│   ├── api/                              # API Layer
│   │   ├── ApiClient.java                # HTTP client
│   │   ├── ApiConfig.java                # Configuration manager
│   │   ├── ScheduleApiProvider.java      # Provider interface
│   │   ├── ScheduleApiManager.java       # Provider coordinator
│   │   ├── MockBusApiProvider.java       # Mock bus data
│   │   ├── MockTrainApiProvider.java     # Mock train data
│   │   ├── ManualBusApiProvider.java     # Manual bus schedules
│   │   ├── ManualTrainApiProvider.java   # Manual train schedules
│   │   ├── ManualScheduleService.java    # Schedule CRUD service
│   │   └── ScheduleDataManager.java      # JSON file handler
│   ├── api/rest/                         # REST API
│   │   ├── RestApiServer.java            # Javalin server
│   │   ├── ScheduleController.java       # Schedule endpoints
│   │   └── RouteController.java          # Route endpoints
│   ├── controller/                       # UI Controllers
│   │   ├── HomeController.java           # Home dashboard
│   │   ├── LoginController.java          # Authentication
│   │   ├── RegisterController.java       # User registration
│   │   ├── CreatePlanController.java     # Plan creation
│   │   ├── EditPlanController.java       # Plan editing
│   │   ├── SavedPlansController.java     # View saved plans
│   │   ├── ManageSchedulesController.java # Schedule management
│   │   ├── ManageRoutesController.java   # Route management
│   │   ├── AccountApprovalController.java # User approval
│   │   ├── ManageUsersController.java    # User management
│   │   ├── ApiTestingToolController.java # API testing
│   │   └── ...
│   ├── model/                            # Data Models
│   │   ├── Schedule.java                 # Schedule entity
│   │   ├── Plan.java                     # Plan entity
│   │   ├── User.java                     # User entity
│   │   ├── Route.java                    # Route entity
│   │   └── ...
│   ├── service/                          # Business Logic
│   │   ├── PlanService.java              # Plan operations
│   │   ├── ScheduleService.java          # Schedule operations
│   │   ├── UserService.java              # User operations
│   │   └── ...
│   ├── database/                         # Database Layer
│   │   ├── DatabaseManager.java          # SQLite connection
│   │   ├── UserRepository.java           # User data access
│   │   ├── PlanRepository.java           # Plan data access
│   │   └── ...
│   ├── util/                             # Utilities
│   │   ├── NavigationManager.java        # Scene navigation
│   │   ├── PasswordHasher.java           # Password security
│   │   ├── ValidationUtil.java           # Input validation
│   │   └── ...
│   ├── domain/                           # Domain Models
│   └── exception/                        # Custom Exceptions
├── src/main/resources/
│   ├── fxml/                             # UI Layouts (20+ files)
│   ├── css/                              # Stylesheets
│   │   ├── styles.css                    # Main styles
│   │   └── dark-theme.css                # Dark theme
│   └── api-config.properties             # API configuration
├── data/
│   ├── bus_schedules.json                # Bus schedule storage
│   ├── train_schedules.json              # Train schedule storage
│   └── travel_plans.db                   # SQLite database
├── START.bat                             # Windows launcher
├── run.bat                               # Alternative launcher
├── run.sh                                # Linux/Mac launcher
└── pom.xml                               # Maven configuration
```

### Database Schema

#### Users Table
```sql
CREATE TABLE users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT NOT NULL UNIQUE,
    email TEXT NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    password_salt TEXT NOT NULL,
    role TEXT NOT NULL,                    -- 'USER', 'DEVELOPER', 'MASTER'
    full_name TEXT NOT NULL,
    created_date TEXT NOT NULL,
    failed_login_attempts INTEGER DEFAULT 0,
    account_locked_until TEXT,             -- ISO 8601 datetime
    last_login TEXT,                       -- ISO 8601 datetime
    status TEXT DEFAULT 'PENDING'          -- 'PENDING', 'APPROVED', 'REJECTED'
);
```

#### Plans Table
```sql
CREATE TABLE plans (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL UNIQUE,
    total_fare REAL NOT NULL,
    created_date TEXT NOT NULL,
    notes TEXT,
    user_id INTEGER,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

#### Schedules Table
```sql
CREATE TABLE schedules (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    plan_id INTEGER NOT NULL,
    schedule_id TEXT NOT NULL,
    type TEXT NOT NULL,                    -- 'BUS' or 'TRAIN'
    origin TEXT NOT NULL,
    destination TEXT NOT NULL,
    departure_date TEXT NOT NULL,
    departure_time TEXT NOT NULL,
    arrival_date TEXT NOT NULL,
    arrival_time TEXT NOT NULL,
    fare REAL NOT NULL,
    company_name TEXT,                     -- For buses
    bus_type TEXT,                         -- For buses
    train_name TEXT,                       -- For trains
    train_class TEXT,                      -- For trains
    FOREIGN KEY (plan_id) REFERENCES plans(id)
);
```

#### Routes Table
```sql
CREATE TABLE routes (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    origin TEXT NOT NULL,
    destination TEXT NOT NULL,
    type TEXT NOT NULL,
    schedule_details TEXT NOT NULL,
    created_by INTEGER,
    created_date TEXT NOT NULL,
    FOREIGN KEY (created_by) REFERENCES users(id)
);
```

---

## 🔒 Security Features

### Password Security
- **PBKDF2** with HMAC SHA-256 algorithm
- **Unique salt** per user (16 bytes)
- **65,536 iterations** for key derivation
- **256-bit** output key length

### Account Protection
- **Failed login tracking** - Records each failed attempt
- **Auto-lockout** - 3 strikes locks account for 30 minutes
- **Lockout timer** - Automatic unlock after timeout
- **Session management** - Track last login timestamp

### Role-Based Access Control

| Feature | USER | DEVELOPER | MASTER |
|---------|------|-----------|--------|
| View Plans | ✅ | ✅ | ✅ |
| Create Plans | ✅ | ✅ | ✅ |
| Edit Plans | ❌ | ✅ | ✅ |
| Delete Plans | ❌ | ✅ | ✅ |
| Manage Schedules | ❌ | ✅ | ✅ |
| Manage Routes | ❌ | ✅ | ✅ |
| API Testing Tool | ❌ | ✅ | ✅ |
| Approve Accounts | ❌ | ❌ | ✅ |
| Manage Users | ❌ | ❌ | ✅ |
| Request History | ❌ | ❌ | ✅ |

---

## 🛠️ Building from Source

### Clone Repository
```bash
git clone <repository-url>
cd -Travel-Schedule-Manager
```

### Compile
```bash
mvn clean compile
```

### Run Tests
```bash
mvn test
```

### Package (Create JAR)
```bash
mvn clean package
```

### Run Application
```bash
mvn javafx:run
```

**Note:** Always use `mvn javafx:run` or `START.bat`. Do NOT run the JAR directly due to JavaFX module requirements.

---

## 🐛 Troubleshooting

### Error: "Module javafx.controls not found"
**Solution:** Always use `mvn javafx:run` or `START.bat`. Do NOT run the JAR directly.

### Error: "Java is not installed"
**Solution:**
1. Download Java 17+ from [Adoptium](https://adoptium.net/)
2. Install and add to PATH
3. Verify: `java -version`

### Error: "Maven not recognized"
**Solution:**
1. Download Maven from [Apache Maven](https://maven.apache.org/download.cgi)
2. Extract and add bin folder to PATH
3. Verify: `mvn -version`

### Application won't start
**Solution:**
1. Ensure Java 17+ is installed: `java -version`
2. Ensure Maven is installed: `mvn -version`
3. Try rebuilding: `mvn clean compile`
4. Run: `mvn javafx:run`

### Database locked error
**Solution:**
1. Close all instances of the application
2. Delete `data/travel_plans.db-journal` if it exists
3. Restart the application

### REST API not starting
**Solution:**
1. Check if port 8080 is already in use
2. Edit `api-config.properties` to change port
3. Ensure `rest.api.enabled=true` in config

### Schedules not loading
**Solution:**
1. Check `api-config.properties` settings
2. Verify `schedules-data.json` exists and is valid JSON
3. Check console for error messages
4. Try toggling between mock and manual data

---

## 🧪 Testing

### Manual Testing
1. **Registration Flow**
   - Register with different roles
   - Test validation (duplicate username, weak password)
   - Verify approval workflow

2. **Authentication**
   - Login with correct credentials
   - Test failed login attempts and lockout
   - Verify session persistence

3. **Plan Creation**
   - Create single-leg journey
   - Create multi-leg journey with connections
   - Test validation warnings

4. **API Testing**
   - Use built-in API testing tool
   - Test all CRUD operations
   - Verify response formats

### Automated Testing
```bash
mvn test
```

---

## 📚 Supported Cities

The application supports autocomplete for these 19 Bangladesh cities:

**Major Cities:**
1. Dhaka
2. Chittagong
3. Sylhet
4. Rajshahi
5. Khulna
6. Barisal
7. Rangpur
8. Mymensingh

**Other Cities:**
9. Comilla
10. Narayanganj
11. Cox's Bazar
12. Jessore
13. Bogra
14. Dinajpur
15. Gazipur
16. Tangail
17. Pabna
18. Faridpur
19. Manikganj

*Note: Additional cities can be added through the Route Management system*

---

## 🚀 Future Enhancements

- [ ] Real-time GPS tracking integration
- [ ] Mobile app (Android/iOS)
- [ ] Payment gateway integration
- [ ] Seat selection and booking
- [ ] Email/SMS notifications
- [ ] Multi-language support
- [ ] Advanced analytics dashboard
- [ ] Route optimization algorithms
- [ ] Weather integration for travel planning
- [ ] Social sharing features

---

## 📄 License

This project is licensed under the MIT License.

---

## 👨‍💻 Author

Developed as part of a smart travel management solution for Bangladesh's transportation network.

---

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

---

## 📞 Support

For issues or questions:
1. Check the [Troubleshooting](#-troubleshooting) section
2. Review the [User Guide](#-user-guide)
3. Test using the built-in API testing tool
4. Check console logs for detailed error messages

---

**Enjoy your travel planning! 🚆🚌**
