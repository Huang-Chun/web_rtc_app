package tw.com.mydomain.screenshare;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class AuthenticationService extends Service {

    private static String LOG_TAG = AuthenticationService.class.getName();

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(LOG_TAG, "onCreate");
    }

    private boolean isLogin = false;
    private String roomId = "";
    private String name = "";
    private String email = "";

    @Override
    public IBinder onBind(Intent intent) {
        return new AuthBinder();
    }

    class AuthBinder extends AuthenticationInterface.Stub {

        public boolean isBinding() {
            return true;
        }

        @Override
        public boolean isLogin() {
            return isLogin;
        }

        public String getName() {
            return name;
        }

        public String getRoomId() {
            return roomId;
        }

        public String getEmail() {
            return email;
        }

        @Override
        public boolean login(String _email, String password) {
            JSONObject data = Authentication.login(_email, password);
            try {
                if (!data.isNull("success") && data.get("success").equals(true)) {
                    Log.i(LOG_TAG, data.get("msg").toString());
                    isLogin = true;
                    if (!data.isNull("roomid")) {
                        roomId = String.valueOf(data.get("roomid"));
                    } else {
                        Log.e(LOG_TAG, "User has no room id");
                    }
                    if (!data.isNull("name")) {
                        name = String.valueOf(data.get("name"));
                    } else {
                        Log.e(LOG_TAG, "User has no name");
                    }
                    if (!data.isNull("email")) {
                        email = String.valueOf(data.get("email"));
                    } else {
                        Log.e(LOG_TAG, "User has no email");
                    }
                    return true;
                } else {
                    Log.e(LOG_TAG, "login failed");
                    Log.e(LOG_TAG, data.get("msg").toString());
                    return false;
                }
            } catch (JSONException e) {
                throw new RuntimeException();
            }
        }

        @Override
        public boolean register(String name, String email, String password, String confirmPassword) {
            JSONObject data = Authentication.register(name, email, password, confirmPassword);
            try {
                if (!data.isNull("success") && data.get("success").equals(true)) {
                    Log.i(LOG_TAG, data.get("msg").toString());
                    return true;
                } else {
                    Log.e(LOG_TAG, "register failed");
                    Log.e(LOG_TAG, data.get("msg").toString());
                    return false;
                }
            } catch (JSONException e) {
                throw new RuntimeException();
            }
        }

        @Override
        public boolean logout(String email) {
            JSONObject data = Authentication.logout(email);
            try {
                if (!data.isNull("success") && data.get("success").equals(true)) {
                    Log.i(LOG_TAG, data.get("msg").toString());
                    isLogin = false;
                    return true;
                } else {
                    Log.e(LOG_TAG, "logout failed");
                    Log.e(LOG_TAG, data.get("msg").toString());
                    return false;
                }
            } catch (JSONException e) {
                throw new RuntimeException();
            }
        }

    }
}
