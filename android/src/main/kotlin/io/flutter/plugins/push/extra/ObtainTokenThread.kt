package io.flutter.plugins.push.extra

import android.content.Context
import android.util.Log
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.messaging.FirebaseMessaging
import java.io.IOException


/**
 * Responsible for obtaining a Token from the GCM service.
 * By design, this must happen in async way in a Thread.
 */
class ObtainTokenThread(private val appContext: Context, private val callbacks: PushPluginListener?) : Thread() {

    private var token: String? = null


    @Override
    override fun run() {
        try {


            FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener {
                task ->

                if(!task.isSuccessful){
                    Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                    return@OnCompleteListener
                }

                val newToken = task.result
                this.token = newToken

                if (callbacks != null) {
                    Log.d(TAG, "Calling listener callback with token: $token")
                    callbacks!!.success(token)
                } else {
                    Log.d(TAG, "Token call returned: $token, but no callback provided.")
                }

                PushPlugin.isActive = true
            })



        } catch (e: IOException) {
            callbacks!!.error("Error while retrieving a token: " + e.message)
            e.printStackTrace()
        }

    }

    companion object {
        private val TAG = "ObtainTokenThread"
    }


}