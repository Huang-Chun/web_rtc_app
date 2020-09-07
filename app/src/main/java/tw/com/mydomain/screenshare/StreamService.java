package tw.com.mydomain.screenshare;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import io.mgmeet.sdk.McuSessionInfo;
import io.mgmeet.sdk.SpSessionInfo;

public class StreamService extends Service {
    private static final String LOG_TAG = StreamService.class.getName();

    private static final String ACTION_STREAMING_START = "tw.com.mydomain.screenshare.action.START";
    private static final String ACTION_STREAMING_STOP = "tw.com.mydomain.screenshare.action.STOP";

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, "on start command");

        ScreenShare.mInstance.init(getApplicationContext());

        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_STREAMING_START.equals(action)) {
                Intent projectionData = intent.getParcelableExtra("projectionData");
                int width = intent.getIntExtra("width", 1280);
                int height = intent.getIntExtra("height", 720);
                int frameRate = intent.getIntExtra("frameRate", 30);
                SpSessionInfo sessionInfo = SpSessionInfo.parseJsonString(intent.getStringExtra("sessionInfo"));
                assert sessionInfo != null;
                McuSessionInfo mcuSessionInfo = McuSessionInfo.parseJsonString(intent.getStringExtra("mcuSessionInfo"));
                assert mcuSessionInfo != null;
//                ScreenShare.mInstance.startStreaming(projectionData, width, height, frameRate, sessionInfo);
                ScreenShare.mInstance.startStreaming(projectionData, width, height, frameRate, sessionInfo, mcuSessionInfo);
            } else if (ACTION_STREAMING_STOP.equals(action)) {
                ScreenShare.mInstance.stopStreaming();
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }
}