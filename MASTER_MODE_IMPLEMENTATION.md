# Master Mode Dashboard - Complete Implementation Guide

## 🎯 Overview

The Master Mode Dashboard provides two powerful management cards exclusively for users with MASTER role:

1. **Edit API Card** - Manage routes and API configurations (CRUD operations)
2. **Pending Application Card** - Review and approve/reject new account registrations

## 🏗️ System Architecture

### Role Hierarchy
```
MASTER (Highest)
  ├─ All DEVELOPER permissions
  ├─ Routes Management (Edit API)
  └─ Account Approval
  
DEVELOPER
  ├─ Create Plans
  ├─ Edit Plans
  └─ Delete Plans
  
USER (Basic)
  ├─ View Plans
  └─ Create Plans (limited)
```

### Authentication Flow
```
Login Screen
    ↓
[Verify Credentials from SQLite]
    ↓
[Check Account Status: APPROVED only]
    ↓
[Load UI based on Role]
    ├─→ USER → Home (3 cards)
    ├─→ DEVELOPER → Home (3 cards)
    └─→ MASTER → Home (5 cards: 3 normal + 2 master-only)
```

## 📊 Database Schema

### 1. Users Table (Authentication & Authorization)
```sql
CREATE TABLE users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT NOT NULL UNIQUE,
    email TEXT NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,           -- PBKDF2 hashed
    password_salt TEXT NOT NULL,           -- 16-byte random salt
    role TEXT NOT NULL,                    -- USER / DEVELOPER / MASTER
    full_name TEXT NOT NULL,
    created_date TEXT NOT NULL,
    failed_login_attempts INTEGER DEFAULT 0,
    account_locked_until TEXT,             -- Lockout timestamp
    last_login TEXT
)
```

**Security Features:**
- Passwords hashed with PBKDF2 (65,536 iterations)
- Unique salt per user (16 bytes, SecureRandom)
- Account lockout after 5 failed attempts (15 minutes)
- Base64 encoding for hash/salt storage

### 2. Pending Users Table (Account Approval Workflow)
```sql
CREATE TABLE pending_users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT NOT NULL UNIQUE,
    email TEXT NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    password_salt TEXT NOT NULL,
    role TEXT NOT NULL,                    -- USER / DEVELOPER
    full_name TEXT NOT NULL,
    created_date TEXT NOT NULL,
    status TEXT DEFAULT 'PENDING'          -- PENDING / APPROVED / REJECTED
)
```

**Status Flow:**
- PENDING → User registered, awaiting master approval
- APPROVED → Moved to `users` table, can login
- REJECTED → Deleted from `pending_users`, blocked

### 3. Routes Table (Edit API Feature)
```sql
CREATE TABLE routes (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    route_name TEXT NOT NULL,              -- e.g., "Express Morning Service"
    origin TEXT NOT NULL,                  -- Starting location
    destination TEXT NOT NULL,             -- End location
    transport_type TEXT NOT NULL,          -- BUS / TRAIN / FLIGHT / FERRY
    status TEXT DEFAULT 'ACTIVE',          -- ACTIVE / INACTIVE / MAINTENANCE
    duration_minutes INTEGER,              -- Journey duration
    price REAL,                            -- Ticket price
    schedule_time TEXT,                    -- e.g., "08:00 AM"
    metadata TEXT,                         -- Additional notes/JSON data
    created_date TEXT NOT NULL,
    modified_date TEXT                     -- Last update timestamp
)
```

**CRUD Operations:**
- CREATE: Add new routes via form
- READ: Display all routes in TableView
- UPDATE: Edit existing route data
- DELETE: Remove routes (with confirmation)

## 🎨 UI Components

### Master Dashboard (home.fxml)
**For MASTER users only, shows 5 cards:**

1. **Create Plan** (Common)
2. **Saved Plans** (Common)
3. **Automatic Route** (Common)
4. **Edit API** (Master-only) 🛠️
   - Icon: 🛠️
   - Action: Navigate to Manage Routes screen
   - Button: "Manage Routes"
5. **Pending Accounts** (Master-only) 👥
   - Icon: 👥
   - Action: Navigate to Account Approval screen
   - Button: "Review Accounts"

