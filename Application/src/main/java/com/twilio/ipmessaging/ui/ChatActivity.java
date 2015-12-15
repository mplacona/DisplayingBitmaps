package com.twilio.ipmessaging.ui;

import android.app.ProgressDialog;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.android.displayingbitmaps.R;
import com.example.android.displayingbitmaps.provider.Tokens;
import com.example.android.displayingbitmaps.ui.ImageDetailActivity;
import com.twilio.ipmessaging.Channel;
import com.twilio.ipmessaging.ChannelListener;
import com.twilio.ipmessaging.Channels;
import com.twilio.ipmessaging.Constants;
import com.twilio.ipmessaging.Member;
import com.twilio.ipmessaging.Message;
import com.twilio.ipmessaging.Messages;
import com.twilio.ipmessaging.application.TwilioApplication;
import com.twilio.ipmessaging.util.BasicIPMessagingClient;
import com.twilio.ipmessaging.util.HttpHelper;
import com.twilio.ipmessaging.util.ILoginListener;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import io.kimo.lib.faker.Faker;
import uk.co.ribot.easyadapter.EasyAdapter;


public class ChatActivity extends FragmentActivity implements ChannelListener, ILoginListener {

    // Authentication
    private static final String AUTH_SCRIPT = "https://twilio-ip-messaging-token.herokuapp.com/token";
    private String capabilityToken = null;
    private BasicIPMessagingClient chatClient;
    private String endpoint_id = "";
    private ProgressDialog progressDialog;

    // Chat
    private static final String TAG = "ChatActivity";
    private List<Message> messages = new ArrayList<>();
    private EasyAdapter<Message> adapter;

    private ListView lvChat;
    private Button btSend;
    private EditText etMessage;

    private Channel channel;

    private int currentImage;

    public static String local_author;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chatClient = TwilioApplication.get().getBasicClient();

        // Authentication
        authenticateUser();

        // Chat
        currentImage = getIntent().getIntExtra(ImageDetailActivity.EXTRA_IMAGE, -1);

        // Message Text
        this.etMessage = (EditText) findViewById(R.id.etMessage);

