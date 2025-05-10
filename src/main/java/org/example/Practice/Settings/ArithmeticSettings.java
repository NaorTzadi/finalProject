package org.example.Practice.Settings;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import org.example.Practice.CoreLogic.Arithmetic;

import java.util.*;
import java.util.stream.Collectors;

@Entity
@Table(name = "arithmetic_settings", schema = "practice_session")
public class ArithmeticSettings extends CategorySettings {

    @Column(name = "practice_session_id", nullable = false)
    private Long practiceSessionId;

    @Column(columnDefinition = "TEXT")
    private String solutionDetail;

    @Column(columnDefinition = "TEXT")
    private String numberTypes;

    @Column(columnDefinition = "TEXT")
    private String questionTypes;

    protected ArithmeticSettings() {
        super(Options.MathCategory.ARITHMETICS);
    }

    public ArithmeticSettings(Long practiceSessionId, Options.SolutionDetail solutionDetail, Set<Arithmetic.NumberType> numberTypes, Set<Arithmetic.QuestionType> questionTypes) {
        super(Options.MathCategory.ARITHMETICS);
        this.practiceSessionId = practiceSessionId;
        this.solutionDetail = solutionDetail.toString();
        this.numberTypes = String.join(",", numberTypes.stream().map(Enum::name).toList());
        this.questionTypes = String.join(",", questionTypes.stream().map(Enum::name).toList());
    }

    public Long getPracticeSessionId() {
        return practiceSessionId;
    }
    public Options.SolutionDetail getSolutionDetail() {
        return solutionDetail==null ||solutionDetail.isBlank() ? Options.SolutionDetail.EXACT : Options.SolutionDetail.valueOf(solutionDetail);
    }

    public Set<Arithmetic.NumberType> getNumberTypes() {
        System.out.println(numberTypes.toString());
        return numberTypes == null || numberTypes.isEmpty()
                ? new HashSet<>()
                : Arrays.stream(numberTypes.split(",")).map(Arithmetic.NumberType::valueOf).collect(Collectors.toSet());
    }

    public Set<Arithmetic.QuestionType> getQuestionTypes() {
        return questionTypes == null || questionTypes.isEmpty()
                ? new HashSet<>()
                : Arrays.stream(questionTypes.split(",")).map(Arithmetic.QuestionType::valueOf).collect(Collectors.toSet());
    }

    public static ArithmeticSettings jsonToObject(String json, Long practiceSessionId) {
        if (json == null) return null;
        try {
            JsonNode configNode = new ObjectMapper().readTree(json);
            Options.SolutionDetail solutionDetail=null;
            try {solutionDetail= Options.SolutionDetail.valueOf(configNode.get("solutionDetail").asText());}catch (Exception ignore){}
            if (solutionDetail==null)solutionDetail=Options.SolutionDetail.EXACT;

            String numberTypeValues = configNode.has("numberTypes") ? configNode.get("numberTypes").asText() : "";
            String questionTypeValues = configNode.has("questionTypes") ? configNode.get("questionTypes").asText() : "";

            return new ArithmeticSettings(
                    practiceSessionId,
                    solutionDetail,
                    Arrays.stream(numberTypeValues.split(",")).map(Arithmetic.NumberType::valueOf).collect(Collectors.toSet()),
                    Arrays.stream(questionTypeValues.split(",")).map(Arithmetic.QuestionType::valueOf).collect(Collectors.toSet())
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    public String toJSON() {
        try {
            Map<String, Object> jsonMap = new HashMap<>();
            jsonMap.put("solutionDetail", solutionDetail);
            jsonMap.put("numberTypes", getNumberTypes().stream().map(Enum::name).collect(Collectors.toList()));
            jsonMap.put("questionTypes", getQuestionTypes().stream().map(Enum::name).collect(Collectors.toList()));
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(jsonMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "{}";
    }

}