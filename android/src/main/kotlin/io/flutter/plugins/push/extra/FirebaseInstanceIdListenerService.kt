package io.flutter.plugins.push.extra

import com.google.firebase.iid.FirebaseInstanceIdService

/**
 * Created by ricardo on 12/12/16.
 */

class FirebaseInstanceIdListenerService : FirebaseInstanceIdService() {

    override fun onTokenRefresh() {
        PushPlugin.executeOnTokenRefreshCallback()
    }

}