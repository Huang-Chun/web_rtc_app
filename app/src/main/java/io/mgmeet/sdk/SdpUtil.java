package io.mgmeet.sdk;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

public class SdpUtil {
    static public String setBandwidth(String sdp, int video, int audio) {
        List<String> list = new ArrayList<String>();
        String[] lines = TextUtils.split(sdp, "\r\n");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
//            Log.e("MGDroid ",+ i + ": <" + line + ">");
            list.add(line);
            if (video > 0 && line.startsWith("m=video ")) {
                list.add("b=AS:" + video);
            } else if (audio > 0 && line.startsWith("m=audio ")) {
                list.add("b=AS:" + audio);
            }
        }
        String newSdp = TextUtils.join("\r\n", list);
        return newSdp;
    }

    static public String setPreferH264(String sdp) {
        if (!sdp.contains(" H264/")) {
            return sdp;
        }

//        Log.w("MGDroid ","orgSdp: <" + sdp + ">");
        List<String> list = new ArrayList<String>();
        String[] lines = TextUtils.split(sdp, "\r\n");

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            if (line.startsWith("a=rtpmap:96") || line.startsWith("a=rtcp-fb:96")) {
                continue; // skip VP8
            } else if (line.startsWith("a=rtpmap:97") || line.startsWith("a=fmtp:97")) {
                continue; // skip VP8
            } else if (line.startsWith("a=rtpmap:98") || line.startsWith("a=rtcp-fb:98")) {
                continue; // skip VP9
            } else if (line.startsWith("a=rtpmap:99") || line.startsWith("a=fmtp:99")) {
                continue; // skip VP9
            }
            list.add(line);
        }

        String newSdp = TextUtils.join("\r\n", list);
//        Log.e("MGDroid ","h264Sdp: <" + newSdp + ">");
        return newSdp;
    }
}