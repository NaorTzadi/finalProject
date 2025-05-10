package org.example.Practice;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import org.example.Practice.Settings.CategorySettings;
import org.example.Practice.Settings.Options.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "practice_session", schema = "practice_session")
public class PracticeSession {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    private long userId;
    private int level;
    @Enumerated(EnumType.STRING) private MathCategory mathCategory;
    @OneToOne(cascade = CascadeType.ALL) private CategorySettings categorySettings;
    @Column(name = "solution_detail", nullable = false) private SolutionDetail solutionDetail;
    @OneToMany(mappedBy = "practiceSession", cascade = CascadeType.ALL, orphanRemoval = true) private List<MathProblem> mathProblems = new ArrayList<>();
    @Column(name = "date", nullable = false) private LocalDateTime date = LocalDateTime.now();
    private long timeTaken;
    protected PracticeSession() {}
    public PracticeSession(long userId,MathCategory mathCategory, CategorySettings categorySettings, SolutionDetail solutionDetail, List<MathProblem> mathProblems) {
        this.userId = userId;
        this.level = 0;
        this.mathCategory = mathCategory;
        this.solutionDetail = solutionDetail;
        this.mathProblems = mathProblems;
        this.categorySettings = categorySettings;
    }

    public void setId(Long id) {this.id = id;}
    public void setLevel(int level) {this.level = level;}
    public void setMathCategory(MathCategory mathCategory) {this.mathCategory = mathCategory;}
    public void setSolutionDetail(SolutionDetail solutionDetail) {this.solutionDetail = solutionDetail;}
    public void setMathProblems(List<MathProblem> mathProblems) {this.mathProblems = mathProblems;}
    public void setDate(LocalDateTime date) {this.date = date;}

    public long getUserId() {return userId;}
    public void setUserId(long userId) {this.userId = userId;}
    public void setCategorySettings(CategorySettings categorySettings) {this.categorySettings = categorySettings;}
    public Long getId() {return id;}
    public int getLevel() {return level;}
    public MathCategory getMathCategory() {return mathCategory;}
    public CategorySettings getCategorySettings() {return categorySettings;}
    public SolutionDetail getSolutionDetail() {return solutionDetail;}
    public List<MathProblem> getMathProblems() {return mathProblems;}
    public LocalDateTime getDate() {return date;}
    public long getTimeTaken() {return timeTaken;}
    public void setTimeTaken(long timeTaken) {this.timeTaken = timeTaken;}

    public String toJSON() {
        try {
            Map<String, Object> jsonMap = new HashMap<>();
            jsonMap.put("level", level);
            jsonMap.put("category", mathCategory);
            jsonMap.put("settings", categorySettings==null?null:categorySettings.toJSON());
            jsonMap.put("solutionDetail", solutionDetail);
            jsonMap.put("mathProblems", mathProblems.stream().map(MathProblem::toJSON).toList());
            jsonMap.put("date", date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            jsonMap.put("timeTaken", timeTaken);
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(jsonMap);
        } catch (Exception e) {e.printStackTrace();}
        return "{}";
    }
    @Override
    public String toString() {
        return "PracticeSession{" +
                "id=" + id +
                ", userId=" + userId +
                ", level=" + level +
                ", mathCategory=" + mathCategory +
                ", categorySettings=" + categorySettings +
                ", solutionDetail=" + solutionDetail +
                ", mathProblems=" + mathProblems +
                ", date=" + date +
                ", timeTaken=" + timeTaken +
                '}';
    }
}