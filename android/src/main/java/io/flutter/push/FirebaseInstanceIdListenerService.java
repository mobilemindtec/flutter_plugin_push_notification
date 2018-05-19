package io.flutter.push;

import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by ricardo on 12/12/16.
 */

public class FirebaseInstanceIdListenerService extends FirebaseInstanceIdService {
    @Override
    public void onTokenRefresh() {
        PushPlugin.executeOnTokenRefreshCallback();
    }

}