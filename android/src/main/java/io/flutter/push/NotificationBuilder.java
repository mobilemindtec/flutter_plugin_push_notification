package io.flutter.push;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.RemoteMessage;
import io.flutter.push.PushHandlerActivity;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import java.util.Random;

/**
 * Notification Builder is responsible for creating notifications from the application.
 * It uses the Notification Manager service to create and publish a new notification.
 */
public class NotificationBuilder {
    private static final String TAG = "NotificationBuilder";


    public static void createNotification(Context context, RemoteMessage remoteMessage) {
        int notId = 0;

        try {
            notId = Integer.parseInt(remoteMessage.getMessageId());
        } catch (NumberFormatException e) {
            Log.e(TAG, "Number format exception - Error parsing Notification ID: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Number format exception - Error parsing Notification ID" + e.getMessage());
        }
        if (notId == 0) {
            // no notId passed, so assume we want to show all notifications, so make it a random number
            notId = new Random().nextInt(100000);
            Log.d(TAG, "Generated random notId: " + notId);
        } else {
            Log.d(TAG, "Received notId: " + notId);
        }


        if (context == null) {
            Log.d(TAG, "Context is null!");
        }

        try {

            NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            String appName = getAppName(context);

            Intent notificationIntent = new Intent(context, PushHandlerActivity.class);
            notificationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            notificationIntent.putExtra("pushBundle", remoteMessage);

            PendingIntent contentIntent = PendingIntent.getActivity(context, notId, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

            int defaults = Notification.DEFAULT_ALL;
            Map<String, String> data = remoteMessage.getData();
            com.google.firebase.messaging.RemoteMessage.Notification notification = remoteMessage.getNotification();


            if (data.containsKey("defaults") && data.get("defaults") != null) {
                try {
                    defaults = Integer.parseInt(data.get("defaults"));
                } catch (NumberFormatException ignore) {
                }
            }

            Log.d(TAG, "notification title = " + notification.getTitle());
            Log.d(TAG, "notification body = " + notification.getBody());

            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(context)
                            .setDefaults(defaults)
                            .setSmallIcon(getSmallIcon(context, remoteMessage))
                            .setWhen(System.currentTimeMillis())
                            .setContentTitle(notification.getTitle())
                            .setTicker(notification.getTitle())
                            .setContentIntent(contentIntent)
                            .setColor(getColor(remoteMessage, context))
                            .setAutoCancel(true);



            String message = notification.getBody();
            if (message != null) {
                if(message.length() > 30){
                    mBuilder.setContentText(message.substring(0, 30) + "..")
                        .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(message));
                }else{
                    mBuilder.setContentText(message);
                }
            } else {
                mBuilder.setContentText("<missing message content>");
            }


            String soundName = notification.getSound();
            if (soundName != null) {
                Resources r = context.getResources();
                int resourceId = r.getIdentifier(soundName, "raw", context.getPackageName());
                Uri soundUri = Uri.parse("android.resource://" + context.getPackageName() + "/" + resourceId);
                mBuilder.setSound(soundUri);
                defaults &= ~Notification.DEFAULT_SOUND;
                mBuilder.setDefaults(defaults);
            }

            final Notification notify = mBuilder.build();
            final int largeIcon = getLargeIcon(context, remoteMessage);
            if (largeIcon > -1) {
                notify.contentView.setImageViewResource(android.R.id.icon, largeIcon);
            }

            mNotificationManager.notify(appName, notId, notify);
        } catch (Exception e) {
            StringWriter stackTraceWriter = new StringWriter();
            e.printStackTrace(new PrintWriter(stackTraceWriter));

            Log.d(TAG, "Exception has been raised: " + e.getMessage() + " and stack trace: " + stackTraceWriter.toString());
        }
    }

    private static String getAppName(Context context) {
        CharSequence appName =
                context
                        .getPackageManager()
                        .getApplicationLabel(context.getApplicationInfo());

        return (String) appName;
    }

    private static int getColor(RemoteMessage remoteMessage, Context context) {

        int theColor = Color.argb(1, 218, 223, 225); // default, transparent
        final String passedColor = remoteMessage.getNotification().getColor(); // something like "#FFFF0000", or "red"
        if (passedColor != null) {
            try {
                theColor = Color.parseColor(passedColor);
            } catch (IllegalArgumentException ignore) {
            }
        }

        try{

            int defaultColor = context.getResources().getIdentifier("ic_stat_notify_color", "color", context.getPackageName());
            Log.d(TAG, "use defaultColor  " + defaultColor);
            if(defaultColor > -1)
                theColor = context.getResources().getColor(defaultColor);
        }catch(Exception e){
            StringWriter stackTraceWriter = new StringWriter();
            e.printStackTrace(new PrintWriter(stackTraceWriter));            
            Log.d(TAG, "Exception has been raised: " + e.getMessage() + " and stack trace: " + stackTraceWriter.toString());
        }

        return theColor;
    }

    private static int getSmallIcon(Context context, RemoteMessage remoteMessage) {

        int icon = -1;

        // first try an iconname possible passed in the server payload
        final String iconNameFromServer = remoteMessage.getNotification().getIcon();
        if (iconNameFromServer != null) {
            icon = getIconValue(context.getPackageName(), iconNameFromServer);
        }

        // try a custom included icon in our bundle named ic_stat_notify(.png)
        if (icon == -1) {
            icon = getIconValue(context.getPackageName(), "ic_stat_notify");
        }

        // fall back to the regular app icon
        if (icon == -1) {
            icon = context.getApplicationInfo().icon;
        }

        return icon;
    }

    private static int getLargeIcon(Context context, RemoteMessage remoteMessage) {

        int icon = -1;

        // first try an iconname possible passed in the server payload
        final String iconNameFromServer = remoteMessage.getData().get("largeIcon");
        if (remoteMessage.getData().containsKey("largeIcon")) {
            if (iconNameFromServer != null) {
                icon = getIconValue(context.getPackageName(), iconNameFromServer);
            }
        }

        // try a custom included icon in our bundle named ic_stat_notify(.png)
        if (icon == -1) {
            icon = getIconValue(context.getPackageName(), "ic_notify");
        }

        // fall back to the regular app icon
        //if (icon == -1) {
        //    icon = context.getApplicationInfo().icon;
        //}

        return icon;
    }

    private static int getIconValue(String className, String iconName) {
        try {
            Class<?> clazz = Class.forName(className + ".R$drawable");
            return (Integer) clazz.getDeclaredField(iconName).get(Integer.class);
        } catch (Exception ignore) {
        }
        return -1;
    }
}
