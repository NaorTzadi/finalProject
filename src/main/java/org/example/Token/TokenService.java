package org.example.Token;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.example.Constants;
import org.example.CustomLogger;
import org.springframework.stereotype.Service;
import javax.sql.DataSource;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;
import java.util.Date;

@Service
public class TokenService {
    private static final CustomLogger logger =new CustomLogger(TokenService.class);
    private final DataSource dataSource;

    public TokenService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public String generateUserSessionToken(long userId) {
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .setIssuedAt(new Date())
                //.setExpiration(new Date(System.currentTimeMillis() + (24 * 60 * 60 * 1000)))
                .signWith(SignatureAlgorithm.HS512, Constants.USER_SESSION_SECRET_KEY)
                .compact();
    }

    public String generateAndStoreResetToken(long userId) {
        String token = generateSecureToken();
        try (Connection conn = dataSource.getConnection()) {
            String insertQuery = "INSERT INTO reset_password_tokens (user_id, token, created_at, expires_at, used) " +
                    "VALUES (?, ?, NOW(), DATE_ADD(NOW(), INTERVAL 1 HOUR), FALSE)";
            try (PreparedStatement stmt = conn.prepareStatement(insertQuery)) {
                stmt.setLong(1, userId);
                stmt.setString(2, token);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            logger.error("Failed to store reset token for user ID {}: {}", userId, e.getMessage());
            return null;
        }
        return token;
    }

    public String generateAndStoreVerifyToken() {
        return generateSecureToken();
    }
    public String storeUpdatedAccessToken(String token) {
        String newToken = generateSecureToken();
        String updateQuery = "UPDATE fast_access_tokens SET token = ?, created_at = NOW() WHERE token = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(updateQuery)) {
            stmt.setString(1, newToken);
            stmt.setString(2, token);

            int rowsUpdated = stmt.executeUpdate();

            if (rowsUpdated == 0) {
                logger.warn("No matching token found to update.");
                return null;
            }
        } catch (SQLException e) {
            logger.error("Error updating fast access token for token: {}", token, e);
            return null;
        }
        return newToken;
    }
    public boolean doesExistInResetPasswordTokensTable(long userId){
        String query = "SELECT COUNT(*) FROM reset_password_tokens WHERE user_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setLong(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) {logger.error("Error checking existence in reset_password_tokens: {}", userId, e);}
        return false;
    }
    public boolean doesExistInResetPasswordTokensTable(String token) {
        String query = "SELECT COUNT(*) FROM reset_password_tokens WHERE token = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, token);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) {logger.error("Error checking existence in reset_password_tokens: {}", token, e);}
        return false;
    }
    public long getUserIdFromResetPasswordTokensTable(String token) {
        String fetchUserIdQuery = "SELECT user_id FROM reset_password_tokens WHERE token = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement fetchUserStmt = conn.prepareStatement(fetchUserIdQuery)) {
            fetchUserStmt.setString(1, token);
            try (ResultSet rs = fetchUserStmt.executeQuery()) {if (rs.next()) return rs.getLong("user_id");}
        } catch (SQLException e) {logger.error("Error fetching user ID for token: {}", token, e);}
        return -1;
    }
    public void removeResetPasswordToken(String token) {
        String sql = "DELETE FROM reset_password_tokens WHERE token = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, token);
            stmt.executeUpdate();
        } catch (SQLException e) {logger.error("Error removing reset password token: {}", token, e);}
    }

    public boolean doesExistInFastAccessTokensTable(String token) {
        String query = "SELECT COUNT(*) FROM fast_access_tokens WHERE token = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, token);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) {logger.error("Error checking existence in fast_access_tokens: {}", token, e);}
        return false;
    }
    public boolean doesExistInFastAccessTokensTable(long userId) {
        String query = "SELECT COUNT(*) FROM fast_access_tokens WHERE user_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setLong(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) {logger.error("Error checking existence in fast_access_tokens for user ID: {}", userId, e);}
        return false;
    }

    public String storeNewFastAccessToken(long userId) {
        String newToken = generateSecureToken();
        String insertQuery = "INSERT INTO fast_access_tokens (user_id, token, created_at) VALUES (?, ?, NOW())";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(insertQuery)) {
            stmt.setLong(1, userId);
            stmt.setString(2, newToken);
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error inserting new fast access token for user ID: {}", userId, e);
            return null;
        }
        return newToken;
    }
    public void removeFastAccessToken(long userId) {
        String deleteRow = "DELETE FROM fast_access_tokens WHERE user_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(deleteRow)) {
            stmt.setLong(1, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error removing fast access token for user ID: {}", userId, e);
        }
    }

    public long getUserIdFromFastAccessTokensTable(String token) {
        String fetchUserIdQuery = "SELECT user_id FROM fast_access_tokens WHERE token = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement fetchUserStmt = conn.prepareStatement(fetchUserIdQuery)) {
            fetchUserStmt.setString(1, token);
            try (ResultSet rs = fetchUserStmt.executeQuery()) {if (rs.next()) return rs.getLong("user_id");}
        } catch (SQLException e) {
            logger.error("Error fetching user ID for token: {}", token, e);
        }
        return -1;
    }

    private String generateSecureToken() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] tokenBytes = new byte[32];
        secureRandom.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }


}
