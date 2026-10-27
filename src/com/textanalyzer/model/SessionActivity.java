package com.textanalyzer.model;

import com.textanalyzer.util.JsonUtil;

/**
 * One recorded action inside a browser/console session
 * (analysis, dictionary lookup, translation, search, replace).
 */
public class SessionActivity {

    private final long id;
    private final String sessionId;
    private final String activityType;
    private final String sourceText;
    private final String resultText;
    private final String extraJson;
    private final String createdAt;

    public SessionActivity(long id, String sessionId, String activityType,
                           String sourceText, String resultText,
                           String extraJson, String createdAt) {
        this.id = id;
        this.sessionId = sessionId != null ? sessionId : "";
        this.activityType = activityType != null ? activityType : "";
        this.sourceText = sourceText != null ? sourceText : "";
        this.resultText = resultText != null ? resultText : "";
        this.extraJson = extraJson != null ? extraJson : "{}";
        this.createdAt = createdAt != null ? createdAt : "";
    }

    public long getId() { return id; }
    public String getSessionId() { return sessionId; }
    public String getActivityType() { return activityType; }
    public String getSourceText() { return sourceText; }
    public String getResultText() { return resultText; }
    public String getExtraJson() { return extraJson; }
    public String getCreatedAt() { return createdAt; }

    public String toJson() {
        return String.format(
                "{\"id\":%d,\"sessionId\":\"%s\",\"activityType\":\"%s\",\"sourceText\":\"%s\"," +
                        "\"resultText\":\"%s\",\"extra\":%s,\"createdAt\":\"%s\"}",
                id,
                JsonUtil.escape(sessionId),
                JsonUtil.escape(activityType),
                JsonUtil.escape(sourceText),
                JsonUtil.escape(resultText),
                extraJson == null || extraJson.isBlank() ? "{}" : extraJson,
                JsonUtil.escape(createdAt)
        );
    }
}
