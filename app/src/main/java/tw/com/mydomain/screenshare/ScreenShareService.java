package tw.com.mydomain.screenshare;

import android.Manifest;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.util.Log;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;

import io.mgmeet.sdk.HttpClient;
import io.mgmeet.sdk.McuSessionConfig;
import io.mgmeet.sdk.McuSessionInfo;
import io.mgmeet.sdk.SdkCallback;
import io.mgmeet.sdk.SdkClient;
import io.mgmeet.sdk.SdkError;
import io.mgmeet.sdk.SpSessionConfig;
import io.mgmeet.sdk.SpSessionInfo;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ScreenShareService extends Service {

    private static String LOG_TAG = ScreenShareService.class.getName();

    private static final String SDK_SERVICE_URL = "https://api.mgmeet.io";
    private static final String SDK_APP_ID = BuildConfig.SDK_APP_ID;
    private static final String SDK_APP_KEY = BuildConfig.SDK_APP_KEY;

    private static final String SERVER_URL = BuildConfig.SERVER_URL;

    private static final int SCREEN_CAPTURE_WIDTH = 1920;
    private static final int SCREEN_CAPTURE_HEIGHT = 1080;
    private static final int SCREEN_CAPTURE_FRAMERATE = 30;
    private static final int SCREEN_CAPTURE_BANDWIDTH = 2048; // kbps

    private static final int CAPTURE_PERMISSION_REQUEST_CODE = 1;
    private static final int AUDIO_PERMISSION_REQUEST_CODE = 2;

    static final String ACTION_CAPTURE_PERMISSION = "capture_permission_result";
    static final String ACTION_AUDIO_PERMISSION = "audio_permission_result";

    private SdkClient mSdkClient;
    private SpSessionInfo mSessionInfo;
    private McuSessionInfo mMcuSessionInfo;

    @Override
    public void onCreate() {
        super.onCreate();
        mSdkClient = new SdkClient(SDK_SERVICE_URL, SDK_APP_ID, SDK_APP_KEY);
        Log.d(LOG_TAG, "onCreate");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new ScreenShareBinder();
    }

    class ScreenShareBinder extends ScreenShareInterface.Stub {

        public boolean isBinding() {
            return true;
        }

        public void startScreenShare(String email, String roomId, String roomPassword) {
            SpSessionConfig spsessionConfig = new SpSessionConfig(roomId, SCREEN_CAPTURE_BANDWIDTH);
            McuSessionConfig mcusessionConfig = new McuSessionConfig(roomId, 1024);

            mSdkClient.createSpSession(spsessionConfig, new SdkCallback() {
                @Override
                public void onError(SdkError error) {
                    Log.i(LOG_TAG, "error: " + error.code + ", " + error.message);
                    if (error.exception != null) {
                        error.exception.printStackTrace();
                    }
                }

                @Override
                public void onResult(SpSessionInfo session) {
                    Log.i(LOG_TAG, "session: " + session.toJsonString());
                    mSessionInfo = session;

                    // update session info to demo server
                    //publishSessionInfo(session, email, roomId, roomPassword);

                    // start capture flow
                    //startCapture();
                }
            });

            mSdkClient.createMcuSession(mcusessionConfig, new SdkCallback() {
                @Override
                public void onError(SdkError error) {
                    Log.i(LOG_TAG, "error: " + error.code + ", " + error.message);
                    if (error.exception != null) {
                        error.exception.printStackTrace();
                    }
                }

                @Override
                public void onResult(McuSessionInfo session) {
                    Log.i(LOG_TAG, "session: " + session.toJsonString());
                    mMcuSessionInfo = session;

                    // update session info to demo server
//                    publishMcuSessionInfo(session, email, roomId, roomPassword);
                    if(mSessionInfo != null) {
                        publishSessionsInfo(mSessionInfo, mMcuSessionInfo, email, roomId, roomPassword );
                    }

                    // start capture flow
                    startCapture();
                }
            });
        }

        @Override
        public void stopScreenCapture() {
            Intent intent = new Intent("tw.com.mydomain.screenshare.action.STOP");
            intent.setComponent(new ComponentName("tw.com.mydomain.screenshare", "tw.com.mydomain.screenshare.StreamService"));
            startService(intent);
        }
    }

    private void onError(SdkError error) {
        Log.i(LOG_TAG, "error: " + error.code + ", " + error.message);
        if (error.exception != null) {
            error.exception.printStackTrace();
        }
    }

    private void publishSessionInfo(SpSessionInfo session, String email, String roomId, String roomPassword) {
        String data;
        Log.i("publish.email", email);
        Log.i("publish.roomId", roomId);
        Log.i("publish.room.password", roomPassword);
        Log.i("publish.room.uri", session.uri);
        Log.i("publish.room.rxToken", session.rxToken);
        try {
            JSONObject d = new JSONObject();
            d.put("uri", session.uri);
            d.put("rxToken", session.rxToken);

            JSONObject json = new JSONObject();
            json.put("data", d);
            json.put("email", email);
            json.put("roomid", roomId);
            json.put("password", roomPassword);

            data = json.toString();
        } catch (JSONException e) {
            return;
        }

        HttpClient.postJson(SERVER_URL.concat("/index/create/").concat(roomId), data, null, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                onError(new SdkError(e));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                try {
                    String data = Objects.requireNonNull(response.body()).string();
                    JSONObject json = new JSONObject(data);

                    SdkError error = SdkError.parseJson(json.optJSONObject("error"));
                    if (error != null) {
                        onError(error);
                    }
                } catch (JSONException e) {
                    onError(new SdkError(e));
                }
            }
        });
    }

    private void publishMcuSessionInfo(McuSessionInfo session, String email, String roomId, String roomPassword) {
        String data;
        Log.i("publish.email", email);
        Log.i("publish.roomId", roomId);
        Log.i("publish.room.password", roomPassword);
        Log.i("publish.room.uri", session.uri);
        Log.i("publish.room.token", session.token);
        try {
            JSONObject d = new JSONObject();
            d.put("uri", session.uri);
            d.put("token", session.token);

            JSONObject json = new JSONObject();
            json.put("data", d);
            json.put("email", email);
            json.put("roomid", roomId);
            json.put("password", roomPassword);

            data = json.toString();
        } catch (JSONException e) {
            return;
        }

        HttpClient.postJson(SERVER_URL.concat("/index/create/").concat(roomId), data, null, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                onError(new SdkError(e));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                try {
                    String data = Objects.requireNonNull(response.body()).string();
                    JSONObject json = new JSONObject(data);

                    SdkError error = SdkError.parseJson(json.optJSONObject("error"));
                    if (error != null) {
                        onError(error);
                    }
                } catch (JSONException e) {
                    onError(new SdkError(e));
                }
            }
        });
    }

    private void publishSessionsInfo(SpSessionInfo session, McuSessionInfo mcuSession,String email, String roomId, String roomPassword) {
        String data;
        Log.i("publish.email", email);
        Log.i("publish.roomId", roomId);
        Log.i("publish.room.password", roomPassword);
        Log.i("publish.room.uri", session.uri);
        Log.i("publish.room.rxToken", session.rxToken);
        Log.i("publish.room.mcuUri", mcuSession.uri);
        Log.i("publish.room.token", mcuSession.token);
        try {
            JSONObject d = new JSONObject();
            d.put("uri", session.uri);
            d.put("rxToken", session.rxToken);

            d.put("mcuUri", mcuSession.uri);
            d.put("token", mcuSession.token);

            JSONObject json = new JSONObject();
            json.put("data", d);
            json.put("email", email);
            json.put("roomid", roomId);
            json.put("password", roomPassword);

            data = json.toString();
        } catch (JSONException e) {
            return;
        }

        HttpClient.postJson(SERVER_URL.concat("/index/create/").concat(roomId), data, null, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                onError(new SdkError(e));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                try {
                    String data = Objects.requireNonNull(response.body()).string();
                    JSONObject json = new JSONObject(data);

                    SdkError error = SdkError.parseJson(json.optJSONObject("error"));
                    if (error != null) {
                        onError(error);
                    }
                } catch (JSONException e) {
                    onError(new SdkError(e));
                }
            }
        });
    }


    //=========================================================================
    // Capture flow
    //=========================================================================
    private void startCapture() {
        if (checkAudioPermissions()) {
            requestScreenCapture();
        }
    }

    private boolean checkAudioPermissions() {
        Log.d(LOG_TAG, "check audio");
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            Intent intent = new Intent(ScreenShareService.this, PermissionActivity.class);
            intent.putExtra("permissionRequestCode", AUDIO_PERMISSION_REQUEST_CODE);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            return false;
        }
    }

    private void requestScreenCapture() {
        Log.d(LOG_TAG, "check capture");
        Intent intent = new Intent(ScreenShareService.this, PermissionActivity.class);
        intent.putExtra("permissionRequestCode", CAPTURE_PERMISSION_REQUEST_CODE);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, "on start command");
        if (intent != null) {
            final String action = intent.getAction();
            if (action != null) {
                switch (action) {
                    case ACTION_AUDIO_PERMISSION:
                        if (intent.getBooleanExtra("result", false)) {
                            requestScreenCapture();
                        } else {
                            Log.e(LOG_TAG, "User didn't give audio permission");
                        }
                        break;
                    case ACTION_CAPTURE_PERMISSION:
                        if (intent.getBooleanExtra("result", false)) {
                            Intent data = intent.getParcelableExtra("data");
                            Intent intentToCapture = new Intent("tw.com.mydomain.screenshare.action.START");
                            intentToCapture.setComponent(new ComponentName("tw.com.mydomain.screenshare", "tw.com.mydomain.screenshare.StreamService"));
                            intentToCapture.putExtra("projectionData", data);
                            intentToCapture.putExtra("width", SCREEN_CAPTURE_WIDTH);
                            intentToCapture.putExtra("height", SCREEN_CAPTURE_HEIGHT);
                            intentToCapture.putExtra("frameRate", SCREEN_CAPTURE_FRAMERATE);
                            intentToCapture.putExtra("sessionInfo", mSessionInfo.toJsonString());
                            intentToCapture.putExtra("mcuSessionInfo", mMcuSessionInfo.toJsonString());
                            startService(intentToCapture);
                            Log.d(LOG_TAG, "service started");
                        } else {
                            Log.e(LOG_TAG, "User didn't give permission to capture the screen.");
                        }
                        break;
                }
            }
        }
        return START_NOT_STICKY;
    }
}
