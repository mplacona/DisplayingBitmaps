package com.twilio.ipmessaging.util;

import android.content.Context;
import android.util.Log;

import com.twilio.ipmessaging.Channel;
import com.twilio.ipmessaging.Constants.InitListener;
import com.twilio.ipmessaging.IPMessagingClientListener;
import com.twilio.ipmessaging.TwilioIPMessagingClient;
import com.twilio.ipmessaging.TwilioIPMessagingSDK;

import java.util.Arrays;
import java.util.List;

public class BasicIPMessagingClient implements IPMessagingClientListener {
    private static final String TAG = "BasicIPMessagingClient";
    private String capabilityToken;
    private long nativeClientParam;
    private TwilioIPMessagingClient ipMessagingClient;
    private Channel[] channels;
    private Context context;

    public BasicIPMessagingClient(Context context) {
        super();
        this.context = context;
    }

    public String getCapabilityToken() {
        return capabilityToken;
    }

    public void setCapabilityToken(String capabilityToken) {
        this.capabilityToken = capabilityToken;
    }


    public void doLogin(final ILoginListener listener) {
        if(!TwilioIPMessagingSDK.isInitialized()) {
            TwilioIPMessagingSDK.initializeSDK(this.context, new InitListener() {
                @Override
                public void onInitialized() {
                    createClientWithToken(listener);
                }

                @Override
                public void onError(Exception error) {
                    Log.d(TAG, error.getMessage());
                }
            });
        } else {
            createClientWithToken(listener);
        }
    }

    public BasicIPMessagingClient() {
        super();
    }

    public void cleanupTest() {
        // TODO
    }

    public List<Channel> getChannelList() {
        List<Channel> list = Arrays.asList(this.channels);
        return list;
    }

    public long getNativeClientParam() {
        return nativeClientParam;
    }

    public void setNativeClientParam(long nativeClientParam) {
        this.nativeClientParam = nativeClientParam;
    }

    @Override
    public void onChannelAdd(Channel channel) {
        if(channel != null) {
            Log.d(TAG, "A Channel :"+ channel.getFriendlyName() + " got added");
        } else {
            Log.d(TAG, "Received onChannelAdd event.");
        }
    }

    @Override
    public void onChannelChange(Channel channel) {
        if(channel != null) {
            Log.d(TAG, "Channel Name : "+ channel.getFriendlyName() + " got Changed");
        } else {
            Log.d(TAG, "received onChannelChange event.");
        }
    }

    @Override
    public void onChannelDelete(Channel channel) {
        if(channel != null) {
            Log.d(TAG, "A Channel :"+ channel.getFriendlyName() + " got deleted");
        } else {
            Log.d(TAG, "received onChannelDelete event.");
        }
    }

    @Override
    public void onError(int errorCode, String errorText) {
        Log.d(TAG, "Received onError event.");
    }

    @Override
    public void onAttributesChange(String attributes) {
        Log.d(TAG, "Received onAttributesChange event.");
    }

    @Override
    public void onChannelHistoryLoaded(Channel channel) {
        Log.d(TAG, "Received onChannelHistoryLoaded callback " + channel.getFriendlyName());
    }

    public TwilioIPMessagingClient getIpMessagingClient() {
        return ipMessagingClient;
    }

    private void createClientWithToken(ILoginListener listener) {
        ipMessagingClient = TwilioIPMessagingSDK.createIPMessagingClientWithToken(capabilityToken, BasicIPMessagingClient.this);
        if(ipMessagingClient != null) {
            if(listener != null) {
                listener.onLoginFinished();
            }
        } else {
            listener.onLoginError("ipMessagingClient is null");
        }
    }
}
