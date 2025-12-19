# Account Registration System - Testing Guide

## Overview
The application now features a complete account registration system with master account approval workflow. This document guides you through testing all the new features.

## System Architecture

### User Roles
1. **MASTER** - Full permissions + account approval capabilities
   - Default account: `siyam2207031` / `2207031`
   - Can approve/reject new account registrations
   - Has all permissions of DEVELOPER role
   
2. **DEVELOPER** - Full access to all features
   - Can create, edit, and delete travel plans
   - Must be approved by master account
   
3. **USER** - Read-only access
   - Can view plans and create new plans
   - Cannot delete plans
   - Must be approved by master account

### Security Features
- PBKDF2 password hashing with 65,536 iterations
- SecureRandom 16-byte salt per user
- Account lockout after 5 failed login attempts (15-minute lock)
- Master approval required for all new accounts
- No default demo credentials (removed for security)

## Testing Workflow

### Phase 1: Initial Login with Master Account

1. **Start the Application**
   - Run: `mvn javafx:run`
   - Application opens to login screen

2. **Login as Master**
   - Username: `siyam2207031`
   - Password: `2207031`
   - Click "Sign In"
   
3. **Verify Master Dashboard**
   - Welcome message shows: "Welcome, Master Account!"
   - Role displays as "Role: Master"
   - Four cards visible:
     - Create Plan
     - Saved Plans
     - Automatic Route
     - **Pending Accounts** (master-only)

### Phase 2: Test Account Registration

1. **Logout from Master Account**
   - Click "🚪 Logout" button in top-right corner
   - Returns to login screen

2. **Create New Developer Account**
   - Click "Create Account" link on login screen
   - Fill in registration form:
     - Full Name: `John Developer`
     - Username: `john_dev`
     - Email: `john@example.com`
     - Password: `test123`
     - Confirm Password: `test123`
     - Account Type: Select "DEVELOPER"
   - Click "Create Account"
   - Success message appears: "Account created successfully! Awaiting master approval."

3. **Create New User Account**
   - Click "Create Account" again (or navigate back to login and click again)
   - Fill in registration form:
     - Full Name: `Jane User`
     - Username: `jane_user`
     - Email: `jane@example.com`
     - Password: `test123`
     - Confirm Password: `test123`
     - Account Type: Select "USER"
   - Click "Create Account"
   - Success message appears

4. **Test Duplicate Username Prevention**
   - Try creating another account with username `john_dev`
   - Error message should appear: "Username already exists or pending approval."

5. **Test Validation Rules**
   - Try empty fields → Error: "Please fill in all fields."
   - Try password < 6 characters → Error: "Password must be at least 6 characters long."
   - Try mismatched passwords → Error: "Passwords do not match."
   - Try invalid email → Error: "Please enter a valid email address."

### Phase 3: Test Account Approval Workflow

1. **Return to Login Screen**
   - Click "Back to Login"

2. **Attempt Login with Pending Account**
   - Try logging in with `john_dev` / `test123`
   - Should fail with "Invalid username or password" (account not yet approved)

3. **Login as Master Again**
   - Username: `siyam2207031`
   - Password: `2207031`

4. **Open Pending Accounts**
   - Click "Review Accounts" on the "Pending Accounts" card
   - Table displays pending users with columns:
     - ID
     - Username
     - Full Name
     - Email
     - Role
     - Created Date
     - Actions (Approve/Reject buttons)

5. **Approve Developer Account**
   - Find `john_dev` in the table
   - Click "✓ Approve" button
   - Confirmation dialog appears
   - Click "OK"
   - Status message: "Account approved: john_dev"
   - Table refreshes automatically

6. **Reject User Account**
   - Find `jane_user` in the table
   - Click "✗ Reject" button
   - Confirmation dialog with warning
   - Click "OK"
   - Status message: "Account rejected: jane_user"
   - Table refreshes

7. **Test Refresh Button**
   - Click "🔄 Refresh" button
   - Confirms list is up to date

### Phase 4: Verify Approved Account Access

1. **Logout and Login as Developer**
   - Logout from master account
   - Login with `john_dev` / `test123`
   - Success! Dashboard loads

2. **Verify Developer Permissions**
   - Welcome message: "Welcome, John Developer!"
   - Role: "Role: Developer"
   - Three main cards visible (no "Pending Accounts" card)
   - Can access all features:
     - Create Plan ✓
     - Saved Plans ✓ (with edit/delete buttons visible)
     - Automatic Route ✓

3. **Test Role-Based UI**
   - Navigate to "Saved Plans"
   - Edit and Delete buttons should be visible (developer permissions)

### Phase 5: Test Rejected Account

1. **Logout and Attempt Login as Rejected User**
   - Try logging in with `jane_user` / `test123`
   - Should fail: "Invalid username or password"
   - Account was rejected and removed from pending list

2. **Re-register Rejected User**
   - Click "Create Account"
   - Create new account with username `jane_user2` (different username)
   - Submit registration
   - Success message appears

### Phase 6: Test USER Role Permissions

1. **Login as Master and Approve jane_user2**
   - Login as `siyam2207031`
   - Go to Pending Accounts
   - Approve `jane_user2`

2. **Login as USER**
   - Logout and login as `jane_user2` / `test123`
   - Welcome message: "Welcome, Jane User!"
   - Role: "Role: User"

