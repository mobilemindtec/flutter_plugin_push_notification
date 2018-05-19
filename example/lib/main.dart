import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_plugin_push_notification/push_notification.dart';

void main() => runApp(new MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => new _MyAppState();
}

class _MyAppState extends State<MyApp> {

  PushNotification push = new PushNotification(
    senderID: "759149683428",

  );

  Future _onMessageReceived(PushResult result){

      print("_onMessageReceived");
      print("-- ${result.message}");
      print("-- ${result.data}");
      print("-- ${result.status}");

  }

  Future _onTokenRefresh(PushResult result){
    print("_onTokenRefresh");
    print("-- ${result.message}");
    print("-- ${result.data}");
    print("-- ${result.status}");

  }

  Future _onRegister(PushResult result){
    print("_onRegister");
    print("-- ${result.message}");
    print("-- ${result.data}");
    print("-- ${result.status}");

    push.registerMessageReceivedListener();
    push.registerTokenRefreshListener();
  }

  Future _onUnregister(PushResult result){
    print("_onUnregister");
    print("-- ${result.message}");
    print("-- ${result.data}");
    print("-- ${result.status}");
  }

  @override
  initState(){
    super.initState();


    pusgRegister();
  }

  Future pusgRegister() async{
    push.onMessageReceived = _onMessageReceived;
    push.onTokenRefresh = _onTokenRefresh;
    push.onRegister= _onRegister;
    push.onUnregister= _onUnregister;
    push.register();
  }


  @override
  Widget build(BuildContext context) {
    return new MaterialApp(
      home: new Scaffold(
        appBar: new AppBar(
          title: new Text('Plugin example app'),
        ),
        body: new Center(
          child: new Text(''),
        ),
      ),
    );
  }
}
