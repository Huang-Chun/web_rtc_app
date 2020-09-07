package io.mgmeet.sdk;


import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public class
McuSessionConfig {
    public String name;
    public boolean video;
    public boolean audio;
    public String resolution;
    public int bandwidth;
    public int attendees;
    public int audiences;
    public Date expiry;

    private String LOG_TAG = McuSessionConfig.class.getName();

    public McuSessionConfig(String name, int bandwidth) {
        this(name, false, true, "720P", bandwidth, 4, 0, SdkUtil.dateAfterMinutes(60 * 4));
    }

    public McuSessionConfig(String name, boolean video, boolean audio, String resolution, int bandwidth, int attendees, int audiences, Date expiry) {
        this.name = name;
        this.video = video;
        this.audio = audio;
        this.resolution = resolution;
        this.bandwidth = bandwidth;
        this.attendees = attendees;
        this.audiences = audiences;
        this.expiry = expiry;
    }

    public String toJsonString(String wrapperName) {
        try {
            JSONObject config = new JSONObject();
            config.put("name", name);
            config.put("video", video);
            config.put("audio", audio);
            config.put("resolution", resolution);
            config.put("bandwidth", bandwidth);
            config.put("attendees", attendees);
            config.put("audiences", audiences);
            Log.d(LOG_TAG, String.valueOf(config));

            if (expiry != null) {
                config.put("expiry", SdkUtil.toDateString(expiry));
            }

            if (wrapperName == null) {
                return config.toString();
            }

            JSONObject wrapper = new JSONObject();
            wrapper.put(wrapperName, config);
            return wrapper.toString();
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}
