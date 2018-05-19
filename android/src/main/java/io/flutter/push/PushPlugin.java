package io.flutter.push;

import android.content.Context;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import io.flutter.push.JsonObjectExtended;
import io.flutter.push.ObtainTokenThread;
import io.flutter.push.PushPluginListener;
import io.flutter.push.UnregisterTokenThread;

import org.json.JSONException;

import java.util.Set;

/**
 * Push plugin extends the GCM Listener Service and has to be registered in the AndroidManifest
 * in order to receive Notification Messages.
 */
public class PushPlugin extends FirebaseMessagingService {
    static final String TAG = "FcmPushPlugin";

    public static boolean isActive = false;
    private static RemoteMessage cachedData;
    private static PushPluginListener onMessageReceivedCallback;
    private static PushPluginListener onTokenRefreshCallback;

    /**
     * Register the application in GCM
     *
     * @param appContext
     * @param projectId
     * @param callbacks
     */
    public static void register(Context appContext, String projectId, PushPluginListener callbacks) {
        if (callbacks == null) {
            Log.d(TAG, "Registering without providing a callback!");
        }

        try {
            ObtainTokenThread t = new ObtainTokenThread(projectId, appContext, callbacks);
            t.start();
        } catch (Exception ex) {
            callbacks.error("Thread failed to start: " + ex.getMessage());
        }
    }

    /**
     * Unregister the application from GCM
     *
     * @param appContext
     * @param projectId
     * @param callbacks
     */
    public static void unregister(Context appContext, String projectId, PushPluginListener callbacks) {
        if (callbacks == null) {
            Log.d(TAG, "Unregister without providing a callback!");
        }
        try {
            UnregisterTokenThread t = new UnregisterTokenThread(projectId, appContext, callbacks);
            t.start();
        } catch (Exception ex) {
            callbacks.error("Thread failed to start: " + ex.getMessage());
        }
    }

    /**
     * Set the on message received callback
     *
     * @param callbacks
     */
    public static void setOnMessageReceivedCallback(PushPluginListener callbacks) {
        onMessageReceivedCallback = callbacks;

        if (cachedData != null) {
            executeOnMessageReceivedCallback(cachedData);
            cachedData = null;
        }
    }

    /**
     * Execute the onMessageReceivedCallback with the data passed.
     * In case the callback is not present, cache the data;
     *
     * @param remoteMessage
     */
    public static void executeOnMessageReceivedCallback(RemoteMessage remoteMessage) {
        if (onMessageReceivedCallback != null) {

            Log.d(TAG, "From: " + remoteMessage.getFrom());
            String message = "";
            String title = "";
            // Check if message contains a data payload.
            if (remoteMessage.getData().size() > 0) {
                Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            }

            // Check if message contains a notification payload.
            if (remoteMessage.getNotification() != null) {
                Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
                Log.d(TAG, "Message Notification Title: " + remoteMessage.getNotification().getBody());
                message = remoteMessage.getNotification().getBody();
                title = remoteMessage.getNotification().getTitle();

            }

            JsonObjectExtended dataAsJson = new JsonObjectExtended();
            Set<String> keys = remoteMessage.getData().keySet();
            for (String key : keys) {
                try {
                    dataAsJson.put(key, JsonObjectExtended.wrap(remoteMessage.getData().get(key)));
                } catch(JSONException e) {
                    Log.d(TAG, "Error thrown while parsing push notification data bundle to json: " + e.getMessage());
                    //Handle exception here
                }
            }
            onMessageReceivedCallback.success(message, dataAsJson.toString());
        } else {
            Log.d(TAG, "No callback function - caching the data for later retrieval.");
            cachedData = remoteMessage;
        }
    }

    /**
     * Handles the push messages receive event.
     */
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "New Push Message From: " + remoteMessage.getFrom());
        // If the application has the callback registered and it must be active
        // execute the callback. Otherwise, create new notification in the notification bar of the user.
        if (onMessageReceivedCallback != null && isActive) {
            Log.d(TAG, "execute message callback");
            executeOnMessageReceivedCallback(remoteMessage);
        } else {
            Log.d(TAG, "not message callback, execute notification builder");
            Context context = getApplicationContext();
            NotificationBuilder.createNotification(context, remoteMessage);
        }
    }

    /**
     * Set the on token refresh callback
     *
     * @param callbacks
     */
    public static void setOnTokenRefreshCallback(PushPluginListener callbacks) {
        onTokenRefreshCallback = callbacks;
    }


    /**
     * Execute the onTokeRefreshCallback.
     */
    public static void executeOnTokenRefreshCallback() {
        if (onTokenRefreshCallback != null) {
            Log.d(TAG, "Executing token refresh callback.");
            onTokenRefreshCallback.success(null);
        } else {
            Log.d(TAG, "No token refresh callback");
        }
    }

    /**
     * This method always returns true. It is here only for legacy purposes.
     */
    public static Boolean areNotificationsEnabled() {
        return true;
    }




}



