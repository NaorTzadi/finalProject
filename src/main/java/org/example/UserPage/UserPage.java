package org.example.UserPage;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_page")
public class UserPage {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_page_id") private Long userPageId;
    @Column(name = "user_id", nullable = false, unique = true) private Long userId;
    @Column(name = "biography") private String biography;
    @Column(name = "profile_image_url") private String profileImagePath;
    @Column(name = "created_at", updatable = false) private Timestamp createdAt;
    @JsonIgnore @Column(name = "updated_at") private Timestamp updatedAt;
    public UserPage() {}
    public UserPage(Long userId,String biography,String profileImagePath,Timestamp createdAt) {
        this.userId = userId;
        this.biography = biography;
        this.profileImagePath = profileImagePath;
        this.createdAt = createdAt;
    }
    public Long getUserPageId() { return userPageId; }
    public Long getUserId() { return userId; }
    public String getBiography() { return biography; }
    public String getProfileImagePath() { return profileImagePath; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setBiography(String biography) { this.biography = biography; }
    public void setProfileImagePath(String profileImagePath) { this.profileImagePath = profileImagePath; }
    @PreUpdate public void setLastUpdate() {this.updatedAt = Timestamp.valueOf(LocalDateTime.now());}

}
