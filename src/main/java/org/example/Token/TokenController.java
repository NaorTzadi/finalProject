package org.example.Token;
import org.example.CustomLogger;
import org.example.ServerSecurity.SessionsManager;
import org.example.Users.UsersService;
import org.example.Users.User;
import org.example.VerificationController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/token")
public class TokenController {
    private static final CustomLogger logger =new CustomLogger(TokenController.class);
    private final UsersService usersService;
    private final TokenService tokenService;

    public TokenController(UsersService usersService, TokenService tokenService) {
        this.usersService = usersService;
        this.tokenService = tokenService;
        logger.setVisibility(false);
    }

    @PostMapping("/fastAccessLogin")
    public ResponseEntity<String> fastAccessLogin(@RequestParam String fastAccessToken) {
        logger.info("received fastAccessLogin request");
        if (tokenService.doesExistInFastAccessTokensTable(fastAccessToken)) {
            long userId= tokenService.getUserIdFromFastAccessTokensTable(fastAccessToken);
            User user = usersService.getUser(userId);
            String sessionToken=user.getSessionToken();
            if (sessionToken==null || sessionToken.isBlank()) {
                sessionToken=tokenService.generateUserSessionToken(userId);
                usersService.setSessionTokenByUsername(user.getUsername(),sessionToken);
                SessionsManager.addSession(sessionToken);
                user.setSessionToken(sessionToken);
            }
            String newToken = tokenService.storeUpdatedAccessToken(fastAccessToken);
            String responseBody = "{ \"user\": " + user.toJson() + ", \"updatedFastAccessToken\": \"" + newToken + "\" }";
            logger.info("fastAccessLogin returning: \n{}", responseBody);
            return ResponseEntity.ok(responseBody);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"error\": \"Invalid or expired token.\"}");
    }
    @PostMapping("/reset-password")
    public ResponseEntity<String> passwordReset(@RequestParam String passwordResetToken,@RequestParam String newPassword) {
        String errorMessage=VerificationController.checkPasswordField(newPassword);
        if (errorMessage!=null)return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
        if (tokenService.doesExistInResetPasswordTokensTable(passwordResetToken)) {
            if (usersService.resetPassword(newPassword, tokenService.getUserIdFromResetPasswordTokensTable(passwordResetToken))){
                tokenService.removeResetPasswordToken(passwordResetToken);
                return ResponseEntity.status(HttpStatus.OK).body("Password reset successful.");
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to reset password.");
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid or expired password reset token.");
    }
    @PostMapping("/validate-password-reset-token")
    public ResponseEntity<String> validateResetPassword(@RequestParam String passwordResetToken) {
        logger.info("Validating reset password token {}", passwordResetToken);
        return ResponseEntity.status(HttpStatus.OK).body(tokenService.doesExistInResetPasswordTokensTable(passwordResetToken)?"true":"false");
    }

    @PostMapping("/verify-email-token")
    public ResponseEntity<Boolean> verifyEmailToken(@RequestParam String emailToken) {
        boolean isVerified = usersService.addVerifiedUser(emailToken) != -1;
        logger.info("verifyEmailToken is {} for token {}", isVerified, emailToken);
        return ResponseEntity.ok(isVerified);
    }
    @PostMapping("/renew-session-token")
    public ResponseEntity<String> renewSessionToken(@RequestParam long userId) {
        logger.info("renewSessionToken for user with user id: {} ", userId);
        User user = usersService.getUser(userId);
        if (user == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"error\": User not found.}");
        String sessionToken = tokenService.generateUserSessionToken(userId);
        usersService.setSessionToken("user_id",userId,sessionToken);
        SessionsManager.addSession(sessionToken);

        return ResponseEntity.status(HttpStatus.CREATED).body(sessionToken);
    }
    @PostMapping("/saveFastAccessToken")
    public ResponseEntity<String> saveFastAccessToken(@RequestParam long userId) {
        logger.info("saveFastAccessToken for user with user id: {} ", userId);
        if (usersService.getUser(userId) == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"error\": User not found.}");
        if (tokenService.doesExistInFastAccessTokensTable(userId)) tokenService.removeFastAccessToken(userId);
        return ResponseEntity.status(HttpStatus.CREATED).body("{\"newFastAccessToken\": \"" + tokenService.storeNewFastAccessToken(userId) + "\" }");
    }
}
