package com.travelmanager.api;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Generic HTTP client for making API requests
 */
public class ApiClient {
    
    private static final int CONNECT_TIMEOUT = 10000; // 10 seconds
    private static final int READ_TIMEOUT = 10000; // 10 seconds
    
    /**
     * Make a GET request to the specified URL
     * @param urlString The URL to request
     * @return Response body as String
     * @throws Exception if request fails
     */
    public static String get(String urlString) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        
        try {
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(CONNECT_TIMEOUT);
            connection.setReadTimeout(READ_TIMEOUT);
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("User-Agent", "TravelScheduleManager/1.0");
            
            int responseCode = connection.getResponseCode();
            
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
                StringBuilder response = new StringBuilder();
                String line;
                
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                
                return response.toString();
            } else {
                throw new Exception("HTTP request failed with response code: " + responseCode);
            }
        } finally {
            connection.disconnect();
        }
    }
    
    /**
     * Make a GET request with custom headers
     * @param urlString The URL to request
     * @param headers Array of header key-value pairs
     * @return Response body as String
     * @throws Exception if request fails
     */
    public static String get(String urlString, String[][] headers) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        
        try {
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(CONNECT_TIMEOUT);
            connection.setReadTimeout(READ_TIMEOUT);
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("User-Agent", "TravelScheduleManager/1.0");
            
            // Add custom headers
            if (headers != null) {
                for (String[] header : headers) {
                    if (header.length == 2) {
                        connection.setRequestProperty(header[0], header[1]);
                    }
                }
            }
            
            int responseCode = connection.getResponseCode();
            
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
                StringBuilder response = new StringBuilder();
                String line;
                
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                
                return response.toString();
            } else {
                throw new Exception("HTTP request failed with response code: " + responseCode);
            }
        } finally {
            connection.disconnect();
        }
    }
}
