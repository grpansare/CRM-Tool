package com.crmplatform.sales.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;
import com.crmplatform.common.security.UserContext;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserService {
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Value("${auth.service.url:http://localhost:8081}")
    private String authServiceUrl;
    
    // Cache to avoid repeated API calls
    private final Map<Long, String> userNameCache = new ConcurrentHashMap<>();
@Autowired
private JdbcTemplate jdbcTemplate;

public String getUserName(Long userId) {
    if (userId == null) {
        return null;
    }
    
    // Check cache first
    if (userNameCache.containsKey(userId)) {
        return userNameCache.get(userId);
    }
    
    try {
        // Query the auth service database directly
        
String sql = "SELECT first_name, last_name, username FROM crm_auth.users WHERE user_id = ?";
Map<String, Object> result = jdbcTemplate.queryForMap(sql, userId);      
        String firstName = (String) result.get("first_name");
        String lastName = (String) result.get("last_name");
        String username = (String) result.get("username");
        
        String displayName;
        if (firstName != null && lastName != null) {
            displayName = firstName + " " + lastName;
        } else if (firstName != null) {
            displayName = firstName;
        } else if (username != null) {
            displayName = username;
        } else {
            displayName = "User " + userId;
        }
        
        // Cache the result
        userNameCache.put(userId, displayName);
        return displayName;
        
    } catch (Exception e) {
        System.err.println("Failed to fetch user from database for userId " + userId + ": " + e.getMessage());
        String fallback = "User " + userId;
        userNameCache.put(userId, fallback);
        return fallback;
    }
}
    
    public Map<Long, String> getUserNames(List<Long> userIds) {
        Map<Long, String> result = new HashMap<>();
        for (Long userId : userIds) {
            result.put(userId, getUserName(userId));
        }
        return result;
    }
    
    public void clearCache() {
        userNameCache.clear();
    }
}
