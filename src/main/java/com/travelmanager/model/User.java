package com.travelmanager.model;

/**
 * Represents a user in the system with role-based access
 */
public class User {
    
    public enum Role {
        USER,       // Normal user - read-only access
        DEVELOPER   // Developer/Admin - full access
    }
    
    private int id;
    private String username;
    private String email;
    private String passwordHash;
    private Role role;
    private String fullName;
    
    // Constructor for creating new users
    public User(String username, String email, String passwordHash, Role role, String fullName) {
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.fullName = fullName;
    }
    
    // Constructor for loading from database
    public User(int id, String username, String email, String passwordHash, Role role, String fullName) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.fullName = fullName;
    }
    
    // Getters
    public int getId() {
        return id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public String getEmail() {
        return email;
    }
    
    public String getPasswordHash() {
        return passwordHash;
    }
    
    public Role getRole() {
        return role;
    }
    
    public String getFullName() {
        return fullName;
    }
    
    // Setters
    public void setId(int id) {
        this.id = id;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
    
    public void setRole(Role role) {
        this.role = role;
    }
    
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    
    // Permission checking methods
    public boolean isDeveloper() {
        return role == Role.DEVELOPER;
    }
    
    public boolean canModifyData() {
        return role == Role.DEVELOPER;
    }
    
    public boolean canDeleteData() {
        return role == Role.DEVELOPER;
    }
    
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", role=" + role +
                ", fullName='" + fullName + '\'' +
                '}';
    }
}
