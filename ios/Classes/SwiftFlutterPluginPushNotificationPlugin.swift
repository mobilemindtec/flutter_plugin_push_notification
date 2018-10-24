import Flutter
import UIKit

public class SwiftFlutterPluginPushNotificationPlugin: NSObject, FlutterPlugin {
  public static func register(with registrar: FlutterPluginRegistrar) {
    let channel = FlutterMethodChannel(name: "flutter_plugin_push_notification", binaryMessenger: registrar.messenger())
    let instance = SwiftFlutterPluginPushNotificationPlugin()
    registrar.addMethodCallDelegate(instance, channel: channel)
  }

  public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
    result("iOS " + UIDevice.current.systemVersion)
  }
}
