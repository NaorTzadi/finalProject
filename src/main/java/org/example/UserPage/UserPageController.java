package org.example.UserPage;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;
import org.example.Constants;
import org.example.CustomLogger;
import org.example.Users.UsersService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import net.coobird.thumbnailator.Thumbnails;
import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@RestController
@RequestMapping("/profile")
public class UserPageController {
    private static final CustomLogger logger =new CustomLogger(UserPageController.class);
    private final UserPageService userPageService;
    private final UsersService usersService;
    private static final String PROFILE_AVATARS_DIR_PATH="springBoot/profile_avatars";
    public UserPageController(UserPageService UserPageService, UsersService UsersService) {
        this.userPageService =UserPageService;
        this.usersService = UsersService;
    }

    @GetMapping("/{username}")
    public ResponseEntity<?> getUserPage(@PathVariable String username, @RequestHeader String SessionToken) {
        logger.info("got request for {} profile page with session token {}", username, SessionToken);
        long viewerId = getUserIdBySessionToken(SessionToken);
        long ownerId=usersService.getUserIdByUsername(username);
        if (viewerId==-1)return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        UserPage userPage = userPageService.getUserPageData(ownerId);
        if (userPage == null) return new ResponseEntity<>(Map.of("status","404"), HttpStatus.NOT_FOUND);

        boolean isOwner = (ownerId == viewerId);
        if (!isOwner && userPageService.getUserSettingsByUserId(ownerId).getIsPrivateProfile()) {
            return new ResponseEntity<>(Map.of("status","private"), HttpStatus.OK);
        }
        boolean isOnline= usersService.getSessionToken(username)!=null;

        return new ResponseEntity<>(Map.of(
                "userPage", userPage,
                "isOnline",isOnline,
                "isOwner", isOwner
        ), HttpStatus.OK);
    }
    @PostMapping("/update-avatar")
    public ResponseEntity<Void> updateAvatar(@RequestHeader String SessionToken, @RequestParam("avatar") MultipartFile avatar) {
        logger.info("updating profile avatar with session token {}", SessionToken);
        final int sizeFactor=320;
        final String outputFormat="jpeg";
        long userId = getUserIdBySessionToken(SessionToken);
        if (userId==-1)return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Thumbnails.of(new ByteArrayInputStream(avatar.getBytes()))
                    .size(sizeFactor, sizeFactor)
                    .outputFormat(outputFormat)
                    .toOutputStream(outputStream);
            byte[] resizedImage = outputStream.toByteArray();
            Path outputPath = Paths.get(PROFILE_AVATARS_DIR_PATH+"/"+userId+"_avatar.jpg");
            Files.write(outputPath, resizedImage);
            userPageService.updateUserProfileAvatar(userId,outputPath.toString());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    @PostMapping("/update-biography")
    public ResponseEntity<Void> updateBiography(@RequestHeader String SessionToken, @RequestParam String Biography) {
        logger.info("updating profile biography with session token {}", SessionToken);
        long userId = getUserIdBySessionToken(SessionToken);
        if (userId==-1)return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        userPageService.updateUserBiography(userId,Biography);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/settings")
    public ResponseEntity<?> getUserSettings(@RequestHeader String SessionToken) {
        logger.info("Fetching user settings with session token {}", SessionToken);
        long userId = getUserIdBySessionToken(SessionToken);
        if (userId==-1)return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        UserSettings userSettings = userPageService.getUserSettingsByUserId(userId);
        if (userSettings == null) return new ResponseEntity<>(Map.of("status", "settings not found"), HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(Map.of("userSettings", userSettings), HttpStatus.OK);
    }

    @PostMapping("/settings/update")
    public ResponseEntity<?> updateUserSettings(@RequestHeader String SessionToken, @RequestBody UserSettings newSettings) {
        logger.info("Updating user settings with session token {}", SessionToken);
        long userId = getUserIdBySessionToken(SessionToken);
        if (userId==-1)return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        UserSettings existingSettings = userPageService.getUserSettingsByUserId(userId);
        if (existingSettings == null) return new ResponseEntity<>("settings not found", HttpStatus.NOT_FOUND);
        existingSettings.setPreferredLanguage(newSettings.getPreferredLanguage());
        existingSettings.setSolutionDetailLevel(newSettings.getSolutionDetailLevel());
        existingSettings.setIsPrivateProfile(newSettings.getIsPrivateProfile());
        userPageService.updateUserSettings(existingSettings);
        return new ResponseEntity<>(Map.of("status", "success", "updatedSettings", existingSettings), HttpStatus.OK);
    }

    @PostMapping("/settings/reset")
    public ResponseEntity<?> resetUserSettings(@RequestHeader String SessionToken) {
        logger.info("Resetting user settings with session token {}", SessionToken);
        long userId = getUserIdBySessionToken(SessionToken);
        if (userId==-1)return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        UserSettings defaultSettings = UserSettings.getDefaultUserSettings();
        defaultSettings.setUserId((int) userId);
        userPageService.updateUserSettings(defaultSettings);
        return new ResponseEntity<>(defaultSettings, HttpStatus.OK);
    }

    @PostMapping("/set-user-preferences")
    public ResponseEntity<?> setUserPreferences(@RequestHeader String SessionToken, @RequestBody UserPreferences userPreferences) {
        logger.info("Setting user preferences with session token {}", SessionToken);
        long userId = getUserIdBySessionToken(SessionToken);
        if (userId==-1)return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        userPageService.setUserPreferences(userId, userPreferences);
        return new ResponseEntity<>(Map.of("status", "success"), HttpStatus.OK);
    }

    private long getUserIdBySessionToken(String sessionToken) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(Constants.USER_SESSION_SECRET_KEY)
                    .parseClaimsJws(sessionToken)
                    .getBody();
            return Long.parseLong(claims.getSubject());
        } catch (Exception e) {
            return -1;
        }
    }

}
