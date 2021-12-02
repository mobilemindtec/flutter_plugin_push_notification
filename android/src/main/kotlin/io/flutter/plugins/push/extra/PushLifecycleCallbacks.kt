package io.flutter.plugins.push.extra

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log

/**
 * Subscribe to the Pause and Resume activity events in order to toggle the PushPlugin's status.
 * When the PushPlugin is not in active state - i.e. at foreground, notifications should be created
 * and published in the Notification Center, otherwise they're passed directly to the application
 * by invoking the onMessageReceived callback.
 */
class PushLifecycleCallbacks : Application.ActivityLifecycleCallbacks {

    override fun onActivityPaused(activity: Activity) {
        Log.d(PushPlugin.TAG, "onActivityPaused: Application has been stopped.")

        // the application is being stopped -> the push plugin is not in active/foreground state anymore
        PushPlugin.isActive = false
    }

    override fun onActivityResumed(activity: Activity) {
        Log.d(PushPlugin.TAG, "onActivityPaused: Application has been started")

        // the application has been resumed-> the push plugin is now in active/foreground state
        PushPlugin.isActive = true
    }

    override fun onActivityCreated(activity: Activity, bundle: Bundle?) {}

    override fun onActivityDestroyed(activity: Activity) {}

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

    override fun onActivityStarted(activity: Activity) {}

    override fun onActivityStopped(activity: Activity) {}

    companion object {

        private val callbacks = PushLifecycleCallbacks()

        /**
         * Register for the application's events
         * @param app
         */
        fun registerCallbacks(app: Application?) {
            Log.d("PushLifecycleCallbacks", "Registering the activity lifecycle callbacks...")
            app!!.registerActivityLifecycleCallbacks(callbacks)
        }

        fun unregisterCallbacks(app: Application?) {
            // clean up, not to leak and register it N times...
            Log.d("PushLifecycleCallbacks", "Unregistering the activity lifecycle callbacks...")
            app!!.unregisterActivityLifecycleCallbacks(callbacks)
        }
    }
}