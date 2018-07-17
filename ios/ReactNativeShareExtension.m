#import "ReactNativeShareExtension.h"
#import "React/RCTRootView.h"
#import <MobileCoreServices/MobileCoreServices.h>

#define URL_IDENTIFIER @"public.url"
#define IMAGE_IDENTIFIER @"public.image"
#define TEXT_IDENTIFIER (NSString *)kUTTypePlainText

NSExtensionContext* extensionContext;

@implementation ReactNativeShareExtension {
    NSTimer *autoTimer;
    NSString* type;
    NSString* value;
    UIImage* image;
   
    NSUInteger _taskTotal;
    NSUInteger _task;
    NSMutableArray* _taskItems;
}

- (UIView*) shareView:(NSString*)url {
    return nil;
}

RCT_EXPORT_MODULE();

- (void)viewDidLoad {
    [super viewDidLoad];

    //object variable for extension doesn't work for react-native. It must be assign to gloabl
    //variable extensionContext. in this way, both exported method can touch extensionContext
    extensionContext = self.extensionContext;

    UIView *rootView = [self shareView];
    if (rootView.backgroundColor == nil) {
        rootView.backgroundColor = [[UIColor alloc] initWithRed:1 green:1 blue:1 alpha:0.1];
    }

    self.view = rootView;
}

 

- (void)loads:(void(^)(NSArray* items,  NSException *exception))callback  {
    NSExtensionItem *inputItem = [extensionContext.inputItems firstObject];
    NSLog(@"loads , extensionContext.inputItems:%@",extensionContext.inputItems);
    NSArray *attachments = inputItem.attachments;
    _task = 0; _taskTotal = [attachments count];
    _taskItems = [NSMutableArray array];
    
    
    [self extractDataFromContext: extensionContext withCallback:^(NSString* val,UIImage* image, NSString* contentType, NSException* err) {
        NSLog(@"callback-> val:%@, contentType:%@, err:%@",val,contentType,err);
        _task = _task + 1;
        if (nil != image){
            NSDictionary* item = NSDictionaryOfVariableBindings(val,image,contentType);
            [_taskItems addObject:item];
        }
        NSLog(@"_taskTotal=%d, _task=%d",_taskTotal, _task);
        if ( _taskTotal == _task){
            callback(_taskItems,nil);
        }
    }];
  
    
}


RCT_EXPORT_METHOD(close) {
    [extensionContext completeRequestReturningItems:nil
                                  completionHandler:nil];
}

RCT_REMAP_METHOD(data,
                 resolver:(RCTPromiseResolveBlock)resolve
                 rejecter:(RCTPromiseRejectBlock)reject)
{

    [self extractDataFromContext: extensionContext withCallback:^(NSString* val,UIImage* image, NSString* contentType, NSException* err) {
        if(err) {
            reject(@"error", err.description, nil);
        } else {
            resolve(@{
                      @"type": contentType,
                      @"value": val,
                      @"image":image
                      });
            
        }
    }];
}

-(UIImage*)harvestImage:(NSString *)imageURL {
    NSFileManager *fileManager = [NSFileManager defaultManager];
    NSData *imgData = [fileManager contentsAtPath:imageURL];
    UIImage *img = [UIImage imageWithData:imgData];
    // Process Image..
    return img;
}

- (void)extractDataFromContext:(NSExtensionContext *)context withCallback:(void(^)(NSString *value, UIImage* image, NSString* contentType,  NSException *exception))callback {
    NSExtensionItem *item = [context.inputItems firstObject];
    NSArray *attachments = item.attachments;
    [attachments enumerateObjectsUsingBlock:^(NSItemProvider *provider, NSUInteger idx, BOOL *stop) {
        [self extractDataFromContextItem:provider withCallback:callback ];
    }];
     
}

 

- (void)extractDataFromContextItem: (NSItemProvider *)provider withCallback:(void(^)(NSString *value, UIImage* image, NSString* contentType,  NSException *exception))callback {
    @try {
      //  NSExtensionItem *item = [context.inputItems firstObject];
     //   NSArray *attachments = item.attachments;
        
        __block NSItemProvider *urlProvider = nil;
        __block NSItemProvider *imageProvider = nil;
        __block NSItemProvider *textProvider = nil;
        
      //  [attachments enumerateObjectsUsingBlock:^(NSItemProvider *provider, NSUInteger idx, BOOL *stop) {
            if([provider hasItemConformingToTypeIdentifier:URL_IDENTIFIER]) {
                urlProvider = provider;
              //  *stop = YES;
            } else if ([provider hasItemConformingToTypeIdentifier:TEXT_IDENTIFIER]){
                textProvider = provider;
               // *stop = YES;
            } else if ([provider hasItemConformingToTypeIdentifier:IMAGE_IDENTIFIER]){
                imageProvider = provider;
              //  *stop = YES;
            }
      //  }];
        
        //  Look for an image inside the NSItemProvider
        if (imageProvider){
            if([imageProvider hasItemConformingToTypeIdentifier:(NSString *)kUTTypeImage]){
                [imageProvider loadItemForTypeIdentifier:(NSString *)kUTTypeImage options:nil completionHandler:^(UIImage *image, NSError *error) {
                    if(image){
                        if(callback) {
                            
                            callback(@"binary image here", image, @".png", nil);
                        }
                    }
                }];
                return ;
            }
            
        }
        
        if(urlProvider) {
            [urlProvider loadItemForTypeIdentifier:URL_IDENTIFIER options:nil completionHandler:^(id<NSSecureCoding> item, NSError *error) {
                NSURL *url = (NSURL *)item;
                
                if(callback) {
                    callback([url absoluteString],nil, @"text/plain", nil);
                }
            }];
        } else if (imageProvider) {
            [imageProvider loadItemForTypeIdentifier:IMAGE_IDENTIFIER options:nil completionHandler:^(id<NSSecureCoding> item, NSError *error) {
                NSURL *url = (NSURL *)item;
                
                if(callback) {
                    NSString* buf = [url absoluteString];
                    NSString* type = [[[url absoluteString] pathExtension] lowercaseString];
                    callback(buf, nil, type, nil);
                    NSLog(@"detected:%@", buf);
                    
                }
            }];
            
        } else if (textProvider) {
            [textProvider loadItemForTypeIdentifier:TEXT_IDENTIFIER options:nil completionHandler:^(id<NSSecureCoding> item, NSError *error) {
                NSString *text = (NSString *)item;
                
                if(callback) {
                    callback(text, nil, @"text/plain", nil);
                }
            }];
        } else {
            if(callback) {
                callback(nil, nil,nil, [NSException exceptionWithName:@"Error" reason:@"couldn't find provider" userInfo:nil]);
            }
        }
    }
    @catch (NSException *exception) {
        if(callback) {
            callback(nil,nil, nil, exception);
        }
    }
}

@end
