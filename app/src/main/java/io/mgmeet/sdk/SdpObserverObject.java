package io.mgmeet.sdk;

import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;

public class SdpObserverObject implements SdpObserver {
    @Override
    public void onCreateSuccess(SessionDescription sessionDescription) {
    }

    @Override
    public void onSetSuccess() {
    }

    @Override
    public void onCreateFailure(String s) {
    }

    @Override
    public void onSetFailure(String s) {
    }
}
