package org.example.Practice.Settings;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import org.example.Practice.CoreLogic.Geometry;

import java.util.*;
import java.util.stream.Collectors;

@Entity
@Table(name = "geometry_settings", schema = "practice_session")
public class GeometrySettings extends CategorySettings {


    @Column(name = "practice_session_id", nullable = false)
    private Long practiceSessionId;

    @Column(columnDefinition = "TEXT")
    private String solutionDetail;

    @Column(columnDefinition = "TEXT")
    private String shapeTypes;

    @Column(columnDefinition = "TEXT")
    private String shapes;

    @Column(columnDefinition = "TEXT")
    private String questionTypeTypes;

    protected GeometrySettings() {
        super(Options.MathCategory.GEOMETRY);
    }

    public GeometrySettings(Long practiceSessionId, Options.SolutionDetail solutionDetail, Set<Geometry.ShapeType> shapeTypes, Set<Geometry.Shape> shapes, Set<Geometry.QuestionType> questionTypeTypes) {
        super(Options.MathCategory.GEOMETRY);
        this.solutionDetail = solutionDetail.toString();
        this.practiceSessionId = practiceSessionId;
        this.shapeTypes = String.join(",", shapeTypes.stream().map(Enum::name).toList());
        this.shapes = String.join(",", shapes.stream().map(Enum::name).toList());
        this.questionTypeTypes = String.join(",", questionTypeTypes.stream().map(Enum::name).toList());
    }

    public Long getPracticeSessionId() {return practiceSessionId;}
    public Options.SolutionDetail getSolutionDetail() {return solutionDetail==null ||solutionDetail.isBlank() ? Options.SolutionDetail.EXACT : Options.SolutionDetail.valueOf(solutionDetail);}
    public Set<Geometry.ShapeType> getShapeTypes() {return shapeTypes == null || shapeTypes.isEmpty() ? new HashSet<>() : Arrays.stream(shapeTypes.split(",")).map(Geometry.ShapeType::valueOf).collect(Collectors.toSet());}
    public Set<Geometry.Shape> getShapes() {return shapes == null || shapes.isEmpty() ? new HashSet<>() : Arrays.stream(shapes.split(",")).map(Geometry.Shape::valueOf).collect(Collectors.toSet());}
    public Set<Geometry.QuestionType> getQuestionTypes() {return questionTypeTypes == null || questionTypeTypes.isEmpty() ? new HashSet<>() : Arrays.stream(questionTypeTypes.split(",")).map(Geometry.QuestionType::valueOf).collect(Collectors.toSet());}

    public static GeometrySettings jsonToObject(String json, Long practiceSessionId) {
        if (json == null) return null;
        try {
            JsonNode configNode = new ObjectMapper().readTree(json);
            Options.SolutionDetail solutionDetail=null;
            try {solutionDetail= Options.SolutionDetail.valueOf(configNode.get("solutionDetail").asText());}catch (Exception ignore){}
            if (solutionDetail==null)solutionDetail=Options.SolutionDetail.EXACT;

            String shapeTypes = configNode.has("shapeTypes") ? configNode.get("shapeTypes").asText() : "";
            shapeTypes = shapeTypes.replace("2D", Geometry.ShapeType.SHAPE2D.toString());
            shapeTypes = shapeTypes.replace("3D", Geometry.ShapeType.SHAPE3D.toString());

            String shapes = configNode.has("shapes") ? configNode.get("shapes").asText() : "";
            String questionTypes = configNode.has("questionTypes") ? configNode.get("questionTypes").asText() : "";

            return new GeometrySettings(
                    practiceSessionId,
                    solutionDetail,
                    Arrays.stream(shapeTypes.split(",")).map(Geometry.ShapeType::valueOf).collect(Collectors.toSet()),
                    Arrays.stream(shapes.split(",")).map(Geometry.Shape::valueOf).collect(Collectors.toSet()),
                    Arrays.stream(questionTypes.split(",")).map(Geometry.QuestionType::valueOf).collect(Collectors.toSet())
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
            jsonMap.put("shapeTypes", getShapeTypes().stream().map(Enum::name).collect(Collectors.toList()));
            jsonMap.put("shapes", getShapes().stream().map(Enum::name).collect(Collectors.toList()));
            jsonMap.put("questionTypes", getQuestionTypes().stream().map(Enum::name).collect(Collectors.toList()));
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(jsonMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "{}";
    }



}