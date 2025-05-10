package org.example.Practice;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "statistics", schema = "practice_session")
public class Statistics {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "statistics_id") private Long statisticsId;
    @Column(name = "user_id", nullable = false) private long userId;
    @Column(name = "geometry_sessions", nullable = false) private int geometrySessions;
    @Column(name = "geometry_problems", nullable = false) private int geometryProblems;
    @Column(name = "geometry_fails", nullable = false) private int geometryFails;
    @Column(name = "geometry_succeeded", nullable = false) private int geometrySucceeded;
    @Column(name = "geometry_hints_used", nullable = false) private int geometryHintsUsed;
    @Column(name = "geometry_solutions_requested", nullable = false) private int geometrySolutionsRequested;
    @Column(name = "geometry_time_spent", nullable = false) private long geometryTimeSpent;
    @Column(name = "arithmetics_sessions", nullable = false) private int arithmeticsSessions;
    @Column(name = "arithmetics_problems", nullable = false) private int arithmeticsProblems;
    @Column(name = "arithmetics_fails", nullable = false) private int arithmeticsFails;
    @Column(name = "arithmetics_succeeded", nullable = false) private int arithmeticsSucceeded;
    @Column(name = "arithmetics_hints_used", nullable = false) private int arithmeticsHintsUsed;
    @Column(name = "arithmetics_solutions_requested", nullable = false) private int arithmeticsSolutionsRequested;
    @Column(name = "arithmetics_time_spent", nullable = false) private long arithmeticsTimeSpent;
    @Column(name = "last_updated", columnDefinition = "TIMESTAMP") private LocalDateTime lastUpdated;

    public Long getStatisticsId() {return statisticsId;}
    public void setStaticsId(Long statisticsId) {this.statisticsId = statisticsId;}
    public long getUserId() {return userId;}
    public void setUserId(long userId) {this.userId = userId;}
    public int getGeometrySessions() {return geometrySessions;}
    public void setGeometrySessions(int geometrySessions) {this.geometrySessions = geometrySessions;}
    public int getGeometryProblems() {return geometryProblems;}
    public void setGeometryProblems(int geometryProblems) {this.geometryProblems = geometryProblems;}
    public int getGeometryFails() {return geometryFails;}
    public void setGeometryFails(int geometryFails) {this.geometryFails = geometryFails;}
    public int getGeometrySucceeded() { return geometrySucceeded; }
    public void setGeometrySucceeded(int geometrySucceeded) { this.geometrySucceeded = geometrySucceeded; }
    public int getGeometryHintsUsed() {return geometryHintsUsed;}
    public void setGeometryHintsUsed(int geometryHintsUsed) {this.geometryHintsUsed = geometryHintsUsed;}
    public int getGeometrySolutionsRequested() {return geometrySolutionsRequested;}
    public void setGeometrySolutionsRequested(int geometrySolutionsRequested) {this.geometrySolutionsRequested = geometrySolutionsRequested;}
    public long getGeometryTimeSpent() {return geometryTimeSpent;}
    public void setGeometryTimeSpent(long geometryTimeSpent) {this.geometryTimeSpent = geometryTimeSpent;}
    public int getArithmeticsSessions() {return arithmeticsSessions;}
    public void setArithmeticsSessions(int arithmeticsSessions) {this.arithmeticsSessions = arithmeticsSessions;}
    public int getArithmeticsProblems() {return arithmeticsProblems;}
    public void setArithmeticsProblems(int arithmeticsProblems) {this.arithmeticsProblems = arithmeticsProblems;}
    public int getArithmeticsFails() {return arithmeticsFails;}
    public void setArithmeticsFails(int arithmeticsFails) {this.arithmeticsFails = arithmeticsFails;}
    public int getArithmeticsSucceeded() { return arithmeticsSucceeded; }
    public void setArithmeticsSucceeded(int arithmeticsSucceeded) { this.arithmeticsSucceeded = arithmeticsSucceeded; }
    public int getArithmeticsHintsUsed() {return arithmeticsHintsUsed;}
    public void setArithmeticsHintsUsed(int arithmeticsHintsUsed) {this.arithmeticsHintsUsed = arithmeticsHintsUsed;}
    public int getArithmeticsSolutionsRequested() {return arithmeticsSolutionsRequested;}
    public void setArithmeticsSolutionsRequested(int arithmeticsSolutionsRequested) {this.arithmeticsSolutionsRequested = arithmeticsSolutionsRequested;}
    public long getArithmeticsTimeSpent() {return arithmeticsTimeSpent;}
    public void setArithmeticsTimeSpent(long arithmeticsTimeSpent) {this.arithmeticsTimeSpent = arithmeticsTimeSpent;}
    public LocalDateTime getLastUpdated() {return lastUpdated;}
    public void setLastUpdated(LocalDateTime lastUpdated) {this.lastUpdated = lastUpdated;}

    public String toJSON(){
        try {
            Map<String, Object> jsonMap=new HashMap<>();
            jsonMap.put("geometrySessions", geometrySessions);
            jsonMap.put("geometryProblems", geometryProblems);
            jsonMap.put("geometryFails", geometryFails);
            jsonMap.put("geometrySucceeded", geometrySucceeded);
            jsonMap.put("geometryHintsUsed", geometryHintsUsed);
            jsonMap.put("geometrySolutionsRequested", geometrySolutionsRequested);
            jsonMap.put("geometryTimeSpent", geometryTimeSpent);
            jsonMap.put("arithmeticsSessions", arithmeticsSessions);
            jsonMap.put("arithmeticsProblems", arithmeticsProblems);
            jsonMap.put("arithmeticsFails", arithmeticsFails);
            jsonMap.put("arithmeticsSucceeded", arithmeticsSucceeded);
            jsonMap.put("arithmeticsHintsUsed", arithmeticsHintsUsed);
            jsonMap.put("arithmeticsSolutionsRequested", arithmeticsSolutionsRequested);
            jsonMap.put("arithmeticsTimeSpent", arithmeticsTimeSpent);
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(jsonMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "{}";
    }

}