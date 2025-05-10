package org.example.Practice;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "math_problem")
public class MathProblem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "practice_session_id", nullable = false) private PracticeSession practiceSession;
    @Column(name = "problemId", nullable = false, unique = true) private long problemId;
    @Column(name = "problem_level", nullable = false) private int problemLevel;
    @Column(name = "problem_content", columnDefinition = "TEXT") private String problemContent;
    @Column(name = "solution_content", columnDefinition = "TEXT") private String solutionContent;
    @Column(name = "fails", nullable = false) private int fails;
    @Column(name = "requested_solution", nullable = false) private boolean requestedSolution;
    @Column(name = "requested_hint", nullable = false) private boolean requestedHint;
    @Column(name = "time_taken", nullable = false) private long timeTaken;
    @Column(name = "created_at", nullable = false) private LocalDateTime createdAt;

    protected MathProblem() {}
    public MathProblem(int problemLevel, String problemContent, String solutionContent) {
        this.problemLevel = problemLevel;
        this.problemContent =problemContent!=null? normalizeDecimalNumbers(problemContent,3):null;
        this.solutionContent =solutionContent!=null? normalizeDecimalNumbers(solutionContent,3):null;
        this.fails = 0;
        this.requestedSolution = false;
        this.requestedHint = false;
        this.timeTaken=System.currentTimeMillis();
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {return id;}
    public long getProblemId() {return problemId;}
    public int getProblemLevel() {return problemLevel;}
    public String getProblemContent() {return problemContent;}
    public String getSolutionContent() {return solutionContent;}
    public int getFails() {return fails;}
    public boolean getRequestedSolution() {return requestedSolution;}
    public boolean getRequestedHint() {return requestedHint;}
    public long getTimeTaken() {return timeTaken;}
    public LocalDateTime getCreatedAt() {return createdAt;}
    public void setProblemLevel(int problemLevel) {this.problemLevel = problemLevel;}
    public void setProblemContent(String problemContent) {this.problemContent = problemContent;}
    public void setSolutionContent(String solutionContent) {this.solutionContent = solutionContent;}
    public void setFails(int fails) {this.fails = fails;}
    public void setRequestedSolution(boolean requestedSolution) {this.requestedSolution = requestedSolution;}
    public void setRequestedHint(boolean hintsTaken) {this.requestedHint = hintsTaken;}
    public void setCreatedAt(LocalDateTime createdAt) {this.createdAt = createdAt;}
    public void setId(Long id) {this.id = id;}
    public void setPracticeSession(PracticeSession practiceSession) {this.practiceSession = practiceSession;}
    public void setTimeTaken(long timeTaken) {this.timeTaken = timeTaken;}


    private String normalizeDecimalNumbers(String str, int digitsAfterDot) {
        StringBuilder result = new StringBuilder();
        int i = 0;

        while (i < str.length()) {
            char currentChar = str.charAt(i);

            if (Character.isDigit(currentChar) && i + 1 < str.length() && str.charAt(i + 1) == '.') {
                int dotIndex = i + 1;

                result.append(currentChar);

                StringBuilder decimalPart = new StringBuilder();
                int j = dotIndex + 1;

                while (j < str.length() && Character.isDigit(str.charAt(j))) {
                    if (decimalPart.length() < digitsAfterDot) {
                        decimalPart.append(str.charAt(j));
                    }
                    j++;
                }

                boolean hasNonZeroDecimal = decimalPart.chars().anyMatch(c -> c != '0');

                boolean allRemainingZeroes = true;
                int checkRemainingDigits = j;
                while (checkRemainingDigits < str.length() && Character.isDigit(str.charAt(checkRemainingDigits))) {
                    if (str.charAt(checkRemainingDigits) != '0') {
                        allRemainingZeroes = false;
                        break;
                    }
                    checkRemainingDigits++;
                }

                if (!hasNonZeroDecimal && allRemainingZeroes) {
                    i = checkRemainingDigits;
                } else {
                    result.append('.').append(decimalPart);
                    i = checkRemainingDigits;
                }
            } else {
                result.append(currentChar);
                i++;
            }
        }

        return result.toString();
    }

    @Override
    public String toString() {
        return "MathProblem{" +
                "id=" + id +
                ", practiceSession=" + practiceSession +
                ", problemId=" + problemId +
                ", problemLevel=" + problemLevel +
                ", problemContent='" + problemContent + '\'' +
                ", solutionContent='" + solutionContent + '\'' +
                ", fails=" + fails +
                ", requestedSolution=" + requestedSolution +
                ", hintsTaken=" + requestedHint +
                ", timeTaken=" + timeTaken +
                ", createdAt=" + createdAt +
                '}';
    }
    public String toJSON() {
        try {
            Map<String, Object> jsonMap = new HashMap<>();
            jsonMap.put("problemLevel", problemLevel);
            jsonMap.put("problemContent", problemContent);
            jsonMap.put("solutionContent", solutionContent);
            jsonMap.put("fails", fails);
            jsonMap.put("requestedSolution", requestedSolution);
            jsonMap.put("requestedHint", requestedHint);
            jsonMap.put("timeTaken", timeTaken/1000);
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(jsonMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "{}";
    }
}