package org.example;
import org.example.Practice.PracticeSessionService;
import org.example.Users.UsersService;
import org.springframework.stereotype.Service;

@Service
public class UserExitCleaner {
    private final CustomLogger logger=new CustomLogger(UserExitCleaner.class);
    private final UsersService usersService;
    private final PracticeSessionService practiceSessionService;

    public UserExitCleaner(UsersService usersService, PracticeSessionService practiceSessionService) {
        this.usersService = usersService;
        this.practiceSessionService = practiceSessionService;
    }

    public void userExitClean(String sessionToken) {
        practiceSessionService.finishPracticeSession(sessionToken);
        usersService.setSessionTokenBySessionToken(sessionToken,null);
        logger.info("Removed inactive session: " + sessionToken);
    }

}
