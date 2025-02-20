#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>

@interface RCT_EXTERN_MODULE(LivelinessDetectionBridge, NSObject)

RCT_EXTERN_METHOD(sendReferenceID:(NSString *)referenceID)

@end
