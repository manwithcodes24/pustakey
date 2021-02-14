package com.yashbuysell.psbuyandsell.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.RemoteMessage;
import com.yashbuysell.psbuyandsell.MainActivity;
import com.yashbuysell.psbuyandsell.R;
import com.yashbuysell.psbuyandsell.ui.chat.chat.ChatActivity;


/**
 * Created by Panacea-Soft on 8/11/16.
 * Contact Email : teamps.is.cool@gmail.com
 */
public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {
    public final String channelId = "PustakeyChannelId1";
    public  String flag,msg, itemId, buyerUserId, sellerUserId, senderName, senderImagePath;
    public int currentNotiIdPosition , notiId;
    public String userId ,sendUserId;


    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);

    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        /* Important : Please don't change this "message" because if you change this, need to update at PHP.  */

        flag = remoteMessage.getData().get("flag");
        msg = remoteMessage.getData().get("message");
        itemId = remoteMessage.getData().get("item_id");
        buyerUserId = remoteMessage.getData().get("buyer_id");
        sellerUserId = remoteMessage.getData().get("seller_id");
        senderName = remoteMessage.getData().get("sender_name");
        senderImagePath = remoteMessage.getData().get("sender_profle_photo");

        checkUserId();

        if(msg == null || msg.equals("")) {
            if(remoteMessage.getNotification() != null) {
                msg = remoteMessage.getNotification().getBody();
            }
        }

        showNotification(msg ,flag);

    }

    private void checkUserId() {

        if(buyerUserId != null && sellerUserId != null) {
            int result = buyerUserId.compareTo(sellerUserId);
            if (result < 0) {//buyerUserId is less than sellerUserId
                sendUserId = sellerUserId + buyerUserId;
            } else {
                sendUserId = buyerUserId + sellerUserId;
            }
        }

    }


    private void showNotification(String message,String flag) {

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean( Constants.NOTI_EXISTS_TO_SHOW, true);
        editor.apply();

        Intent i = new Intent(getApplicationContext(), MainActivity.class);


        i.putExtra(Constants.NOTI_MSG, message);
        String displayMessage;
        Utils.psLog("Message From Server : " + message);

        if (message == null || message.equals("")) {
            i.putExtra(Constants.NOTI_EXISTS_TO_SHOW, false);
            displayMessage = "Welcome from Pustakey";
        } else {
            i.putExtra(Constants.NOTI_EXISTS_TO_SHOW, true);
            displayMessage = message; //"You've received new message.";
        }

        i.putExtra(Constants.SELECTED_LOCATION_ID ,pref.getString(Constants.SELECTED_LOCATION_ID,Constants.EMPTY_STRING));
        i.putExtra(Constants.SELECTED_LOCATION_NAME ,pref.getString(Constants.SELECTED_LOCATION_NAME,Constants.EMPTY_STRING));
        i.putExtra(Constants.LAT ,pref.getString(Constants.LAT,Constants.EMPTY_STRING));
        i.putExtra(Constants.LNG ,pref.getString(Constants.LNG,Constants.EMPTY_STRING));

        i.putExtra(Constants.NOTI_MSG ,msg);
        i.putExtra(Constants.NOTI_FLAG ,flag);
        i.putExtra(Constants.NOTI_ITEM_ID ,itemId);
        i.putExtra(Constants.NOTI_BUYER_ID ,buyerUserId);
        i.putExtra(Constants.NOTI_SELLER_ID ,sellerUserId);
        i.putExtra(Constants.NOTI_SENDER_NAME ,senderName);
        i.putExtra(Constants.NOTI_SENDER_URL ,senderImagePath);


        //i.addFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP  | Intent.FLAG_ACTIVITY_SINGLE_TOP );
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, i, PendingIntent.FLAG_UPDATE_CURRENT);

