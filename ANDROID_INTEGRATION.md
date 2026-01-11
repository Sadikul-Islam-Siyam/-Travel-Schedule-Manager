# Android App Integration Guide

## Desktop App REST API - Complete Reference

Your desktop app now has full REST API support for Android integration. The API server runs on **port 8080**.

---

## 🔧 Setup

### 1. Desktop App CORS (Already Configured ✅)
CORS headers are now configured in `RestApiServer.java` to allow Android app access.

### 2. Windows Firewall (For Physical Devices)
Run as Administrator:
```powershell
netsh advfirewall firewall add rule name="Javalin API" dir=in action=allow protocol=TCP localport=8080
```

### 3. Android Network Configuration
Add to `AndroidManifest.xml`:
```xml
<uses-permission android:name="android.permission.INTERNET" />

<application
    android:usesCleartextTraffic="true"
    ...>
```

Create `res/xml/network_security_config.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <domain-config cleartextTrafficPermitted="true">
        <domain includeSubdomains="true">10.0.2.2</domain>
        <domain includeSubdomains="true">192.168.0.0/16</domain>
    </domain-config>
</network-security-config>
```

Reference it in `AndroidManifest.xml`:
```xml
<application
    android:networkSecurityConfig="@xml/network_security_config"
    ...>
```

---

## 📡 API Base URL

| Device Type | Base URL |
|-------------|----------|
| Android Emulator | `http://10.0.2.2:8080/api` |
| Physical Device | `http://<PC-IP>:8080/api` (e.g., `http://192.168.1.100:8080/api`) |

Find your PC's IP: Run `ipconfig` in Command Prompt

---

## 🔐 Authentication Endpoints

### POST `/api/auth/login`
Login and get session token.

**Request:**
```json
{
    "username": "myuser",
    "password": "mypassword"
}
```

**Response (Success):**
```json
{
    "success": true,
    "message": "Login successful",
    "user": {
        "id": 1,
        "username": "myuser",
        "email": "user@example.com",
        "fullName": "John Doe",
        "role": "USER"
    },
    "token": "BASE64_TOKEN_STRING"
}
```

**Response (Error):**
```json
{
    "success": false,
    "error": "Invalid username or password"
}
```

---

### POST `/api/auth/register`
Register new account (pending approval).

**Request:**
```json
{
    "username": "newuser",
    "email": "newuser@example.com",
    "password": "password123",
    "fullName": "Jane Doe",
    "role": "USER"
}
```

**Role options:** `USER`, `DEVELOPER`

**Response (Success):**
```json
{
    "success": true,
    "message": "Account created successfully. Awaiting master approval.",
    "status": "PENDING"
}
```

**Response (Errors):**
```json
{
    "success": false,
    "errors": ["Username must be at least 3 characters", "Password must be at least 6 characters"]
}
```

---

### GET `/api/auth/status/{username}`
Check account approval status.

**Response:**
```json
{
    "username": "newuser",
    "status": "PENDING",
    "message": "Account is awaiting approval from master."
}
```

Status values: `APPROVED`, `PENDING`, `NOT_FOUND`

---

### GET `/api/users/profile`
Get current user's profile.

**Headers:**
```
Authorization: Bearer <token>
```

**Response:**
```json
{
    "success": true,
    "user": {
        "id": 1,
        "username": "myuser",
        "email": "user@example.com",
        "fullName": "John Doe",
        "role": "USER"
    }
}
```

---

## 👤 Admin Endpoints (Master/Developer Only)

### GET `/api/admin/pending-users`
Get all pending registrations.

**Headers:**
```
Authorization: Bearer <token>
```

**Response:**
```json
{
    "success": true,
    "count": 2,
    "pendingUsers": [
        {
            "id": 1,
            "username": "newuser1",
            "email": "new1@example.com",
            "fullName": "New User 1",
            "role": "USER",
            "createdDate": "2026-01-11T10:30:00",
            "status": "PENDING"
        }
    ]
}
```

