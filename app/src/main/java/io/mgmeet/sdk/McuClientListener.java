package io.mgmeet.sdk;


import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;

public class McuClientListener {
    public void onWsOpen(McuClient client) {
    }

    public void onWsClosed(McuClient client, int code, String reason) {
    }

    public void onWsClosing(McuClient client, int code, String reason) {
    }

    public void onWsFailure(McuClient client, Throwable t, int code, String message) {
    }

    public void onIceConnectionChange(McuClient client, PeerConnection.IceConnectionState iceConnectionState) {
    }

    public void onAddStream(McuClient client, MediaStream stream) {
    }

    public void onRemoveStream(McuClient client, MediaStream stream) {
    }
}
