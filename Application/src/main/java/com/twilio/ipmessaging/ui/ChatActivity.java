package com.twilio.ipmessaging.ui;

import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.example.android.displayingbitmaps.R;
import com.example.android.displayingbitmaps.ui.ImageDetailActivity;
import com.twilio.ipmessaging.util.BasicIPMessagingClient;
import com.twilio.ipmessaging.util.Logger;
import com.twilio.ipmessaging.Channel;
import com.twilio.ipmessaging.ChannelListener;
import com.twilio.ipmessaging.Channels;
import com.twilio.ipmessaging.Constants;
import com.twilio.ipmessaging.Member;
import com.twilio.ipmessaging.Message;
import com.twilio.ipmessaging.Messages;
import com.twilio.ipmessaging.application.TwilioApplication;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import uk.co.ribot.easyadapter.EasyAdapter;


public class ChatActivity extends FragmentActivity implements ChannelListener {

    private static final String TAG = "ChatActivity";
    private BasicIPMessagingClient basicClient;
    private List<Message> messages = new ArrayList<>();
    private EasyAdapter<Message> adapter;

    private ListView lvChat;
    private Button btSend;
    private EditText etMessage;

    private Channel channel;

    private int currentImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

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

        basicClient = TwilioApplication.get().getRtdJni();

        final String channelName = "TestChannel" + String.valueOf(currentImage);
        Channels channelsLocal= basicClient.getIpMessagingClient().getChannels();

        Channel test = channelsLocal.getChannel("CHe2c442dcdf4d47d58d9980ee43a5eb56");


        // Creates a new public channel if one doesn't already exist
        if(channelsLocal.getChannelByUniqueName(channelName) != null) {
            //join it
            final Channel newChannel = channelsLocal.getChannelByUniqueName(channelName);
            newChannel.join(new Constants.StatusListener() {
                @Override
                public void onSuccess() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Messages messagesObject = newChannel.getMessages();
                            Message[] messagesArray = messagesObject.getMessages();
                            if (messagesArray.length > 0) {
                                messages = new ArrayList<>(Arrays.asList(messagesArray));
                            }
                            channel = newChannel;
                            setupListView(channel);
                        }
                    });
                    Log.d(TAG, "Successfully joined existing channel");
                }

                @Override
                public void onError() {
                    Log.e(TAG, "failed to join existing channel");
                }

            });

        }else {
            channelsLocal.createChannel(channelName, Channel.ChannelType.CHANNEL_TYPE_PUBLIC, new Constants.CreateChannelListener() {
                @Override
                public void onCreated(final Channel newChannel) {
                    Log.e(TAG, "Successfully created a channel");
                    if (newChannel != null) {
                        final String sid = newChannel.getSid();
                        Channel.ChannelType type = newChannel.getType();
                        newChannel.setListener(ChatActivity.this);
                        Log.e(TAG, "channel Type is : " + type.toString());
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
                        newChannel.join(new Constants.StatusListener() {
                            @Override
                            public void onSuccess() {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Messages messagesObject = newChannel.getMessages();
                                        Message[] messagesArray = messagesObject.getMessages();
                                        if (messagesArray.length > 0) {
                                            messages = new ArrayList<>(Arrays.asList(messagesArray));
                                        }
                                        channel = newChannel;
                                        setupListView(channel);
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

    private void setupListView(Channel channel){
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

        if(lvChat != null) {
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
        setupListView(this.channel);
    }

    @Override
    public void onMessageChange(Message message) {

    }

    @Override
    public void onMessageDelete(Message message) {

    }

    @Override
    public void onMemberJoin(Member member) {
        setupListView(this.channel);
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
    public void onChannelHistoryLoaded() {
        setupListView(channel);
    }
}
