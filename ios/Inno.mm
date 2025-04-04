#import <React/RCTBridgeModule.h>

@interface RCT_EXTERN_MODULE(Inno, NSObject)

RCT_EXTERN_METHOD(multiply:(float)a withB:(float)b
                 withResolver:(RCTPromiseResolveBlock)resolve
                 withRejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(getHelloWorld:(RCTPromiseResolveBlock)resolve
                 withRejecter:(RCTPromiseRejectBlock)reject)

// Update the showEkycUI extern method to include the referenceId parameter
RCT_EXTERN_METHOD(showEkycUI:(NSString *)referenceId resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(startLivelinessDetection)

+ (BOOL)requiresMainQueueSetup
{
  return NO;
}

@end