//        if( !itemId.isEmpty() ) {
//
//            Intent productIntent = new Intent(this, ItemActivity.class);
//            productIntent.putExtra(Constants.ITEM_ID, itemId);
//            productIntent.putExtra(Constants.HISTORY_FLAG, Constants.ZERO);
//            productIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
//                    Intent.FLAG_ACTIVITY_SINGLE_TOP);
//            PendingIntent intent = PendingIntent.getBroadcast(this, 0, productIntent, 0);
//
//            NotificationCompat.Builder builder2 = new NotificationCompat.Builder(this, Constants.ZERO)
//                    .setAutoCancel(true)
//                    .setContentTitle(getString(R.string.notification__alert))
//                    .setContentText(message)////
//                    .setSmallIcon(R.drawable.ic_notifications_white)
//                    .setContentIntent(intent);
//
//            NotificationManager manager2 = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//
//            if (manager2 != null) {
//                manager2.notify(0, builder2.build());
//            }
//        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), Constants.ZERO)
                .setAutoCancel(true)
                .setContentTitle(getString(R.string.notification__alert))
                .setContentText(displayMessage)
                .setSmallIcon(R.drawable.ic_notifications_white)
                .setContentIntent(pendingIntent)
                ;


        // Set Vibrate, Sound and Light
        int defaults = 0;
        defaults = defaults | Notification.DEFAULT_LIGHTS;
        defaults = defaults | Notification.DEFAULT_VIBRATE;
        defaults = defaults | Notification.DEFAULT_SOUND;

        builder.setDefaults(defaults);
        // Set the content for Notification
        builder.setContentText(displayMessage);
        // Set autocancel
        builder.setAutoCancel(true);

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            String channelId = this.channelId;
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    getString(R.string.notification__alert),
                    NotificationManager.IMPORTANCE_DEFAULT);

            AudioAttributes audioAttrib = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build();
            channel.setSound(Settings.System.DEFAULT_RINGTONE_URI, audioAttrib);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

            if(manager != null) {
                manager.createNotificationChannel(channel);
            }

            builder.setChannelId(channelId);
        }

        if (manager != null) {

            if( sendUserId != null) {
                currentNotiIdPosition = pref.getInt(Constants.C_NOTI_ID, 0);
                notiId = pref.getInt(sendUserId, 0);

                if (notiId == 0) {
                    currentNotiIdPosition++;
                    notiId = currentNotiIdPosition;
                    pref.edit().putInt(sendUserId, notiId).apply();
                    pref.edit().putInt(Constants.C_NOTI_ID, currentNotiIdPosition).apply();
                }

                manager.notify(notiId, builder.build());
            }else {
                manager.notify(0, builder.build());
            }

        }

//        My included notification
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            CharSequence name = "Message";
//            String description = "You have got new message";
//            int importance = NotificationManager.IMPORTANCE_HIGH;
//            NotificationChannel channel = new NotificationChannel(CHANNEL_ID1, name, importance);
//            channel.setDescription(description);
//            AudioAttributes audioAttrib = new AudioAttributes.Builder()
//                    .setUsage(AudioAttributes.USAGE_MEDIA)
//                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
//                    .build();
//            channel.setSound(Settings.System.DEFAULT_RINGTONE_URI, audioAttrib);
//            // Register the channel with the system; you can't change the importance
//            // or other notification behaviors after this
//            NotificationManager notificationManager = getApplicationContext().getSystemService(NotificationManager.class);
//
//            notificationManager.createNotificationChannel(channel);
//        }
//
//        Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
//        PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, 0);
//        NotificationCompat.Builder buyernotification = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID1)
//                .setSmallIcon(R.drawable.app_icon)
//                .setContentTitle(getApplicationContext().getString(R.string.chatNotificationTitle))
//                .setContentText(getApplicationContext().getString(R.string.chatNotificationBuyerDis))
//                .setContentIntent(pi)
//                .setAutoCancel(true);
//        ;
//        Log.d("chat" , "Chat notification sent") ;
//        buyernotification.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
//
//        NotificationManager notificationManager1 = (NotificationManager) getApplicationContext().getSystemService(Service.NOTIFICATION_SERVICE);
//        notificationManager1.notify(1, buyernotification.build());


    }


}
