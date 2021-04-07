package io.flutter.plugins.push.extra

import android.content.Context
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Created by ricardo on 12/12/16.
 */

class FirebaseInstanceIdListenerService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        Log.e("NEW_TOKEN", token);
        PushPlugin.executeOnTokenRefreshCallback()
    }
}