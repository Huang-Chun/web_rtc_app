package io.mgmeet.sdk;

import org.json.JSONObject;

public class SdkError {
    public int code;
    public String message;
    public Exception exception;

    public SdkError(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public SdkError(Exception e) {
        this.code = -1;
        this.exception = e;
    }

    public static SdkError parseJson(JSONObject json) {
        if (json == null) {
            return null;
        }

        return new SdkError(
            json.optInt("code", 0),
            json.optString("message", "")
        );
    }
}