---

### POST `/api/admin/approve/{userId}`
Approve a pending user.

**Headers:**
```
Authorization: Bearer <token>
```

**Response:**
```json
{
    "success": true,
    "message": "User approved successfully"
}
```

---

### POST `/api/admin/reject/{userId}`
Reject a pending user.

**Headers:**
```
Authorization: Bearer <token>
```

**Response:**
```json
{
    "success": true,
    "message": "User rejected successfully"
}
```

---

## 🚌🚂 Schedule Search Endpoints

### GET `/api/routes?start={origin}&destination={dest}`
Search routes (bus + train combined).

**Example:** `/api/routes?start=Colombo&destination=Kandy`

**Response:**
```json
[
    {
        "id": "bus_001",
        "type": "BUS",
        "origin": "Colombo",
        "destination": "Kandy",
        "departureTime": "06:00",
        "arrivalTime": "09:30",
        "fare": 450.00,
        "availableSeats": 45,
        "companyName": "Lanka Ashok Leyland",
        "busType": "A/C"
    },
    {
        "id": "train_intercity",
        "type": "TRAIN",
        "origin": "Colombo Fort",
        "destination": "Kandy",
        "departureTime": "07:00",
        "arrivalTime": "09:30",
        "fare": 600.00,
        "availableSeats": 150,
        "trainName": "Intercity Express",
        "trainNumber": "1015",
        "seatClass": "Second Class"
    }
]
```

---

### GET `/api/schedules`
Get all schedules (bus + train).

### GET `/api/schedules/bus`
Get all bus schedules.

### GET `/api/schedules/train`
Get all train schedules.

### GET `/api/health`
Health check endpoint.

---

## 📱 Android Code Examples (Kotlin + Retrofit)

### 1. ApiService Interface
```kotlin
interface TravelApiService {
    companion object {
        // Change based on your setup
        const val BASE_URL = "http://10.0.2.2:8080/api/"  // Emulator
        // const val BASE_URL = "http://192.168.1.100:8080/api/"  // Physical device
    }

    // Authentication
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>

    @GET("auth/status/{username}")
    suspend fun checkStatus(@Path("username") username: String): Response<StatusResponse>

    @GET("users/profile")
    suspend fun getProfile(@Header("Authorization") token: String): Response<ProfileResponse>

    // Schedule Search
    @GET("routes")
    suspend fun searchRoutes(
        @Query("start") start: String,
        @Query("destination") destination: String
    ): Response<List<ScheduleDTO>>

    @GET("schedules")
    suspend fun getAllSchedules(): Response<List<ScheduleDTO>>

    // Admin (Master/Developer only)
    @GET("admin/pending-users")
    suspend fun getPendingUsers(@Header("Authorization") token: String): Response<PendingUsersResponse>

    @POST("admin/approve/{userId}")
    suspend fun approveUser(
        @Header("Authorization") token: String,
        @Path("userId") userId: Int
    ): Response<ApiResponse>

    @POST("admin/reject/{userId}")
    suspend fun rejectUser(
        @Header("Authorization") token: String,
        @Path("userId") userId: Int
    ): Response<ApiResponse>
}
```

### 2. Data Classes
```kotlin
// Request models
data class LoginRequest(val username: String, val password: String)
data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String,
    val fullName: String,
    val role: String = "USER"
)

// Response models
data class LoginResponse(
    val success: Boolean,
    val message: String?,
    val user: UserDTO?,
    val token: String?,
    val error: String?
)

data class RegisterResponse(
    val success: Boolean,
    val message: String?,
    val status: String?,
    val errors: List<String>?
)

data class StatusResponse(
    val username: String,
    val status: String,
    val message: String
)

data class UserDTO(
    val id: Int,
    val username: String,
    val email: String,
    val fullName: String,
    val role: String
)

data class ScheduleDTO(
    val id: String,
    val type: String,
    val origin: String,
    val destination: String,
    val departureTime: String,
    val arrivalTime: String,
    val fare: Double,
    val availableSeats: Int,
    // Bus specific
    val companyName: String?,
    val busType: String?,
    // Train specific
    val trainName: String?,
    val trainNumber: String?,
    val seatClass: String?
)

data class PendingUserDTO(
    val id: Int,
    val username: String,
    val email: String,
    val fullName: String,
    val role: String,
    val createdDate: String,
    val status: String
)

data class PendingUsersResponse(
    val success: Boolean,
    val count: Int,
    val pendingUsers: List<PendingUserDTO>
)

data class ApiResponse(val success: Boolean, val message: String?, val error: String?)
data class ProfileResponse(val success: Boolean, val user: UserDTO?, val error: String?)
```

