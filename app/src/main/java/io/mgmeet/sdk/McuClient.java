package io.mgmeet.sdk;


import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.RtpReceiver;
import org.webrtc.SessionDescription;

import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class McuClient {
    private static final String TAG = "MGSdk";

    private McuClientListener mListener;
    private int mVideoBandwidth;
    private int mAudioBandwidth = 32;
    private String mDisplayName = "";

    private String mUrl;
    private String mRxToken;
    private String mTxToken;
    private MediaStream mTxStream;
    private MediaStream mRxStream;

    private WebSocket mWebSocket;
    private Timer mPingTimer;
    private PeerConnection mPeerConnection;
    private boolean mOfferSent;

    public McuClient() {
    }

    public void setListener(McuClientListener listener) {
        mListener = listener;
    }

    public void setVideoBandwidth(int videoBandwidth) {
        mVideoBandwidth = videoBandwidth;
    }

    public void setDisplayName(String name) {
        mDisplayName = name;
    }

    public void startTx(String url, String txToken, MediaStream stream) {
        mUrl = url;
        mTxToken = txToken;
        mTxStream = stream;

        Log.e(TAG, "wsUrl: " + mUrl + "?access_token=" + mTxToken);
        mWebSocket = HttpClient.connectWs(mUrl + "?access_token=" + mTxToken, null, mWebSocketListener);
    }

    public void startRx(String url, String rxToken) {
        mUrl = url;
        mRxToken = rxToken;

        Log.e(TAG, "wsUrl: " + mUrl + "?access_token=" + mRxToken);
        mWebSocket = HttpClient.connectWs(mUrl + "?access_token=" + mRxToken, null, mWebSocketListener);
    }

    public void close() {
        if (mWebSocket != null) {
            mWebSocket.close(1000, null);
        }
        if (mPingTimer != null) {
            mPingTimer.cancel();
            mPingTimer = null;
        }
    }

    //===============================================================
    // WebSocket
    //===============================================================
    private WebSocketListener mWebSocketListener = new WebSocketListener() {
        @Override
        public void onOpen(WebSocket webSocket, Response response) {
            if (mListener != null) {
                mListener.onWsOpen(McuClient.this);
            }

            mPingTimer = new Timer();
            mPingTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    webSocket.send("{}");
                }
            }, 3000, 5000);

            mPeerConnection = createPeerConnection();
            createOffer(mTxStream);
        }

        @Override
        public void onClosed(WebSocket webSocket, int code, String reason) {
            if (mListener != null) {
                mListener.onWsClosed(McuClient.this, code, reason);
            }
        }

        @Override
        public void onClosing(WebSocket webSocket, int code, String reason) {
            webSocket.close(1000, null);
            if (mListener != null) {
                mListener.onWsClosing(McuClient.this, code, reason);
            }
        }

        @Override
        public void onFailure(WebSocket webSocket, Throwable t, Response response) {
            if (mListener != null) {
                if (response != null) {
                    mListener.onWsFailure(McuClient.this, t, response.code(), response.message());
                } else {
                    mListener.onWsFailure(McuClient.this, t, -1, null);
                }
            }
        }

        @Override
        public void onMessage(WebSocket webSocket, String text) {
            onWsMessage(text);
        }

        @Override
        public void onMessage(WebSocket webSocket, ByteString bytes) {
            onWsMessage(bytes.utf8());
        }
    };

    private void onWsMessage(String msg) {
        try {
            JSONObject json = new JSONObject(msg);
            String method = json.optString("method", "");

            switch (method) {
                case "answer": {
                    JSONObject description = json.optJSONObject("description");
                    if (description == null) {
                        Log.e(TAG, "McuClient remote sdp null");
                        close();
                        return;
                    }

                    String fixedDescription = description.optString("sdp", "");
//                fixedDescription = SdpUtils.setPreferH264(fixedDescription);
                    SessionDescription sdp = new SessionDescription(SessionDescription.Type.ANSWER, fixedDescription);

                    mPeerConnection.setRemoteDescription(new SdpObserverObject() {
                        @Override
                        public void onSetFailure(String s) {
                            Log.e(TAG, "McuClient setRemoteDescription onSetFailure: " + s);
                            close();
                        }
                    }, sdp);
                    break;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void sendOffer(SessionDescription sdp) {
        try {
            JSONObject description = new JSONObject();
            description.put("type", sdp.type.canonicalForm());
            description.put("sdp", sdp.description);

            JSONObject json = new JSONObject();
            json.put("method", "offer");
            json.put("description", description);
            json.put("displayName", mDisplayName);

            String msg = json.toString();
            Log.d(TAG, "sendOffer: " + msg);
            mWebSocket.send(msg);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //===============================================================
    // PeerConnection
    //===============================================================
    private PeerConnection createPeerConnection() {
        PeerConnection.IceServer[] iceServers = new PeerConnection.IceServer[0];
        MediaConstraints constraints = new MediaConstraints();
        constraints.optional.add(new MediaConstraints.KeyValuePair("DtlsSrtpKeyAgreement", "true"));
        PeerConnection pc = WebrtcFactory.createPeerConnection(Arrays.asList(iceServers), constraints, mPeerConnectionObserver);

        return pc;
    }

    private void createOffer(MediaStream stream) {
        if (stream != null) {
            mPeerConnection.addStream(stream);
        }

        MediaConstraints constraints = new MediaConstraints(); //限制拿到的資訊要有audio或video
        constraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", stream == null ? "true" : "false"));
        constraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveAudio", stream == null ? "true" : "false"));

        mPeerConnection.createOffer(new SdpObserverObject() {
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
                String fixedDescription = SdpUtil.setBandwidth(sessionDescription.description, mVideoBandwidth, mAudioBandwidth);
                fixedDescription = SdpUtil.setPreferH264(fixedDescription);
                SessionDescription sdp = new SessionDescription(sessionDescription.type, fixedDescription);

                mPeerConnection.setLocalDescription(new SdpObserverObject() {
                    @Override
                    public void onSetFailure(String s) {
                        Log.e(TAG, "McuClient setLocalDescription onSetFailure: " + s);
                        close();
                    }
                }, sdp);
            }

            @Override
            public void onCreateFailure(String s) {
                Log.e(TAG, "McuClient createOffer onCreateFailure: " + s);
                close();
            }
        }, constraints);
    }

    private PeerConnection.Observer mPeerConnectionObserver = new PeerConnection.Observer() {
        @Override
        public void onSignalingChange(PeerConnection.SignalingState signalingState) {
            Log.d(TAG, "onSignalingChange: " + signalingState);
        }

        @Override
        public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {
//            Log.d(TAG, "onIceConnectionChange: " + iceConnectionState);
            if (mListener != null) {
                mListener.onIceConnectionChange(McuClient.this, iceConnectionState);
            }
        }

        @Override
        public void onIceConnectionReceivingChange(boolean b) {
        }

        @Override
        public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {
            Log.d(TAG, "onIceGatheringChange: " + iceGatheringState);

            if (iceGatheringState == PeerConnection.IceGatheringState.COMPLETE && !mOfferSent) {
                SessionDescription sessionDescription = mPeerConnection.getLocalDescription();
                String fixedDescription = sessionDescription.description;
//                fixedDescription = SdpUtils.setPreferH264(fixedDescription);
                SessionDescription sdp = new SessionDescription(sessionDescription.type, fixedDescription);

                sendOffer(sdp);
                mOfferSent = true;
            }
        }

        @Override
        public void onIceCandidate(IceCandidate iceCandidate) {
            Log.d(TAG, "onIceCandidate: " + iceCandidate.sdp);
        }

        @Override
        public void onIceCandidatesRemoved(IceCandidate[] iceCandidates) {
        }

        @Override
        public void onAddStream(MediaStream mediaStream) {
            mRxStream = mediaStream;
            if (mListener != null) {
                mListener.onAddStream(McuClient.this, mediaStream);
            }
        }

        @Override
        public void onRemoveStream(MediaStream mediaStream) {
            if (mListener != null) {
                mListener.onRemoveStream(McuClient.this, mediaStream);
            }
        }

        @Override
        public void onDataChannel(DataChannel dataChannel) {
        }

        @Override
        public void onRenegotiationNeeded() {
        }

        @Override
        public void onAddTrack(RtpReceiver rtpReceiver, MediaStream[] mediaStreams) {
        }
    };
}
