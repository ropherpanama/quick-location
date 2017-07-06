package com.codebase.quicklocation.fcm;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.codebase.quicklocation.R;
import com.codebase.quicklocation.WelcomeActivity;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by fgcanga on 6/27/17.
 * recibe los mensajes de fcm.
 */

public class FCMListenerService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        String title = "";
        String message = "";
        String messageid = "";
        title = remoteMessage.getData().get("title");
        message  = remoteMessage.getData().get("body");
        Log.e("FCMListenerService", "else getTitle: " + title);
        Log.e("FCMListenerService", "else getBody: " + message);
        messageid = System.currentTimeMillis()+"-"+remoteMessage.getFrom();
        sendNotification(title, message,messageid);
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     */
    private void sendNotification(String title, String message, String messageid) {
        Intent intent = new Intent(this, WelcomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        //  intent.putExtra("title", title);
        //intent.putExtra("title",listMsg.get(0).title);
        //intent.putExtra("body",listMsg.get(0).msnGCM);
        //intent.putExtra("body", message);
        // intent.putExtra("fromInt",fromInt);
        // Sets an ID for the notification, so it can be updated
        int notifyID = 0;
        long[] vibraPattern = {0, 500, 250, 500};
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(WelcomeActivity.class);
        stackBuilder.addNextIntent(intent);
        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_ONE_SHOT);
        Uri defaultSoundUri = soundMetodo("");
        NotificationCompat.Builder notificationBuilder = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            notificationBuilder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.notifications_icon)
                    .setGroup(title)
                    .setGroupSummary(true)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setVibrate(vibraPattern)
                    .setAutoCancel(true)
                    .setColor(0xFFDA1919)
                    .setSound(defaultSoundUri)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                    .setVisibility(Notification.VISIBILITY_PUBLIC)
                    .setContentIntent(pendingIntent);
        } else {
            notificationBuilder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.notifications_icon)
                    .setContentTitle(title)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                    .setContentTitle(title)
                    .setContentText(message)
                    .setVibrate(vibraPattern)
                    .setAutoCancel(true)
                    .setColor(0xFFDA1919)
                    .setSound(defaultSoundUri)
                    .setContentIntent(pendingIntent);
        }

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(notifyID, notificationBuilder.build());
    }


    private Uri soundMetodo(String sound_) {
        int soundID = getResources().getIdentifier(sound_, "raw", getPackageName());
        Log.i("sound URI", soundID + "");
        Uri sound;
        try {
            sound = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + this.getResources().getResourcePackageName(soundID) + '/' + this.getResources().getResourceTypeName(soundID) + '/' + this.getResources().getResourceEntryName(soundID));
        } catch (Resources.NotFoundException ex) {
            Log.e("error sound", ex + "");
            sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        }

        return sound;

    }
}
