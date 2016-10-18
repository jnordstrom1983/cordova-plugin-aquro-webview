/********* AquroWebView.m Cordova Plugin Implementation *******/

#import <Cordova/CDV.h>

@interface AquroWebView : CDVPlugin <UIWebViewDelegate> {
  // Member variables go here.
}

-(BOOL)webView:(UIWebView*)webView shouldStartLoadWithRequest:(NSURLRequest*)request navigationType:(UIWebViewNavigationType)navigationType;


- (void)CreateView:(CDVInvokedUrlCommand*)command;
- (void)HideView:(CDVInvokedUrlCommand*)command;
@end

@implementation AquroWebView

NSMutableDictionary *viewsDic;



- (BOOL)webView:(UIWebView *)myWebView shouldStartLoadWithRequest:(NSURLRequest *)request          navigationType:(UIWebViewNavigationType)navigationType {

    NSLog(@"inside Webview");
    if([[request.URL absoluteString] hasPrefix:@"aquro://"]) {
        // do stuff
        NSString* value = [request.URL absoluteString];
        value = [value componentsSeparatedByString:@"//"][1];

        NSMutableString* ret = @"AquroWebViewEvent('";
        [ret appendString:value];
        [ret appendString:@"')"];
        [self.commandDelegate evalJs:ret];
        return NO;
    }

    return YES;
}


- (void)RemoveAllViews:(CDVInvokedUrlCommand*)command{

    [viewsDic enumerateKeysAndObjectsUsingBlock: ^(NSString *key, UIWebView *obj, BOOL *stop) {
        [obj removeFromSuperview];
        obj = nil;

    }];


    [self.commandDelegate sendPluginResult:[CDVPluginResult resultWithStatus:CDVCommandStatus_OK] callbackId:command.callbackId];

}


- (void)HideView:(CDVInvokedUrlCommand*)command{
    NSString* name = [command.arguments objectAtIndex:0];
    UIWebView *webview=[viewsDic valueForKey:name];
    if(webview != nil){
        [webview setHidden:true];
    }


    [self.commandDelegate sendPluginResult:[CDVPluginResult resultWithStatus:CDVCommandStatus_OK] callbackId:command.callbackId];


}
- (void)ShowView:(CDVInvokedUrlCommand*)command{
    NSString* name = [command.arguments objectAtIndex:0];
    UIWebView *webview=[viewsDic valueForKey:name];
    if(webview != nil){
        [webview setHidden:false];
    }
    [self.commandDelegate sendPluginResult:[CDVPluginResult resultWithStatus:CDVCommandStatus_OK] callbackId:command.callbackId];

}
- (void)DeleteView:(CDVInvokedUrlCommand*)command{
    NSString* name = [command.arguments objectAtIndex:0];
    UIWebView *webview=[viewsDic valueForKey:name];
    if(webview != nil){
        [webview removeFromSuperview];
        webview = nil;
    }
    [self.commandDelegate sendPluginResult:[CDVPluginResult resultWithStatus:CDVCommandStatus_OK] callbackId:command.callbackId];

}

- (void)MoveView:(CDVInvokedUrlCommand*)command
{


    NSString* name = [command.arguments objectAtIndex:0];
    UIWebView *webview=[viewsDic valueForKey:name];
    if(webview != nil){

        NSInteger top = [[command.arguments objectAtIndex:1] integerValue];
        NSInteger left = [[command.arguments objectAtIndex:2] integerValue];
        NSInteger width = [[command.arguments objectAtIndex:3] integerValue];
        NSInteger height = [[command.arguments objectAtIndex:4] integerValue];


        CGRect webFrame = webview.frame;
        webFrame.origin.x = left;
        webFrame.origin.y = top;
        webFrame.size.height = height;
        webFrame.size.width = width;
        webview.frame = webFrame;


    }




    [self.commandDelegate sendPluginResult:[CDVPluginResult resultWithStatus:CDVCommandStatus_OK] callbackId:command.callbackId];



}



- (void)SetURL:(CDVInvokedUrlCommand*)command
{


    NSString* name = [command.arguments objectAtIndex:0];
    NSString* urladdress = [command.arguments objectAtIndex:1];

    UIWebView *webview=[viewsDic valueForKey:name];
    if(webview != nil){

        NSURL *url = [NSURL URLWithString:urladdress];
        NSURLRequest *requestObj = [NSURLRequest requestWithURL:url];
        [webview loadRequest:requestObj];
    }

    [self.commandDelegate sendPluginResult:[CDVPluginResult resultWithStatus:CDVCommandStatus_OK] callbackId:command.callbackId];



}





- (void)CreateView:(CDVInvokedUrlCommand*)command
{
    if(viewsDic == nil){
        viewsDic = [NSMutableDictionary dictionary];
    }


    CDVPluginResult* pluginResult = nil;
    NSString* url = [command.arguments objectAtIndex:1];
    NSString* name = [command.arguments objectAtIndex:0];



    NSInteger top = [[command.arguments objectAtIndex:2] integerValue];
    NSInteger left = [[command.arguments objectAtIndex:3] integerValue];
    NSInteger width = [[command.arguments objectAtIndex:4] integerValue];
    NSInteger height = [[command.arguments objectAtIndex:5] integerValue];


    CGRect webFrame = CGRectMake(top, left, width, height);

    UIWebView *webview=[[UIWebView alloc]initWithFrame:webFrame];



    NSURL *nsurl=[NSURL URLWithString:url];
    NSURLRequest *nsrequest=[NSURLRequest requestWithURL:nsurl];
    [webview loadRequest:nsrequest];

    [viewsDic setObject:webview forKey:name];
    webview.delegate = self;
    [self.webView addSubview:webview];

    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:name];

    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

@end
