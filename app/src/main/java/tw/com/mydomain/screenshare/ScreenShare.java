package tw.com.mydomain.screenshare;

import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjection;
import android.util.Log;

import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.ScreenCapturerAndroid;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import io.mgmeet.sdk.McuClient;
import io.mgmeet.sdk.McuClientListener;
import io.mgmeet.sdk.McuSessionInfo;
import io.mgmeet.sdk.SpClient;
import io.mgmeet.sdk.SpClientListener;
import io.mgmeet.sdk.SpSessionInfo;
import io.mgmeet.sdk.WebrtcFactory;

class ScreenShare {
    private static final String TAG = "MGScreenShare";

    static ScreenShare mInstance = new ScreenShare();

    private Context mContext;
    private VideoCapturer mVideoCapturer;
    private MediaStream mMediaStream;
    private VideoTrack mVideoTrack;
    private AudioTrack mAudioTrack;

    private SpClient mSpClient;
    private McuClient mMcuClient;

    void init(Context context) {
        mContext = context;
        WebrtcFactory.init(context);
    }

    void startStreaming(Intent projectionData, int width, int height, int frameRate, SpSessionInfo session) {
        PeerConnectionFactory peerConnectionFactory = WebrtcFactory.getFactory();

        mVideoCapturer = new ScreenCapturerAndroid(projectionData, new MediaProjection.Callback() {
            @Override
            public void onStop() {
                Log.e(TAG, "User revoked permission to capture the screen.");
                stopStreaming();
            }
        });

        SurfaceTextureHelper surfaceTextureHelper = SurfaceTextureHelper.create("ScreenCaptureThread", WebrtcFactory.getEglBaseContext());
        VideoSource videoSource = peerConnectionFactory.createVideoSource(mVideoCapturer.isScreencast());
        mVideoCapturer.initialize(surfaceTextureHelper, mContext, videoSource.getCapturerObserver());
        mVideoTrack = peerConnectionFactory.createVideoTrack("videoTrack", videoSource);

        AudioSource audioSource = peerConnectionFactory.createAudioSource(new MediaConstraints());
        mAudioTrack = peerConnectionFactory.createAudioTrack("audioTrack", audioSource);

        mMediaStream = peerConnectionFactory.createLocalMediaStream("localStream");
        mMediaStream.addTrack(mVideoTrack);
        mMediaStream.addTrack(mAudioTrack);

        mVideoCapturer.startCapture(width, height, frameRate);

        mSpClient = new SpClient();
        mSpClient.setListener(mSpClientListener);
        mSpClient.setVideoBandwidth(session.bandwidth);
        mSpClient.startTx(session.uri, session.txToken, mMediaStream);
    }

    void startStreaming(Intent projectionData, int width, int height, int frameRate, SpSessionInfo session, McuSessionInfo mcuSession) {
        PeerConnectionFactory peerConnectionFactory = WebrtcFactory.getFactory();

        mVideoCapturer = new ScreenCapturerAndroid(projectionData, new MediaProjection.Callback() {
            @Override
            public void onStop() {
                Log.e(TAG, "User revoked permission to capture the screen.");
                stopStreaming();
            }
        });

        SurfaceTextureHelper surfaceTextureHelper = SurfaceTextureHelper.create("ScreenCaptureThread", WebrtcFactory.getEglBaseContext());
        VideoSource videoSource = peerConnectionFactory.createVideoSource(mVideoCapturer.isScreencast());
        mVideoCapturer.initialize(surfaceTextureHelper, mContext, videoSource.getCapturerObserver());
        mVideoTrack = peerConnectionFactory.createVideoTrack("videoTrack", videoSource);

        AudioSource audioSource = peerConnectionFactory.createAudioSource(new MediaConstraints());
        mAudioTrack = peerConnectionFactory.createAudioTrack("audioTrack", audioSource);

        mMediaStream = peerConnectionFactory.createLocalMediaStream("localStream");
        mMediaStream.addTrack(mVideoTrack);
        mMediaStream.addTrack(mAudioTrack);

        mVideoCapturer.startCapture(width, height, frameRate);

        mSpClient = new SpClient();
        mSpClient.setListener(mSpClientListener);
        mSpClient.setVideoBandwidth(session.bandwidth);
        mSpClient.startTx(session.uri, session.txToken, mMediaStream);

        mMcuClient = new McuClient();
        mMcuClient.setListener(mMcuClientListener);
        mMcuClient.startRx(mcuSession.uri, mcuSession.token);
    }

    void stopStreaming() {
        if (mSpClient != null) {
            mSpClient.setListener(null);
            mSpClient.close();
            mSpClient = null;
        }
        if (mMcuClient != null) {
            mMcuClient.setListener(null);
            mMcuClient.close();
            mMcuClient = null;
        }
        if (mMediaStream != null) {
            mMediaStream.removeTrack(mVideoTrack);
            mMediaStream.removeTrack(mAudioTrack);
            mMediaStream = null;
            mVideoTrack = null;
            mAudioTrack = null;
        }
        if (mVideoCapturer != null) {
            try {
                mVideoCapturer.stopCapture();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mVideoCapturer = null;
        }
    }

    SpClientListener mSpClientListener = new SpClientListener() {
        @Override
        public void onWsOpen(SpClient client) {
            Log.d(TAG, "SpClientTx onWsOpen");
        }

        @Override
        public void onWsClosed(SpClient client, int code, String reason) {
            Log.d(TAG, "SpClientTx onWsClosed");
        }

        @Override
        public void onWsFailure(SpClient client, Throwable t, int code, String message) {
            Log.d(TAG, "SpClientTx onWsFailure: " + code + ", " + message);
            if (t != null) {
                t.printStackTrace();
            }
        }

        @Override
        public void onIceConnectionChange(SpClient client, PeerConnection.IceConnectionState iceConnectionState) {
            Log.d(TAG, "SpClientTx onIceConnectionChange: " + iceConnectionState);
            switch (iceConnectionState) {
                case COMPLETED:
                    // stream online
                    break;
                case CLOSED:
                    // stream offline
                    break;
                case DISCONNECTED:
                    // stream offline
                    break;
                default:
                    break;
            }
        }
    };

    McuClientListener mMcuClientListener = new McuClientListener() {
        @Override
        public void onWsOpen(McuClient client) {
            Log.d(TAG, "McuClientRx onWsOpen");
        }

        @Override
        public void onWsClosed(McuClient client, int code, String reason) {
            Log.d(TAG, "McuClientRx onWsClosed");
        }

        @Override
        public void onWsFailure(McuClient client, Throwable t, int code, String message) {
            Log.d(TAG, "McuClientRx onWsFailure: " + code + ", " + message);
            if (t != null) {
                t.printStackTrace();
            }
        }

        @Override
        public void onIceConnectionChange(McuClient client, PeerConnection.IceConnectionState iceConnectionState) {
            Log.d(TAG, "McuClientRx onIceConnectionChange: " + iceConnectionState);
            switch (iceConnectionState) {
                case COMPLETED:
                    // stream online
                    break;
                case CLOSED:
                    // stream offline
                    break;
                case DISCONNECTED:
                    // stream offline
                    break;
                default:
                    break;
            }
        }
    };
}
