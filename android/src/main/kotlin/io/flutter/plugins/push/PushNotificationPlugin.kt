package io.flutter.plugins.push

import android.app.Activity
import android.app.Application
import android.util.Log
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry
import io.flutter.plugins.push.extra.PushLifecycleCallbacks
import io.flutter.plugins.push.extra.PushPlugin
import io.flutter.plugins.push.extra.PushPluginListener

/**
 * PushNotificationPlugin
 */
class PushNotificationPlugin : MethodCallHandler, FlutterPlugin, ActivityAware {

    companion object {

        private const val CHANNEL_NAME = "plugins.flutter.io/push_notification_plugin"
        private const val METHOD_REGISTER = "register"
        private const val METHOD_UNREGISTER = "unregister"
        private const val METHOD_MESSAGE_RECEIVED = "onMessageReceived"
        private const val METHOD_TOKEN_REFRESH = "onTokenRefresh"
        private const val METHOD_ARE_NOTIFICATION_ENABLED = "areNotificationsEnabled"
        private const val METHOD_APP_ICON_BADGE_NUMBER = "applicationIconBadgeNumber"
        private const val METHOD_CLEAN_APP_ICON_BADGE = "applicationCleanIconBadge"
        private const val METHOD_NOTIFICATION_CLICK = "notificationClick"
        private const val METHOD_GET_PUSH_DATA = "getPushData"
    }




    private var activity: Activity? = null
    private var application: Application? = null
    private var channel: MethodChannel? = null
    private var methodResult: Result? = null


    override fun onMethodCall(call: MethodCall, result: Result) {

        this.methodResult = result

        when(call.method) {

            METHOD_REGISTER -> this.register()

            METHOD_UNREGISTER -> this.unregister()

            METHOD_MESSAGE_RECEIVED -> this.onMessageReceived()

            METHOD_TOKEN_REFRESH -> this.onTokenRefresh()

            METHOD_ARE_NOTIFICATION_ENABLED -> this.areNotificationsEnabled()

            METHOD_APP_ICON_BADGE_NUMBER -> this.methodResult!!.success(null)

            METHOD_CLEAN_APP_ICON_BADGE -> this.methodResult!!.success(null)

            METHOD_NOTIFICATION_CLICK -> this.onNotificationClickRegister()

            METHOD_GET_PUSH_DATA -> this.getPushData()

            else -> result.notImplemented()
        }

    }


    fun register() {
        PushPlugin.register(this.application!!, PushPluginListenerImpl(METHOD_REGISTER))
    }

    fun unregister() {
        PushPlugin.unregister(this.application!!, PushPluginListenerImpl(METHOD_UNREGISTER))
    }

    fun onMessageReceived() {
        PushPlugin.setOnMessageReceivedCallback(PushPluginListenerImpl(METHOD_MESSAGE_RECEIVED))
    }

    fun onTokenRefresh() {
        PushPlugin.setOnTokenRefreshCallback(PushPluginListenerImpl(METHOD_TOKEN_REFRESH))
    }

    fun areNotificationsEnabled() {
        val value = PushPlugin.areNotificationsEnabled()


        var data = mutableMapOf(
                "status" to "success",
                "value" to value
        )

        methodResult!!.success(data)
    }

    fun getPushData(){

        //Log.i("FLUTTER_NOTE", "********* intent = ${this.activity.intent}")

        if(this.activity!!.intent != null && this.activity!!.intent.extras != null) {

            //Log.i("FLUTTER_NOTE", "********* extras = ${this.activity.intent.extras}")

            val extras = this.activity!!.intent.extras!!

            //Log.i("FLUTTER_NOTE", "********* extras appName = ${extras.containsKey("APP_NAME")}")

            var data = mutableMapOf<String, Any>()

            if (extras.containsKey("APP_NAME")) {

                var bundle = extras.getBundle("NOTE_DATA")!!
                var bundleData = mutableMapOf<String, String>()

                for (key in bundle.keySet()) {
                    bundleData[key] = bundle.getString(key)!!
                }

                data["data"] = bundleData
            }

            for (key in extras.keySet()) {
                data["$key"] = "${extras[key]}"
            }


            methodResult!!.success(data)

        }else{
            methodResult!!.success(null)
        }


    }

    fun onNotificationClickRegister() {

    }

    private inner class PushPluginListenerImpl internal constructor(private val methodName: String) : PushPluginListener {

        override fun success(message: String?, title: String?, data: Any?) {
            val map = mutableMapOf(
                    "status" to "success",
                    "message" to message,
                    "title" to title,
                    "data" to data
            )

            if(channel != null) {
                this@PushNotificationPlugin.activity!!.runOnUiThread(java.lang.Runnable {
                    channel!!.invokeMethod(methodName, map)
                })
            }
        }

        override fun success(message: String?) {
            val map = mutableMapOf(
                    "status" to "success",
                    "message" to message,
                    "data" to null
            )
            if(channel != null) {
                this@PushNotificationPlugin.activity!!.runOnUiThread(java.lang.Runnable {
                    channel!!.invokeMethod(methodName, map)
                })
            }
        }

        override fun error(data: Any?) {
            val map = mutableMapOf(
                    "status" to "error",
                    "message" to data,
                    "data" to null
            )
            if(channel != null) {
                this@PushNotificationPlugin.activity!!.runOnUiThread(java.lang.Runnable {
                    channel!!.invokeMethod(methodName, map)
                })
            }
        }
    }

    override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, CHANNEL_NAME)
        channel!!.setMethodCallHandler(this)
    }

    override fun onDetachedFromEngine(p0: FlutterPlugin.FlutterPluginBinding) {
        if(channel != null) {
            channel!!.setMethodCallHandler(null)
        }
    }

    override fun onAttachedToActivity(activityPluginBinding: ActivityPluginBinding) {
        this.activity = activityPluginBinding.activity;
        this.application = activityPluginBinding.activity.application;
        //this.application!!.registerActivityLifecycleCallbacks(activityHandler)
        //activityPluginBinding.addActivityResultListener(activityHandler)
        PushLifecycleCallbacks.registerCallbacks(this.application)
    }

    override fun onDetachedFromActivityForConfigChanges() {
        TODO("Not yet implemented")
    }

    override fun onReattachedToActivityForConfigChanges(p0: ActivityPluginBinding) {
        TODO("Not yet implemented")
    }

    override fun onDetachedFromActivity() {
        PushLifecycleCallbacks.unregisterCallbacks(this.application)
        this.activity = null;
        this.application = null;
        //this.application!!.unregisterActivityLifecycleCallbacks(activityHandler)
    }

}