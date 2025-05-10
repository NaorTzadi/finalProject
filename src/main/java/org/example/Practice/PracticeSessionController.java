package org.example.Practice;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.example.CustomLogger;
import org.example.Practice.CoreLogic.Arithmetic;
import org.example.Practice.CoreLogic.Geometry;
import org.example.Practice.Settings.CategorySettings;
import org.example.Practice.Settings.Options;
import org.example.Users.UsersService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/practice")
public class PracticeSessionController {
    private final CustomLogger logger=new CustomLogger(this.getClass());
    private final PracticeSessionService practiceSessionService;
    private final UsersService usersService;
    public PracticeSessionController(PracticeSessionService practiceSessionService, UsersService usersService) {
        this.practiceSessionService = practiceSessionService;
        this.usersService = usersService;
    }

    @PostMapping("/new-session")
    public ResponseEntity<String> newPracticeSession(
            @RequestHeader String SessionToken,
            @RequestBody(required = false) String json ,
            @RequestParam(value = "category") String category
    ) {
        long userId = usersService.getUserIdBySessionToken(SessionToken);
        practiceSessionService.removeActivePracticeSession(userId);
        Options.MathCategory mathCategory;
        try {mathCategory=Options.MathCategory.valueOf(category.toUpperCase().trim().replace(" ", "_"));
        } catch (Exception e) {return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid category");}
        long activePracticeSessionId = practiceSessionService.addActivePracticeSession(userId,SessionToken, json,mathCategory);
        if (activePracticeSessionId == -1) return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/new-question")
    public ResponseEntity<String> newQuestion(@RequestHeader String SessionToken) {
        ActivePracticeSession practiceSession=practiceSessionService.getActivePracticeSessionByToken(SessionToken);
        CategorySettings categorySettings=practiceSession.getCategorySettings();
        Options.MathCategory mathCategory=practiceSession.getMathCategory();
        Options.SolutionDetail solutionDetail=practiceSession.getSolutionDetail();
        int level=practiceSession.getLevel();
        if (mathCategory==null)return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        MathProblem mathProblem=null;
        if (mathCategory== Options.MathCategory.GEOMETRY) {
            if (categorySettings==null) mathProblem=Geometry.generateQuestion(level,solutionDetail);
            else mathProblem=Geometry.generateQuestion(categorySettings);
        }else if (mathCategory==Options.MathCategory.ARITHMETICS){
            if (categorySettings==null) mathProblem=Arithmetic.generateQuestion(level,solutionDetail);
            else mathProblem=Arithmetic.generateQuestion(categorySettings,solutionDetail);
        }
        if (mathProblem==null)return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        practiceSessionService.addMathProblemToActivePracticeSession(mathProblem,practiceSession.getPracticeSessionId());
        return ResponseEntity.ok(mathProblem.toJSON());
    }

    @PostMapping("/used-hint")
    public ResponseEntity<String> usedHint(@RequestHeader String SessionToken) {
        ActivePracticeSession practiceSession=practiceSessionService.getActivePracticeSessionByToken(SessionToken);
        practiceSessionService.setUsedProblemHint(practiceSession.getPracticeSessionId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/requested-solution")
    public ResponseEntity<String> requestedSolution(@RequestHeader String SessionToken) {
        ActivePracticeSession practiceSession=practiceSessionService.getActivePracticeSessionByToken(SessionToken);
        practiceSessionService.setUsedProblemSolution(practiceSession.getPracticeSessionId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/question-answer")
    public ResponseEntity<String> questionAnswerResult(@RequestHeader String SessionToken, @RequestParam(value = "success") String success) {
        ActivePracticeSession practiceSession=practiceSessionService.getActivePracticeSessionByToken(SessionToken);
        if(practiceSession.getCategorySettings()==null && success.equals("true")) practiceSessionService.incrementActivePracticeSessionLevel(SessionToken);
        else practiceSessionService.incrementProblemFails(practiceSession.getPracticeSessionId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/finish-session")
    public ResponseEntity<String> finishPracticeSession(@RequestHeader String SessionToken) {
        practiceSessionService.finishPracticeSession(SessionToken);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/statistics")
    public ResponseEntity<String> statistics(@RequestHeader String SessionToken) {
        long userId = usersService.getUserIdBySessionToken(SessionToken);
        Statistics statistics = practiceSessionService.getStatistics(userId);
        if (statistics == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

        String inferredJson = practiceSessionService.getInferredStatistics(userId);
        try {
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode fullJson = mapper.createObjectNode();

            ObjectNode basicStats = (ObjectNode) mapper.readTree(statistics.toJSON());
            ObjectNode inferredStats = (ObjectNode) mapper.readTree(inferredJson);

            fullJson.set("basic", basicStats);
            fullJson.set("inferred", inferredStats);

            return ResponseEntity.ok(mapper.writeValueAsString(fullJson));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/history")
    public ResponseEntity<String> history(@RequestHeader String SessionToken,@RequestParam int index) {
        long userId=usersService.getUserIdBySessionToken(SessionToken);
        ArrayList<PracticeSession> practiceSessions= practiceSessionService.getPracticeSessions(userId,index,10);
        List<String> jsonSessions = practiceSessions.stream().map(PracticeSession::toJSON).toList();
        return ResponseEntity.ok(jsonSessions.toString());
    }



}