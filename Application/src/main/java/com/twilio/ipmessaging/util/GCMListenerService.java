package com.twilio.ipmessaging.util;


import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;
import com.twilio.ipmessaging.application.TwilioApplication;

import java.util.HashMap;

public class GCMListenerService extends GcmListenerService {

    public static final String TAG = "GCMListenerService";
    private BasicIPMessagingClient chatClient;

    @Override
    public void onMessageReceived(String from, Bundle data) {
        Log.d(TAG, "onMessageReceived for GCM");
        HashMap<String, String> pushNotification = new HashMap<>();
        for( String key : data.keySet() ){
            pushNotification.put(key, data.getString(key));
        }
        chatClient = TwilioApplication.get().getBasicClient();
        chatClient.getIpMessagingClient().handleNotification(pushNotification);
        notify(data);
    }

    private void notify(Bundle bundle) {

      /*  Intent intent = new Intent(this, MessageActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        if (bundle.containsKey("channel_id")) {
            intent.putExtra("C_SID", bundle.getString("channel_id"));
        }

        String message = "";
        if (bundle.containsKey("channel_id")) {
            message = bundle.getString("text_message");
        }
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_notification).setContentTitle("Twilio Notification").setContentText(message)
                .setAutoCancel(true).setContentIntent(pendingIntent).setColor(Color.rgb(214, 10, 37));


        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0, notificationBuilder.build()); */
    }

}
