package com.codebase.quicklocation.fcm;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by AUrriola on 6/27/17.
 * recibe los mensajes de fcm.
 */

public class FCMListenerService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
    }

    /**
     * Muestra la vista de notificación.
     * @param title titulo
     * @param message mensaje
     * @param messageid identificador.
     */
    public void setmNotification(String title, String message, String messageid) {

    }
}
