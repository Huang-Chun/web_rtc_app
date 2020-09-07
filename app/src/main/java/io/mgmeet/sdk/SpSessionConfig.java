package io.mgmeet.sdk;


import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public class
SpSessionConfig {
    public String name;
    public int bandwidth;
    public int rxCount;
    public Date expiry;

    private String LOG_TAG = SpSessionConfig.class.getName();

    public SpSessionConfig(String name, int bandwidth) {
        this(name, bandwidth, 4, SdkUtil.dateAfterMinutes(60 * 4));
    }

    public SpSessionConfig(String name, int bandwidth, int rxCount, Date expiry) {
        this.name = name;
        this.bandwidth = bandwidth;
        this.rxCount = rxCount;
        this.expiry = expiry;
    }

    public String toJsonString(String wrapperName) {
        try {
            JSONObject config = new JSONObject();
            config.put("name", name);
            config.put("bandwidth", bandwidth);
            config.put("rxCount", rxCount);
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
