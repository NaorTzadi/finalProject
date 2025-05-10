package org.example;
import jakarta.annotation.PostConstruct;
import org.example.Admin.AdminService;
import org.example.Practice.PracticeSessionService;
import org.example.Token.TokenService;
import org.example.Users.UsersService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class MainController {
    private static final Logger logger = LoggerFactory.getLogger(MainController.class);

    private final Utility utility;
    private final UsersService usersService;
    private final TokenService tokenService;
    private final PracticeSessionService practiceSessionService;
    private final AdminService adminService;

    public MainController(Utility utility, UsersService usersService, TokenService tokenService, PracticeSessionService practiceSessionService,AdminService adminService) {
        this.utility = utility;
        this.usersService = usersService;
        this.tokenService = tokenService;
        this.practiceSessionService = practiceSessionService;
        this.adminService = adminService;
    }
    @PostConstruct
    public void init() {
        //utility.clearTable(Constants.USERS_TABLE);
        //utility.clearTable(Constants.UNVERIFIED_USERS_TABLE);
        //utility.clearTable(Constants.USER_SETTINGS_TABLE);
        //utility.clearTable(Constants.FAST_ACCESS_TOKENS_TABLE);
        //utility.clearTable(Constants.RESET_PASSWORD_TOKENS_TABLE);
        //utility.clearTable(Constants.FAILED_LOGIN_ATTEMPTS_TABLE);
        practiceSessionService.clearAllTables();
        usersService.removeAllSessionTokens();
        adminService.populateAdminPermissionsTable();
        adminService.addAllPermissions(1);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader String SessionToken) {
        logger.info("received logout request for: {}",SessionToken);
        if (SessionToken != null && !SessionToken.isBlank()) {
            tokenService.removeFastAccessToken(usersService.getUserIdBySessionToken(SessionToken));
            usersService.setSessionTokenBySessionToken(SessionToken,null);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/server-status")
    public ResponseEntity<String> checkServerStatus() {
        return new ResponseEntity<>("Status response succeeded.", HttpStatus.OK);
    }

}
