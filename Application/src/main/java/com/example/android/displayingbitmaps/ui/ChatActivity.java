package com.example.android.displayingbitmaps.ui;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.example.android.displayingbitmaps.R;
import com.example.android.displayingbitmaps.util.BasicIPMessagingClient;
import com.example.android.displayingbitmaps.util.Logger;
import com.twilio.ipmessaging.Channel;
import com.twilio.ipmessaging.ChannelListener;
import com.twilio.ipmessaging.Channels;
import com.twilio.ipmessaging.Constants;
import com.twilio.ipmessaging.Member;
import com.twilio.ipmessaging.Message;

import java.util.Map;


public class ChatActivity extends FragmentActivity implements ChannelListener {

    private static final Logger logger = Logger.getLogger(ChatActivity.class);
    private BasicIPMessagingClient basicClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        basicClient = new BasicIPMessagingClient(getApplicationContext());

        String channelName = "TestChannel";
        Channels channelsLocal= basicClient.getIpMessagingClient().getChannels();
        final Channel channel;

        channelsLocal.createChannel(channelName, Channel.ChannelType.CHANNEL_TYPE_PUBLIC, new Constants.CreateChannelListener() {
            @Override
            public void onCreated(final Channel newChannel) {
                logger.e("Successfully created a channel");
                if (newChannel != null) {
                    final String sid = newChannel.getSid();
                    Channel.ChannelType type = newChannel.getType();
                    newChannel.setListener(ChatActivity.this);
                    logger.e("channel Type is : " + type.toString());
                    newChannel.join(new Constants.StatusListener() {
                        @Override
                        public void onSuccess() {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    //adapter.notifyDataSetChanged();
                                }
                            });
                            logger.d("Successfully joined channel");
                        }

                        @Override
                        public void onError() {
                            logger.e("failed to join channel");
                        }

                    });

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //getChannels(sid);
                        }
                    });
                }
            }

            @Override
            public void onError() {

            }
        });
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

    }

    @Override
    public void onMessageChange(Message message) {

    }

    @Override
    public void onMessageDelete(Message message) {

    }

    @Override
    public void onMemberJoin(Member member) {

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
}
