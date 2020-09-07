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
import android.text.InputType;
import android.text.Selection;
import android.text.Spannable;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class RegisterActivity extends Activity {

    private String LOG_TAG = RegisterActivity.class.getName();

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
        intent.setClass(RegisterActivity.this, AuthenticationService.class);
        startService(intent);

        // run ScreenShareService
        intent.setClass(RegisterActivity.this, ScreenShareService.class);
        startService(intent);

        // finish MainActivity
        // this.finish();


        // bind Authentication and ScreenShare Services
        bindRemoteServices();

        // set View and Button Click Listeners

        setContentView(R.layout.activity_register);
        registerNameEditText = findViewById(R.id.et_register_name);
        registerMailEditText = findViewById(R.id.et_register_email);
        registerPasswordEditText = findViewById(R.id.et_register_password);
        registerConfirmPasswordEditText = findViewById(R.id.et_register_confirm_password);
//        loginEmailEditText = findViewById(R.id.et_login_email);
//        loginPasswordEditText = findViewById(R.id.et_login_password);
//        roomPasswordEditText = findViewById(R.id.et_room_password);
//        userInformationTextView = findViewById(R.id.tv_user);

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
                            Toast toast = Toast.makeText(RegisterActivity.this, "Register succeed", Toast.LENGTH_LONG);
                            toast.show();
                            Intent tologin = new Intent();
                            tologin.setClass(RegisterActivity.this  , LoginActivity.class);
                            startActivity(tologin);
                        } else {
                            Toast toast = Toast.makeText(RegisterActivity.this, "Register failed", Toast.LENGTH_LONG);
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
            tologin.setClass(RegisterActivity.this  , LoginActivity.class);
            startActivity(tologin);
        });

        // an eye to control the password is visible or not
        final CheckBox chkShow = ((CheckBox)findViewById(R.id.checkbox_eye));
        chkShow.setOnClickListener(
                new View.OnClickListener()
                {
                    public void onClick(View v)
                    {
                        TextView txtPwd = (TextView)findViewById(R.id.et_register_confirm_password);
                        final boolean isChecked = chkShow.isChecked();
                        if(isChecked)
                        {// show password
                            txtPwd.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                        }
                        else
                        {// not show password
                            txtPwd.setTransformationMethod(PasswordTransformationMethod.getInstance());
                        }
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
