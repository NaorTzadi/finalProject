package org.example.Admin;
import org.example.Users.User;
import org.example.Users.UsersService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminController {
    //anyone who uses the /admin url needs to be checked to see if he is really an admin.

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);
    private final DataSource dataSource;
    private final AdminService adminService;
    private final UsersService usersService;

    public AdminController(DataSource dataSource, AdminService adminService, UsersService usersService) {
        this.dataSource = dataSource;
        this.adminService=adminService;
        this.usersService = usersService;
    }

    @PostMapping("/add_admin")
    public ResponseEntity<String> addAdmin(
            @RequestParam long adminId,
            @RequestParam long userId,
            @RequestBody List<Admin.Permission> permissions) {
        final Admin.Permission permission=Admin.Permission.ADD_ADMIN;
        if (!adminService.isUserAdmin(adminId)) return ResponseEntity.badRequest().body("User with ID " + userId + " is not an admin!");
        if (!adminService.isAdminPermitted(adminId, permission))return ResponseEntity.badRequest().body("admin doesnt have "+ permission+" permission.");
        if (!usersService.doesUserExist(userId))return ResponseEntity.badRequest().body("User with ID " + userId + " does not exist!");
        if (adminService.isUserAdmin(userId)) return ResponseEntity.badRequest().body("User with ID " + userId + " is already an admin!");

        StringBuilder response = new StringBuilder();
        for (Admin.Permission adminPermission : permissions) response.append(adminService.isAdminPermitted(adminId, adminPermission) ? "" : "user by " + adminId + " doesnt have permission for '" + adminPermission+"'");
        if (!response.isEmpty()) return ResponseEntity.badRequest().body(response.toString());

        String permissionsString = permissions.stream().map(Enum::name).reduce((p1, p2) -> p1 + "," + p2).orElse("");
        String sql = "INSERT INTO admins (admin_id, permissions) VALUES (?, ?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setString(2, permissionsString);
            ps.executeUpdate();
            return ResponseEntity.ok(HttpStatus.OK.toString());
        } catch (SQLException e) {logger.error("Error adding user to admin table", e);}
        return ResponseEntity.status(500).body("Error: Could not add user to admin table.");
    }
    @DeleteMapping("/remove_admin")
    public ResponseEntity<String> removeAdmin(
            @RequestParam long adminId,
            @RequestParam long userId){
        final Admin.Permission permission = Admin.Permission.REMOVE_USER;
        if (!adminService.isUserAdmin(userId)) return ResponseEntity.badRequest().body("User with ID " + userId + " is not an admin!");
        if (!adminService.isAdminPermitted(adminId, permission))return ResponseEntity.badRequest().body("admin doesnt have "+ permission+" permission.");
        String sql = "DELETE FROM admins WHERE admin_id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.executeUpdate();
            return ResponseEntity.ok(HttpStatus.OK.toString());
        } catch (SQLException e) {logger.error("Error removing admin from admin.", e);}
        return new ResponseEntity<>("Error removing admin from admin.",HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @PostMapping("/add_topic")
    public ResponseEntity<String> addTopic(
            @RequestParam int adminId,
            @RequestParam String topicName,
            @RequestParam(required = false) Integer parentTopicId) {
        final Admin.Permission permission = Admin.Permission.ADD_USER;
        if (!adminService.isUserAdmin(adminId)) return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User is not an admin!");
        if (!adminService.isAdminPermitted(adminId, permission))return ResponseEntity.status(HttpStatus.FORBIDDEN).body("admin doesnt have "+ permission+" permission.");
        String sql = "INSERT INTO topics (topic_name, parent_topic_id) VALUES (?, ?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, topicName);
            if (parentTopicId != null) {
                ps.setInt(2, parentTopicId);
            } else {
                ps.setNull(2, java.sql.Types.INTEGER);
            }
            ps.executeUpdate();
            return ResponseEntity.ok("Topic added successfully!");
        } catch (SQLException e) {
            logger.error("Error adding topic to database", e);
        }
        return new ResponseEntity<>("Error adding topic to database.",HttpStatus.INTERNAL_SERVER_ERROR);
    }
    @GetMapping("/permissions/{adminId}")
    public ResponseEntity<?> getAdminPermissions(@PathVariable long adminId) {
        if (!adminService.isUserAdmin(adminId)) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Admin with ID " + adminId + " does not exist.");
        List<Admin.Permission> permissions=adminService.getAdminPermissions(adminId);
        if (permissions!=null)return ResponseEntity.ok(permissions);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Permissions not found for admin ID: " + adminId);
    }



}
