package tw.com.mydomain.screenshare;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.ConnectException;
import java.util.Objects;

import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

class Authentication {

    private static String LOG_TAG = Authentication.class.getName();

    static JSONObject login(String email, String password) {

        Log.i(LOG_TAG.concat(" Email"), email);
        Log.i(LOG_TAG.concat(" Password"), password);

        OkHttpClient client = new OkHttpClient();

        RequestBody formBody = new FormBody.Builder()
                .add("email", email)
                .add("password", password)
                .build();
        Request request = new Request.Builder()
                .url(BuildConfig.SERVER_URL.concat("/users/login"))
                .header("Content-Type", "application/json")
                .post(formBody)
                .build();
        try {
            Response response = client.newCall(request).execute();
            String responseBodyStr = Objects.requireNonNull(response.body()).string();
            Log.i(LOG_TAG.concat(" Login response body "), responseBodyStr);
            return new JSONObject(responseBodyStr);
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return new JSONObject();
    }

    static JSONObject register(String name, String email, String password, String confirmPassword) {
        Log.i(LOG_TAG.concat(" Name"), name);
        Log.i(LOG_TAG.concat(" Email"), email);
        Log.i(LOG_TAG.concat(" Password"), password);
        Log.i(LOG_TAG.concat(" ConfirmPassword"), confirmPassword);

        OkHttpClient client = new OkHttpClient();

        RequestBody formBody = new FormBody.Builder()
                .add("name", name)
                .add("email", email)
                .add("password", password)
                .add("password2", confirmPassword)
                .build();
        Request request = new Request.Builder()
                .url(BuildConfig.SERVER_URL.concat("/users/register"))
                .header("Content-Type", "application/json")
                .post(formBody)
                .build();
        try {
            Response response = client.newCall(request).execute();
            String responseBodyStr = Objects.requireNonNull(response.body()).string();
            Log.d(LOG_TAG.concat(" Register response body"), responseBodyStr);
            return new JSONObject(responseBodyStr);
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return new JSONObject();
    }

    static JSONObject logout(String email) {

        OkHttpClient client = new OkHttpClient();

        RequestBody formBody = new FormBody.Builder()
                .add("email", email)
                .build();
        Request request = new Request.Builder()
                .url(BuildConfig.SERVER_URL.concat("/users/logout"))
                .post(formBody)
                .build();
        try {
            Response response = client.newCall(request).execute();
            String responseBodyStr = Objects.requireNonNull(response.body()).string();
            Log.d(LOG_TAG.concat(" Logout response body"), responseBodyStr);
            return new JSONObject(responseBodyStr);
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return new JSONObject();
    }
}
