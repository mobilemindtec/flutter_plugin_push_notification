import Flutter
import UIKit
import UserNotifications

public class SwiftPushNotificationPlugin: NSObject, FlutterPlugin, UIApplicationDelegate, UNUserNotificationCenterDelegate {
    
    private static let CHANNEL_NAME = "plugins.flutter.io/push_notification_plugin"
    private static let METHOD_REGISTER = "register"
    private static let METHOD_UNREGISTER = "unregister"
    private static let METHOD_MESSAGE_RECEIVED = "onMessageReceived"
    private static let METHOD_TOKEN_REFRESH = "onTokenRefresh"
    private static let METHOD_ARE_NOTIFICATION_ENABLED = "areNotificationsEnabled"
    private static let METHOD_APP_ICON_BADGE_NUMBER = "applicationIconBadgeNumber"
    private static let METHOD_CLEAN_APP_ICON_BADGE = "applicationCleanIconBadge"
    private static let METHOD_NOTIFICATION_CLICK = "notificationClick"
    private static let METHOD_NOTIFICATION_GET_PUSH_DATA = "getPushData"
    private static let METHOD_PUSH_APP_OPEN = "onPushAppOpen"

    private var flutterResult: FlutterResult?!
    private var flutterChannel: FlutterMethodChannel?!
    
    init(channel: FlutterMethodChannel){
        self.flutterChannel = channel
    }
    
    public static func register(with registrar: FlutterPluginRegistrar) {
        let channel = FlutterMethodChannel(name: SwiftPushNotificationPlugin.CHANNEL_NAME, binaryMessenger: registrar.messenger())
        let instance = SwiftPushNotificationPlugin(channel: channel)
        registrar.addMethodCallDelegate(instance, channel: channel)
        registrar.addApplicationDelegate(instance)
        
        
        let center = UNUserNotificationCenter.current()
        center.delegate = instance
    }

    public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
        
        self.flutterResult = result
        
        switch call.method {
            
            case SwiftPushNotificationPlugin.METHOD_REGISTER:
                
                var center: UNUserNotificationCenter?
                
                
                center = UNUserNotificationCenter.current()
                
                
                center!.requestAuthorization(options: [.alert, .sound, .badge], completionHandler: {
                    (granted, error) in
                    
                    if granted {
                        DispatchQueue.main.async {
                            UIApplication.shared.registerForRemoteNotifications()
                        }
                        //result(["status": "success", "message": nil, "data": nil])
                    }else{
                        let data = ["status": "error", "message": error?.localizedDescription, "data": nil]
                        self.flutterChannel!.invokeMethod(SwiftPushNotificationPlugin.METHOD_REGISTER, arguments: data)
                    }

                })
                
                break
            case SwiftPushNotificationPlugin.METHOD_UNREGISTER:
                UIApplication.shared.unregisterForRemoteNotifications()
                let data = ["status": "success", "message": nil, "data": nil]
                self.flutterChannel!.invokeMethod(SwiftPushNotificationPlugin.METHOD_UNREGISTER, arguments: data)
                break
            case SwiftPushNotificationPlugin.METHOD_MESSAGE_RECEIVED:
                let center = UNUserNotificationCenter.current()
                center.delegate = self
                break
            case SwiftPushNotificationPlugin.METHOD_TOKEN_REFRESH:
                // not apply from ios
                break
            case SwiftPushNotificationPlugin.METHOD_ARE_NOTIFICATION_ENABLED:
                result(["value": UIApplication.shared.isRegisteredForRemoteNotifications])
                break
            case SwiftPushNotificationPlugin.METHOD_APP_ICON_BADGE_NUMBER:
                let iconBadgeNumber = call.arguments as! Int
                UIApplication.shared.applicationIconBadgeNumber = iconBadgeNumber
                
                result(["status": "success", "message": nil, "data": nil])
                break
            case SwiftPushNotificationPlugin.METHOD_CLEAN_APP_ICON_BADGE:
                //UIApplication.shared.applicationIconBadgeNumber = 0
                self.clearNotifications()
                result(["status": "success", "message": nil, "data": nil])
                break
            case SwiftPushNotificationPlugin.METHOD_NOTIFICATION_CLICK:
                break
            case SwiftPushNotificationPlugin.METHOD_NOTIFICATION_GET_PUSH_DATA:
                result(nil)
                break
            default:
                result(FlutterMethodNotImplemented)
        }
        
    }
    
    // UIApplicationDelegate
    public func applicationDidFinishLaunching(_ application: UIApplication) {
        //UIApplication.shared.registerForRemoteNotifications()
    }
    
    public func application(_ application: UIApplication, didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data) {
        
        //let characterSet = CharacterSet(charactersIn: "<>")
        //let nsdataStr = NSData.init(data: deviceToken)
        //nsdataStr.description.trimmingCharacters(in: characterSet).replacingOccurrences(of: " ", with: "")
        let token = deviceToken.map { String(format: "%02hhx", $0) }.joined()
        let data = [
            "data": nil,
            "status": "success",
            "message": token
            ]
        
        self.flutterChannel!.invokeMethod(SwiftPushNotificationPlugin.METHOD_REGISTER, arguments: data)
    }
    
    public func application(_ application: UIApplication, didFailToRegisterForRemoteNotificationsWithError error: Error) {
        let data = [
            "status": "error",
            "message": error.localizedDescription,
            "data": nil
        ]
        
        self.flutterChannel!.invokeMethod(SwiftPushNotificationPlugin.METHOD_REGISTER, arguments: data)
    }
    
    // UNUserNotificationCenterDelegate
    public func userNotificationCenter(_ center: UNUserNotificationCenter, didReceive response: UNNotificationResponse, withCompletionHandler completionHandler: @escaping () -> Void) {
        
        NSLog("userNotificationCenter didReceive withCompletionHandler")
     
        let categoryIdentifier = response.notification.request.content.categoryIdentifier
        let userInfo = response.notification.request.content.userInfo
        let actionIdentifier = response.actionIdentifier
        
        let notificationData: [String: Any] = [
            "title": response.notification.request.content.title,
            "body": response.notification.request.content.body,
            "data": response.notification.request.content.userInfo,
            "categoryIdentifier": categoryIdentifier,
            "actionIdentifier": actionIdentifier
        ]
        
        let data: [String: Any] = [
            "data": notificationData,
            "message": response.notification.request.content.body,
            "status": "success"
            ]
        
        self.flutterChannel!.invokeMethod(SwiftPushNotificationPlugin.METHOD_PUSH_APP_OPEN, arguments: data)

        completionHandler()
    }
    
    public func userNotificationCenter(_ center: UNUserNotificationCenter, willPresent notification: UNNotification, withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void) {
        
        NSLog("userNotificationCenter willPresent withCompletionHandler")
        
        let notificationData: [String: Any] = [
            "title": notification.request.content.title,
            "body": notification.request.content.body,
            "data": notification.request.content.userInfo
        ]
        
        let data: [String: Any] = [
            "data": notificationData,
            "message": notification.request.content.body,
            "status": "success"
            ]
        
        self.flutterChannel!.invokeMethod(SwiftPushNotificationPlugin.METHOD_MESSAGE_RECEIVED, arguments: data)

        completionHandler(UNNotificationPresentationOptions.sound)
                
    }
    
    public func clearNotifications(){
        
        let shared = UIApplication.shared
        shared.applicationIconBadgeNumber = 0
        //shared.cancelAllLocalNotifications()
        
        let center = UNUserNotificationCenter.current()
        center.removeAllPendingNotificationRequests()
    }

}
