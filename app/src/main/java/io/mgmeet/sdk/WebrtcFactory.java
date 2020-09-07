package io.mgmeet.sdk;


import android.content.Context;
import android.util.Log;

import org.webrtc.DefaultVideoDecoderFactory;
import org.webrtc.DefaultVideoEncoderFactory;
import org.webrtc.EglBase;
import org.webrtc.MediaConstraints;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.VideoCodecInfo;
import org.webrtc.VideoDecoderFactory;
import org.webrtc.VideoEncoderFactory;

import java.util.List;

public class WebrtcFactory {
    private static final String TAG = "MGSdk";

    static private PeerConnectionFactory mInstance;
    static private EglBase mEglBase;

    public static void init(Context context) {
        if (mInstance == null) {
            mEglBase = EglBase.create();

            PeerConnectionFactory.initialize(
                    PeerConnectionFactory.InitializationOptions.builder(context)
                            .setEnableInternalTracer(true)
                            .createInitializationOptions());

            VideoDecoderFactory decoderFactory = new DefaultVideoDecoderFactory(mEglBase.getEglBaseContext());
            VideoCodecInfo[] decoderInfos = decoderFactory.getSupportedCodecs();
            for (int i = 0; i < decoderInfos.length; i++) {
                VideoCodecInfo info = decoderInfos[i];
                Log.i(TAG, "rtc decoder" + i + ": " + info.name);
            }

            VideoEncoderFactory encoderFactory = new DefaultVideoEncoderFactory(mEglBase.getEglBaseContext(),true,true );
            VideoCodecInfo[] encoderInfos = encoderFactory.getSupportedCodecs();
            for (int i = 0; i < encoderInfos.length; i++) {
                VideoCodecInfo info = encoderInfos[i];
                Log.i(TAG, "rtc encoder" + i + ": " + info.name);
            }

            mInstance = PeerConnectionFactory.builder().setOptions(new PeerConnectionFactory.Options())
                    .setVideoDecoderFactory(new DefaultVideoDecoderFactory(mEglBase.getEglBaseContext()))
                    .setVideoEncoderFactory(new DefaultVideoEncoderFactory(mEglBase.getEglBaseContext(),true,true ))
                    .createPeerConnectionFactory();
        }
    }

    public static EglBase.Context getEglBaseContext() {
        return mEglBase.getEglBaseContext();
    }

    public static PeerConnectionFactory getFactory() {
        return mInstance;
    }

    public static PeerConnection createPeerConnection(List<PeerConnection.IceServer> iceServers, MediaConstraints constraints, PeerConnection.Observer observer) {
        PeerConnection.RTCConfiguration rtcConfig = new PeerConnection.RTCConfiguration(iceServers);
        rtcConfig.tcpCandidatePolicy = PeerConnection.TcpCandidatePolicy.DISABLED;
//        rtcConfig.networkPreference = PeerConnection.AdapterType.WIFI;
        return mInstance.createPeerConnection(rtcConfig, constraints, observer);
    }
}
