package org.example.Practice;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.example.CustomLogger;
import org.example.Practice.CoreLogic.Arithmetic;
import org.example.Practice.CoreLogic.Geometry;
import org.example.Practice.Settings.ArithmeticSettings;
import org.example.Practice.Settings.CategorySettings;
import org.example.Practice.Settings.GeometrySettings;
import org.example.Practice.Settings.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PracticeSessionService {
    private static final Logger log = LoggerFactory.getLogger(PracticeSessionService.class);
    @PersistenceContext
    private EntityManager entityManager;
    private final CustomLogger logger=new CustomLogger(PracticeSessionService.class);

    @Transactional
    public ArrayList<PracticeSession> getPracticeSessions(long userId, int startIndex, int size) {
        //once it works we would need to make a json structure that assures each settings and math problems are attached to the right practice session.
        try {
            if (startIndex < 0 || size <= 0) return new ArrayList<>();
            String fetchSessionsSql = """
            SELECT id, user_id, level, math_category, category_settings_id, solution_detail, date, time_taken
            FROM practice_session.practice_session
            WHERE user_id = :userId
            ORDER BY date DESC
        """;
            @SuppressWarnings("unchecked")
            List<Object[]> resultSet = entityManager.createNativeQuery(fetchSessionsSql).setParameter("userId", userId).getResultList();

            ArrayList<PracticeSession> practiceSessions = new ArrayList<>();
            for (int i = startIndex; i < startIndex + size && i < resultSet.size(); i++) {
                Object[] sessionData = resultSet.get(i);
                PracticeSession session = new PracticeSession();
                session.setId(((Number) sessionData[0]).longValue());
                session.setUserId(((Number) sessionData[1]).longValue());
                session.setLevel((int) sessionData[2]);
                session.setMathCategory(Options.MathCategory.valueOf((String) sessionData[3]));
                Long categorySettingsId = sessionData[4] != null ? ((Number) sessionData[4]).longValue() : null;
                if (categorySettingsId != null) session.setCategorySettings(getCategorySettings(categorySettingsId));
                session.setSolutionDetail(Options.SolutionDetail.valueOf((String) sessionData[5]));
                session.setDate(((java.sql.Timestamp) sessionData[6]).toLocalDateTime());
                session.setTimeTaken(((Number) sessionData[7]).longValue());
                session.setMathProblems(getPracticeSessionMathProblems(session.getId()));
                practiceSessions.add(session);
            }

            return practiceSessions;
        } catch (Exception e) {logger.error("Error fetching practice sessions for user ID " + userId, e);}
        return new ArrayList<>();
    }
    @Transactional
    public PracticeSession getRecentCampaignPracticeSession(long userId, Options.MathCategory mathCategory) {
        try {
            String sql = """
        SELECT id, user_id, level, math_category, category_settings_id, solution_detail, date, time_taken
        FROM practice_session.practice_session
        WHERE user_id = :userId AND math_category = :mathCategory AND category_settings_id IS NULL
        ORDER BY date DESC
        LIMIT 1
        """;

            Object[] sessionData = (Object[]) entityManager.createNativeQuery(sql)
                    .setParameter("userId", userId)
                    .setParameter("mathCategory", mathCategory.name())
                    .getSingleResult();

            PracticeSession session = new PracticeSession();
            session.setId(((Number) sessionData[0]).longValue());
            session.setUserId(((Number) sessionData[1]).longValue());
            session.setLevel((int) sessionData[2]);
            session.setMathCategory(Options.MathCategory.valueOf((String) sessionData[3]));
            session.setCategorySettings(null);
            session.setSolutionDetail(Options.SolutionDetail.valueOf((String) sessionData[5]));
            session.setDate(((java.sql.Timestamp) sessionData[6]).toLocalDateTime());
            session.setTimeTaken(((Number) sessionData[7]).longValue());
            session.setMathProblems(getPracticeSessionMathProblems(session.getId()));
            return session;

        } catch (Exception e) {
            logger.error("Error fetching recent practice session for user ID " + userId + " and category " + mathCategory, e);
        }
        return null;
    }

    //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%<<{ STATISTICS }>>%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    @Transactional
    public String getInferredStatistics(long userId) {
        long startOfWeekMillis = System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000);
        java.sql.Timestamp startOfWeek = new java.sql.Timestamp(startOfWeekMillis);

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root = mapper.createObjectNode();

        for (Options.MathCategory category : Options.MathCategory.values()) {
            String timeStatsSql = """
            SELECT MIN(mp.time_taken), MAX(mp.time_taken), AVG(mp.time_taken)
            FROM practice_session.math_problem mp
            JOIN practice_session.practice_session ps ON mp.practice_session_id = ps.id
                    WHERE ps.user_id = :userId AND ps.math_category = :category AND ps.category_settings_id IS NULL
                                                                               """;

            Object[] timeStats = (Object[]) entityManager.createNativeQuery(timeStatsSql)
                    .setParameter("userId", userId)
                    .setParameter("category", category.name())
                    .getSingleResult();

            long min = timeStats[0] != null ? ((Number) timeStats[0]).longValue() : 0;
            long max = timeStats[1] != null ? ((Number) timeStats[1]).longValue() : 0;
            double avg = timeStats[2] != null ? ((Number) timeStats[2]).doubleValue() : 0;

            String weeklySessionsSql = """
    SELECT COUNT(*) FROM practice_session.practice_session
    WHERE user_id = :userId AND math_category = :category AND date >= :startOfWeek AND category_settings_id IS NULL
""";

            long sessions = ((Number) entityManager.createNativeQuery(weeklySessionsSql)
                    .setParameter("userId", userId)
                    .setParameter("category", category.name())
                    .setParameter("startOfWeek", startOfWeek)
                    .getSingleResult()).longValue();

            ObjectNode catStats = mapper.createObjectNode();
            catStats.put("minTime", min);
            catStats.put("maxTime", max);
            catStats.put("avgTime", avg);
            catStats.put("sessionsThisWeek", sessions);
            catStats.put("maxSuccessStreak", getLongestSuccessRate(userId, category.name()));
            catStats.put("maxFailStreak", getLongestFailStreak(userId, category.name()));

            root.set(category.name(), catStats);
        }

        try {
            return mapper.writeValueAsString(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "{}";
    }
    public int getLongestSuccessRate(long userId, String category) {
        String condition = "mp.fails = 0 AND mp.requested_solution = 0";

        String sql = """
        SELECT MAX(streak_length) AS max_streak
        FROM (
            SELECT COUNT(*) AS streak_length
            FROM (
                SELECT
                    mp.id,
                    ROW_NUMBER() OVER (ORDER BY mp.created_at) -
                    ROW_NUMBER() OVER (PARTITION BY mp.fails ORDER BY mp.created_at) AS grp
                FROM practice_session.math_problem mp
                JOIN practice_session.practice_session ps ON mp.practice_session_id = ps.id
                WHERE ps.user_id = :userId
                  AND ps.math_category = :category
                  AND ps.category_settings_id IS NULL
                  AND %s
                ORDER BY mp.created_at
            ) AS streaks
            GROUP BY grp
        ) AS streak_groups
    """.formatted(condition);

        Object result = entityManager.createNativeQuery(sql)
                .setParameter("userId", userId)
                .setParameter("category", category)
                .getSingleResult();

        return result != null ? ((Number) result).intValue() : 0;
    }

    public int getLongestFailStreak(long userId, String category) {
        String sql = """
        SELECT MAX(streak_length) AS max_streak
        FROM (
            SELECT COUNT(*) AS streak_length
            FROM (
                SELECT
                    mp.id,
                    ROW_NUMBER() OVER (ORDER BY mp.created_at) -
                    ROW_NUMBER() OVER (PARTITION BY mp.fails ORDER BY mp.created_at) AS grp
                FROM practice_session.math_problem mp
                JOIN practice_session.practice_session ps ON mp.practice_session_id = ps.id
                WHERE ps.user_id = :userId
                  AND ps.math_category = :category
                  AND ps.category_settings_id IS NULL
                  AND mp.fails != 0
                ORDER BY mp.created_at
            ) AS streaks
            GROUP BY grp
        ) AS streak_groups
    """;

        Object result = entityManager.createNativeQuery(sql)
                .setParameter("userId", userId)
                .setParameter("category", category)
                .getSingleResult();

        return result != null ? ((Number) result).intValue() : 0;
    }

    @Transactional
    public Statistics getStatistics(long userId) {
        String selectSql = "SELECT statistics_id, user_id, geometry_sessions, geometry_problems, geometry_fails, " +
                "geometry_hints_used, geometry_solutions_requested, geometry_time_spent, " +
                "arithmetics_sessions, arithmetics_problems, arithmetics_fails, arithmetics_hints_used, " +
                "arithmetics_solutions_requested, arithmetics_time_spent, last_updated " +
                "FROM practice_session.statistics WHERE user_id = :userId";
        try {
            Object[] result = (Object[]) entityManager.createNativeQuery(selectSql).setParameter("userId", userId).getSingleResult();
            if (result==null) return null;
            Statistics statistics = new Statistics();
            statistics.setStaticsId(((Number) result[0]).longValue());
            statistics.setUserId(((Number) result[1]).longValue());
            statistics.setGeometrySessions(((Number) result[2]).intValue());
            statistics.setGeometryProblems(((Number) result[3]).intValue());
            statistics.setGeometryFails(((Number) result[4]).intValue());
            statistics.setGeometryHintsUsed(((Number) result[5]).intValue());
            statistics.setGeometrySolutionsRequested(((Number) result[6]).intValue());
            statistics.setGeometryTimeSpent(((Number) result[7]).longValue());
            statistics.setArithmeticsSessions(((Number) result[8]).intValue());
            statistics.setArithmeticsProblems(((Number) result[9]).intValue());
            statistics.setArithmeticsFails(((Number) result[10]).intValue());
            statistics.setArithmeticsHintsUsed(((Number) result[11]).intValue());
            statistics.setArithmeticsSolutionsRequested(((Number) result[12]).intValue());
            statistics.setArithmeticsTimeSpent(((Number) result[13]).longValue());
            statistics.setLastUpdated(((java.sql.Timestamp) result[14]).toLocalDateTime());
            return statistics;
        } catch (Exception e) {logger.error("Error fetching statistics for user ID " + userId, e);}
        return null;
    }

    @Transactional
    public void updateStatistics(PracticeSession practiceSession) {
        if (practiceSession.getCategorySettings() != null) return;
        Options.MathCategory category = practiceSession.getMathCategory();
        long userId = practiceSession.getUserId();

        List<MathProblem> mathProblems = getPracticeSessionMathProblems(practiceSession.getId());
        int problemsFailed = mathProblems.stream().mapToInt(MathProblem::getFails).sum();
        int solutionRequests = (int) mathProblems.stream().filter(MathProblem::getRequestedSolution).count();
        int hintsUsed = (int) mathProblems.stream().filter(MathProblem::getRequestedHint).count();
        long timeTaken = mathProblems.stream().mapToLong(MathProblem::getTimeTaken).sum();
        int succeeded = (int) mathProblems.stream()
                .filter(p -> p.getFails() == 0 && !p.getRequestedSolution() && mathProblems.size()>1)
                .count();

        String selectSql = "SELECT statistics_id FROM practice_session.statistics WHERE user_id = :userId";
        Long statisticsId = (Long) entityManager.createNativeQuery(selectSql)
                .setParameter("userId", userId)
                .getResultStream()
                .map(r -> ((Number) r).longValue())
                .findFirst()
                .orElse(null);

        if (statisticsId == null) {
            if (category.equals(Options.MathCategory.GEOMETRY)) {
                String insertSql = "INSERT INTO practice_session.statistics " +
                        "(user_id, geometry_sessions, geometry_problems, geometry_fails, geometry_succeeded, geometry_hints_used, geometry_solutions_requested, geometry_time_spent, " +
                        "arithmetics_sessions, arithmetics_problems, arithmetics_fails, arithmetics_succeeded, arithmetics_hints_used, arithmetics_solutions_requested, arithmetics_time_spent, last_updated) " +
                        "VALUES (:userId, :gSessions, :gProblems, :gFails, :gSucceeded, :gHints, :gSolutions, :gTime, 0, 0, 0, 0, 0, 0, 0, NOW())";
                entityManager.createNativeQuery(insertSql)
                        .setParameter("userId", userId)
                        .setParameter("gSessions", 1)
                        .setParameter("gProblems", mathProblems.size())
                        .setParameter("gFails", problemsFailed)
                        .setParameter("gSucceeded", succeeded)
                        .setParameter("gHints", hintsUsed)
                        .setParameter("gSolutions", solutionRequests)
                        .setParameter("gTime", timeTaken)
                        .executeUpdate();
            } else {
                String insertSql = "INSERT INTO practice_session.statistics " +
                        "(user_id, geometry_sessions, geometry_problems, geometry_fails, geometry_succeeded, geometry_hints_used, geometry_solutions_requested, geometry_time_spent, " +
                        "arithmetics_sessions, arithmetics_problems, arithmetics_fails, arithmetics_succeeded, arithmetics_hints_used, arithmetics_solutions_requested, arithmetics_time_spent, last_updated) " +
                        "VALUES (:userId, 0, 0, 0, 0, 0, 0, 0, :aSessions, :aProblems, :aFails, :aSucceeded, :aHints, :aSolutions, :aTime, NOW())";
                entityManager.createNativeQuery(insertSql)
                        .setParameter("userId", userId)
                        .setParameter("aSessions", 1)
                        .setParameter("aProblems", mathProblems.size())
                        .setParameter("aFails", problemsFailed)
                        .setParameter("aSucceeded", succeeded)
                        .setParameter("aHints", hintsUsed)
                        .setParameter("aSolutions", solutionRequests)
                        .setParameter("aTime", timeTaken)
                        .executeUpdate();
            }
            return;
        }

        if (category.equals(Options.MathCategory.GEOMETRY)) {
            String updateSql = "UPDATE practice_session.statistics SET " +
                    "geometry_sessions = geometry_sessions + 1, " +
                    "geometry_problems = geometry_problems + :gProblems, " +
                    "geometry_fails = geometry_fails + :gFails, " +
                    "geometry_succeeded = geometry_succeeded + :gSucceeded, " +
                    "geometry_hints_used = geometry_hints_used + :gHints, " +
                    "geometry_solutions_requested = geometry_solutions_requested + :gSolutions, " +
                    "geometry_time_spent = geometry_time_spent + :gTime, " +
                    "last_updated = NOW() " +
                    "WHERE statistics_id = :statisticsId";
            entityManager.createNativeQuery(updateSql)
                    .setParameter("gProblems", mathProblems.size())
                    .setParameter("gFails", problemsFailed)
                    .setParameter("gSucceeded", succeeded)
                    .setParameter("gHints", hintsUsed)
                    .setParameter("gSolutions", solutionRequests)
                    .setParameter("gTime", timeTaken)
                    .setParameter("statisticsId", statisticsId)
                    .executeUpdate();
        } else {
            String updateSql = "UPDATE practice_session.statistics SET " +
                    "arithmetics_sessions = arithmetics_sessions + 1, " +
                    "arithmetics_problems = arithmetics_problems + :aProblems, " +
                    "arithmetics_fails = arithmetics_fails + :aFails, " +
                    "arithmetics_succeeded = arithmetics_succeeded + :aSucceeded, " +
                    "arithmetics_hints_used = arithmetics_hints_used + :aHints, " +
                    "arithmetics_solutions_requested = arithmetics_solutions_requested + :aSolutions, " +
                    "arithmetics_time_spent = arithmetics_time_spent + :aTime, " +
                    "last_updated = NOW() " +
                    "WHERE statistics_id = :statisticsId";
            entityManager.createNativeQuery(updateSql)
                    .setParameter("aProblems", mathProblems.size())
                    .setParameter("aFails", problemsFailed)
                    .setParameter("aSucceeded", succeeded)
                    .setParameter("aHints", hintsUsed)
                    .setParameter("aSolutions", solutionRequests)
                    .setParameter("aTime", timeTaken)
                    .setParameter("statisticsId", statisticsId)
                    .executeUpdate();
        }
    }


//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%<<{ ACTIVE PRACTICE SESSION }>>%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

    @Transactional
    public ActivePracticeSession getActivePracticeSessionByToken(String sessionToken) {
        try {
            String fetchSessionSql = """
            SELECT practice_session_id, user_id, level, math_category, category_settings_id, solution_detail, date
            FROM practice_session.active_practice_session
            WHERE session_token = :sessionToken
        """;

            Object[] sessionData = (Object[]) entityManager.createNativeQuery(fetchSessionSql)
                    .setParameter("sessionToken", sessionToken)
                    .getSingleResult();

            if (sessionData == null) {
                logger.error("No active PracticeSession found with session token " + sessionToken);
                return null;
            }
            ActivePracticeSession activePracticeSession = new ActivePracticeSession();
            activePracticeSession.setPracticeSessionId(((Number) sessionData[0]).longValue());
            activePracticeSession.setUserId(((Number) sessionData[1]).longValue());
            activePracticeSession.setLevel((int) sessionData[2]);
            activePracticeSession.setMathCategory(Options.MathCategory.valueOf((String) sessionData[3]));
            Long categorySettingsId = sessionData[4] != null ? ((Number) sessionData[4]).longValue() : null;
            activePracticeSession.setSolutionDetail(Options.SolutionDetail.valueOf((String) sessionData[5]));
            activePracticeSession.setDate(((java.sql.Timestamp) sessionData[6]).toLocalDateTime());
            if (categorySettingsId != null) activePracticeSession.setCategorySettings(getCategorySettings(categorySettingsId));
            return activePracticeSession;
        } catch (NoResultException e) {logger.error("No active PracticeSession found with session token " + sessionToken, e);
        } catch (Exception e) {logger.error("Error fetching active PracticeSession with session token " + sessionToken, e);}
        return null;
    }
    @Transactional
    public PracticeSession getPracticeSessionById(Long practiceSessionId) {
        try {
            String fetchSessionSql = """
            SELECT id, user_id, level, math_category, category_settings_id, solution_detail, date, time_taken
            FROM practice_session.practice_session
            WHERE id = :sessionId
        """;

            Object[] sessionData = (Object[]) entityManager.createNativeQuery(fetchSessionSql)
                    .setParameter("sessionId", practiceSessionId)
                    .getSingleResult();

            if (sessionData == null) {
                logger.error("No PracticeSession found with ID " + practiceSessionId);
                return null;
            }
            PracticeSession practiceSession = new PracticeSession();
            practiceSession.setId(((Number) sessionData[0]).longValue());
            practiceSession.setUserId(((Number) sessionData[1]).longValue());
            practiceSession.setLevel(((Number) sessionData[2]).intValue());
            practiceSession.setMathCategory(sessionData[3] != null ? Options.MathCategory.valueOf((String) sessionData[3]) : null);
            Long categorySettingsId = sessionData[4] != null ? ((Number) sessionData[4]).longValue() : null;
            practiceSession.setSolutionDetail(sessionData[5] != null ? Options.SolutionDetail.valueOf((String) sessionData[5]) : null);
            practiceSession.setDate(((java.sql.Timestamp) sessionData[6]).toLocalDateTime());
            practiceSession.setTimeTaken(((Number) sessionData[7]).longValue());
            if (categorySettingsId != null) practiceSession.setCategorySettings(getCategorySettings(categorySettingsId));
            practiceSession.setMathProblems(getPracticeSessionMathProblems(practiceSessionId));
            return practiceSession;
        } catch (NoResultException e) {logger.error("No PracticeSession found with ID " + practiceSessionId, e);
        } catch (Exception e) {logger.error("Error fetching PracticeSession with ID " + practiceSessionId, e);}
        return null;
    }

    @Transactional
    public void incrementActivePracticeSessionLevel(String sessionToken) {
        try {
            String fetchSessionSql = """
        SELECT practice_session_id, level
        FROM practice_session.active_practice_session
        WHERE session_token = :sessionToken
        """;

            Object[] sessionData = (Object[]) entityManager.createNativeQuery(fetchSessionSql)
                    .setParameter("sessionToken", sessionToken)
                    .getSingleResult();
            if (sessionData == null) {
                logger.error("Active PracticeSession with session token " + sessionToken + " not found.");
                return;
            }
            long practiceSessionId = ((Number) sessionData[0]).longValue();
            int currentLevel = ((Number) sessionData[1]).intValue();
            int newLevel = currentLevel + 1;

            String updateLevelSql = """
        UPDATE practice_session.active_practice_session
        SET level = :newLevel
        WHERE practice_session_id = :practiceSessionId
        """;
            int rowsUpdated = entityManager.createNativeQuery(updateLevelSql)
                    .setParameter("newLevel", newLevel)
                    .setParameter("practiceSessionId", practiceSessionId)
                    .executeUpdate();
            if (rowsUpdated == 0) logger.error("Failed to increment level for PracticeSession with ID " + practiceSessionId);
        } catch (NoResultException e) {logger.error("No active PracticeSession found with session token " + sessionToken, e);
        } catch (Exception e) {logger.error("Error incrementing level for active PracticeSession with session token " + sessionToken, e);}
    }

    @Transactional
    public void removeActivePracticeSession(long userId){
        try {
            String sql = "DELETE FROM practice_session.active_practice_session WHERE user_id = :userId";
            entityManager.createNativeQuery(sql).setParameter("userId", userId).executeUpdate();
        } catch (Exception e) {logger.error("Error removing active practice session for user id: " + userId + ": " + e.getMessage(), e);}
    }
    @Transactional
    public void removeActivePracticeSession(String sessionToken) {
        try {
            String sql = "DELETE FROM practice_session.active_practice_session WHERE session_token = :sessionToken";
            entityManager.createNativeQuery(sql).setParameter("sessionToken", sessionToken).executeUpdate();
        } catch (Exception e) {logger.error("Error removing active practice session for session token: " + sessionToken + ": " + e.getMessage(), e);}
    }

    public Options.SolutionDetail extractSolutionDetail(String json){
        try {return Options.SolutionDetail.valueOf(new ObjectMapper().readTree(json).get("solutionDetail").asText());
        } catch (Exception e) {return Options.SolutionDetail.EXACT;}
    }
    @Transactional
    public void finishPracticeSession(String sessionToken) {
        addInactivePracticeSessionToPracticeSessions(sessionToken);
        removeActivePracticeSession(sessionToken);
    }
    @Transactional
    public void addInactivePracticeSessionToPracticeSessions(String sessionToken) {
        ActivePracticeSession activePracticeSession=getActivePracticeSessionByToken(sessionToken);
        try {
            long activePracticeSessionId=activePracticeSession.getPracticeSessionId();
            setProblemTimeTaken(activePracticeSessionId);
            CategorySettings categorySettings=activePracticeSession.getCategorySettings();

            Long categorySettingsId=categorySettings!=null?categorySettings.getId():null;
            String insertSql = "INSERT INTO practice_session.practice_session " +
                    "(user_id, level, math_category, category_settings_id, solution_detail, date, time_taken) " +
                    "VALUES (:userId, :level, :mathCategory, :categorySettingsId, :solutionDetail, :date, :timeTaken)";

            entityManager.createNativeQuery(insertSql)
                    .setParameter("userId", activePracticeSession.getUserId())
                    .setParameter("level", activePracticeSession.getLevel())
                    .setParameter("mathCategory", activePracticeSession.getMathCategory().name())
                    .setParameter("categorySettingsId", categorySettingsId)
                    .setParameter("solutionDetail", activePracticeSession.getSolutionDetail().name())
                    .setParameter("date", Timestamp.valueOf(activePracticeSession.getDate()))
                    .setParameter("timeTaken", getPracticeSessionTimeTakenBasedOnMathProblemsTimeTaken(activePracticeSessionId))
                    .executeUpdate();
            Long newSessionId = ((Number) entityManager.createNativeQuery("SELECT LAST_INSERT_ID()").getSingleResult()).longValue();

            String updateMathProblemsSql = """
            UPDATE practice_session.math_problem
            SET practice_session_id = :newSessionId
            WHERE practice_session_id = :activeSessionId
        """;

            entityManager.createNativeQuery(updateMathProblemsSql)
                    .setParameter("newSessionId", newSessionId)
                    .setParameter("activeSessionId", activePracticeSession.getPracticeSessionId())
                    .executeUpdate();

            updateStatistics(getPracticeSessionById(newSessionId));
        } catch (Exception e) {logger.printCrushLine(e);}
    }

    @Transactional
    public long addActivePracticeSession(long userId, String sessionToken, String json, Options.MathCategory category) {
        
        try {
            logger.info("[addActivePracticeSession] user id: " + userId+" session token: " + sessionToken+" json: " + json+" category: " + category);
            Options.SolutionDetail solutionDetail = extractSolutionDetail(json);
            CategorySettings categorySettings=null;
            if (category== Options.MathCategory.GEOMETRY) categorySettings = json != null ? GeometrySettings.jsonToObject(json, null) : null;
            else if (category==Options.MathCategory.ARITHMETICS) categorySettings = json != null ? ArithmeticSettings.jsonToObject(json, null) : null;

            PracticeSession recentPracticeSession=null;
            if (categorySettings==null) recentPracticeSession = getRecentCampaignPracticeSession(userId, category);
            int level=recentPracticeSession==null?0:recentPracticeSession.getLevel();

            String insertSessionSql = "INSERT INTO practice_session.active_practice_session " +
                    "(session_token, user_id, level, math_category, solution_detail, date)" +
                    " VALUES (:sessionToken, :userId, :level, :category, :solutionDetail, NOW())";

            entityManager.createNativeQuery(insertSessionSql)
                    .setParameter("sessionToken", sessionToken)
                    .setParameter("userId", userId)
                    .setParameter("level", level)
                    .setParameter("category", category.name())
                    .setParameter("solutionDetail", solutionDetail != null ? solutionDetail.name() : null)
                    .executeUpdate();

            Long sessionId = ((Number) entityManager.createNativeQuery("SELECT LAST_INSERT_ID()").getSingleResult()).longValue();

            if (categorySettings != null) {
                String insertCategorySettingsSql = "INSERT INTO practice_session.category_settings (practice_session_id, math_category) VALUES (:practiceSessionId, :mathCategory)";
                entityManager.createNativeQuery(insertCategorySettingsSql)
                        .setParameter("practiceSessionId", sessionId)
                        .setParameter("mathCategory", category.name())
                        .executeUpdate();

                Long categorySettingsId = ((Number) entityManager.createNativeQuery(
                                "SELECT id FROM practice_session.category_settings WHERE practice_session_id = :practiceSessionId")
                        .setParameter("practiceSessionId", sessionId)
                        .getSingleResult()).longValue();

                String updatePracticeSessionSql = "UPDATE practice_session.active_practice_session SET category_settings_id = :categorySettingsId WHERE practice_session_id = :practiceSessionId";
                entityManager.createNativeQuery(updatePracticeSessionSql)
                        .setParameter("categorySettingsId", categorySettingsId)
                        .setParameter("practiceSessionId", sessionId)
                        .executeUpdate();

                if (categorySettings instanceof GeometrySettings geometrySettings){
                    String insertSettingsSql = "INSERT INTO practice_session.geometry_settings (id, practice_session_id,solution_detail, shape_types, shapes, question_type_types) VALUES (:id, :sessionId,:solutionDetail, :shapeTypes, :shapes, :questionTypes)";
                    entityManager.createNativeQuery(insertSettingsSql)
                            .setParameter("id", categorySettingsId)
                            .setParameter("sessionId", sessionId)
                            .setParameter("solutionDetail",geometrySettings.getSolutionDetail().name())
                            .setParameter("shapeTypes", geometrySettings.getShapeTypes().stream().map(Enum::name).collect(Collectors.joining(",")))
                            .setParameter("shapes", geometrySettings.getShapes().stream().map(Enum::name).collect(Collectors.joining(",")))
                            .setParameter("questionTypes", geometrySettings.getQuestionTypes().stream().map(Enum::name).collect(Collectors.joining(",")))
                            .executeUpdate();
                }else if (categorySettings instanceof ArithmeticSettings arithmeticSettings){
                    String insertSettingsSql="INSERT INTO practice_session.arithmetic_settings (id,practice_session_id,solution_detail, number_types,question_types) VALUES (:id, :sessionId,:solutionDetail, :numberTypes, :questionTypes)";
                    entityManager.createNativeQuery(insertSettingsSql)
                            .setParameter("id", categorySettingsId)
                            .setParameter("sessionId", sessionId)
                            .setParameter("solutionDetail",arithmeticSettings.getSolutionDetail().name())
                            .setParameter("numberTypes", String.join(",", arithmeticSettings.getNumberTypes().stream().map(Enum::name).collect(Collectors.toList())))
                            .setParameter("questionTypes", String.join(",", arithmeticSettings.getQuestionTypes().stream().map(Enum::name).collect(Collectors.toList())))
                            .executeUpdate();
                }
            } else {
                String updatePracticeSessionSql = "UPDATE practice_session.active_practice_session " +
                        "SET category_settings_id = NULL WHERE practice_session_id = :practiceSessionId";

                entityManager.createNativeQuery(updatePracticeSessionSql)
                        .setParameter("practiceSessionId", sessionId)
                        .executeUpdate();
            }
            return sessionId;
        } catch (Exception e) {logger.error("Error creating a new Active Practice Session: " + e.getMessage());}
        return -1;
    }

//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%<<{ MATH PROBLEM }>>%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

    @Transactional
    public void addMathProblemToActivePracticeSession(MathProblem mathProblem, long practiceSessionId) {
        setProblemTimeTaken(practiceSessionId);
        try {
            String checkSessionSql = "SELECT COUNT(*) FROM practice_session.active_practice_session WHERE practice_session_id = :sessionId";
            long sessionCount = ((Number) entityManager.createNativeQuery(checkSessionSql)
                    .setParameter("sessionId", practiceSessionId)
                    .getSingleResult()).longValue();

            if (sessionCount == 0) {
                logger.error("Active PracticeSession with ID " + practiceSessionId + " not found.");
                return;
            }
            String insertProblemSql = """
    INSERT INTO practice_session.math_problem
    (practice_session_id, problem_level, problem_content, solution_content, fails, requested_solution, requested_hint, time_taken, created_at)
    VALUES (:practiceSessionId, :problemLevel, :problemContent, :solutionContent, :fails, :requestedSolution, :requestedHint, :timeTaken, :createdAt)
""";
            entityManager.createNativeQuery(insertProblemSql)
                    .setParameter("practiceSessionId", practiceSessionId)
                    .setParameter("problemLevel", mathProblem.getProblemLevel())
                    .setParameter("problemContent", mathProblem.getProblemContent())
                    .setParameter("solutionContent", mathProblem.getSolutionContent())
                    .setParameter("fails", mathProblem.getFails())
                    .setParameter("requestedSolution", mathProblem.getRequestedSolution())
                    .setParameter("requestedHint", mathProblem.getRequestedHint())
                    .setParameter("timeTaken", mathProblem.getTimeTaken())
                    .setParameter("createdAt", mathProblem.getCreatedAt())
                    .executeUpdate();

        } catch (Exception e) {logger.error("Error adding MathProblem to Active PracticeSession: " + e.getMessage(), e);}
    }
    @Transactional
    public ArrayList<MathProblem> getPracticeSessionMathProblems(long practiceSessionId) {
        try {
            String fetchProblemsSql = """
        SELECT id, practice_session_id, problem_level, problem_content, solution_content,\s
               fails, requested_solution, requested_hint, time_taken, created_at
        FROM practice_session.math_problem
        WHERE practice_session_id = :sessionId
        ORDER BY created_at ASC
       \s""";

            @SuppressWarnings("unchecked")
            List<Object[]> resultSet = entityManager.createNativeQuery(fetchProblemsSql)
                    .setParameter("sessionId", practiceSessionId)
                    .getResultList();

            ArrayList<MathProblem> mathProblems = new ArrayList<>();
            for (Object[] problemData : resultSet) {
                MathProblem problem = new MathProblem();
                problem.setId(((Number) problemData[0]).longValue());
                problem.setPracticeSession(null);
                problem.setProblemLevel((int) problemData[2]);
                problem.setProblemContent((String) problemData[3]);
                problem.setSolutionContent((String) problemData[4]);
                problem.setFails(((Number) problemData[5]).intValue());
                problem.setRequestedSolution(((Boolean) problemData[6]));
                problem.setRequestedHint(((Boolean) problemData[7]));
                problem.setTimeTaken(((Number) problemData[8]).longValue());
                problem.setCreatedAt(((java.sql.Timestamp) problemData[9]).toLocalDateTime());
                mathProblems.add(problem);
            }
            return mathProblems;
        } catch (Exception e) {logger.error("Error fetching math problems for practice session ID " + practiceSessionId, e);}
        return new ArrayList<>();
    }
    @Transactional
    public long getPracticeSessionTimeTakenBasedOnMathProblemsTimeTaken(long practiceSessionId) {
        BigDecimal totalTimeTaken = (BigDecimal) entityManager.createNativeQuery(
                "SELECT COALESCE(SUM(time_taken), 0) FROM practice_session.math_problem WHERE practice_session_id = :sessionId"
        ).setParameter("sessionId", practiceSessionId).getSingleResult();
        return totalTimeTaken != null ? totalTimeTaken.longValue() : 0L;
    }
    @Transactional
    public void setUsedProblemHint(long practiceSessionId) {
        try {
            Long latestProblemId = getLatestProblemId(practiceSessionId);
            if (latestProblemId==-1)return;
            if (doProblemUpdate("UPDATE practice_session.math_problem SET requested_hint=TRUE WHERE id=:problemId", latestProblemId) == 0) logger.warn("No rows updated for problem ID: " + latestProblemId);
        } catch (Exception e) {logger.error("Failed to update used hint for math problem in practice session ID: " + practiceSessionId, e);}
    }
    @Transactional
    public void setUsedProblemSolution(long practiceSessionId) {
        try {
            Long latestProblemId = getLatestProblemId(practiceSessionId);
            if (latestProblemId==-1)return;
            if (doProblemUpdate("UPDATE practice_session.math_problem SET requested_solution=TRUE WHERE id=:problemId", latestProblemId) == 0) logger.warn("No rows updated for problem ID: " + latestProblemId);
        } catch (Exception e) {logger.error("Failed to update requested solution for math problem in practice session ID: " + practiceSessionId, e);}
    }
    @Transactional
    public void incrementProblemFails(long practiceSessionId) {
        try {
            Long latestProblemId = getLatestProblemId(practiceSessionId);
            if (latestProblemId==-1)return;
            if (doProblemUpdate("UPDATE practice_session.math_problem SET fails = fails + 1 WHERE id=:problemId", latestProblemId) == 0) logger.warn("No rows updated for problem ID: " + latestProblemId);
        } catch (Exception e) {logger.error("Failed to update fails for math problem in practice session ID: " + practiceSessionId, e);}
    }
    @Transactional
    public void setProblemTimeTaken(long practiceSessionId) {
        try {
            Long latestProblemId = getLatestProblemId(practiceSessionId);
            if (latestProblemId == -1) return;

            Long storedTimeTaken = (Long) entityManager.createNativeQuery(
                    "SELECT time_taken FROM practice_session.math_problem WHERE id = :problemId"
            ).setParameter("problemId", latestProblemId).getSingleResult();

            long timeDifferenceMillis = System.currentTimeMillis() - storedTimeTaken;

            String updateQuery = "UPDATE practice_session.math_problem " +
                    "SET time_taken = :newTimeTaken WHERE id = :problemId";

            if (entityManager.createNativeQuery(updateQuery)
                    .setParameter("newTimeTaken", timeDifferenceMillis)
                    .setParameter("problemId", latestProblemId)
                    .executeUpdate() == 0) {
                logger.warn("No rows updated for problem ID: " + latestProblemId);
            }
        } catch (Exception e) {logger.error("Failed to update time_taken for math problem in practice session ID: " + practiceSessionId, e);}
    }

    private Long getLatestProblemId(long practiceSessionId) {
        return (Long) entityManager.createNativeQuery(
                "SELECT id FROM practice_session.math_problem WHERE practice_session_id=:sessionId ORDER BY created_at DESC LIMIT 1"
        ).setParameter("sessionId", practiceSessionId).getSingleResult();
    }

    private int doProblemUpdate(String query, Long id) {return entityManager.createNativeQuery(query).setParameter("problemId", id).executeUpdate();}

//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%<<{ SETTINGS }>>%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

    public CategorySettings getCategorySettings(long categorySettingsId) {
        String fetchCategorySql = " SELECT id, practice_session_id, math_category FROM practice_session.category_settings WHERE id = :categorySettingsId ";

        List<Object[]> categoryDataList = entityManager.createNativeQuery(fetchCategorySql)
                .setParameter("categorySettingsId", categorySettingsId)
                .getResultList();

        if (categoryDataList.isEmpty()) return null;

        Object[] categoryData = categoryDataList.get(0);
        if (categoryData == null) return null;
        Long id = ((Number) categoryData[0]).longValue();
        String mathCategory = (String) categoryData[2];

        return switch (Options.MathCategory.valueOf(mathCategory)) {
            case GEOMETRY -> getGeometrySettings(id);
            case ARITHMETICS -> getArithmeticSettings(id);
            default -> throw new IllegalArgumentException("Unsupported MathCategory: " + mathCategory);
        };
    }

    private GeometrySettings getGeometrySettings(Long categorySettingsId) {
        String fetchGeometrySql = "SELECT id, practice_session_id,solution_detail, shape_types, shapes, question_type_types FROM practice_session.geometry_settings WHERE id = :categorySettingsId";

        Object[] geometryData = (Object[]) entityManager.createNativeQuery(fetchGeometrySql)
                .setParameter("categorySettingsId", categorySettingsId)
                .getSingleResult();

        if (geometryData != null) {
            Long id = ((Number) geometryData[0]).longValue();
            Long practiceSessionId = ((Number) geometryData[1]).longValue();
            String solutionDetailRaw=(String) geometryData[2];
            String shapeTypesRaw = (String) geometryData[3];
            String shapesRaw = (String) geometryData[4];
            String questionTypeTypesRaw = (String) geometryData[5];

            Options.SolutionDetail solutionDetail=(solutionDetailRaw==null || solutionDetailRaw.isBlank())? Options.SolutionDetail.EXACT : Options.SolutionDetail.valueOf(solutionDetailRaw);

            Set<Geometry.ShapeType> shapeTypes = shapeTypesRaw == null || shapeTypesRaw.isEmpty()
                    ? new HashSet<>()
                    : Arrays.stream(shapeTypesRaw.split(","))
                    .map(Geometry.ShapeType::valueOf)
                    .collect(Collectors.toSet());

            Set<Geometry.Shape> shapes = shapesRaw == null || shapesRaw.isEmpty()
                    ? new HashSet<>()
                    : Arrays.stream(shapesRaw.split(","))
                    .map(Geometry.Shape::valueOf)
                    .collect(Collectors.toSet());

            Set<Geometry.QuestionType> questionTypeTypes = questionTypeTypesRaw == null || questionTypeTypesRaw.isEmpty()
                    ? new HashSet<>()
                    : Arrays.stream(questionTypeTypesRaw.split(","))
                    .map(Geometry.QuestionType::valueOf)
                    .collect(Collectors.toSet());

            GeometrySettings geometrySettings= new GeometrySettings(practiceSessionId, solutionDetail, shapeTypes, shapes, questionTypeTypes);
            geometrySettings.setId(id);
            return geometrySettings;
        }
        return null;
    }

    private ArithmeticSettings getArithmeticSettings(Long categorySettingsId) {
        String fetchArithmeticSql = "SELECT id, practice_session_id,solution_detail, number_types, question_types FROM practice_session.arithmetic_settings WHERE id = :categorySettingsId";

        Object[] arithmeticData = (Object[]) entityManager.createNativeQuery(fetchArithmeticSql)
                .setParameter("categorySettingsId", categorySettingsId)
                .getSingleResult();

        if (arithmeticData != null) {
            Long id = ((Number) arithmeticData[0]).longValue();
            Long practiceSessionId = ((Number) arithmeticData[1]).longValue();
            String solutionDetailRaw=(String) arithmeticData[2];
            String numberTypesRaw = (String) arithmeticData[3];
            String questionTypesRaw = (String) arithmeticData[4];

            Options.SolutionDetail solutionDetail=(solutionDetailRaw==null || solutionDetailRaw.isBlank())? Options.SolutionDetail.EXACT : Options.SolutionDetail.valueOf(solutionDetailRaw);

            Set<Arithmetic.NumberType> numberTypes = numberTypesRaw == null || numberTypesRaw.isEmpty()
                    ? new HashSet<>()
                    : Arrays.stream(numberTypesRaw.split(","))
                    .map(Arithmetic.NumberType::valueOf)
                    .collect(Collectors.toSet());

            Set<Arithmetic.QuestionType> questionTypes = questionTypesRaw == null || questionTypesRaw.isEmpty()
                    ? new HashSet<>()
                    : Arrays.stream(questionTypesRaw.split(","))
                    .map(Arithmetic.QuestionType::valueOf)
                    .collect(Collectors.toSet());


            ArithmeticSettings arithmeticSettings = new ArithmeticSettings(practiceSessionId,solutionDetail, numberTypes, questionTypes);
            arithmeticSettings.setId(id);
            return arithmeticSettings;
        }
        return null;
    }

//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%<<{ UTILITY }>>%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

    @Transactional
    public void clearAllTables() {
        try {
            entityManager.createNativeQuery("DELETE FROM practice_session.math_problem").executeUpdate();
            entityManager.createNativeQuery("DELETE FROM practice_session.geometry_settings").executeUpdate();
            entityManager.createNativeQuery("DELETE FROM practice_session.arithmetic_settings").executeUpdate();
            entityManager.createNativeQuery("DELETE FROM practice_session.practice_session").executeUpdate();
            entityManager.createNativeQuery("DELETE FROM practice_session.category_settings").executeUpdate();
            entityManager.createNativeQuery("DELETE FROM practice_session.active_practice_session").executeUpdate();
            entityManager.createNativeQuery("ALTER TABLE practice_session.practice_session AUTO_INCREMENT = 1").executeUpdate();
            entityManager.createNativeQuery("ALTER TABLE practice_session.category_settings AUTO_INCREMENT = 1").executeUpdate();
            entityManager.createNativeQuery("ALTER TABLE practice_session.geometry_settings AUTO_INCREMENT = 1").executeUpdate();
            entityManager.createNativeQuery("ALTER TABLE practice_session.arithmetic_settings AUTO_INCREMENT = 1").executeUpdate();
            entityManager.createNativeQuery("ALTER TABLE practice_session.math_problem AUTO_INCREMENT = 1").executeUpdate();
            entityManager.createNativeQuery("ALTER TABLE practice_session.active_practice_session AUTO_INCREMENT = 1").executeUpdate();
            entityManager.createNativeQuery("DELETE FROM practice_session.statistics").executeUpdate();
            entityManager.createNativeQuery("ALTER TABLE practice_session.statistics AUTO_INCREMENT = 1").executeUpdate();
        } catch (Exception e) {
            logger.error("Error resetting tables: " + e.getMessage(), e);
        }
    }

}
