package io.mgmeet.sdk;


import org.json.JSONObject;

import java.util.Date;

public class ApiTokenInfo {
    public String token;
    public Date expiry;

    public ApiTokenInfo(String token) {
        this.token = token;
    }

    public static ApiTokenInfo parseJson(JSONObject json) {
        if (json == null) {
            return null;
        }

        String token = json.optString("token", "");
        ApiTokenInfo apiToken = new ApiTokenInfo(token);

        String expiry = json.optString("expiry", "");
        apiToken.expiry = SdkUtil.parseJsonDate(expiry);

        return apiToken;
    }
}
