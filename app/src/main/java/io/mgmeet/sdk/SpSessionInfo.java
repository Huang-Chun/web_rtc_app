package io.mgmeet.sdk;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public class SpSessionInfo {
    public String id;
    public String appId;

    public String name;
    public int bandwidth;
    public int rxCount;
    public Date expiry;

    public String uri;
    public String txToken;
    public String rxToken;

    public static SpSessionInfo parseJson(JSONObject json) {
        if (json == null) {
            return null;
        }

        SpSessionInfo session = new SpSessionInfo();

        session.id = json.optString("id", "");
        session.appId = json.optString("appId", "");

        session.name = json.optString("name", "");
        session.bandwidth = json.optInt("bandwidth", 0);
        session.rxCount = json.optInt("rxCount", 0);
        session.expiry = SdkUtil.parseJsonDate(json.optString("expiry", ""));

        session.uri = json.optString("uri", "");
        session.txToken = json.optString("txToken", "");
        session.rxToken = json.optString("rxToken", "");

        return session;
    }

    public static SpSessionInfo parseJsonString(String s) {
        try {
            JSONObject json = new JSONObject(s);
            return parseJson(json);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String toJsonString() {
        try {
            JSONObject config = new JSONObject();
            config.put("id", id);
            config.put("appId", appId);

            config.put("name", name);
            config.put("bandwidth", bandwidth);
            config.put("rxCount", rxCount);
            if (expiry != null) {
                config.put("expiry", SdkUtil.toDateString(expiry));
            }

            config.put("uri", uri);
            config.put("txToken", txToken);
            config.put("rxToken", rxToken);

            return config.toString();
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

}
