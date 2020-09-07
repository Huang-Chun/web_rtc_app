package tw.com.mydomain.screenshare;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    private String LOG_TAG = MainActivity.class.getName();

    boolean tempBoolFlag = false;
    boolean isLogin = false;
    String name = "";
    String roomId = "";
    String email = "";

    EditText registerNameEditText;
    EditText registerMailEditText;
    EditText registerPasswordEditText;
    EditText registerConfirmPasswordEditText;
    EditText loginEmailEditText;
    EditText loginPasswordEditText;
    EditText roomPasswordEditText;
    TextView userInformationTextView;

    AuthenticationInterface authenticationInterface;
    ScreenShareInterface screenShareInterface;

    ServiceConnection authenticationServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            authenticationInterface = AuthenticationInterface.Stub.asInterface(binder);
            try {
                Log.i(LOG_TAG.concat(" AuthenticationInterface is binding "), String.valueOf(authenticationInterface.isBinding()));
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            authenticationInterface = null;
        }
    };

    ServiceConnection screenShareServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            screenShareInterface = ScreenShareInterface.Stub.asInterface(binder);
            try {
                Log.i(LOG_TAG.concat(" ScreenShareInterface is binding "), String.valueOf(screenShareInterface.isBinding()));
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            screenShareInterface = null;
        }
    };
    private Object View;

    void bindRemoteServices() {
        final Intent authIntent = new Intent();
        authIntent.setAction("tw.com.mydomain.screenshare.AuthenticationService");
        authIntent.setPackage("tw.com.mydomain.screenshare");
        bindService(authIntent, authenticationServiceConnection, Context.BIND_AUTO_CREATE);
        final Intent sIntent = new Intent();
        sIntent.setAction("tw.com.mydomain.screenshare.ScreenShareService");
        sIntent.setPackage("tw.com.mydomain.screenshare");
        bindService(sIntent, screenShareServiceConnection, Context.BIND_AUTO_CREATE);
    }


    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = new Intent();

        // run AuthenticationService
        intent.setClass(MainActivity.this, AuthenticationService.class);
        startService(intent);

        // run ScreenShareService
        intent.setClass(MainActivity.this, ScreenShareService.class);
        startService(intent);

        // finish MainActivity
        // this.finish();


        // bind Authentication and ScreenShare Services
        bindRemoteServices();

        // set View and Button Click Listeners

        setContentView(R.layout.activity_main);
        registerNameEditText = findViewById(R.id.et_register_name);
        registerMailEditText = findViewById(R.id.et_register_email);
        registerPasswordEditText = findViewById(R.id.et_register_password);
        registerConfirmPasswordEditText = findViewById(R.id.et_register_confirm_password);
        loginEmailEditText = findViewById(R.id.et_login_email);
        loginPasswordEditText = findViewById(R.id.et_login_password);
        roomPasswordEditText = findViewById(R.id.et_room_password);
        userInformationTextView = findViewById(R.id.tv_user);

        Button registerButton = findViewById(R.id.b_register);
        registerButton.setOnClickListener(v -> {
            if (authenticationInterface != null) {
                new Thread(() -> {
                    try {
                        tempBoolFlag = authenticationInterface.register(registerNameEditText.getText().toString(),
                                registerMailEditText.getText().toString(),
                                registerPasswordEditText.getText().toString(),
                                registerConfirmPasswordEditText.getText().toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    runOnUiThread(() -> {
                        if (tempBoolFlag) {
                            Toast toast = Toast.makeText(MainActivity.this, "Register succeed", Toast.LENGTH_LONG);
                            toast.show();
                        } else {
                            Toast toast = Toast.makeText(MainActivity.this, "Register failed", Toast.LENGTH_LONG);
                            toast.show();
                        }
                    });
                }).start();
            } else {
                Log.e(LOG_TAG, "AuthenticationInterface is null");
            }
        });

        Button to_login = findViewById(R.id.to_login);
        to_login.setOnClickListener(v -> {
            Intent tologin = new Intent();
            tologin.setClass(MainActivity.this  , LoginActivity.class);
            startActivity(tologin);
        });



        Button loginButton = findViewById(R.id.b_login);
        loginButton.setOnClickListener(v -> {
            if (authenticationInterface != null) {
                new Thread(() -> {
                    try {
                        isLogin = authenticationInterface.login(loginEmailEditText.getText().toString(),
                                loginPasswordEditText.getText().toString());
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    if (isLogin) {
                        try {
                            name = authenticationInterface.getName();
                            roomId = authenticationInterface.getRoomId();
                            email = authenticationInterface.getEmail();
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                    runOnUiThread(() -> {
                        if (isLogin) {
                            Toast toast = Toast.makeText(MainActivity.this, "Login succeed", Toast.LENGTH_LONG);
                            toast.show();
                            userInformationTextView.setText("Name: " + name + "\nEmail: " + email + "\nRoom ID: " + roomId);
                        } else {
                            Toast toast = Toast.makeText(MainActivity.this, "Login failed", Toast.LENGTH_LONG);
                            toast.show();
                        }
                    });
                }).start();
            } else {
                Log.e(LOG_TAG, "AuthenticationInterface is null");
            }
        });
        Button logoutButton = findViewById(R.id.b_logout);
        logoutButton.setOnClickListener(v -> {
            if (authenticationInterface != null) {
                new Thread(() -> {
                    try {
                        boolean logout = authenticationInterface.logout(email);
                        if (logout) {
                            isLogin = false;
                        }
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    runOnUiThread(() -> {
                        if (!isLogin) {
                            Toast toast = Toast.makeText(MainActivity.this, "Logout succeed", Toast.LENGTH_LONG);
                            toast.show();
                            userInformationTextView.setText("User information");
                        } else {
                            Toast toast = Toast.makeText(MainActivity.this, "Logout failed", Toast.LENGTH_LONG);
                            toast.show();
                        }
                    });
                }).start();
            } else {
                Log.e(LOG_TAG, "AuthenticationInterface is null");
            }
        });
        Button startShareButton = findViewById(R.id.b_start);
        startShareButton.setOnClickListener(v -> {
            String password = roomPasswordEditText.getText().toString();
            if (authenticationInterface != null) {
                try {
                    email = authenticationInterface.getEmail();
                    roomId = authenticationInterface.getRoomId();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            } else {
                Log.e(LOG_TAG, "AuthenticationInterface is null");
            }
            if (screenShareInterface != null) {
                try {
                    screenShareInterface.startScreenShare(email, roomId, password);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            } else {
                Log.e(LOG_TAG, "ScreenShareInterface is null");
            }
        });
        Button stopShareButton = findViewById(R.id.b_stop);
        stopShareButton.setOnClickListener(v -> {
            if (screenShareInterface != null) {
                try {
                    screenShareInterface.stopScreenCapture();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            } else {
                Log.e(LOG_TAG, "ScreenShareInterface is null");
            }
        });
    }

    @Override
    protected void onDestroy() {
        unbindService(authenticationServiceConnection);
        unbindService(screenShareServiceConnection);
        super.onDestroy();
    }
}
