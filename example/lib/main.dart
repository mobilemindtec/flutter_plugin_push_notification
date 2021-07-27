import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:push_notification/push_notification.dart';

void main() => runApp(new MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => new _MyAppState();
}

class _MyAppState extends State<MyApp> {

  PushNotification? push;

  Future<Null> _onMessageReceived(PushResult result) async {
    print("_onMessageReceived");
    print("-- ${result.message}");
    print("-- ${result.data}");
    print("-- ${result.status}");
  }

  Future<Null> _onTokenRefresh(PushResult result) async {
    print("_onTokenRefresh");
    print("-- ${result.message}");
    print("-- ${result.data}");
    print("-- ${result.status}");
  }

  Future<Null> _onRegister(PushResult result) async {
    print("_onRegister");
    print("-- ${result.message}");
    print("-- ${result.data}");
    print("-- ${result.status}");

    push!.registerMessageReceivedListener();
    push!.registerTokenRefreshListener();
  }

  Future<Null> _onUnregister(PushResult result) async {
    print("_onUnregister");
    print("-- ${result.message}");
    print("-- ${result.data}");
    print("-- ${result.status}");
  }

  @override
  initState(){
    super.initState();


    pusgRegister();

    push!.getPushData().then((data){
      if(data != null){
        print("push data -----------------------");
        print(data);
        print("push data -----------------------");
      }else{
        print("not push data");
      }
    }).catchError((err){
      print("get push data error: $err");
    });

  }

  Future pusgRegister() async{
    this.push = new PushNotification(
        onMessageReceived: _onMessageReceived,
        onTokenRefresh: _onTokenRefresh,
        onRegister: _onRegister,
        onUnregister: _onUnregister
    );
    this.push!.register();
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
