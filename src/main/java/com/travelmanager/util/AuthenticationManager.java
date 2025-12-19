package com.travelmanager.util;

import com.travelmanager.model.User;

/**
 * Manages user authentication session
 */
public class AuthenticationManager {
    
    private static AuthenticationManager instance;
    private User currentUser;
    
    private AuthenticationManager() {
    }
    
    public static AuthenticationManager getInstance() {
        if (instance == null) {
            instance = new AuthenticationManager();
        }
        return instance;
    }
    
    public void login(User user) {
        this.currentUser = user;
    }
    
    public void logout() {
        this.currentUser = null;
    }
    
    public boolean isLoggedIn() {
        return currentUser != null;
    }
    
    public User getCurrentUser() {
        return currentUser;
    }
    
    public boolean isDeveloper() {
        return currentUser != null && currentUser.isDeveloper();
    }
    
    public boolean canModifyData() {
        return currentUser != null && currentUser.canModifyData();
    }
    
    public boolean canDeleteData() {
        return currentUser != null && currentUser.canDeleteData();
    }
    
    public String getCurrentUsername() {
        return currentUser != null ? currentUser.getUsername() : "Guest";
    }
    
    public String getCurrentUserFullName() {
        return currentUser != null ? currentUser.getFullName() : "Guest";
    }
}
