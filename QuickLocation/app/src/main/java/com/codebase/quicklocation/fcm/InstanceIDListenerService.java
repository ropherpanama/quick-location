package com.codebase.quicklocation.fcm;

import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by AUrriola on 6/27/17.
 */

public class InstanceIDListenerService extends FirebaseInstanceIdService {
    /**
     * actualiza el token de fcm.
     */
    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
    }

    /**
     * Envio del token a la base de datos de firebase.
     * @param token
     */
    private void sendRegistrationToServer(String token) {

    }

    }
