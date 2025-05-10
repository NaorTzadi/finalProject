package org.example;
import jakarta.servlet.http.HttpServletRequest;
import org.example.Admin.Admin;
import org.example.Admin.AdminService;
import org.example.ServerSecurity.SessionsManager;
import org.example.Users.User;
import org.example.Users.UsersService;
import org.example.Token.TokenService;
import org.example.UserPage.UserSettings;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;

@RestController
public class VerificationController {
    private final CustomLogger logger=new CustomLogger(VerificationController.class);
    private final UsersService usersService;
    private final EmailService emailService;
    private final TokenService tokenService;
    private final AdminService adminService;
    private final Utility utility;
    public static final String USERNAME_REGEX="^.{3,20}$";
    public static final String PASSWORD_SPECIAL_CHARACTERS_REGEX=".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]+.*";
    public static final String PASSWORD_UPPER_LOWER_CASE_REGEX="^(?=.*[A-Z])(?=.*[a-z]).*$";
    public static final String EMAIL_REGEX="^[\\w.-]+@[\\w.-]+\\.\\w+$";

    public VerificationController(UsersService usersService, EmailService emailService, TokenService tokenService,AdminService adminService, Utility utility) {
        this.usersService = usersService;
        this.emailService = emailService;
        this.tokenService = tokenService;
        this.adminService = adminService;
        this.utility = utility;
    }
    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestParam String username, @RequestParam String password, @RequestParam String email) {
        String errorMessage = checkSignupFields(username, password, email);
        if (errorMessage!=null)return new ResponseEntity<>(errorMessage,HttpStatus.BAD_REQUEST);
        if (usersService.isUsernameTaken(username)) return new ResponseEntity<>("Username taken", HttpStatus.BAD_REQUEST);
        if (usersService.isEmailTaken(email)) return new ResponseEntity<>("Email already exists", HttpStatus.BAD_REQUEST);
        String token = emailService.sendEmailVerification(email, username);
        if (token==null) return new ResponseEntity<>("Failed to send verification email", HttpStatus.INTERNAL_SERVER_ERROR);
        User user=new User(-1,username,email,new ArrayList<>(), UserSettings.getDefaultUserSettings(),null);
        byte[] salt = PasswordSecurity.getNewSalt();
        usersService.addUnverifiedUser(user,PasswordSecurity.hashPassword(password,salt),salt,token);
        return new ResponseEntity<>("email verification was sent to: "+email, HttpStatus.CREATED);
    }
    public static String checkPasswordField(String password){
        if (!password.matches(PASSWORD_SPECIAL_CHARACTERS_REGEX)) return "password must contain at least one special character!";
        if (!password.matches(PASSWORD_UPPER_LOWER_CASE_REGEX)) return "password must contain at least one uppercase and one lowercase letter!";
        return null;
    }
    private static String checkSignupFields(String username, String password, String email) {
        if (!username.matches(USERNAME_REGEX))return "username must be between 3 to 20 characters";
        String passwordErrorMessage=checkPasswordField(password);
        if (passwordErrorMessage!=null) return passwordErrorMessage;
        if (!email.matches(EMAIL_REGEX)) return "email format invalid!";
        return null;
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestParam String username, @RequestParam String password, HttpServletRequest request) {
        boolean doesUserExist= usersService.doesUserExist(username, password);
        String existingSessionToken=usersService.getSessionToken(username);
        if (doesUserExist) {
            if (existingSessionToken!=null && SessionsManager.isHeartBeating(existingSessionToken)) return new ResponseEntity<>("you are already logged in", HttpStatus.OK);
            usersService.removeUserFailedLoginAttempts(username);
            String sessionToken=tokenService.generateUserSessionToken(usersService.getUserIdByUsername(username));
            usersService.setSessionTokenByUsername(username,sessionToken);
            SessionsManager.addSession(sessionToken);
            User user=adminService.getUser(username);
            return new ResponseEntity<>(user.toJson(), HttpStatus.OK);
        }
        if (usersService.shouldSendAlert(username)) emailService.sendAlertMultipleLoginAttempts(username);
        return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestParam String identifier) {
        if (identifier.contains("@") && usersService.isEmailTaken(identifier)){
            //if (tokenService.doesExistInResetPasswordTokensTable(usersService.getUserIdByEmail(identifier))) return new ResponseEntity<>("reset token was already sent!", HttpStatus.BAD_REQUEST);
            if (emailService.forgotPassword(usersService.getUsernameByEmail(identifier))) return new ResponseEntity<>("we sent a password reset mail to "+ identifier, HttpStatus.OK);
        }else if (usersService.isUsernameTaken(identifier)){
            //if (tokenService.doesExistInResetPasswordTokensTable(usersService.getUserIdByUsername(identifier))) return new ResponseEntity<>("reset token was already sent!", HttpStatus.BAD_REQUEST);
            if (emailService.forgotPassword(identifier)){
                String email = usersService.getEmailByUsername(identifier);
                int atIndex = email.indexOf("@");
                String maskedPart = "*".repeat(Math.max(0, atIndex - 3));
                String visiblePart = email.substring(Math.max(0, atIndex - 3), atIndex);
                String maskedEmail = maskedPart + visiblePart + email.substring(atIndex);
                return new ResponseEntity<>("we sent a password reset mail to "+maskedEmail, HttpStatus.OK);
            }
        }
        return ResponseEntity.ok("no user found with "+identifier+"!");
    }
}
