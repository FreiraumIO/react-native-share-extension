#import "ReactNativeShareExtension.h"
#import "React/RCTRootView.h"
#import <MobileCoreServices/MobileCoreServices.h>

#define URL_IDENTIFIER @"public.url"
#define IMAGE_IDENTIFIER @"public.image"
#define MOVIE_IDENTIFIER @"public.movie"
#define TEXT_IDENTIFIER (NSString *)kUTTypePlainText

NSExtensionContext* extensionContext;

@implementation ReactNativeShareExtension {
    NSTimer *autoTimer;
    NSString* value;
   
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
    
    
    [self extractDataFromContext: extensionContext withCallback:^(NSString* val, NSException* err) {
        _task = _task + 1;

        NSDictionary* item = NSDictionaryOfVariableBindings(val);
        [_taskItems addObject:item];

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
    NSExtensionItem *inputItem = [extensionContext.inputItems firstObject];
    NSLog(@"loads , extensionContext.inputItems:%@",extensionContext.inputItems);
    NSArray *attachments = inputItem.attachments;
    _task = 0; _taskTotal = [attachments count];
    _taskItems = [NSMutableArray array];

    [self extractDataFromContext: extensionContext withCallback:^(NSString* val, NSException* err) {
        _task = _task + 1;
        
        [_taskItems addObject:val];
            
        if (_taskTotal == _task){
            if(err) {
                reject(@"error", err.description, nil);
            } else {
                resolve(@{ @"mixedFiles": _taskItems });
            }
        }
    }];
}

- (void)extractDataFromContext:(NSExtensionContext *)context withCallback:(void(^)(NSString *value, NSException *exception))callback {
    NSExtensionItem *item = [context.inputItems firstObject];
    NSArray *attachments = item.attachments;
    [attachments enumerateObjectsUsingBlock:^(NSItemProvider *provider, NSUInteger idx, BOOL *stop) {
        [self extractDataFromContextItem:provider withCallback:callback ];
    }];
     
}

- (void)extractDataFromContextItem: (NSItemProvider *)provider withCallback:(void(^)(NSString *value, NSException *exception))callback {
    @try {
        __block NSItemProvider *urlProvider = nil;
        __block NSItemProvider *imageProvider = nil;
        __block NSItemProvider *textProvider = nil;
        __block NSItemProvider *videoProvider = nil;

        if([provider hasItemConformingToTypeIdentifier:URL_IDENTIFIER]) {
            urlProvider = provider;
          //  *stop = YES;
        } else if ([provider hasItemConformingToTypeIdentifier:TEXT_IDENTIFIER]){
            textProvider = provider;
           // *stop = YES;
        } else if ([provider hasItemConformingToTypeIdentifier:IMAGE_IDENTIFIER]){
            imageProvider = provider;
          //  *stop = YES;
        } else if ([provider hasItemConformingToTypeIdentifier:MOVIE_IDENTIFIER]){
            videoProvider = provider;
          //  *stop = YES;
        }
        
        //  Look for an image inside the NSItemProvider
        if (imageProvider){
            if([imageProvider hasItemConformingToTypeIdentifier:(NSString *)kUTTypeImage]){
                [imageProvider loadItemForTypeIdentifier:(NSString *)kUTTypeImage options:nil completionHandler:^(id<NSSecureCoding> item, NSError *error) {
                    NSURL *url = (NSURL *)item;
                    if(url){
                        if(callback) {
                            callback([url absoluteString], nil);
                        }
                    }
                }];
            }
        } else if (videoProvider){
            if([videoProvider hasItemConformingToTypeIdentifier:(NSString *)kUTTypeMovie]){
                [videoProvider loadItemForTypeIdentifier:(NSString *)kUTTypeMovie options:nil completionHandler:^(id<NSSecureCoding> item, NSError *error) {
                    NSURL *url = (NSURL *)item;
                    if(url){
                        if(callback) {
                            callback([url absoluteString], nil);
                        }
                    }
                }];
            }
        } else if(urlProvider) {
            [urlProvider loadItemForTypeIdentifier:URL_IDENTIFIER options:nil completionHandler:^(id<NSSecureCoding> item, NSError *error) {
                NSURL *url = (NSURL *)item;
                
                if(callback) {
                    callback([url absoluteString], nil);
                }
            }];
        } else if (textProvider) {
            [textProvider loadItemForTypeIdentifier:TEXT_IDENTIFIER options:nil completionHandler:^(id<NSSecureCoding> item, NSError *error) {
                NSString *text = (NSString *)item;
                
                if(callback) {
                    callback(text, nil);
                }
            }];
        } else {
            if(callback) {
                callback(nil, [NSException exceptionWithName:@"Error" reason:@"couldn't find provider" userInfo:nil]);
            }
        }
    }
    @catch (NSException *exception) {
        if(callback) {
            callback(nil, exception);
        }
    }
}

@end
