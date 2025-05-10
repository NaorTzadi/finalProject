package org.example.UserPage;
import jakarta.persistence.*;

@Entity
@Table(name = "user_preferences")
public class UserPreferences {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "preference_id") private long id;
    @Column(name = "user_id", nullable = false, unique = true) private long userId;
    @Column(name = "date_of_birth") private String dateOfBirth;
    @Column(name = "gender") private String gender;
    @Column(name = "purpose") private String purpose;
    @Column(name = "background") private String background;
    @Column(name = "is_private", columnDefinition = "TINYINT DEFAULT 1") private boolean isPrivate;
    public UserPreferences() {}
    public UserPreferences(long userId, String dateOfBirth, String gender, String purpose, String background, boolean isPrivate) {
        this.userId = userId;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.purpose = purpose;
        this.background = background;
        this.isPrivate = isPrivate;
    }
    public long getId() { return id; }
    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }
    public String getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }
    public String getBackground() { return background; }
    public void setBackground(String background) { this.background = background; }
    public boolean getIsPrivate() { return isPrivate; }
    public void setIsPrivate(boolean isPrivate) { this.isPrivate = isPrivate; }
}