### 3. Retrofit Setup
```kotlin
object RetrofitClient {
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    val api: TravelApiService by lazy {
        Retrofit.Builder()
            .baseUrl(TravelApiService.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TravelApiService::class.java)
    }
}
```

### 4. Usage in ViewModel
```kotlin
class AuthViewModel : ViewModel() {
    private val api = RetrofitClient.api

    fun login(username: String, password: String) = viewModelScope.launch {
        try {
            val response = api.login(LoginRequest(username, password))
            if (response.isSuccessful && response.body()?.success == true) {
                val token = response.body()?.token
                val user = response.body()?.user
                // Save token to SharedPreferences
                // Navigate to home screen
            } else {
                // Show error: response.body()?.error
            }
        } catch (e: Exception) {
            // Handle network error
        }
    }

    fun register(username: String, email: String, password: String, fullName: String) = viewModelScope.launch {
        try {
            val response = api.register(RegisterRequest(username, email, password, fullName))
            if (response.isSuccessful && response.body()?.success == true) {
                // Show success message - account pending approval
            } else {
                // Show errors
            }
        } catch (e: Exception) {
            // Handle network error
        }
    }

    fun searchRoutes(start: String, destination: String) = viewModelScope.launch {
        try {
            val response = api.searchRoutes(start, destination)
            if (response.isSuccessful) {
                val schedules = response.body() ?: emptyList()
                // Update UI with schedules
            }
        } catch (e: Exception) {
            // Handle error
        }
    }
}
```

### 5. Token Storage Helper
```kotlin
class TokenManager(context: Context) {
    private val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    fun saveToken(token: String) {
        prefs.edit().putString("auth_token", token).apply()
    }

    fun getToken(): String? = prefs.getString("auth_token", null)

    fun getBearerToken(): String? = getToken()?.let { "Bearer $it" }

    fun clearToken() {
        prefs.edit().remove("auth_token").apply()
    }

    fun isLoggedIn(): Boolean = getToken() != null
}
```

---

## 🔄 Workflow: Mobile Registration & Desktop Approval

1. **Mobile User** → Registers via `/api/auth/register`
2. **Mobile User** → Polls `/api/auth/status/{username}` to check approval
3. **Desktop Master** → Sees new pending user in "Account Approval" screen
4. **Desktop Master** → Approves/Rejects the user
5. **Mobile User** → Status changes to `APPROVED` → Can now login

---

## 🚨 Error Codes

| HTTP Code | Meaning |
|-----------|---------|
| 200 | Success |
| 201 | Created (registration successful) |
| 400 | Bad Request (validation errors) |
| 401 | Unauthorized (invalid credentials) |
| 403 | Forbidden (insufficient permissions) |
| 404 | Not Found |
| 409 | Conflict (username already exists) |
| 500 | Server Error |

---

## 📋 Gradle Dependencies (Android)

```gradle
dependencies {
    // Retrofit
    implementation 'com.squareup.retrofit2:retrofit:2.9.0'
    implementation 'com.squareup.retrofit2:converter-gson:2.9.0'
    
    // OkHttp (logging)
    implementation 'com.squareup.okhttp3:logging-interceptor:4.11.0'
    
    // Coroutines
    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3'
    
    // ViewModel
    implementation 'androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2'
}
```
