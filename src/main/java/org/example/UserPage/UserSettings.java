package org.example.UserPage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import java.sql.Timestamp;

@Entity
@Table(name = "user_settings")
public class UserSettings {
    //todo: add public/private profile
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "setting_id") private int settingId;
    @Column(name = "user_id") private Integer userId;
    @Column(name = "preferred_language") private String preferredLanguage;
    @Column(name = "solution_detail_level") private String solutionDetailLevel;
    @Column(name= "is_private_profile") private boolean isPrivateProfile;
    @Column(name = "created_at", updatable = false, insertable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP") private Timestamp createdAt;
    @Column(name = "updated_at", insertable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP") private Timestamp updatedAt;
    public UserSettings() {}
    public UserSettings(Integer userId, String preferredLanguage, String solutionDetailLevel,boolean isPrivateProfile) {
        this.userId = userId;
        this.preferredLanguage = preferredLanguage;
        this.solutionDetailLevel = solutionDetailLevel;
        this.isPrivateProfile = isPrivateProfile;
    }
    public int getSettingId() {return settingId;}
    public Integer getUserId() {return userId;}
    public void setUserId(Integer userId) {this.userId = userId;}
    public String getPreferredLanguage() {return preferredLanguage;}
    public boolean getIsPrivateProfile() {return isPrivateProfile;}
    public void setPreferredLanguage(String preferredLanguage) {this.preferredLanguage = preferredLanguage;}
    public String getSolutionDetailLevel() {return solutionDetailLevel;}
    public void setSolutionDetailLevel(String solutionDetailLevel) {this.solutionDetailLevel = solutionDetailLevel;}
    public void setIsPrivateProfile(boolean isPrivateProfile) {this.isPrivateProfile = isPrivateProfile;}
    public Timestamp getCreatedAt() {return createdAt;}
    public Timestamp getUpdatedAt() {return updatedAt;}
    public static UserSettings getDefaultUserSettings() {return new UserSettings(-1,"en","basic",true);}

    public String toJson() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert UserSettings to JSON", e);
        }
    }
}
