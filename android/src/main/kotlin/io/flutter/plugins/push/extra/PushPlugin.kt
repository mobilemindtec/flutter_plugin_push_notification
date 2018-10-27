package io.flutter.plugins.push.extra

import android.content.Context
import android.util.Log

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

import org.json.JSONException

/**
 * Push plugin extends the GCM Listener Service and has to be registered in the AndroidManifest
 * in order to receive Notification Messages.
 */
class PushPlugin : FirebaseMessagingService() {

    /**
     * Handles the push messages receive event.
     */

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "New Push Message From: " + remoteMessage.from)
        // If the application has the callback registered and it must be active
        // execute the callback. Otherwise, create new notification in the notification bar of the user.
        if (_onMessageReceivedCallback != null && isActive) {
            Log.d(TAG, "execute message callback")
            executeOnMessageReceivedCallback(remoteMessage)
        } else {
            Log.d(TAG, "not message callback, execute notification builder")
            val context = applicationContext
            NotificationBuilder.createNotification(context, remoteMessage)
        }
    }

    companion object {
        internal val TAG = "FcmPushPlugin"

        var isActive = false
        private var cachedData: RemoteMessage? = null
        private var _onMessageReceivedCallback: PushPluginListener? = null
        private var _onTokenRefreshCallback: PushPluginListener? = null

        /**
         * Register the application in GCM
         *
         * @param appContext
         * @param projectId
         * @param callbacks
         */
        fun register(appContext: Context, callbacks: PushPluginListener?) {
            if (callbacks == null) {
                Log.d(TAG, "Registering without providing a callback!")
            }

            try {
                val t = ObtainTokenThread(appContext, callbacks)
                t.start()
            } catch (ex: Exception) {
                callbacks!!.error("Thread failed to start: " + ex.message)
            }

        }

        /**
         * Unregister the application from GCM
         *
         * @param appContext
         * @param projectId
         * @param callbacks
         */
        fun unregister(appContext: Context, callbacks: PushPluginListener?) {
            if (callbacks == null) {
                Log.d(TAG, "Unregister without providing a callback!")
            }
            try {
                val t = UnregisterTokenThread(appContext, callbacks)
                t.start()
            } catch (ex: Exception) {
                callbacks!!.error("Thread failed to start: " + ex.message)
            }

        }

        /**
         * Set the on message received callback
         *
         * @param callbacks
         */
        fun setOnMessageReceivedCallback(callbacks: PushPluginListener) {
            _onMessageReceivedCallback = callbacks

            if (cachedData != null) {
                executeOnMessageReceivedCallback(cachedData!!)
                cachedData = null
            }
        }

        /**
         * Execute the _onMessageReceivedCallback with the data passed.
         * In case the callback is not present, cache the data;
         *
         * @param remoteMessage
         */
        fun executeOnMessageReceivedCallback(remoteMessage: RemoteMessage) {
            if (_onMessageReceivedCallback != null) {

                Log.d(TAG, "From: " + remoteMessage.getFrom())
                var message: String? = ""
                var title: String? = ""
                // Check if message contains a data payload.
                if (remoteMessage.data.isNotEmpty()) {
                    Log.d(TAG, "Message data payload: " + remoteMessage.data)
                }

                // Check if message contains a notification payload.
                if (remoteMessage.getNotification() != null) {
                    Log.d(TAG, "Message Notification Body: " + remoteMessage.notification!!.body)
                    Log.d(TAG, "Message Notification Title: " + remoteMessage.notification!!.title)
                    message = remoteMessage.notification!!.body
                    title = remoteMessage.notification!!.title
                }
                /*
                val dataAsJson = mutableMapOf<String, String>()
                val keys = remoteMessage.data.keys
                for (key in keys) {
                    try {
                        dataAsJson.put(key, JsonObjectExtended.wrap(remoteMessage.data[key]))
                    } catch (e: JSONException) {
                        Log.d(TAG, "Error thrown while parsing push notification data bundle to json: " + e.message)
                        //Handle exception here
                    }

                }
                */
                _onMessageReceivedCallback!!.success(message, title,  remoteMessage.data)
            } else {
                Log.d(TAG, "No callback function - caching the data for later retrieval.")
                cachedData = remoteMessage
            }
        }

        /**
         * Set the on token refresh callback
         *
         * @param callbacks
         */
        fun setOnTokenRefreshCallback(callbacks: PushPluginListener) {
            _onTokenRefreshCallback = callbacks
        }


        /**
         * Execute the onTokeRefreshCallback.
         */
        fun executeOnTokenRefreshCallback() {
            if (_onTokenRefreshCallback != null) {
                Log.d(TAG, "Executing token refresh callback.")
                _onTokenRefreshCallback!!.success(null)
            } else {
                Log.d(TAG, "No token refresh callback")
            }
        }

        /**
         * This method always returns true. It is here only for legacy purposes.
         */
        fun areNotificationsEnabled(): Boolean {
            return true
        }
    }


}



