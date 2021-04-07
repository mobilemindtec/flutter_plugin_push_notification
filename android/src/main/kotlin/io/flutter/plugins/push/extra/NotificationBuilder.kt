package io.flutter.plugins.push.extra

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.RemoteMessage
import java.io.PrintWriter
import java.io.StringWriter
import java.util.*


/**
 * Notification Builder is responsible for creating notifications from the application.
 * It uses the Notification Manager service to create and publish a new notification.
 */
object NotificationBuilder {
    private val TAG = "NotificationBuilder"

    fun createNotification(context: Context?, remoteMessage: RemoteMessage) {
        var notId = 0


        try {
            notId = Integer.parseInt(remoteMessage.messageId!!)
        } catch (e: NumberFormatException) {
            Log.e(TAG, "Number format exception - Error parsing Notification ID: " + e.message)
        } catch (e: Exception) {
            Log.e(TAG, "Number format exception - Error parsing Notification ID" + e.message)
        }

        if (notId == 0) {
            // no notId passed, so assume we want to show all notifications, so make it a random number
            notId = Random().nextInt(100000)
            Log.d(TAG, "Generated random notId: $notId")
        } else {
            Log.d(TAG, "Received notId: $notId")
        }


        if (context == null) {
            Log.d(TAG, "Context is null!")
        }

        try {

            val mNotificationManager = context!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val appName = getAppName(context!!)

            var defaults = Notification.DEFAULT_ALL
            val data = remoteMessage.data
            val notification = remoteMessage.notification!!

            val notificationIntent = Intent(context, PushHandlerActivity::class.java)
            notificationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)

            var actionNameId = context.resources.getIdentifier("flutter_note_receiver_action_name", "strings", context.packageName)

            Log.d(TAG, "actionNameId = $actionNameId")

            if(actionNameId > -1) {
                notificationIntent.action = context.resources.getString(actionNameId)
            }else{
                notificationIntent.action = "FLUTTER_NOTE_ACTION_RECEIVER"
            }

            notificationIntent.putExtra("APP_NAME", appName)
            notificationIntent.putExtra("NOTE_TITLE", notification.title)
            notificationIntent.putExtra("NOTE_MESSAGE", notification.body)

            var bundle = Bundle()
            for(key in data.keys)
                bundle.putString(key, data[key])

            notificationIntent.putExtra("NOTE_DATA", bundle)

            val contentIntent = PendingIntent.getActivity(context, notId, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT)

            if (data.containsKey("defaults") && data.get("defaults") != null) {
                try {
                    defaults = Integer.parseInt(data.get("defaults").toString())
                } catch (ignore: NumberFormatException) {
                }
            }

            val mBuilder = NotificationCompat.Builder(context)
                    .setDefaults(defaults)
                    .setSmallIcon(getSmallIcon(context, remoteMessage))
                    .setWhen(System.currentTimeMillis())
                    .setContentTitle(notification.title)
                    .setTicker(notification.title)
                    .setContentIntent(contentIntent)
                    .setColor(getColor(remoteMessage, context))
                    .setAutoCancel(true)

            val message = notification.getBody()
            if (message != null) {
                if (message!!.length > 30) {
                    mBuilder.setContentText(message!!.substring(0, 30) + "..")
                            .setStyle(NotificationCompat.BigTextStyle()
                                    .bigText(message))
                } else {
                    mBuilder.setContentText(message)
                }
            } else {
                mBuilder.setContentText("<missing message content>")
            }


            val soundName = notification.sound
            if (soundName != null) {
                val r = context!!.resources
                val resourceId = r.getIdentifier(soundName, "raw", context!!.packageName)
                val soundUri = Uri.parse("android.resource://" + context!!.packageName + "/" + resourceId)
                mBuilder.setSound(soundUri)
                defaults = defaults and Notification.DEFAULT_SOUND.inv()
                mBuilder.setDefaults(defaults)
            }

            val notify = mBuilder.build()
            val largeIcon = getLargeIcon(context, remoteMessage)
            if (largeIcon > -1) {
                notify.contentView.setImageViewResource(android.R.id.icon, largeIcon)
            }

            mNotificationManager.notify(appName, notId, notify)
        } catch (e: Exception) {
            val stackTraceWriter = StringWriter()
            e.printStackTrace(PrintWriter(stackTraceWriter))

            Log.d(TAG, "Exception has been raised: " + e.message + " and stack trace: " + stackTraceWriter.toString())
        }

    }

    private fun getAppName(context: Context): String {

        return context
                .packageManager
                .getApplicationLabel(context.applicationInfo).toString()
    }

    private fun getColor(remoteMessage: RemoteMessage, context: Context): Int {

        var theColor = Color.argb(1, 218, 223, 225) // default, transparent
        val passedColor = remoteMessage.notification!!.color // something like "#FFFF0000", or "red"
        if (passedColor != null) {
            try {
                theColor = Color.parseColor(passedColor)
            } catch (ignore: IllegalArgumentException) {
            }

        }

        try {

            val defaultColor = context.resources.getIdentifier("ic_stat_notify_color", "color", context.packageName)
            Log.d(TAG, "use defaultColor  $defaultColor")
            if (defaultColor > -1)
                theColor = context.resources.getColor(defaultColor)
        } catch (e: Exception) {
            val stackTraceWriter = StringWriter()
            e.printStackTrace(PrintWriter(stackTraceWriter))
            Log.d(TAG, "Exception has been raised: " + e.message + " and stack trace: " + stackTraceWriter.toString())
        }

        return theColor
    }

    private fun getSmallIcon(context: Context, remoteMessage: RemoteMessage): Int {

        var icon = -1

        // first try an iconname possible passed in the server payload
        val iconNameFromServer = remoteMessage.notification!!.icon
        if (iconNameFromServer != null) {
            icon = getIconValue(context.packageName, iconNameFromServer)
        }

        // try a custom included icon in our bundle named ic_stat_notify(.png)
        if (icon == -1) {
            icon = getIconValue(context.packageName, "ic_stat_notify")
        }

        // fall back to the regular app icon
        if (icon == -1) {
            icon = context.applicationInfo.icon
        }

        return icon
    }

    private fun getLargeIcon(context: Context, remoteMessage: RemoteMessage): Int {

        var icon = -1

        // first try an iconname possible passed in the server payload
        val iconNameFromServer = remoteMessage.data.get("largeIcon")
        if (remoteMessage.data.containsKey("largeIcon")) {
            if (iconNameFromServer != null) {
                icon = getIconValue(context.packageName, iconNameFromServer)
            }
        }

        // try a custom included icon in our bundle named ic_stat_notify(.png)
        if (icon == -1) {
            icon = getIconValue(context.packageName, "ic_notify")
        }

        // fall back to the regular app icon
        //if (icon == -1) {
        //    icon = context.getApplicationInfo().icon;
        //}

        return icon
    }

    private fun getIconValue(className: String, iconName: String): Int {
        try {
            val clazz = Class.forName("$className.R\$drawable")
            return (clazz.getDeclaredField(iconName).get(Integer::class.java) as Integer).toInt()
        } catch (ignore: Exception) {
        }

        return -1
    }

}
