package io.flutter.plugins.push.extra

import android.content.Context
import android.util.Log
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.InstanceIdResult
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


            FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener(OnSuccessListener<InstanceIdResult> { instanceIdResult ->
                val newToken = instanceIdResult.token

                Log.e(TAG, newToken)

                this.token = newToken

                if (callbacks != null) {
                    Log.d(TAG, "Calling listener callback with token: " + token)
                    callbacks!!.success(token)
                } else {
                    Log.d(TAG, "Token call returned, but no callback provided.")
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