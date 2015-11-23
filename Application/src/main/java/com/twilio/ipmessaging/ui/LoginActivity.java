package com.twilio.ipmessaging.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.android.displayingbitmaps.R;
import com.example.android.displayingbitmaps.provider.Tokens;
import com.example.android.displayingbitmaps.ui.ImageDetailActivity;
import com.twilio.ipmessaging.util.BasicIPMessagingClient;
import com.twilio.ipmessaging.util.HttpHelper;
import com.twilio.ipmessaging.util.ILoginListener;
import com.twilio.ipmessaging.util.Logger;
import com.twilio.ipmessaging.application.TwilioApplication;

import java.net.URLEncoder;

public class LoginActivity extends FragmentActivity implements ILoginListener {

    private static final Logger logger = Logger.getLogger(LoginActivity.class);
    private static final String DEFAULT_CLIENT_EMAIL = "test@foo.com";
    private static final String AUTH_SCRIPT = "https://twilio-ip-messaging-token.herokuapp.com/token";
    private String capabilityToken = null;
    private BasicIPMessagingClient chatClient;
    private EditText clientEmail;
    private String endpoint_id = "";
    public static String local_author = DEFAULT_CLIENT_EMAIL;

    private Button login;
    private ProgressDialog progressDialog;

    private int currentImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        this.clientEmail = (EditText) findViewById(R.id.etEmail);
        this.clientEmail.setText(DEFAULT_CLIENT_EMAIL);
        this.endpoint_id = Settings.Secure.getString(this.getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        this.login = (Button) findViewById(R.id.btSend);

        this.login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String idChosen = clientEmail.getText().toString();
                String endpointIdFull = idChosen + "-" + LoginActivity.this.endpoint_id + "-android-" + getApplication().getPackageName();

                StringBuilder url = new StringBuilder();
                url.append(AUTH_SCRIPT);
                url.append("?identity=");
                url.append(URLEncoder.encode(idChosen));
                url.append("&endpointId=" + URLEncoder.encode(endpointIdFull));
                url.append(clientEmail.getText().toString());
                url.append("&endpoint_id=" + LoginActivity.this.endpoint_id);
                url.append("&ttl=3600");

                // Replace this with hard coded values or change Tokens.java

                url.append("&account_sid=" + Tokens.AccountSid);
                url.append("&auth_token=" + Tokens.AuthToken);
                url.append("&service_sid=" + Tokens.ServiceSid);

                logger.e("url string : " + url.toString());

                new GetCapabilityTokenAsyncTask().execute(url.toString());
            }
        });

        chatClient = TwilioApplication.get().getBasicClient();

        currentImage = getIntent().getIntExtra(ImageDetailActivity.EXTRA_IMAGE, -1);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onLoginStarted() {
        logger.d("Log in started");
    }

    @Override
    public void onLoginFinished() {
        LoginActivity.this.progressDialog.dismiss();
        Intent i = new Intent(this, ChatActivity.class);
        i.putExtra(ImageDetailActivity.EXTRA_IMAGE, currentImage);
        this.startActivity(i);
    }

    @Override
    public void onLoginError(String errorMessage) {
        LoginActivity.this.progressDialog.dismiss();
        logger.e("Error logging in : " + errorMessage);
        Toast.makeText(getBaseContext(), errorMessage, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onLogoutFinished() {

    }

    private class GetCapabilityTokenAsyncTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);


            new Thread(new Runnable() {
                @Override
                public void run() {
                    LoginActivity.this.chatClient.doLogin(LoginActivity.this);
                }
            }).start();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            LoginActivity.this.progressDialog = ProgressDialog.show(LoginActivity.this, "",
                    "Logging in. Please wait...", true);
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                capabilityToken = HttpHelper.httpGet(params[0]);
                logger.e("capabilityToken string : " + capabilityToken);
                chatClient.setCapabilityToken(capabilityToken);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return capabilityToken;
        }
    }
}
