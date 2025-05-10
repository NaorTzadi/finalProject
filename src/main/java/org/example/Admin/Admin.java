package org.example.Admin;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.Practice.PracticeSession;
import org.example.Users.User;
import org.example.UserPage.UserSettings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Admin extends User {
    public enum Permission {ADD_USER, REMOVE_USER,ADD_ADMIN, UPDATE,REMOVE_BLOCKED_IP,REMOVE_SUSPICIOUS_IP,
        REMOVE_SUSPICIOUS_ACTIVITY,REMOVE_BLOCKED_ACTIVITY,REMOVE_ALL_BLOCKED_IPS,REMOVE_ALL_SUSPICIOUS_IPS,SERVER_HEALTH}

    private final List<Permission> permissions;

    public Admin(long userId, String username, String email, ArrayList<PracticeSession> practiceSessions, UserSettings userSettings, String sessionToken, List<Permission> permissions) {
        super(userId, username, email, practiceSessions, userSettings,sessionToken);
        if (permissions == null || permissions.isEmpty()) {
            this.permissions = Collections.singletonList(Permission.UPDATE);
        } else {
            this.permissions = permissions;
        }
    }
    public List<Permission> getPermissions() {
        return permissions;
    }
    @Override
    public String toJson() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting Admin to JSON", e);
        }
    }


}
