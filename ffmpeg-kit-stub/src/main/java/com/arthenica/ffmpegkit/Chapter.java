package com.arthenica.ffmpegkit;

import org.json.JSONObject;

/* loaded from: classes.dex */
public class Chapter {
    public static final String KEY_END = "end";
    public static final String KEY_END_TIME = "end_time";
    public static final String KEY_ID = "id";
    public static final String KEY_START = "start";
    public static final String KEY_START_TIME = "start_time";
    public static final String KEY_TAGS = "tags";
    public static final String KEY_TIME_BASE = "time_base";
    private final JSONObject jsonObject;

    public Chapter(JSONObject jSONObject) {
        this.jsonObject = jSONObject;
    }

    public Long getId() {
        return getNumberProperty("id");
    }

    public String getTimeBase() {
        return getStringProperty("time_base");
    }

    public Long getStart() {
        return getNumberProperty("start");
    }

    public String getStartTime() {
        return getStringProperty("start_time");
    }

    public Long getEnd() {
        return getNumberProperty("end");
    }

    public String getEndTime() {
        return getStringProperty(KEY_END_TIME);
    }

    public JSONObject getTags() {
        return getProperty("tags");
    }

    public String getStringProperty(String str) {
        JSONObject allProperties = getAllProperties();
        if (allProperties != null && allProperties.has(str)) {
            return allProperties.optString(str);
        }
        return null;
    }

    public Long getNumberProperty(String str) {
        JSONObject allProperties = getAllProperties();
        if (allProperties != null && allProperties.has(str)) {
            return Long.valueOf(allProperties.optLong(str));
        }
        return null;
    }

    public JSONObject getProperty(String str) {
        JSONObject allProperties = getAllProperties();
        if (allProperties == null) {
            return null;
        }
        return allProperties.optJSONObject(str);
    }

    public JSONObject getAllProperties() {
        return this.jsonObject;
    }
}
