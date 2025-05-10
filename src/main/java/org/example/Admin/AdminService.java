package org.example.Admin;
import org.example.Users.UsersService;
import org.example.VerificationController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.example.Users.User;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Service
public class AdminService {
    private static final Logger logger = LoggerFactory.getLogger(AdminService.class);
    private final DataSource dataSource;
    private final UsersService usersService;
    @Autowired
    public AdminService(DataSource dataSource, UsersService usersService) {
        this.dataSource = dataSource;
        this.usersService = usersService;
    }
    public List<Admin.Permission> getAdminPermissions(long adminId) {
        String sql = "SELECT p.permission_name FROM admin_permissions ap " +
                "JOIN permissions p ON ap.permission_id = p.permission_id " +
                "WHERE ap.user_id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, adminId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Admin.Permission> permissions = new ArrayList<>();
                while (rs.next()) {
                    String permissionName = rs.getString("permission_name");
                    permissions.add(Admin.Permission.valueOf(permissionName));
                }
                return permissions;
            }
        } catch (SQLException e) {
            logger.error("Error fetching permissions for admin ID: " + adminId, e);
        }
        return null;
    }
    public boolean isUserAdmin(long userId) {
        String sql = "SELECT COUNT(*) FROM admins WHERE admin_id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {if (rs.next()) return rs.getInt(1) > 0;}
        } catch (SQLException e) {logger.error("Error checking if user is an admin", e);}
        return false;
    }

    public boolean isAdminPermitted(long adminId, Admin.Permission permission) {
        String sql = "SELECT COUNT(*) FROM admin_permissions ap " +
                "JOIN permissions p ON ap.permission_id = p.permission_id " +
                "WHERE ap.user_id = ? AND p.permission_name = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, adminId);
            ps.setString(2, permission.name());
            try (ResultSet rs = ps.executeQuery()) {if (rs.next()) return rs.getInt(1) > 0;}
        } catch (SQLException e) {logger.error("Error checking if admin has permission", e);}
        return false;
    }

    public User getUser(String identifier) {
        long userId;
        if (identifier.matches(VerificationController.USERNAME_REGEX)) userId = usersService.getUserIdByUsername(identifier);
        else userId = usersService.getUserIdBySessionToken(identifier);
        return userId != -1 ? getUser(userId) : null;
    }
    public User getUser(long userId) {
        String sql = "SELECT u.user_id, u.username, u.password, u.email, u.salt,u.session_token, " +
                "GROUP_CONCAT(p.permission_name) AS permissions " +
                "FROM users u " +
                "LEFT JOIN admins a ON u.user_id = a.admin_id " +
                "LEFT JOIN admin_permissions ap ON a.admin_id = ap.user_id " +
                "LEFT JOIN permissions p ON ap.permission_id = p.permission_id " +
                "WHERE u.user_id = ? " +
                "GROUP BY u.user_id, u.username, u.password, u.email, u.salt, u.session_token";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User user = new User(
                            rs.getLong("user_id"),
                            rs.getString("username"),
                            rs.getString("email"),
                            usersService.getUserSessions(userId),
                            usersService.getUserSettings(userId),
                            rs.getString("session_token")
                    );
                    String permissionsString = rs.getString("permissions");
                    if (permissionsString != null && !permissionsString.isEmpty()) {
                        return new Admin(
                                user.getUser_id(),
                                user.getUsername(),
                                user.getEmail(),
                                user.getSessions(),
                                user.getUserSettings(),
                                rs.getString("session_token"),
                                getAdminPermissions(userId)
                        );
                    }
                    return user;
                }
            }
        } catch (SQLException e) {logger.error("Error fetching user details by ID", e);}
        return null;
    }
    //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%<<{ DEVELOPMENT }>>%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    public void populateAdminPermissionsTable() {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement clearStmt = connection.prepareStatement("TRUNCATE TABLE permissions")) {clearStmt.executeUpdate();}
            String sql = "INSERT INTO permissions (permission_name) VALUES (?)";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                for (Admin.Permission permission : Admin.Permission.values()) {
                    ps.setString(1, permission.name());
                    ps.addBatch();
                }
                ps.executeBatch();
            }
        } catch (SQLException e) {logger.error("Error populating permissions", e);}
    }
    public void addAllPermissions(long userId) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            String insertAdminSql = "INSERT IGNORE INTO admins (admin_id) VALUES (?)";
            try (PreparedStatement ps = connection.prepareStatement(insertAdminSql)) {
                ps.setLong(1, userId);
                ps.executeUpdate();
            }
            String deletePermissionsSql = "DELETE FROM admin_permissions WHERE user_id = ?";
            try (PreparedStatement ps = connection.prepareStatement(deletePermissionsSql)) {
                ps.setLong(1, userId);
                ps.executeUpdate();
            }
            String insertPermissionsSql = "INSERT INTO admin_permissions (user_id, permission_id) SELECT ?, permission_id FROM permissions";
            try (PreparedStatement ps = connection.prepareStatement(insertPermissionsSql)) {
                ps.setLong(1, userId);
                ps.executeUpdate();
            }
            connection.commit();
        } catch (SQLException e) {logger.error("Error resetting user permissions and assigning all permissions: " + userId, e);}
    }

}
