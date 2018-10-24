# flutter_plugin_push_notification

## create google-service.json

## android/build.gradle
```
buildscript {
    dependencies {
        classpath 'com.google.gms:google-services:3.1.1'
    }
}
```

## android/app/build.gradle (end of file)
```
apply plugin: 'com.google.gms.google-services'
```

## Colors.xml

```
<color name="ic_stat_notify_color">#54a0ff</color>
```

## AndroidManifest.xml

```
  <meta-data android:name="com.google.firebase.messaging.default_notification_color"
             android:resource="@color/ic_stat_notify_color" />

  <service
          android:stopWithTask="false"
          android:name="io.flutter.push.PushPlugin">
      <intent-filter>
          <action android:name="com.google.firebase.MESSAGING_EVENT"/>
      </intent-filter>
  </service>

  <service
          android:name="io.flutter.push.FirebaseInstanceIdListenerService">
      <intent-filter>
          <action android:name="com.google.firebase.INSTANCE_ID_EVENT"/>
      </intent-filter>
  </service>
```

## init on home 

```
	PushCallback pushCallback;

  @override
  void initState(){
    super.initState();
    this.pushCallback = new PushCallback();
    this.pushCallback.pushRegister();
  }

```

```
class PushCallback{

  PushNotification push;

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

  Future pushRegister() async{
    this.push = new PushNotification(
        senderID: "",
        onMessageReceived: _onMessageReceived,
        onTokenRefresh: _onTokenRefresh,
        onRegister: _onRegister,
        onUnregister: _onUnregister
    );
    this.push.register();
  }
}
```
