package org.example.ServerSecurity;
import org.example.UserExitCleaner;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SessionsManager {
    private final static long INACTIVE_SESSION_THRESHOLD = 60 * 1000;
    private final static long HEAD_START=1000;
    private final static long HEART_BEAT_STOPPED=10000;
    private static final Map<String, Session> activeSessions = new ConcurrentHashMap<>();

    public static void addSession(String sessionToken) {
        activeSessions.put(sessionToken, new Session(sessionToken, System.currentTimeMillis()+HEAD_START));
    }
    public static void cleanupInactiveSessions(UserExitCleaner userExitCleaner) {
        long currentTime = System.currentTimeMillis();
        activeSessions.entrySet().removeIf(entry -> {
            Session session = entry.getValue();
            boolean isInactive = (currentTime - session.getLastHeartbeatTime()) >= INACTIVE_SESSION_THRESHOLD;
            if (isInactive) userExitCleaner.userExitClean(session.getSessionToken());
            return isInactive;
        });
    }
    public static boolean updateSessionHeartbeat(String sessionToken) {
        if (sessionToken == null) {return false;}
        Session session = activeSessions.get(sessionToken);
        if (session != null) {
            session.updateHeartbeat();
            return true;
        }
        return false;
    }
    public static boolean isHeartBeating(String sessionToken) {
        long currentTime = System.currentTimeMillis();
        long lastHeartbeatTime = activeSessions.get(sessionToken).lastHeartbeatTime;
        return currentTime - lastHeartbeatTime <= HEART_BEAT_STOPPED;
    }
    public static class Session {
        private final String sessionToken;
        private long lastHeartbeatTime;
        public Session(String sessionToken, long lastHeartbeatTime) {
            this.sessionToken = sessionToken;
            this.lastHeartbeatTime = lastHeartbeatTime;
        }
        public String getSessionToken() {return sessionToken;}
        public long getLastHeartbeatTime() {return lastHeartbeatTime;}
        public void updateHeartbeat() {this.lastHeartbeatTime = System.currentTimeMillis();}
    }

}
