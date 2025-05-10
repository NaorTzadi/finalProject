package org.example;
import org.example.ServerSecurity.SessionsManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class Daemon {
    private static final CustomLogger logger = new CustomLogger(Daemon.class);
    private static final long INACTIVE_SESSIONS_CHECK_SCHEDULE =20000;
    private final UserExitCleaner userExitCleaner;

    @Autowired
    public Daemon(UserExitCleaner userExitCleaner) {
        this.userExitCleaner = userExitCleaner;
        logger.setVisibility(false);
    }

    @Scheduled(fixedRate = INACTIVE_SESSIONS_CHECK_SCHEDULE)
    public void checkForInactiveSessions() {
        SessionsManager.cleanupInactiveSessions(userExitCleaner);
    }
}
