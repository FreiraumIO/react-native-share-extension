#import <UIKit/UIKit.h>
#import "React/RCTBridgeModule.h"

@interface ReactNativeShareExtension : UIViewController<RCTBridgeModule>
- (UIView*) shareView;

- (void)load:(void(^)(NSString *value, UIImage* image, NSString* contentType,  NSException *exception))callback ;

@end
