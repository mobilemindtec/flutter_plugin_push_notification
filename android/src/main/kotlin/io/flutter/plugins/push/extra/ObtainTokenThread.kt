package io.flutter.plugins.push.extra

import android.content.Context
import android.util.Log
import com.google.firebase.iid.FirebaseInstanceId
import java.io.IOException

/**
 * Responsible for obtaining a Token from the GCM service.
 * By design, this must happen in async way in a Thread.
 */
class ObtainTokenThread(String, private val appContext: Context, private val callbacks: PushPluginListener?) : Thread() {

    private var token: String? = null

    private// TODO: Wrap the whole callback.
    val tokenFrom: String?
        @Throws(IOException::class)
        get() {

            Log.d(TAG, "getTokenFromFCM.")

            val instanceId = FirebaseInstanceId.getInstance()
            this.token = instanceId.getToken()


            Log.d(TAG, "" + this.token)

            if (callbacks != null) {
                Log.d(TAG, "Calling listener callback with token: " + token)
                callbacks!!.success(token)
            } else {
                Log.d(TAG, "Token call returned, but no callback provided.")
            }
            PushPlugin.isActive = true
            return this.token
        }

    @Override
    override fun run() {
        try {
            token = tokenFrom
        } catch (e: IOException) {
            callbacks!!.error("Error while retrieving a token: " + e.message)
            e.printStackTrace()
        }

    }

    companion object {
        private val TAG = "ObtainTokenThread"
    }


}