package io.mgmeet.sdk;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class SdkClient {
    private static final String TAG = "MGSdk";

    String mServerUrl;
    String mAppId;
    String mAppKey;
    String mAppToken;

    public SdkClient(String serverUrl, String appId, String appKey) {
        mServerUrl = serverUrl;
        mAppId = appId;
        mAppKey = appKey;
        mAppToken = appId + ":" + appKey;
    }

    public void requestApiToken(SdkCallback callback) {
        try {
            JSONArray scope = new JSONArray();
            scope.put("sp");
            scope.put("mcu");

            JSONObject config = new JSONObject();
            config.put("scope", scope);
            config.put("expiry", SdkUtil.toDateString(SdkUtil.dateAfterMinutes(10)));

            JSONObject wrapper = new JSONObject();
            wrapper.put("config", config);

            String apiUrl = mServerUrl + "/api/services/auth/v1/apps/" + mAppId + "/tokens";
            HttpClient.postJson(apiUrl, wrapper.toString(), mAppToken, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callback.onError(new SdkError(e));
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        String data = response.body().string();
                        JSONObject json = new JSONObject(data);

                        SdkError error = SdkError.parseJson(json.optJSONObject("error"));
                        if (error != null) {
                            callback.onError(error);
                            return;
                        }

                        ApiTokenInfo apiToken = ApiTokenInfo.parseJson(json.optJSONObject("token"));
                        if (apiToken == null) {
                            callback.onError(new SdkError(-1, "no token result"));
                            return;
                        }

                        callback.onResult(apiToken);
                    } catch (JSONException e) {
                        callback.onError(new SdkError(e));
                    }
                }
            });
        } catch (JSONException e) {
            callback.onError(new SdkError(e));
        }
    }

    public void createSpSession(SpSessionConfig config, SdkCallback callback) {
        requestApiToken(new SdkCallback() {
            @Override
            public void onError(SdkError error) {
                callback.onError(error);
            }

            @Override
            public void onResult(ApiTokenInfo apiToken) {
                String apiUrl = mServerUrl + "/api/services/sp/v1/sessions";
                HttpClient.postJson(apiUrl, config.toJsonString("config"), apiToken.token, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        callback.onError(new SdkError(e));
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        try {
                            String data = response.body().string();
                            JSONObject json = new JSONObject(data);

                            SdkError error = SdkError.parseJson(json.optJSONObject("error"));
                            if (error != null) {
                                callback.onError(error);
                                return;
                            }

                            SpSessionInfo session = SpSessionInfo.parseJson(json.optJSONObject("session"));
                            if (session == null) {
                                callback.onError(new SdkError(-1, "no sp session result"));
                                return;
                            }

                            session.name = config.name;
                            session.bandwidth = config.bandwidth;
                            session.rxCount = config.rxCount;
                            session.expiry = config.expiry;

                            callback.onResult(session);
                        } catch (JSONException e) {
                            callback.onError(new SdkError(e));
                        }
                    }
                });
            }
        });
    }

    public void createMcuSession(McuSessionConfig config, SdkCallback callback) {
        requestApiToken(new SdkCallback() {
            @Override
            public void onError(SdkError error) {
                callback.onError(error);
            }

            @Override
            public void onResult(ApiTokenInfo apiToken) {
                String apiUrl = mServerUrl + "/api/services/mcu/v1/sessions";
                HttpClient.postJson(apiUrl, config.toJsonString("config"), apiToken.token, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        callback.onError(new SdkError(e));
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        try {
                            String data = response.body().string();
                            JSONObject json = new JSONObject(data);

                            SdkError error = SdkError.parseJson(json.optJSONObject("error"));
                            if (error != null) {
                                callback.onError(error);
                                return;
                            }

                            McuSessionInfo session = McuSessionInfo.parseJson(json.optJSONObject("session"));
                            if (session == null) {
                                callback.onError(new SdkError(-1, "no mcu session result"));
                                return;
                            }

                            session.name = config.name;
                            session.video = config.video;
                            session.audio = config.audio;
                            session.resolution = config.resolution;
                            session.bandwidth = config.bandwidth;
                            session.attendees = config.attendees;
                            session.audiences = config.audiences;
                            session.expiry = config.expiry;

                            callback.onResult(session);
                        } catch (JSONException e) {
                            callback.onError(new SdkError(e));
                        }
                    }
                });
            }
        });
    }

    public void deleteSpSession(String sessionId, SdkCallback callback) {
    }

    public void querySpSession(String sessionId, SdkCallback callback) {
    }

    public void querySpSessions(SdkCallback callback) {
    }
}
