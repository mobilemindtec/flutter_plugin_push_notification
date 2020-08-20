package io.flutter.plugins.push.extra

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log


class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        Log.d(TAG, "##########################")
        Log.d(TAG, "###### NotificationReceiver")
        Log.d(TAG, "##########################")
        val pm = context.packageManager
        val launchIntent = pm.getLaunchIntentForPackage(context.packageName)

        val extras = intent.extras

        Log.d(TAG, "### extras.containsKey(\"APP_NAME\"): " + extras.containsKey("APP_NAME"))

        if (extras.containsKey("APP_NAME")) {
            var bundle = extras.getBundle("NOTE_DATA")!!
            launchIntent.putExtra("NOTE_DATA", bundle)
            launchIntent.putExtra("APP_NAME", extras.getString("APP_NAME"))
            launchIntent.putExtra("NOTE_TITLE", extras.getString("NOTE_TITLE"))
            launchIntent.putExtra("NOTE_MESSAGE", extras.getString("NOTE_MESSAGE"))
        }


        Log.d(TAG, "### starting activity for package: " + context.applicationContext.packageName)
        launchIntent.setPackage(null)
        context.startActivity(launchIntent)

    }

    companion object {
        var TAG = "FLUTTER_NOTE"
    }
}