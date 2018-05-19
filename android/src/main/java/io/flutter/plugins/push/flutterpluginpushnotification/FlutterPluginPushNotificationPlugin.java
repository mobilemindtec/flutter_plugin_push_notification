package io.flutter.plugins.push.flutterpluginpushnotification;

import android.util.Log;
import io.flutter.push.*;

import android.app.Activity;
import android.app.Application;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;

import java.util.HashMap;
import java.util.Map;

/**
 * FlutterPluginPushNotificationPlugin
 */
public class FlutterPluginPushNotificationPlugin implements MethodCallHandler {

  private static String CHANNEL_NAME  = "plugins.flutter.io/push_notification_plugin";
  private static String METHOD_REGISTER = "register";
  private static String METHOD_UNREGISTER = "unregister";
  private static String METHOD_MESSAGE_RECEIVED = "onMessageReceived";
  private static String METHOD_TOKEN_REFRESH = "onTokenRefresh";
  private static String METHOD_ARE_NOTIFICATION_ENABLED = "areNotificationsEnabled";

  /**
   * Plugin registration.
   */
  public static void registerWith(Registrar registrar) {
    final MethodChannel channel = new MethodChannel(registrar.messenger(), CHANNEL_NAME);
    channel.setMethodCallHandler(new FlutterPluginPushNotificationPlugin(registrar, channel));
  }

  private Registrar registrar;
  private MethodChannel channel;
  private Activity activity;
  private Application application;

  private Result methodResult;

  private String senderID;


  public FlutterPluginPushNotificationPlugin(Registrar registrar, MethodChannel channel) {

    this.registrar = registrar;
    this.activity = registrar.activity();
    this.application = (Application) registrar.context();
    this.channel = channel;

    PushLifecycleCallbacks.registerCallbacks(this.application);
  }

  @Override
  public void onMethodCall(MethodCall call, Result result) {

    this.methodResult = result;
    if(call.hasArgument("senderID"))
      this.senderID = call.argument("senderID").toString();

    if (call.method.equals(METHOD_REGISTER)) {
      this.register();
    }  else  if (call.method.equals(METHOD_UNREGISTER)) {
      this.unregister();
    }  else  if (call.method.equals(METHOD_MESSAGE_RECEIVED)) {
      this.onMessageReceived();
    }  else  if (call.method.equals(METHOD_TOKEN_REFRESH)) {
      this.onTokenRefresh();
    }  else  if (call.method.equals(METHOD_ARE_NOTIFICATION_ENABLED)) {
      this.areNotificationsEnabled();
    } else {
      result.notImplemented();
    }
  }



  public void register() {
    PushPlugin.register(this.application, this.senderID, new PushPluginListenerImpl(METHOD_REGISTER));
  }

  public void unregister() {
    PushPlugin.unregister(this.application, this.senderID, new PushPluginListenerImpl(METHOD_UNREGISTER));
  }

  public void onMessageReceived() {
    PushPlugin.setOnMessageReceivedCallback(new PushPluginListenerImpl(METHOD_MESSAGE_RECEIVED));
  }

  public void onTokenRefresh() {
    PushPlugin.setOnTokenRefreshCallback(new PushPluginListenerImpl(METHOD_TOKEN_REFRESH));
  }

  public void areNotificationsEnabled() {
    final boolean value = PushPlugin.areNotificationsEnabled();

    Map data = new HashMap() {{
      put("status", "success");
      put("value", value);
    }};

    methodResult.success(data);
  }

  private class PushPluginListenerImpl implements PushPluginListener {

    private String methodName;

    PushPluginListenerImpl(String methodName){
      this.methodName = methodName;
    }

    @Override
    public void success(final Object message, final Object data) {

      Map map = new HashMap() {{
        put("status", "success");
        put("message", message);
        put("data", data);
      }};

      channel.invokeMethod(methodName, map);

    }

    @Override
    public void success(final Object message) {

      Map map = new HashMap() {{
        put("status", "success");
        put("message", message);
        put("data", "{}");
      }};

      channel.invokeMethod(methodName, map);

    }

    @Override
    public void error(final Object data) {

      Map map = new HashMap() {{
        put("status", "error");
        put("message", data);
      }};

      channel.invokeMethod(methodName, map);

    }

  }
}
