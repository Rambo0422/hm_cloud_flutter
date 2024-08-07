//
//  RequestTool.m
//  hm_cloud
//
//  Created by a水 on 2024/8/7.
//

#import <AFNetworking/AFNetworking.h>
#import <MJExtension/MJExtension.h>
#import "HmCloudTool.h"
#import "RequestTool.h"

@interface RequestTool ()

@property (nonatomic, strong) AFHTTPSessionManager *s;

@end

@implementation RequestTool

+ (RequestTool *)share {
    static RequestTool *instance = nil;
    static dispatch_once_t onceToken;

    dispatch_once(&onceToken, ^{
        instance = [[self alloc] init];
    });
    return instance;
}

- (instancetype)init {
    self = [super init];

    if (self) {
        self.s = [AFHTTPSessionManager manager];

        self.s.requestSerializer.timeoutInterval = 20;
        self.s.requestSerializer = [AFJSONRequestSerializer serializer];
    }

    return self;
}

- (void)requestUrl:(NSString *)uri methodType:(RequestMethodType)methodType params:(NSDictionary *_Nullable)param faildCallBack:(nullable void (^)(void))faildCallBack successCallBack:(nullable void (^)(id obj))successCallBack {
    NSString *token = [HmCloudTool share].userToken;
    NSString *host = [k_api_host stringByAppendingString:uri];

    [self.s.requestSerializer setValue:token forHTTPHeaderField:@"token"];
    [self.s.requestSerializer setValue:@"ios" forHTTPHeaderField:@"platform"];

    self.s.responseSerializer.acceptableContentTypes = [NSSet setWithObjects:@"application/json", @"text/json", @"text/javascript", @"text/html", nil];

    if (methodType == Request_POST) {
        [self.s   POST:host
            parameters:param
               headers:nil
              progress:nil
               success:^(NSURLSessionDataTask *_Nonnull task, id _Nullable responseObject)
        {
#ifdef DEBUG
            NSLog(@"\n------------  Success  ------------\n"
                  " \n \n"
                  " url = %@ \n"
                  " \n \n"
                  " param = %@ \n"
                  " \n \n"
                  " response = %@ \n"
                  " \n \n"
                  "------------  Success  ------------\n", host, [param mj_JSONString], [responseObject mj_JSONString]);
#endif

            if (successCallBack) {
                NSNumber *code = responseObject[@"code"];

                if (code.intValue == 0) {
                    successCallBack(responseObject);
                }
            }
        }
               failure:^(NSURLSessionDataTask *_Nullable task, NSError *_Nonnull error)
        {
            if (faildCallBack) {
                faildCallBack();
            }
        }];
    }

    if (methodType == Request_GET) {
        [self.s    GET:host
            parameters:param
               headers:nil
              progress:nil
               success:^(NSURLSessionDataTask *_Nonnull task, id _Nullable responseObject)
        {
#ifdef DEBUG
            NSLog(@"\n------------  Success  ------------\n"
                  " \n \n"
                  " url = %@ \n"
                  " \n \n"
                  " param = %@ \n"
                  " \n \n"
                  " response = %@ \n"
                  " \n \n"
                  "------------  Success  ------------\n", host, [param mj_JSONString], [responseObject mj_JSONString]);
#endif

            if (successCallBack) {
                NSNumber *code = responseObject[@"code"];

                if (code.intValue == 0) {
                    successCallBack(responseObject);
                }
            }
        }
               failure:^(NSURLSessionDataTask *_Nullable task, NSError *_Nonnull error)
        {
            if (faildCallBack) {
                faildCallBack();
            }
        }];
    }
}

@end
