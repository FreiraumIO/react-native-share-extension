#import <UIKit/UIKit.h>
#import "React/RCTBridgeModule.h"

@interface ReactNativeShareExtension : UIViewController<RCTBridgeModule>
- (UIView*) shareView;

- (void)loads:(void(^)(NSArray* items,  NSException *exception))callback ;

@end
