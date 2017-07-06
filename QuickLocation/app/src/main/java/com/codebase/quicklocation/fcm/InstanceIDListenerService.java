package com.codebase.quicklocation.fcm;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by fgcanga on 6/27/17.
 */

public class InstanceIDListenerService extends FirebaseInstanceIdService {
    public static final String PROPERTY_REG_ID = "registration_id";

    public static  final String TAG = "IDListenerService";
    /**
     * actualiza el token de fcm.
     */
    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        // Get updated InstanceID token.
        String refreshedToken = "";
        refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d("IDListenerService", "Refreshed token: " + refreshedToken);
        sendRegistrationToServer(refreshedToken);
    }


    private void sendRegistrationToServer(String token) {
        // Add custom implementation, as needed.
        final SharedPreferences prefs = this.getSharedPreferences("FCMID", Context.MODE_PRIVATE);  //PreferenceManager.getDefaultSharedPreferences(context);
        // int appVersion = getAppVersion(this);
        // Log.i(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, token);
        //editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.apply();
        Log.e(TAG, "save token fcm");
        ///PresenterTokenFcm tokenFcm = new PresenterTokenFcm();
        // tokenFcm.changeFCMId(token);
        //  SaveFcmInterfaceService.changeFCMId(token);
    }

    private static int getAppVersion(Context context) {
        Log.e(TAG, "getAppVersion");

        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

}
