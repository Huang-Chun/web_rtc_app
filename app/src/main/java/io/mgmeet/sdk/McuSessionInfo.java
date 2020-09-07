package io.mgmeet.sdk;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public class McuSessionInfo {
    public String id;
    public String appId;

    public String name;
    public boolean video;
    public boolean audio;
    public String resolution;
    public int bandwidth;
    public int attendees;
    public int audiences;
    public Date expiry;

    public String uri;
    public String token;
    public String rxToken;

    public static McuSessionInfo parseJson(JSONObject json) {
        if (json == null) {
            return null;
        }

        McuSessionInfo session = new McuSessionInfo();

        session.id = json.optString("id", "");
        session.appId = json.optString("appId", "");

        session.name = json.optString("name", "");
        session.video = json.optBoolean("video", false);
        session.audio = json.optBoolean("audio", false);
        session.resolution = json.optString("resolution", "");
        session.bandwidth = json.optInt("bandwidth", 0);
        session.attendees = json.optInt("attendees", 0);
        session.audiences = json.optInt("audiences", 0);
        session.expiry = SdkUtil.parseJsonDate(json.optString("expiry", ""));

        session.uri = json.optString("uri", "");
        session.token = json.optString("token", "");
        session.rxToken = json.optString("rxToken", "");

        return session;
    }

    public static McuSessionInfo parseJsonString(String s) {
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
            config.put("video", video);
            config.put("audio", audio);
            config.put("resolution", resolution);
            config.put("bandwidth", bandwidth);
            config.put("attendees", attendees);
            config.put("audiences", audiences);

            if (expiry != null) {
                config.put("expiry", SdkUtil.toDateString(expiry));
            }

            config.put("uri", uri);
            config.put("token", token);
            config.put("rxToken", rxToken);

            return config.toString();
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

}
