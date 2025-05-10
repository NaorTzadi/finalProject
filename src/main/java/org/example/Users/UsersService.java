package org.example.Users;
import org.example.CustomLogger;
import org.example.PasswordSecurity;
import org.example.UserPage.UserSettings;
import org.example.VerificationController;
import org.example.Practice.*;
import org.springframework.stereotype.Service;
import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

@Service
public class UsersService {
    private final static CustomLogger logger = new CustomLogger(UsersService.class);
    private final static String USERS_TABLE = "users";
    public final static String UNVERIFIED_USERS_TABLE="unverified_users";
    private final DataSource dataSource;
    public UsersService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public String getSessionToken(long userId) {
        String sql = "SELECT session_token FROM "+ USERS_TABLE+" WHERE userId = ? AND session_token IS NOT NULL AND session_token != ''";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getString("session_token");
        } catch (Exception e) {logger.error("Error during getSessionToken.", e);}
        return null;
    }

    public String getSessionToken(String username) {
        String sql = "SELECT session_token FROM "+ USERS_TABLE+" WHERE username = ? AND session_token IS NOT NULL AND session_token != ''";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getString("session_token");
        } catch (Exception e) {logger.error("Error during getSessionToken retrieval.", e);}
        return null;
    }

    public void setSessionTokenBySessionToken(String oldSessionToken,String newSessionToken) {
        String sql = "UPDATE "+ USERS_TABLE+" SET session_token = ? WHERE session_token = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newSessionToken);
            stmt.setString(2, oldSessionToken);
            stmt.executeUpdate();
        } catch (Exception e) {logger.error("Error while setting session token: " + oldSessionToken, e);}
    }

    public void setSessionTokenByUsername(String username, String sessionToken) {
        String sql = "UPDATE "+ USERS_TABLE+" SET session_token = ? WHERE username = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, sessionToken);
            stmt.setString(2, username);
            stmt.executeUpdate();
        } catch (Exception e) {logger.error("Error while setting session token: " + username, e);}
    }
    public void setSessionTokenByUserId(long userId, String sessionToken) {
        String sql = "UPDATE "+ USERS_TABLE+" SET session_token = ? WHERE userId = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, sessionToken);
            stmt.setLong(2, userId);
            stmt.executeUpdate();
        } catch (Exception e) {logger.error("Error while setting session token: " + userId, e);}
    }
    public void setSessionToken(String columnName, Object columnValue, String sessionToken) {
        String sql = "UPDATE " + USERS_TABLE + " SET session_token = ? WHERE " + columnName + " = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, sessionToken);
            stmt.setObject(2, columnValue); // Use setObject to handle different data types
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error while updating session token for column: " + columnName + " with value: " + columnValue, e);
        }
    }

    public void removeAllSessionTokens() {
        String sql = "UPDATE "+USERS_TABLE+" SET session_token = NULL";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean resetPassword(String newPassword, long userId) {
        try (Connection connection = dataSource.getConnection()) {
            byte[] salt = PasswordSecurity.getNewSalt();
            String hashedPassword = PasswordSecurity.hashPassword(newPassword,salt);
            String sql = "UPDATE "+USERS_TABLE+" SET password = ?, salt = ?, updated_at = CURRENT_TIMESTAMP WHERE user_id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, hashedPassword);
                stmt.setBytes(2, salt);
                stmt.setLong(3, userId);
                int rowsUpdated = stmt.executeUpdate();
                return rowsUpdated > 0;
            }
        } catch (Exception e) {logger.error("Error during password reset.", e);}
        return false;
    }
    public boolean doesUserExist(long userId){
        String sql = "SELECT COUNT(*) FROM users WHERE user_id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {if (rs.next()) return rs.getInt(1) > 0;}
        } catch (SQLException e) {logger.error("Error checking if user is an admin", e);}
        return false;
    }
    public boolean doesUserExist(String username, String password) {
        String sql = "SELECT password, salt FROM "+USERS_TABLE+" WHERE username = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String storedPassword = rs.getString("password");
                    byte[] salt = rs.getBytes("salt");
                    String hashedInputPassword = PasswordSecurity.hashPassword(password, salt);
                    return storedPassword.equals(hashedInputPassword);
                }
            }
        } catch (SQLException e) {logger.error("Error checking user existence", e);}
        return false;
    }

    public void addUnverifiedUser(User user, String password, byte[] salt, String token) {
        String sql = "INSERT INTO unverified_users (username, email, password, salt, token, expires_at) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getEmail());
            ps.setString(3, password);
            ps.setBytes(4, salt);
            ps.setString(5, token);
            ps.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now().plusHours(24)));
            ps.executeUpdate();
        } catch (SQLException e) {logger.error("Error adding unverified user to database", e);}
    }

    public User getUser(String identifier) {
        long userId;
        if (identifier.matches(VerificationController.USERNAME_REGEX)) userId = getUserIdByUsername(identifier);
        else userId = getUserIdBySessionToken(identifier);
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
                "GROUP BY u.user_id, u.username, u.password, u.email, u.salt,u.session_token";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new User(
                            rs.getLong("user_id"),
                            rs.getString("username"),
                            rs.getString("email"),
                            getUserSessions(userId),
                            getUserSettings(userId),
                            rs.getString("session_token")
                    );
                }
            }
        } catch (SQLException e) {logger.error("Error fetching user details by ID", e);}
        return null;
    }
    public Long addVerifiedUser(String token) {
        String selectSql = "SELECT * FROM unverified_users WHERE token = ?";
        String insertSql = "INSERT INTO users (username, email, password, salt, created_at) VALUES (?, ?, ?, ?, NOW())";
        String deleteSql = "DELETE FROM unverified_users WHERE token = ?";

        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement selectPs = connection.prepareStatement(selectSql)) {
                selectPs.setString(1, token);
                try (ResultSet rs = selectPs.executeQuery()) {
                    if (rs.next()) {
                        String username = rs.getString("username");
                        String email = rs.getString("email");
                        String password = rs.getString("password");
                        byte[] salt = rs.getBytes("salt");

                        try (PreparedStatement insertPs = connection.prepareStatement(insertSql, PreparedStatement.RETURN_GENERATED_KEYS)) {
                            insertPs.setString(1, username);
                            insertPs.setString(2, email);
                            insertPs.setString(3, password);
                            insertPs.setBytes(4, salt);
                            insertPs.executeUpdate();

                            try (ResultSet generatedKeys = insertPs.getGeneratedKeys()) {
                                if (generatedKeys.next()) {
                                    long userId = generatedKeys.getLong(1);
                                    try (PreparedStatement deletePs = connection.prepareStatement(deleteSql)) {
                                        deletePs.setString(1, token);
                                        deletePs.executeUpdate();
                                    }
                                    connection.commit();
                                    logger.info("User {} has been verified and added to the users table with ID: {}", username, userId);
                                    return userId;
                                }
                            }
                        }
                    } else {
                        logger.warn("Invalid or expired token: {}", token);
                    }
                }
            }
            connection.rollback();
        } catch (SQLException e) {
            logger.error("Error handling verified user for token: {}", token, e);
        }
        return -1L;
    }

    public boolean isUsernameTaken(String username) {
        String usersSql = "SELECT COUNT(*) FROM users WHERE username = ?";
        String unverifiedUsersSql = "SELECT COUNT(*) FROM unverified_users WHERE username = ?";
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement(usersSql)) {
                ps.setString(1, username);
                try (ResultSet rs = ps.executeQuery()) {if (rs.next() && rs.getInt(1) > 0) return true;}
            }
            try (PreparedStatement psUnverified = connection.prepareStatement(unverifiedUsersSql)) {
                psUnverified.setString(1, username);
                try (ResultSet rsUnverified = psUnverified.executeQuery()) {if (rsUnverified.next() && rsUnverified.getInt(1) > 0) return true;}
            }
        } catch (Exception e) {
            logger.error("Error checking if username exists in both tables", e);
        }
        return false;
    }
    public boolean isEmailTaken(String email) {
        String usersSql = "SELECT COUNT(*) FROM users WHERE email = ?";
        String unverifiedUsersSql = "SELECT COUNT(*) FROM unverified_users WHERE email = ?";
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement(usersSql)) {
                ps.setString(1, email);
                try (ResultSet rs = ps.executeQuery()) {if (rs.next() && rs.getInt(1) > 0) return true;}
            }
            try (PreparedStatement psUnverified = connection.prepareStatement(unverifiedUsersSql)) {
                psUnverified.setString(1, email);
                try (ResultSet rsUnverified = psUnverified.executeQuery()) {if (rsUnverified.next() && rsUnverified.getInt(1) > 0) return true;}
            }
        } catch (Exception e) {
            logger.error("Error checking if email exists in both tables", e);
        }
        return false;
    }
    public UserSettings getUserSettings(long userId){
        String sql="SELECT * FROM user_settings WHERE user_id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String preferredLanguage = rs.getString("preferred_language");
                    String solutionDetailLevel = rs.getString("solution_detail_level");
                    boolean isPrivate=rs.getBoolean("is_private_profile");
                    return new UserSettings((int)userId,preferredLanguage, solutionDetailLevel,isPrivate);
                }
            }
        } catch (SQLException e) {
            logger.error("Error fetching user settings for user ID: {}", userId, e);
        }
        return null;
    }
    public ArrayList<PracticeSession> getUserSessions(long userId) {
        return null;
    }
    public void addFailedLoginAttempt(String username, String ipAddress, String userAgent) {
        long userId = getUserIdByUsername(username);
        String checkQuery = "SELECT attempt_count FROM failed_login_attempts WHERE user_id = ?";
        String updateQuery = "UPDATE failed_login_attempts SET attempt_count = attempt_count + 1, last_attempt_time = CURRENT_TIMESTAMP WHERE user_id = ?";
        String insertQuery = "INSERT INTO failed_login_attempts (user_id, username, ip_address, user_agent, attempt_count, created_at, last_attempt_time) " +
                "VALUES (?, ?, ?, ?, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";

        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
                checkStmt.setLong(1, userId);
                ResultSet rs = checkStmt.executeQuery();

                if (rs.next()) {
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                        updateStmt.setLong(1, userId);
                        updateStmt.executeUpdate();
                    }
                } else {
                    try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
                        insertStmt.setLong(1, userId);
                        insertStmt.setString(2, username);
                        insertStmt.setString(3, ipAddress);
                        insertStmt.setString(4, userAgent);
                        insertStmt.executeUpdate();
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Error adding login attempt {}", userId, e);
        }
    }
    public boolean shouldSendAlert(String username) {
        final int maxAttempts=10;
        long userId = getUserIdByUsername(username);

        String query = "SELECT COUNT(*) FROM failed_login_attempts " +
                "WHERE user_id = ? AND last_attempt_time >= (CURRENT_TIMESTAMP - INTERVAL 10 MINUTE)";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setLong(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int attemptCount = rs.getInt(1);
                return attemptCount >= maxAttempts;
            }
        } catch (SQLException e) {
            logger.error("Error checking failed login attempts for user {}", userId, e);
        }
        return false;
    }

    public void removeUserFailedLoginAttempts(String username) {
        String sql="DELETE FROM failed_login_attempts WHERE username = ?";
        try (Connection connection=dataSource.getConnection();
        PreparedStatement ps=connection.prepareStatement(sql)){
            ps.setString(1, username);
            ps.executeUpdate();
        }catch (SQLException e) {
            logger.error("Error deleting failed login attempts for user {}: {}", username, e.getMessage());
        }
    }

