package io.mgmeet.sdk;


import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;

public class SpClientListener {
    public void onWsOpen(SpClient client) {
    }

    public void onWsClosed(SpClient client, int code, String reason) {
    }

    public void onWsClosing(SpClient client, int code, String reason) {
    }

    public void onWsFailure(SpClient client, Throwable t, int code, String message) {
    }

    public void onIceConnectionChange(SpClient client, PeerConnection.IceConnectionState iceConnectionState) {
    }

    public void onAddStream(SpClient client, MediaStream stream) {
    }

    public void onRemoveStream(SpClient client, MediaStream stream) {
    }
}
