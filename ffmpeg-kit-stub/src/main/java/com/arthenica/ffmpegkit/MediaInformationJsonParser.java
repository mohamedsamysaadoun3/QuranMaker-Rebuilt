package com.arthenica.ffmpegkit;

import com.arthenica.smartexception.java.Exceptions;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/* loaded from: classes.dex */
public class MediaInformationJsonParser {
    public static final String KEY_CHAPTERS = "chapters";
    public static final String KEY_STREAMS = "streams";

    public static MediaInformation from(String str) {
        try {
            return fromWithError(str);
        } catch (JSONException e) {
            android.util.Log.e("ffmpeg-kit", String.format("MediaInformation parsing failed.%s", Exceptions.getStackTraceString(e)));
            return null;
        }
    }

    public static MediaInformation fromWithError(String str) throws JSONException {
        JSONObject jSONObject = new JSONObject(str);
        JSONArray optJSONArray = jSONObject.optJSONArray(KEY_STREAMS);
        JSONArray optJSONArray2 = jSONObject.optJSONArray(KEY_CHAPTERS);
        ArrayList arrayList = new ArrayList();
        for (int i = 0; optJSONArray != null && i < optJSONArray.length(); i++) {
            JSONObject optJSONObject = optJSONArray.optJSONObject(i);
            if (optJSONObject != null) {
                arrayList.add(new StreamInformation(optJSONObject));
            }
        }
        ArrayList arrayList2 = new ArrayList();
        for (int i2 = 0; optJSONArray2 != null && i2 < optJSONArray2.length(); i2++) {
            JSONObject optJSONObject2 = optJSONArray2.optJSONObject(i2);
            if (optJSONObject2 != null) {
                arrayList2.add(new Chapter(optJSONObject2));
            }
        }
        return new MediaInformation(jSONObject, arrayList, arrayList2);
    }
}