### Edit API Screen (manage-routes.fxml)

**Layout:**
- Top Bar: Back button, Title, Add Route button, Refresh button
- Center: TableView with routes
- Right Panel: Add/Edit form (slide-in)

**TableView Columns:**
| Column | Width | Data |
|--------|-------|------|
| ID | 50px | Route ID |
| Route Name | 150px | Display name |
| Origin | 120px | Starting point |
| Destination | 120px | End point |
| Type | 80px | Transport type |
| Duration | 100px | Minutes |
| Price | 80px | Cost |
| Status | 80px | ACTIVE/INACTIVE |
| Actions | 218px | Edit/Delete buttons |

**Form Fields:**
- Route Name * (required)
- Origin * (required)
- Destination * (required)
- Transport Type * (ComboBox: BUS/TRAIN/FLIGHT/FERRY)
- Duration (minutes) * (required, positive integer)
- Price * (required, non-negative)
- Schedule Time (optional, e.g., "08:00 AM")
- Status (ComboBox: ACTIVE/INACTIVE/MAINTENANCE)
- Metadata / Notes (TextArea for additional info)

**Actions:**
- **Add Route:** Opens form in "Add" mode
- **Edit:** Opens form with pre-filled data
- **Delete:** Shows confirmation dialog, removes from DB
- **Save:** Validates and saves to database
- **Cancel:** Closes form without saving

### Account Approval Screen (account-approval.fxml)

**Layout:**
- Top Bar: Back button, Title, Refresh button
- Center: TableView with pending accounts
- Actions: Approve/Reject buttons per row

**TableView Columns:**
| Column | Data |
|--------|------|
| ID | Pending user ID |
| Username | Requested username |
| Full Name | User's name |
| Email | Contact email |
| Role | USER or DEVELOPER |
| Created Date | Registration timestamp |
| Actions | Approve ✓ / Reject ✗ |

**Workflow:**
1. User registers via registration form
2. Account stored in `pending_users` table with status=PENDING
3. Master logs in, sees "Pending Accounts" card
4. Master clicks "Review Accounts"
5. Master clicks "Approve" or "Reject"
6. Confirmation dialog shown
7. Approved: User moved to `users` table, can login
8. Rejected: User deleted from `pending_users`, cannot login

## 🔧 Implementation Details

### File Structure

```
src/main/
├── java/com/travelmanager/
│   ├── controller/
│   │   ├── HomeController.java              [Updated: Added handleManageRoutes()]
│   │   ├── ManageRoutesController.java      [NEW: Routes CRUD logic]
│   │   └── AccountApprovalController.java   [Existing: Approval logic]
│   ├── database/
│   │   └── DatabaseManager.java             [Updated: Added routes CRUD methods]
│   └── model/
│       └── User.java                        [Existing: Role enum with MASTER]
└── resources/fxml/
    ├── home.fxml                            [Updated: Added Edit API card]
    ├── manage-routes.fxml                   [NEW: Routes management UI]
    └── account-approval.fxml                [Existing: Approval UI]
```

### Key Methods in DatabaseManager.java

#### Routes Management
```java
// CREATE
public boolean createRoute(String routeName, String origin, String destination, 
                          String transportType, int durationMinutes, double price,
                          String scheduleTime, String metadata)

// READ
public List<RouteData> getAllRoutes() throws SQLException

// UPDATE
public boolean updateRoute(int routeId, String routeName, String origin, String destination,
                          String transportType, int durationMinutes, double price,
                          String scheduleTime, String metadata, String status)

// DELETE
public boolean deleteRoute(int routeId)
```

#### Account Approval (Already Implemented)
```java
public boolean createPendingUser(String username, String email, String password, 
                                 User.Role role, String fullName)
public List<PendingUser> getPendingUsers() throws SQLException
public boolean approvePendingUser(int pendingUserId) throws SQLException
public boolean rejectPendingUser(int pendingUserId) throws SQLException
```

### Role-Based UI Loading Logic

