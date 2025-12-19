# Authentication System Documentation
## Travel Schedule Manager - Security Architecture

---

## 📋 Table of Contents
1. [Database Schema Design](#database-schema-design)
2. [Security Features](#security-features)
3. [Login Authentication Logic](#login-authentication-logic)
4. [Role-Based Access Control](#role-based-access-control)
5. [JavaFX UI Flow](#javafx-ui-flow)
6. [Security Best Practices](#security-best-practices)

---

## 🗄️ Database Schema Design

### Users Table
```sql
CREATE TABLE users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT NOT NULL UNIQUE,
    email TEXT NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    password_salt TEXT NOT NULL,
    role TEXT NOT NULL,                    -- 'USER' or 'DEVELOPER'
    full_name TEXT NOT NULL,
    created_date TEXT NOT NULL,
    failed_login_attempts INTEGER DEFAULT 0,
    account_locked_until TEXT,             -- ISO 8601 datetime
    last_login TEXT                        -- ISO 8601 datetime
);
```

### Plans Table
```sql
CREATE TABLE plans (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL UNIQUE,
    total_fare REAL NOT NULL,
    created_date TEXT NOT NULL,
    notes TEXT
);
```

### Schedules Table
```sql
CREATE TABLE schedules (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    plan_id INTEGER NOT NULL,
    schedule_id TEXT NOT NULL,
    type TEXT NOT NULL,
    origin TEXT NOT NULL,
    destination TEXT NOT NULL,
    departure_time TEXT NOT NULL,
    arrival_time TEXT NOT NULL,
    fare REAL NOT NULL,
    available_seats INTEGER NOT NULL,
    company_name TEXT,
    bus_type TEXT,
    train_name TEXT,
    train_number TEXT,
    seat_class TEXT,
    leg_order INTEGER NOT NULL,
    FOREIGN KEY (plan_id) REFERENCES plans(id) ON DELETE CASCADE
);
```

---

## 🔒 Security Features

### 1. Password Security

#### **PBKDF2 with Salt**
- **Algorithm**: PBKDF2WithHmacSHA1
- **Iterations**: 65,536 (OWASP recommended)
- **Key Length**: 128 bits
- **Salt**: 16-byte random salt per user
- **Storage**: Base64-encoded hash and salt

```java
// Password hashing implementation
KeySpec spec = new PBEKeySpec(password.toCharArray(), saltBytes, 65536, 128);
SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
byte[] hash = factory.generateSecret(spec).getEncoded();
```

#### **Why PBKDF2 over SHA-256?**
| Feature | SHA-256 | PBKDF2 |
|---------|---------|---------|
| Brute Force Protection | ❌ Fast (vulnerable) | ✅ Slow (65k iterations) |
| Salt Support | Manual | ✅ Built-in |
| Rainbow Table Protection | ❌ Without salt | ✅ Each password unique |
| Industry Standard | Legacy | ✅ OWASP recommended |

### 2. Account Lockout Protection

**Brute Force Prevention:**
- **Failed Attempts Threshold**: 5 attempts
- **Lockout Duration**: 15 minutes
- **Auto-unlock**: After lockout period expires
- **Counter Reset**: After successful login

```java
// Account lockout logic
if (failedAttempts >= 5) {
    LocalDateTime lockUntil = LocalDateTime.now().plusMinutes(15);
    // Lock account until lockUntil
}
```

### 3. Session Management

**AuthenticationManager (Singleton)**
- Stores current user session
- Provides role-based permission checks
- No session timeout (can be added - see recommendations)

```java
AuthenticationManager.getInstance().login(user);
boolean canModify = AuthenticationManager.getInstance().canModifyData();
```

---

## 🔑 Login Authentication Logic

### Authentication Flow

```
┌─────────────────┐
│  Login Screen   │
└────────┬────────┘
         │
         ▼
┌─────────────────────────┐
│ Input Validation        │
│ - Username/Email filled │
│ - Password filled       │
└────────┬────────────────┘
         │
         ▼
┌─────────────────────────┐
│ Check Account Lock      │
│ - Is locked?            │
│ - Lock expired?         │
└────────┬────────────────┘
         │
         ▼
┌─────────────────────────┐
│ Retrieve User from DB   │
│ - Get password hash     │
│ - Get password salt     │
└────────┬────────────────┘
         │
         ▼
┌─────────────────────────┐
│ Hash Input Password     │
│ - Use stored salt       │
│ - Apply PBKDF2          │
└────────┬────────────────┘
         │
         ▼
┌─────────────────────────┐
│ Compare Hashes          │
└────────┬────────────────┘
         │
    ┌────┴────┐
    │         │
  Match    No Match
    │         │
    ▼         ▼
┌───────┐  ┌──────────────────┐
│Success│  │Increment Attempts│
│       │  │Check Lock         │
└───┬───┘  └────────┬─────────┘
    │               │
    ▼               ▼
┌───────┐      ┌────────┐
│ Reset │      │ Lock?  │
│Attempts│     └────────┘
│Update │
│Login  │
└───┬───┘
    │
    ▼
┌─────────────────┐
│ Start Session   │
│ Navigate to Home│
└─────────────────┘
```

### Code Implementation

**LoginController.java**
```java
@FXML
private void handleLogin() {
    String usernameOrEmail = usernameField.getText().trim();
    String password = passwordField.getText();
    
    // Validation
    if (usernameOrEmail.isEmpty() || password.isEmpty()) {
        showError("Please enter both username and password");
        return;
    }
    
    try {
        // Authenticate
        User user = DatabaseManager.getInstance()
            .authenticateUser(usernameOrEmail, password);
        
        if (user != null) {
            // Success - start session
            AuthenticationManager.getInstance().login(user);
            NavigationManager.navigateTo("home");
        } else {
            // Failed
            showError("Invalid username or password");
            passwordField.clear();
        }
    } catch (SQLException e) {
        // Handle account locked or DB error
        showError("Database error: " + e.getMessage());
    }
}
```

---

## 👥 Role-Based Access Control (RBAC)

### User Roles

| Role | Code | Permissions |
|------|------|-------------|
| Normal User | `USER` | ✅ View routes<br>✅ Create plans<br>✅ View saved plans<br>❌ Edit plans<br>❌ Delete plans |
| Developer | `DEVELOPER` | ✅ All USER permissions<br>✅ Edit plans<br>✅ Delete plans<br>✅ Full database access |

### Permission Checking

**User Model**
```java
public class User {
    public enum Role { USER, DEVELOPER }
    
    public boolean isDeveloper() {
        return role == Role.DEVELOPER;
    }
    
    public boolean canModifyData() {
        return role == Role.DEVELOPER;
    }
    
    public boolean canDeleteData() {
        return role == Role.DEVELOPER;
    }
}
```

**Controller Implementation**
```java
@FXML
public void initialize() {
    AuthenticationManager auth = AuthenticationManager.getInstance();
    
    // Hide buttons for normal users
    if (!auth.canModifyData()) {
        editButton.setVisible(false);
        editButton.setManaged(false);
    }
    
    if (!auth.canDeleteData()) {
        deleteButton.setVisible(false);
        deleteButton.setManaged(false);
    }
}

@FXML
private void handleDelete() {
    // Runtime permission check
    if (!AuthenticationManager.getInstance().canDeleteData()) {
        showAlert("You don't have permission to delete plans.");
        return;
    }
    // Proceed with delete
}
```

### UI Separation by Role

**Normal User UI:**
- Home page with Create/View/Auto Route
- View saved plans (read-only)
- Help section
- Logout button

**Developer UI:**
- Everything from Normal User
- Edit button on saved plans
- Delete button on saved plans
- Full CRUD operations

---

## 🖥️ JavaFX UI Flow

### Application Flow Diagram

```
┌──────────────┐
│   App.java   │ (Entry Point)
│  start()     │
└──────┬───────┘
       │
       ▼
┌──────────────────┐
│  login.fxml      │
│  LoginController │
└──────┬───────────┘
       │
       │ (Authentication)
       │
       ▼
┌──────────────────┐
│  home.fxml       │
│  HomeController  │
│  - Welcome User  │
│  - Show Role     │
│  - Logout Button │
└──────┬───────────┘
       │
   ┌───┴───┬────────┬──────────┐
   │       │        │          │
   ▼       ▼        ▼          ▼
┌────┐ ┌─────┐ ┌────────┐ ┌──────┐
│Plan│ │Saved│ │Auto    │ │Help  │
│    │ │Plans│ │Route   │ │      │
└────┘ └──┬──┘ └────────┘ └──────┘
          │
          │ (If Developer)
          │
      ┌───┴───┐
      │       │
      ▼       ▼
   ┌────┐ ┌──────┐
   │Edit│ │Delete│
   └────┘ └──────┘
```

### Navigation Implementation

**NavigationManager.java**
```java
public class NavigationManager {
    private static Stage primaryStage;
    
    public static void navigateTo(String fxmlFile) {
        FXMLLoader loader = new FXMLLoader(
            NavigationManager.class.getResource("/fxml/" + fxmlFile + ".fxml")
        );
        Parent root = loader.load();
        primaryStage.getScene().setRoot(root);
    }
    
    public static <T> T navigateToWithController(String fxmlFile, Class<T> controllerClass) {
        FXMLLoader loader = new FXMLLoader(
            NavigationManager.class.getResource("/fxml/" + fxmlFile + ".fxml")
        );
        Parent root = loader.load();
        T controller = loader.getController();
        primaryStage.getScene().setRoot(root);
        return controller;
    }
}
```

---

## 🛡️ Security Best Practices Implementation

### ✅ Implemented Security Measures

1. **✅ Password Hashing with Salt**
   - PBKDF2 with 65,536 iterations
   - Random 16-byte salt per user
   - Base64 encoding for storage

2. **✅ Brute Force Protection**
   - 5 failed attempts → 15-minute lockout
   - Automatic unlock after timeout
   - Counter reset on successful login

3. **✅ Secure Session Management**
   - Singleton AuthenticationManager
   - In-memory session storage
   - No sensitive data in session

4. **✅ Input Validation**
   - Empty field checks
   - SQL injection prevention (PreparedStatements)
   - Parameterized queries only

5. **✅ Role-Based Access Control**
   - UI-level hiding of restricted buttons
   - Runtime permission checks
   - Dual-layer security (UI + backend)

6. **✅ Database Security**
   - Foreign key constraints
   - Unique constraints on username/email
   - Proper indexing for performance

### 🔧 Additional Recommendations

#### 1. Session Timeout (Not Yet Implemented)
```java
public class AuthenticationManager {
    private LocalDateTime lastActivityTime;
    private static final int SESSION_TIMEOUT_MINUTES = 30;
    
    public boolean isSessionValid() {
        if (lastActivityTime == null) return false;
        return LocalDateTime.now()
            .isBefore(lastActivityTime.plusMinutes(SESSION_TIMEOUT_MINUTES));
    }
    
    public void updateActivity() {
        lastActivityTime = LocalDateTime.now();
    }
}
```

#### 2. Password Strength Validation
```java
public boolean isPasswordStrong(String password) {
    return password.length() >= 8 &&
           password.matches(".*[A-Z].*") &&  // Uppercase
           password.matches(".*[a-z].*") &&  // Lowercase
           password.matches(".*\\d.*") &&    // Digit
           password.matches(".*[@#$%^&+=].*"); // Special char
}
```

#### 3. Logging (Audit Trail)
```java
public void logSecurityEvent(String event, String username) {
    String log = String.format("[%s] %s - User: %s",
        LocalDateTime.now(), event, username);
    // Write to security log file
}
```

#### 4. Database Encryption
```java
// Use SQLCipher for database encryption
String DB_URL = "jdbc:sqlite:data/travel_plans.db?cipher=aes256";
Properties props = new Properties();
props.setProperty("password", "encryption_key");
Connection conn = DriverManager.getConnection(DB_URL, props);
```

#### 5. Code Obfuscation
- Use ProGuard for bytecode obfuscation
- Protects against decompilation
- Configuration in `proguard-rules.pro`

---

## 🔐 Default Credentials

### For Testing/Development Only

| Role | Username | Password | Email |
|------|----------|----------|-------|
| Normal User | `user` | `user123` | user@travelmanager.com |
| Developer | `developer` | `dev123` | developer@travelmanager.com |

**⚠️ WARNING**: Change these credentials in production!

---

## 📊 Security Comparison

### Before vs After Enhancement

| Feature | Before | After |
|---------|--------|-------|
| Password Hashing | SHA-256 (fast, vulnerable) | PBKDF2 (65k iterations) |
| Salt | ❌ None | ✅ 16-byte per user |
| Brute Force Protection | ❌ None | ✅ Account lockout |
| Failed Attempt Tracking | ❌ None | ✅ Database tracked |
| Session Management | ✅ Basic | ✅ Role-based |
| Access Control | ❌ None | ✅ UI + Runtime |
| Backward Compatibility | N/A | ✅ Legacy support |

---

## 🚀 Future Enhancements

1. **Two-Factor Authentication (2FA)**
2. **Email verification**
3. **Password reset functionality**
4. **Session timeout**
5. **Security audit logging**
6. **Database encryption**
7. **Password strength meter**
8. **CAPTCHA after failed attempts**

---

## 📝 Notes

- All passwords are stored hashed with unique salts
- Database uses SQLite (file: `data/travel_plans.db`)
- Session is in-memory (lost on app restart)
- Role checking happens both UI and backend
- Backward compatible with SHA-256 (for migration)

---

**Last Updated**: December 20, 2025
**Version**: 2.0 - Enhanced Security
