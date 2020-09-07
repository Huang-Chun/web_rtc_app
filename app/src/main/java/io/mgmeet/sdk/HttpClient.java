package io.mgmeet.sdk;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Callback;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class HttpClient {
    public static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");

    private static ConcurrentHashMap<String, List<Cookie>> mCookieStore = new ConcurrentHashMap<>();

    public static OkHttpClient.Builder newBuilderWithoutSslCheck() {
        X509TrustManager x509TrustManager = new X509TrustManager() {
            @Override
            public X509Certificate[] getAcceptedIssuers() {
                X509Certificate[] x509Certificates = new X509Certificate[0];
                return x509Certificates;
            }

            @Override
            public void checkServerTrusted(final X509Certificate[] chain,
                                           final String authType) throws CertificateException {
//                System.out.println(TAG + ": authType: " + String.valueOf(authType));
            }

            @Override
            public void checkClientTrusted(final X509Certificate[] chain,
                                           final String authType) throws CertificateException {
//                System.out.println(TAG + ": authType: " + String.valueOf(authType));
            }
        };

        final TrustManager[] trustAllCerts = new TrustManager[]{
                x509TrustManager,
        };

        SSLContext sslContext = null;
        try {
            sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        } catch (KeyManagementException e) {
            e.printStackTrace();
            return null;
        }

        HostnameVerifier hostnameVerifier = new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
//                Log.d(TAG, "Trust Host :" + hostname);
                return true;
            }
        };

        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .sslSocketFactory(sslContext.getSocketFactory(), x509TrustManager)
                .hostnameVerifier(hostnameVerifier);

        // add cookie support
        builder = builder.cookieJar(new CookieJar() {
            @Override
            public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
                mCookieStore.put(url.host(), cookies);
            }

            @Override
            public List<Cookie> loadForRequest(HttpUrl url) {
                List<Cookie> cookies = mCookieStore.get(url.host());
                return cookies != null ? cookies : new ArrayList<Cookie>();
            }
        });

        return builder;
    }

    public static void getJson(String url, String token, Callback callback) {
        OkHttpClient client = newBuilderWithoutSslCheck().build();

        Request.Builder request = new Request.Builder()
                .url(url)
                .get();

        if (token != null) {
            request.addHeader("Authorization", "Bearer " + token);
        }

        client.newCall(request.build()).enqueue(callback);
    }

    public static void postJson(String url, String json, String token, Callback callback) {
        OkHttpClient client = newBuilderWithoutSslCheck().build();

        Request.Builder request = new Request.Builder()
            .url(url)
            .post(RequestBody.create(JSON_MEDIA_TYPE, json));

        if (token != null) {
            request.addHeader("Authorization", "Bearer " + token);
        }

        client.newCall(request.build()).enqueue(callback);

//        Response response = client.newCall(request).execute();
//        return response.body().string();
    }

    public static WebSocket connectWs(String url, String protocol, WebSocketListener listener) {
        OkHttpClient client = HttpClient.newBuilderWithoutSslCheck()
                .readTimeout(0,  TimeUnit.MILLISECONDS)
                .build();

        Request.Builder request = new Request.Builder().url(url);
        if (protocol != null) {
            request.addHeader("Sec-WebSocket-Protocol", "binary");
        }

        WebSocket ws = client.newWebSocket(request.build(), listener);

        // Trigger shutdown of the dispatcher's executor so this process can exit cleanly.
        client.dispatcher().executorService().shutdown();

        return ws;
    }
}
