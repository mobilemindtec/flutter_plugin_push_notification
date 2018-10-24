#import "FlutterPluginPushNotificationPlugin.h"
#import <flutter_plugin_push_notification/flutter_plugin_push_notification-Swift.h>

@implementation FlutterPluginPushNotificationPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftFlutterPluginPushNotificationPlugin registerWithRegistrar:registrar];
}
@end