**HomeController.java:**
```java
@FXML
public void initialize() {
    AuthenticationManager auth = AuthenticationManager.getInstance();
    if (auth.isLoggedIn()) {
        // Display user info
        welcomeLabel.setText("Welcome, " + auth.getCurrentUserFullName() + "!");
        
        // Set role label and show/hide master section
        if (auth.getCurrentUser().isMaster()) {
            roleLabel.setText("Role: Master");
            // Show Edit API + Pending Accounts cards
            masterSection.setVisible(true);
            masterSection.setManaged(true);
        } else if (auth.isDeveloper()) {
            roleLabel.setText("Role: Developer");
        } else {
            roleLabel.setText("Role: User");
        }
    }
}
```

**LoginController.java:**
```java
@FXML
private void handleLogin() {
    String identifier = usernameField.getText().trim();
    String password = passwordField.getText();
    
    if (identifier.isEmpty() || password.isEmpty()) {
        showError("Please enter username and password");
        return;
    }
    
    try {
        // Authenticate with database
        User user = databaseManager.authenticate(identifier, password);
        
        if (user != null) {
            // Check if account is locked
            if (databaseManager.isAccountLocked(identifier)) {
                showError("Account locked. Try again in 15 minutes.");
                return;
            }
            
            // Login successful - set session
            AuthenticationManager.getInstance().login(user);
            
            // Navigate to home (UI loaded based on role)
            NavigationManager.navigateTo("home");
        } else {
            // Login failed
            showError("Invalid username or password");
        }
    } catch (SQLException e) {
        showError("Database error: " + e.getMessage());
    }
}
```

## 🔐 Security Implementation

### Password Hashing (PBKDF2)
```java
private String hashPasswordWithSalt(String password, String saltString) {
    try {
        byte[] salt = Base64.getDecoder().decode(saltString);
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 256);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] hash = factory.generateSecret(spec).getEncoded();
        return Base64.getEncoder().encodeToString(hash);
    } catch (Exception e) {
        throw new RuntimeException("Error hashing password", e);
    }
}

private String generateSalt() {
    SecureRandom random = new SecureRandom();
    byte[] salt = new byte[16];
    random.nextBytes(salt);
    return Base64.getEncoder().encodeToString(salt);
}
```

### Account Lockout Mechanism
```java
public boolean isAccountLocked(String username) throws SQLException {
    String query = "SELECT account_locked_until FROM users WHERE username = ?";
    
    try (Connection conn = DriverManager.getConnection(DB_URL);
         PreparedStatement pstmt = conn.prepareStatement(query)) {
        
        pstmt.setString(1, username);
        ResultSet rs = pstmt.executeQuery();
        
        if (rs.next()) {
            String lockedUntil = rs.getString("account_locked_until");
            if (lockedUntil != null) {
                LocalDateTime lockTime = LocalDateTime.parse(lockedUntil);
                return LocalDateTime.now().isBefore(lockTime);
            }
        }
    }
    return false;
}
```

### Role-Based Access Control
```java
// User.java
public boolean canModifyData() {
    return role == Role.DEVELOPER || role == Role.MASTER;
}

public boolean canDeleteData() {
    return role == Role.DEVELOPER || role == Role.MASTER;
}

public boolean canApproveAccounts() {
    return role == Role.MASTER;
}

public boolean canManageRoutes() {
    return role == Role.MASTER;
}
```

### Prevent Direct Database Access
- All database operations go through `DatabaseManager` singleton
- PreparedStatements prevent SQL injection
- Role checks enforced at UI level (button visibility) and runtime (permission methods)
- No direct file system access to SQLite database from user UI

## 📝 Usage Guide

### For Master Users

#### Managing Routes (Edit API)
1. **Login** as master (siyam2207031 / 2207031)
2. Click **"Manage Routes"** on Edit API card
3. View existing routes in table

**To Add Route:**
1. Click **"➕ Add Route"** button
2. Fill in form fields (all * fields required)
3. Click **"Save Route"**
4. Route appears in table

**To Edit Route:**
1. Click **"✏ Edit"** button on route row
2. Modify fields in form
3. Click **"Update Route"**
4. Changes saved to database

**To Delete Route:**
1. Click **"🗑 Delete"** button on route row
2. Confirm deletion in dialog
3. Route removed from database

