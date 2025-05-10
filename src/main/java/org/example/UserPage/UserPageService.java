package org.example.UserPage;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import javax.sql.DataSource;
import jakarta.transaction.Transactional;
import java.sql.*;

@Service
public class UserPageService {
    private static final Logger logger = LoggerFactory.getLogger(UserPageService.class);
    private final DataSource dataSource;
    public UserPageService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public UserPage getUserPageData(long userId) {
        String query = "SELECT profile_image_url, biography, created_at FROM user_page WHERE user_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setLong(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String profileImageUrl = rs.getString("profile_image_url");
                String biography = rs.getString("biography");
                Timestamp createdAt = rs.getTimestamp("created_at");
                return new UserPage(userId, profileImageUrl, biography, createdAt);
            }
        } catch (SQLException e) {logger.error("Error retrieving user page for userId {}: {}", userId, e.getMessage());}
        return null;
    }

    @Transactional
    public void updateUserProfileAvatar(long userId, String avatarPath) {
        try {
            String sql = "UPDATE user_page SET profile_image_url = ? WHERE user_id = ?";
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, avatarPath);
                ps.setLong(2, userId);
                ps.executeUpdate();
            }
        } catch (SQLException e) {logger.error("Error updating profile image for userId {}: {}", userId, e.getMessage());}
    }

    @Transactional
    public void updateUserBiography(long userId, String biography) {
        String sql = "UPDATE user_page SET biography = ? WHERE user_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, biography);
            ps.setLong(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {logger.error("Error updating biography for userId {}: {}", userId, e.getMessage());}
    }
//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%<<{ Settings }>>%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

    @PersistenceContext
    private EntityManager entityManager;
    @Transactional
    public UserSettings getUserSettingsByUserId(long userId) {
        try {
            return entityManager.createQuery(
                            "SELECT u FROM UserSettings u WHERE u.userId = :userId", UserSettings.class)
                    .setParameter("userId", userId)
                    .getSingleResult();
        } catch (Exception e) {logger.error("Unexpected error while fetching UserSettings for user_id: {}", userId, e);}
        return null;
    }
    @Transactional
    public void updateUserSettings(UserSettings userSettings) {
        try {
            if (userSettings.getSettingId() == 0) entityManager.persist(userSettings);
            else entityManager.merge(userSettings);
        } catch (Exception e) {logger.error("Failed to save UserSettings for user_id: {}", userSettings.getUserId(), e);}
    }

//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%<<{ PREFERENCES }>>%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

    @Transactional
    public void setUserPreferences(long userId, UserPreferences userPreferences) {
        try {
            UserPreferences existingPreferences = entityManager.createQuery(
                            "SELECT u FROM UserPreferences u WHERE u.userId = :userId", UserPreferences.class)
                    .setParameter("userId", userId)
                    .getSingleResult();
            existingPreferences.setDateOfBirth(userPreferences.getDateOfBirth());
            existingPreferences.setGender(userPreferences.getGender());
            existingPreferences.setPurpose(userPreferences.getPurpose());
            existingPreferences.setBackground(userPreferences.getBackground());
            existingPreferences.setIsPrivate(userPreferences.getIsPrivate());
            entityManager.merge(existingPreferences);
        } catch (Exception e) {logger.error("Error updating user preferences for userId {}: {}", userId, e.getMessage());}
    }

}
