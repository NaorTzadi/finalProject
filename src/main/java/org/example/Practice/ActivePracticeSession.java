package org.example.Practice;
import jakarta.persistence.*;
import org.example.Practice.Settings.CategorySettings;
import org.example.Practice.Settings.Options.*;
import java.time.LocalDateTime;
@Entity
@Table(name = "active_practice_session", schema = "practice_session")
public class ActivePracticeSession {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long practiceSessionId;
    @Column(nullable = false, unique = true) private String sessionToken;
    @Column(nullable = false) private long userId;
    @Column(nullable = false) private int level;
    @Enumerated(EnumType.STRING) private MathCategory mathCategory;
    @OneToOne(cascade = CascadeType.ALL) private CategorySettings categorySettings;
    @Enumerated(EnumType.STRING)
    @Column(name = "solution_detail", nullable = false) private SolutionDetail solutionDetail;
    @Column(nullable = false) private LocalDateTime date = LocalDateTime.now();
    protected ActivePracticeSession() {}
    public ActivePracticeSession(String sessionToken, long userId, int level, MathCategory mathCategory,
                                 CategorySettings categorySettings, SolutionDetail solutionDetail) {
        this.sessionToken = sessionToken;
        this.userId = userId;
        this.level = level;
        this.mathCategory = mathCategory;
        this.categorySettings = categorySettings;
        this.solutionDetail = solutionDetail;
    }
    public Long getPracticeSessionId() {
        return practiceSessionId;
    }
    public void setPracticeSessionId(Long practiceSessionId) {
        this.practiceSessionId = practiceSessionId;
    }
    public String getSessionToken() {
        return sessionToken;
    }
    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }
    public long getUserId() {
        return userId;
    }
    public void setUserId(long userId) {
        this.userId = userId;
    }
    public int getLevel() {
        return level;
    }
    public void setLevel(int level) {
        this.level = level;
    }
    public MathCategory getMathCategory() {
        return mathCategory;
    }
    public void setMathCategory(MathCategory mathCategory) {
        this.mathCategory = mathCategory;
    }
    public CategorySettings getCategorySettings() {
        return categorySettings;
    }
    public void setCategorySettings(CategorySettings categorySettings) {
        this.categorySettings = categorySettings;
    }
    public SolutionDetail getSolutionDetail() {
        return solutionDetail;
    }
    public void setSolutionDetail(SolutionDetail solutionDetail) {
        this.solutionDetail = solutionDetail;
    }
    public LocalDateTime getDate() {
        return date;
    }
    public void setDate(LocalDateTime date) {this.date = date;}

    @Override
    public String toString() {
        return "ActivePracticeSession{" +
                "practiceSessionId=" + practiceSessionId +
                ", sessionToken='" + sessionToken + '\'' +
                ", userId=" + userId +
                ", level=" + level +
                ", mathCategory=" + mathCategory +
                ", categorySettings=" + categorySettings +
                ", solutionDetail=" + solutionDetail +
                ", date=" + date +
                '}';
    }
}
