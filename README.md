# Travel Schedule Manager 

A smart multi-modal travel schedule management application built with JavaFX.

## Features 

-  **Create Travel Plans** - Build multi-leg journeys with ease
-  **Smart Search** - Autocomplete for 14 Bangladesh cities
-  **Filter Options** - View All, Bus Only, or Train Only schedules
-  **Save & Manage** - SQLite database for persistent storage
-  **Edit Plans** - Add or remove schedules from existing plans
-  **View Details** - See complete itinerary with fare breakdown
-  **Calculate Totals** - Automatic calculation of total fare and duration

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
    controller/                   # UI Controllers
       HomeController.java
       CreatePlanController.java
       SummarizePlanController.java
       EditPlanController.java
       SavedPlansController.java
    model/                        # Data Models
    service/                      # Business Logic
    database/                     # SQLite Database
    util/                        # Utilities

 src/main/resources/
    fxml/                        # UI Layouts
    css/                         # Stylesheets

 START.bat                        # Windows Quick Launcher
 run.bat                          # Alternative launcher
 pom.xml                          # Maven configuration
 README.md                        # This file
```

---

##  Database

- **Type**: SQLite
- **Location**: data/travel_plans.db
- **Auto-created**: On first save
- **Tables**: 
  - plans - Travel plan metadata
  - schedules - Individual schedule legs

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

### Creating a Plan
1. Click **\"Create Plan\"** on home screen
2. Enter start and destination (autocomplete available)
3. Select date and transport type
4. Click **\"Search Schedules\"**
5. Select schedule and click **\"Add to Plan\"**
6. Repeat for multi-leg journeys
7. Click **\"Summarize & Save Plan\"**
8. Enter plan name and save

### Editing a Plan
1. Go to **\"Saved Plans\"**
2. Select a plan from the list
3. Click **\"Edit Plan\"**
4. Remove unwanted schedules OR add new ones
5. Click **\"Save Changes\"**

### Viewing Plans
1. Go to **\"Saved Plans\"**
2. Select a plan
3. Click **\"View Details\"**

---

##  License

This project is licensed under the MIT License.

---

##  Author

Developed as part of a smart travel management solution.

---

##  Enjoy Your Travel Planning!

For any issues or questions, please refer to the troubleshooting section above.
