package org.example.Users;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.Practice.PracticeSession;
import org.example.UserPage.UserSettings;

import java.util.ArrayList;

public class User {
    private final long user_id;
    private final String username;
    private final String email;
    private final ArrayList<PracticeSession> practiceSessions;
    private final UserSettings userSettings;
    private String sessionToken;

    public User(long user_id, String username, String email, ArrayList<PracticeSession> practiceSessions, UserSettings userSettings, String sessionToken) {
        this.user_id = user_id;
        this.username = username;
        this.email = email;
        this.practiceSessions = practiceSessions;
        this.userSettings =userSettings;
        this.sessionToken = sessionToken;
    }

    public ArrayList<PracticeSession> getSessions() {return practiceSessions;}
    public long getUser_id() {return user_id;}
    public String getUsername() {return username;}
    public String getEmail() {return email;}
    public UserSettings getUserSettings() {return userSettings;}
    public String getSessionToken() {return sessionToken;}
    public void setSessionToken(String sessionToken){this.sessionToken=sessionToken;}


    public String toJson() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting User to JSON", e);
        }
    }

}