/*%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%<<<< GARBAGE_METHODS>>>>%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%*/

    private <T> Optional<T> fetchSingleResult(String query, Object parameter, String columnLabel, Class<T> type) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setObject(1, parameter);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.ofNullable(rs.getObject(columnLabel, type));
        } catch (SQLException e) {System.err.println("Database query error: " + e.getMessage());}
        return Optional.empty();
    }
    public long getUserIdBySessionToken(String sessionToken) {
        String sql = "SELECT user_id FROM "+ USERS_TABLE+" WHERE session_token = ?";
        return fetchSingleResult(sql, sessionToken, "user_id", Long.class).orElse(-1L);
    }
    public long getUserIdByEmail(String email) {
        String query = "SELECT user_id FROM "+ USERS_TABLE+" WHERE email = ?";
        return fetchSingleResult(query, email, "user_id", Long.class).orElse(-1L);
    }
    public long getUserIdByUsername(String username) {
        String query = "SELECT user_id FROM "+ USERS_TABLE+" WHERE username = ?";
        return fetchSingleResult(query, username, "user_id", Long.class).orElse(-1L);
    }
    public Optional<String> getUsernameById(long userId) {
        String query = "SELECT username FROM "+ USERS_TABLE+" WHERE user_id = ?";
        return fetchSingleResult(query, userId, "username", String.class);
    }
    public String getUsernameByEmail(String email) {
        String query = "SELECT username FROM "+ USERS_TABLE+" users WHERE email = ?";
        return fetchSingleResult(query, email, "username", String.class).orElse(null);
    }
    public String getEmailByUsername(String username) {
        String query = "SELECT email FROM "+ USERS_TABLE +" WHERE username = ?";
        return fetchSingleResult(query, username, "email", String.class).orElse(null);
    }
}