        // Send Button
        this.btSend = (Button) findViewById(R.id.btSend);
        this.btSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String input = etMessage.getText().toString();
                Messages messagesObject = channel.getMessages();
                final Message message = messagesObject.createMessage(input);
                messagesObject.sendMessage(message, new Constants.StatusListener() {
                    @Override
                    public void onSuccess() {
                        Log.e(TAG, "Successful at sending message.");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                messages.add(message);
                                etMessage.setText("");
                                adapter.notifyDataSetChanged();
                            }
                        });
                    }

                    @Override
                    public void onError() {
                        Log.e(TAG, "Error sending message.");
                    }
                });
            }
        });
    }

    private class CustomMessageComparator implements Comparator<Message> {
        @Override
        public int compare(Message lhs, Message rhs) {
            return lhs.getTimeStamp().compareTo(rhs.getTimeStamp());
        }
    }

    private void setupListView() {
        final Messages messagesObject = channel.getMessages();

        if(messagesObject != null) {
            Message[] messagesArray = messagesObject.getMessages();
            if(messagesArray.length > 0 ) {
                messages = new ArrayList<>(Arrays.asList(messagesArray));
                Collections.sort(messages, new CustomMessageComparator());
            }
        }

        adapter = new EasyAdapter<>(this, MessageViewHolder.class, messages,
                new MessageViewHolder.OnMessageClickListener() {

                    @Override
                    public void onMessageClicked(Message message) {
                        // TODO: Implement options for deletion or edit
                    }
                });

        // List View
        lvChat = (ListView) findViewById(R.id.lvChat);
        lvChat.setAdapter(adapter);

        if (lvChat != null) {
            lvChat.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
            lvChat.setStackFromBottom(true);
            adapter.registerDataSetObserver(new DataSetObserver() {
                @Override
                public void onChanged() {
                    super.onChanged();
                    lvChat.setSelection(adapter.getCount() - 1);
                }
            });
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_chat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMessageAdd(Message message) {
        setupListView();
    }

    @Override
    public void onMessageChange(Message message) {

    }

    @Override
    public void onMessageDelete(Message message) {

    }

    @Override
    public void onMemberJoin(Member member) {
        setupListView();
    }

    @Override
    public void onMemberChange(Member member) {

    }

    @Override
    public void onMemberDelete(Member member) {

    }

    @Override
    public void onAttributesChange(Map<String, String> map) {

    }

    @Override
    public void onTypingStarted(Member member) {

    }

    @Override
    public void onTypingEnded(Member member) {

    }

    @Override
    public void onChannelHistoryLoaded(Channel channel) {
        setupListView();
    }

    @Override
    public void onLoginStarted() {
        Log.d(TAG, "Log in started");
    }

    @Override
    public void onLoginFinished() {
        ChatActivity.this.progressDialog.dismiss();

        final String channelName = "TestChannel" + String.valueOf(currentImage);
        Channels channelsLocal = chatClient.getIpMessagingClient().getChannels();
        // Creates a new public channel if one doesn't already exist
        if (channelsLocal.getChannelByUniqueName(channelName) != null) {
            //join it
            final Channel newChannel = channelsLocal.getChannelByUniqueName(channelName);
            newChannel.join(new Constants.StatusListener() {
                @Override
                public void onSuccess() {
                    channel.setListener(ChatActivity.this);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Messages messagesObject = newChannel.getMessages();
                            Message[] messagesArray = messagesObject.getMessages();
                            if (messagesArray.length > 0) {
                                messages = new ArrayList<>(Arrays.asList(messagesArray));
                            }
                            channel = newChannel;
                            setupListView();
                        }
                    });
                    Log.d(TAG, "Successfully joined existing channel");
                }

                @Override
                public void onError() {
                    Log.e(TAG, "failed to join existing channel");
                }

            });

        } else {
            channelsLocal.createChannel(channelName, Channel.ChannelType.CHANNEL_TYPE_PUBLIC, new Constants.CreateChannelListener() {
                @Override
                public void onCreated(final Channel newChannel) {
                    Log.e(TAG, "Successfully created a channel");
                    if (newChannel != null) {
                        final String sid = newChannel.getSid();
                        Channel.ChannelType type = newChannel.getType();
                        newChannel.setListener(ChatActivity.this);
                        Log.e(TAG, "channel Type is : " + type.toString());
                        newChannel.join(new Constants.StatusListener() {
                            @Override
                            public void onSuccess() {
                                channel.setListener(ChatActivity.this);
                                // Set unique name
                                newChannel.setUniqueName(channelName, new Constants.StatusListener() {
                                    @Override
                                    public void onSuccess() {
                                        Log.d(TAG, "Successfully set new channel unique name");
                                    }

                                    @Override
                                    public void onError() {
                                        Log.e(TAG, "Failed to set new channel unique name");
                                    }
                                });

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Messages messagesObject = newChannel.getMessages();
                                        Message[] messagesArray = messagesObject.getMessages();
                                        if (messagesArray.length > 0) {
                                            messages = new ArrayList<>(Arrays.asList(messagesArray));
                                        }
                                        channel = newChannel;
                                        setupListView();
                                    }
                                });
                                Log.d(TAG, "Successfully joined new channel");
                            }

                            @Override
                            public void onError() {
                                Log.e(TAG, "failed to join new channel");
                            }

                        });

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                            }
                        });
                    }
                }

                @Override
                public void onError() {

                }
            });
        }
    }

    @Override
    public void onLoginError(String errorMessage) {
        ChatActivity.this.progressDialog.dismiss();
        Log.d(TAG, "Error logging in : " + errorMessage);
        Toast.makeText(getBaseContext(), errorMessage, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onLogoutFinished() {

    }

    private void authenticateUser() {
        String idChosen = URLEncoder.encode(Faker.with(this.getApplicationContext()).Name.randomText());
        this.local_author = idChosen;
        this.endpoint_id = Settings.Secure.getString(this.getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        String endpointIdFull = idChosen + "-" + ChatActivity.this.endpoint_id + "-android-" + getApplication().getPackageName();

        StringBuilder url = new StringBuilder();
        url.append(AUTH_SCRIPT);
        url.append("?identity=");
        url.append(URLEncoder.encode(idChosen));
        url.append("&endpointId=" + URLEncoder.encode(endpointIdFull));
        url.append(idChosen);
        url.append("&endpoint_id=" + ChatActivity.this.endpoint_id);
        url.append("&ttl=3600");

        // Replace the tokens below with your own values

        url.append("&account_sid=" + Tokens.AccountSid);
        url.append("&auth_token=" + Tokens.AuthToken);
        url.append("&service_sid=" + Tokens.ServiceSid);
        Log.d(TAG, "url string : " + url.toString());

        try {
            new GetCapabilityTokenAsyncTask().execute(url.toString()).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    private class GetCapabilityTokenAsyncTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);


            new Thread(new Runnable() {
                @Override
                public void run() {
                    ChatActivity.this.chatClient.doLogin(ChatActivity.this);
                }
            }).start();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            ChatActivity.this.progressDialog = ProgressDialog.show(ChatActivity.this, "",
                    "Logging in. Please wait...", true);
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                capabilityToken = HttpHelper.httpGet(params[0]);
                chatClient.setCapabilityToken(capabilityToken);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return capabilityToken;
        }
    }
}