#### Approving Accounts (Pending Application)
1. **Login** as master
2. Click **"Review Accounts"** on Pending Accounts card
3. View pending registrations in table

**To Approve:**
1. Click **"✓ Approve"** button
2. Confirm in dialog
3. User moved to `users` table
4. User can now login

**To Reject:**
1. Click **"✗ Reject"** button
2. Confirm in dialog (shows warning)
3. User deleted from pending list
4. User must re-register

### For Regular Users

#### Registering Account
1. On login screen, click **"Create Account"**
2. Fill registration form:
   - Full Name
   - Username (min 3 chars)
   - Email (valid format)
   - Password (min 6 chars)
   - Confirm Password
   - Account Type (USER or DEVELOPER)
3. Click **"Create Account"**
4. Success message: "Account created successfully! Awaiting master approval."
5. **Wait for master approval** before attempting login

## 🧪 Testing Checklist

### Edit API Feature
- [ ] Master can access "Manage Routes" card
- [ ] Non-master users cannot see Edit API card
- [ ] Add route form validates all required fields
- [ ] Duration must be positive integer
- [ ] Price must be non-negative number
- [ ] Route saves to database successfully
- [ ] Edit route pre-fills form with existing data
- [ ] Update route saves changes correctly
- [ ] Delete route shows confirmation dialog
- [ ] Delete route removes from database
- [ ] Refresh button reloads routes list
- [ ] Back button returns to home screen
- [ ] Empty state message shows when no routes

### Pending Application Feature
- [ ] Master can access "Review Accounts" card
- [ ] Non-master users cannot see Pending Accounts card
- [ ] Pending users appear in table
- [ ] Approve moves user to users table
- [ ] Approved user can login
- [ ] Reject deletes user from pending_users
- [ ] Rejected user cannot login
- [ ] Confirmation dialogs appear for approve/reject
- [ ] Table refreshes after approve/reject
- [ ] Empty state message shows when no pending accounts

### Security
- [ ] Passwords stored as hashes in database
- [ ] Each user has unique salt
- [ ] Account lockout after 5 failed logins
- [ ] Locked accounts show proper error message
- [ ] Role checks prevent unauthorized access
- [ ] SQL injection protection (PreparedStatements)
- [ ] Session management works correctly

## 🚀 Running the Application

1. **Clean and Compile:**
   ```bash
   mvn clean compile
   ```

2. **Run Application:**
   ```bash
   mvn javafx:run
   ```

3. **Login as Master:**
   - Username: `siyam2207031`
   - Password: `2207031`

4. **You'll see 5 cards:**
   - Create Plan
   - Saved Plans
   - Automatic Route
   - **Edit API** (Manage Routes) 🛠️
   - **Pending Accounts** (Review Accounts) 👥

## 📦 Deliverables Summary

### ✅ Completed Features

1. **Edit API Card (Routes Management)**
   - ✅ Routes table in SQLite
   - ✅ CRUD operations implemented
   - ✅ Master-only access control
   - ✅ Form validation
   - ✅ Add/Edit/Delete functionality
   - ✅ UI with TableView and slide-in form

2. **Pending Application Card (Account Approval)**
   - ✅ Pending users table
   - ✅ Approval workflow (PENDING → APPROVED/REJECTED)
   - ✅ Master-only access control
   - ✅ Approve/Reject actions
   - ✅ Confirmation dialogs
   - ✅ Table auto-refresh

3. **Security Features**
   - ✅ PBKDF2 password hashing
   - ✅ Unique salt per user
   - ✅ Account lockout (5 attempts → 15 min)
   - ✅ Role-based UI visibility
   - ✅ Permission enforcement
   - ✅ PreparedStatements (SQL injection prevention)

4. **Role-Based UI**
   - ✅ Single login screen for all roles
   - ✅ Status check (only APPROVED users allowed)
   - ✅ Dynamic UI loading based on role
   - ✅ Master features inaccessible to normal users
   - ✅ Session management with AuthenticationManager

---

**Implementation Status:** ✅ COMPLETE
**Build Status:** ✅ SUCCESS
**Testing Status:** ✅ READY FOR TESTING

All requirements from the specification have been implemented successfully!
