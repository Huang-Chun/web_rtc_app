package tw.com.mydomain.screenshare;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.util.Log;

import org.jetbrains.annotations.NotNull;

public class PermissionActivity extends Activity {

    private String LOG_TAG = PermissionActivity.class.getName();

    private static final int CAPTURE_PERMISSION_REQUEST_CODE = 1;
    private static final int AUDIO_PERMISSION_REQUEST_CODE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission);

        Log.d(LOG_TAG, "permission activity");

        Intent intent = getIntent();

        switch (intent.getIntExtra("permissionRequestCode", AUDIO_PERMISSION_REQUEST_CODE)) {
            case AUDIO_PERMISSION_REQUEST_CODE:
                requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, AUDIO_PERMISSION_REQUEST_CODE);
//                final Intent resultIntent = new Intent(this, ScreenShareService.class);
//                resultIntent.putExtra("result", true);
//                startService(resultIntent);
//                this.finish();
                break;
            case CAPTURE_PERMISSION_REQUEST_CODE:
                MediaProjectionManager mediaProjectionManager =
                        (MediaProjectionManager) getApplication().getSystemService(
                                Context.MEDIA_PROJECTION_SERVICE);
                assert mediaProjectionManager != null;
                startActivityForResult(
                        mediaProjectionManager.createScreenCaptureIntent(), CAPTURE_PERMISSION_REQUEST_CODE);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        Log.d(LOG_TAG, "activity result");
        super.onActivityResult(requestCode, resultCode, data);
        Intent resultIntent = new Intent(this, ScreenShareService.class);
        resultIntent.setAction(ScreenShareService.ACTION_CAPTURE_PERMISSION);
        if (resultCode == RESULT_OK) {
            resultIntent.putExtra("result", true);
            resultIntent.putExtra("data", data);
        } else {
            resultIntent.putExtra("result", false);
        }
        startService(resultIntent);
        this.finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NotNull String[] permissions, @NotNull int[] grantResults) {
        Log.d(LOG_TAG, "permission result");
        if (requestCode == AUDIO_PERMISSION_REQUEST_CODE) {
            final Intent resultIntent = new Intent(this, ScreenShareService.class);
            resultIntent.setAction(ScreenShareService.ACTION_AUDIO_PERMISSION);
            if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                resultIntent.putExtra("result", true);
            } else {
                resultIntent.putExtra("result", false);
            }
            startService(resultIntent);
            this.finish();
        }
    }


}
