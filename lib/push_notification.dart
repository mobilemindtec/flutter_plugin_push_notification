import 'dart:async';

import 'package:flutter/services.dart';



class PushException implements Exception {
  final message;

  PushException([this.message]);

  String toString() {
    if (message == null) return "Exception";
    return "Exception: $message";
  }
}

enum PushResultStatus {
  Error,
  Success
}

class PushResult {

  PushResultStatus status;
  String message;
  String data;

  PushResult({this.status, this.message, this.data});
}

typedef Future<dynamic> MessageHandler(PushResult result);


class PushNotification {

  static const MethodChannel _channel =
      const MethodChannel('plugins.flutter.io/push_notification_plugin');

  final MessageHandler onMessageReceived;
  final MessageHandler onTokenRefresh;
  final MessageHandler onRegister;
  final MessageHandler onUnregister;

  // Android settings
  String senderID;
  //iOS settings
  bool badge; //# Enable setting badge through Push Notification
  bool sound; // # Enable playing a sound
  bool alert; // # Enable creating a alert
  bool resgiterMessageReceivedListener;
  bool resgiterTokenRefreshListener;

  PushNotification({
    this.onMessageReceived,
    this.onTokenRefresh,

    this.senderID,
    this.badge,
    this.sound,
    this.alert}){
    _channel.setMethodCallHandler(_handleMethod);
  }

  void register()  {
    try{
      var data = {
        "senderID": this.senderID,
        "badge": this.badge,
        "sound": this.sound,
        "alert": this.alert,
      };
       _channel.invokeMethod('register', data);

    }on PlatformException catch (e) {
      throw new PushException(e.message);
    }
  }

  void unregister() {
    try{
      _channel.invokeMethod('unregister');
    }on PlatformException catch (e) {
      throw new PushException(e.message);
    }
  }

  void registerMessageReceivedListener() {
    try{
      _channel.invokeMethod('onMessageReceived');
    }on PlatformException catch (e) {
      throw new PushException(e.message);
    }
  }

  void registerTokenRefreshListener() {
    try{
      _channel.invokeMethod('onTokenRefresh');
    }on PlatformException catch (e) {
      throw new PushException(e.message);
    }
  }

  Future<bool> areNotificationsEnabled() async {
    try{
      final Map result = await _channel.invokeMethod('areNotificationsEnabled');

      return result["value"];

    }on PlatformException catch (e) {
      throw new PushException(e.message);
    }
  }

  Future<Null> _handleMethod(MethodCall call) async {

    print("_handleMethod = ${call.method}");

    switch (call.method) {
      case "onMessageReceived":
          if(onMessageReceived != null){
            var status = call.arguments["status"] == "success" ? PushResultStatus.Success : PushResultStatus.Error;
            var result = new PushResult(status: status, message: call.arguments["message"], data: call.arguments["data"]);
            onMessageReceived(result);
          }
        break;
      case "onTokenRefresh":
        if(onTokenRefresh != null){
          var status = call.arguments["status"] == "success" ? PushResultStatus.Success : PushResultStatus.Error;
          var result = new PushResult(status: status, message: call.arguments["message"], data: call.arguments["data"]);
          onTokenRefresh(result);
        }
        break;
      case "register":
        if(onRegister != null){
          var status = call.arguments["status"] == "success" ? PushResultStatus.Success : PushResultStatus.Error;
          var result = new PushResult(status: status, message: call.arguments["message"], data: call.arguments["data"]);
          onRegister(result);
        }
        break;
      case "unregister":
        if(onUnregister != null){
          var status = call.arguments["status"] == "success" ? PushResultStatus.Success : PushResultStatus.Error;
          var result = new PushResult(status: status, message: call.arguments["message"], data: call.arguments["data"]);
          onUnregister(result);
        }
        break;
    }
  }
}
