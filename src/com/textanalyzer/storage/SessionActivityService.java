package com.textanalyzer.storage;

import com.textanalyzer.model.SessionActivity;
import com.textanalyzer.util.JsonUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * JDBC persistence for user sessions and their activities
 * (translations, dictionary lookups, analyses, etc.).
 */
public class SessionActivityService {

    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final int TEXT_LIMIT = 8000;
    private static volatile SessionActivityService instance;

    private final DatabaseManager databaseManager;

    private SessionActivityService() {
        this.databaseManager = DatabaseManager.getInstance();
    }

    public static SessionActivityService getInstance() {
        if (instance == null) {
            synchronized (SessionActivityService.class) {
                if (instance == null) {
                    instance = new SessionActivityService();
                }
            }
        }
        return instance;
    }

    public String resolveSessionId(String candidate) {
        if (candidate != null && !candidate.trim().isEmpty()) {
            return candidate.trim();
        }
        return "SESS-" + UUID.randomUUID();
    }

    public void log(String sessionId, String activityType, String sourceText,
                    String resultText, String extraJson) {
        if (!databaseManager.isAvailable()) {
            return;
        }
        String sid = resolveSessionId(sessionId);
        String now = LocalDateTime.now().format(TS);
        ensureSession(sid, now);

        String sql = "INSERT INTO session_activities " +
                "(session_id, activity_type, source_text, result_text, extra_json, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sid);
            ps.setString(2, activityType != null ? activityType : "UNKNOWN");
            ps.setString(3, clip(sourceText));
            ps.setString(4, clip(resultText));
            ps.setString(5, extraJson != null && !extraJson.isBlank() ? extraJson : "{}");
            ps.setString(6, now);
            ps.executeUpdate();
            touchSession(sid, now);
        } catch (Exception e) {
            System.err.println("[SessionActivityService] Failed to log activity: " + e.getMessage());
        }
    }

    public List<SessionActivity> listActivities(String sessionId, int limit) {
        List<SessionActivity> list = new ArrayList<>();
        if (!databaseManager.isAvailable()) {
            return list;
        }
        int safeLimit = limit <= 0 ? 50 : Math.min(limit, 200);
        boolean filter = sessionId != null && !sessionId.trim().isEmpty();
        String sql = filter
                ? "SELECT id, session_id, activity_type, source_text, result_text, extra_json, created_at " +
                  "FROM session_activities WHERE session_id = ? ORDER BY id DESC LIMIT ?"
                : "SELECT id, session_id, activity_type, source_text, result_text, extra_json, created_at " +
                  "FROM session_activities ORDER BY id DESC LIMIT ?";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (filter) {
                ps.setString(1, sessionId.trim());
                ps.setInt(2, safeLimit);
            } else {
                ps.setInt(1, safeLimit);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (Exception e) {
            System.err.println("[SessionActivityService] Query error: " + e.getMessage());
        }
        return list;
    }

    public String sessionsToJson() {
        if (!databaseManager.isAvailable()) {
            return "[]";
        }
        String sql = "SELECT s.id, s.created_at, s.last_active_at, " +
                "COUNT(a.id) AS activity_count " +
                "FROM sessions s LEFT JOIN session_activities a ON a.session_id = s.id " +
                "GROUP BY s.id, s.created_at, s.last_active_at " +
                "ORDER BY s.last_active_at DESC LIMIT 50";
        StringBuilder sb = new StringBuilder("[");
        try (Connection conn = databaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            int i = 0;
            while (rs.next()) {
                if (i++ > 0) sb.append(",");
                sb.append("{");
                sb.append("\"id\":\"").append(JsonUtil.escape(rs.getString("id"))).append("\",");
                sb.append("\"createdAt\":\"").append(JsonUtil.escape(rs.getString("created_at"))).append("\",");
                sb.append("\"lastActiveAt\":\"").append(JsonUtil.escape(rs.getString("last_active_at"))).append("\",");
                sb.append("\"activityCount\":").append(rs.getInt("activity_count"));
                sb.append("}");
            }
        } catch (Exception e) {
            System.err.println("[SessionActivityService] Session list error: " + e.getMessage());
        }
        sb.append("]");
        return sb.toString();
    }

    public String activitiesToJson(List<SessionActivity> activities) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < activities.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(activities.get(i).toJson());
        }
        sb.append("]");
        return sb.toString();
    }

    private void ensureSession(String sessionId, String now) {
        String sql = "INSERT OR IGNORE INTO sessions (id, created_at, last_active_at) VALUES (?, ?, ?)";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sessionId);
            ps.setString(2, now);
            ps.setString(3, now);
            ps.executeUpdate();
        } catch (Exception e) {
            System.err.println("[SessionActivityService] ensureSession error: " + e.getMessage());
        }
    }

    private void touchSession(String sessionId, String now) {
        String sql = "UPDATE sessions SET last_active_at = ? WHERE id = ?";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, now);
            ps.setString(2, sessionId);
            ps.executeUpdate();
        } catch (Exception e) {
            System.err.println("[SessionActivityService] touchSession error: " + e.getMessage());
        }
    }

    private SessionActivity mapRow(ResultSet rs) throws Exception {
        return new SessionActivity(
                rs.getLong("id"),
                rs.getString("session_id"),
                rs.getString("activity_type"),
                rs.getString("source_text"),
                rs.getString("result_text"),
                rs.getString("extra_json"),
                rs.getString("created_at")
        );
    }

    private static String clip(String value) {
        if (value == null) return "";
        if (value.length() <= TEXT_LIMIT) return value;
        return value.substring(0, TEXT_LIMIT) + "...";
    }
}