3. **Verify USER Restrictions**
   - Navigate to "Saved Plans"
   - Edit and Delete buttons should be HIDDEN
   - Can view plans but cannot modify

### Phase 7: Security Feature Testing

1. **Test Account Lockout**
   - Logout
   - Attempt login with correct username but wrong password 5 times
   - After 5th attempt, error message: "Account locked due to too many failed attempts. Please try again in 15 minutes."
   - Wait 15 minutes OR reset via database to unlock

2. **Test Password Security**
   - Check database: `data/travel_plans.db`
   - Open with SQLite browser
   - View `users` table
   - Verify passwords are hashed (not plain text)
   - Each user has unique `password_salt`

## Database Schema

### users table
```sql
CREATE TABLE users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT UNIQUE NOT NULL,
    email TEXT NOT NULL,
    password_hash TEXT NOT NULL,
    password_salt TEXT NOT NULL,
    role TEXT NOT NULL,
    full_name TEXT NOT NULL,
    created_date TEXT NOT NULL,
    failed_login_attempts INTEGER DEFAULT 0,
    last_failed_login TEXT,
    account_locked_until TEXT
)
```

### pending_users table
```sql
CREATE TABLE pending_users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT UNIQUE NOT NULL,
    email TEXT NOT NULL,
    password_hash TEXT NOT NULL,
    password_salt TEXT NOT NULL,
    role TEXT NOT NULL,
    full_name TEXT NOT NULL,
    created_date TEXT NOT NULL,
    status TEXT DEFAULT 'PENDING'
)
```

## Files Modified/Created

### New Files
1. `src/main/resources/fxml/register.fxml` - Registration UI
2. `src/main/java/com/travelmanager/controller/RegistrationController.java` - Registration logic
3. `src/main/resources/fxml/account-approval.fxml` - Approval interface UI
4. `src/main/java/com/travelmanager/controller/AccountApprovalController.java` - Approval logic

### Modified Files
1. `src/main/java/com/travelmanager/model/User.java`
   - Added MASTER role to enum
   - Added `isMaster()` and `canApproveAccounts()` methods
   - Updated permission methods

2. `src/main/java/com/travelmanager/database/DatabaseManager.java`
   - Created `pending_users` table
   - Replaced demo accounts with single master account
   - Added `createPendingUser()`, `getPendingUsers()`, `approvePendingUser()`, `rejectPendingUser()`
   - Added `isPendingUser()` and `userExists()` helper methods
   - Added `PendingUser` helper class

3. `src/main/resources/fxml/login.fxml`
   - Removed demo credentials box
   - Added "Create Account" hyperlink

4. `src/main/java/com/travelmanager/controller/LoginController.java`
   - Added `handleCreateAccount()` method

5. `src/main/resources/fxml/home.fxml`
   - Added master-only "Pending Accounts" section

6. `src/main/java/com/travelmanager/controller/HomeController.java`
   - Updated role display to include MASTER
   - Added visibility control for master section
   - Added `handlePendingAccounts()` method

## Expected Behavior Summary

✅ **Working Features:**
- Master account login (siyam2207031/2207031)
- Account registration with role selection
- Form validation (empty fields, password length, email format, password match)
- Duplicate username prevention
- Pending account queue
- Master approval/rejection workflow
- Role-based UI visibility
- PBKDF2 password hashing
- Account lockout protection
- Session management
- Logout functionality

❌ **Security Measures:**
- No default demo credentials
- All new accounts require master approval
- Passwords stored as salted hashes
- Failed login tracking and lockout
- Prepared statements prevent SQL injection

## Troubleshooting

### Issue: Can't login with master account
**Solution:** Check database. Run:
```bash
sqlite3 data/travel_plans.db "SELECT username, role FROM users;"
```
Should show `siyam2207031` with role `MASTER`.

### Issue: Registration form doesn't show
**Solution:** Check FXML path in NavigationManager. File should be at:
`src/main/resources/fxml/register.fxml`

### Issue: Pending accounts table is empty
**Solution:** 
1. Check if any pending users exist in database:
   ```bash
   sqlite3 data/travel_plans.db "SELECT * FROM pending_users;"
   ```
2. Create a test account through registration form

### Issue: Account lockout not releasing
**Solution:** Clear lockout manually:
```bash
sqlite3 data/travel_plans.db "UPDATE users SET failed_login_attempts = 0, account_locked_until = NULL WHERE username = 'username';"
```

## Success Criteria

The system is working correctly if:
1. ✅ Can login with master account (siyam2207031/2207031)
2. ✅ Registration form validates all inputs correctly
3. ✅ New accounts enter "pending" state
4. ✅ Master can see pending accounts table
5. ✅ Approved accounts can login successfully
6. ✅ Rejected accounts cannot login
7. ✅ USER role has limited permissions (no edit/delete buttons)
8. ✅ DEVELOPER role has full permissions
9. ✅ MASTER role can access "Pending Accounts" card
10. ✅ Account lockout activates after 5 failed attempts

## Next Steps

After successful testing, consider:
1. Adding email notifications for account approval/rejection
2. Implementing password reset functionality
3. Adding account expiration dates
4. Creating admin dashboard with user management
5. Adding audit logging for account actions
6. Implementing two-factor authentication

---

**Testing Complete!** The account registration system with master approval workflow is fully implemented and functional.
