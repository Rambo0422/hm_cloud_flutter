# The point of this package is to specify that a dependent plugin intends to
# use the AndroidX lifecycle classes. Make sure no R8 heuristics shrink classes
# brought in by the embedding's pom.
#
# This isn't strictly needed since by definition, plugins using Android
# lifecycles should implement DefaultLifecycleObserver and therefore keep it
# from being shrunk. But there seems to be an R8 bug so this needs to stay
# https://issuetracker.google.com/issues/142778206.
-keep class androidx.lifecycle.DefaultLifecycleObserver

# haima cloud
-dontwarn org.codehaus.**
-keep class org.codehaus.** {*;}
-keep interface com.haima.hmcp.listeners.*{*;}
-keep class com.haima.hmcp.beans.*{*;}
-keep enum com.haima.hmcp.enums.*{*;}
-keep class com.haima.hmcp.**{*;}
-keep enum com.haima.hmcp.websocket.WebSocketCloseNotification{*;}
-keep interface com.haima.hmcp.websocket.WebSocket{*;}
-keep interface com.haima.hmcp.websocket.WebSocketConnectionObserver{*;}
-keep class com.haima.hmcp.websocket.WebSocketConnection{public <methods>;}
-keep class com.haima.hmcp.websocket.WebSocketOptions{public <methods>;}
-keep class com.haima.hmcp.websocket.WebSocketException{*;}
-keep interface com.hmcp.saas.sdk.listeners.*{*;}
-keep class com.hmcp.saas.sdk.beans.*{*;}
-keep class com.hmcp.saas.sdk.enums.*{*;}
-keep class com.hmcp.saas.sdk.SaasSDK{public <methods>;}
-keep class de.tavendo.autobahn.**{*;}
-keep class tv.haima.ijk.media.player.** { *; }
-keep interface tv.haima.ijk.media.player.listeners.*{*;}
-keep interface tv.haima.ijk.media.player.IMediaPlayer{*;}
-keep class com.netcheck.LDNetDiagnoService.LDNetDiagnoService{public <methods>;}
-keep interface com.netcheck.LDNetDiagnoService.LDNetDiagnoListener{public <methods>;}
-keep class com.netcheck.LDNetDiagnoService.LDNetTraceRoute { *; }
-dontwarn org.openudid.**
-keep class org.openudid.**{*;}
-keep class org.hmwebrtc.**{*;}
-keep class org.webrtc.haima.**{*;}
-keep class io.socket.**{*;}
-keep class com.haima.hmcp.rtc.widgets.RtcTextureViewRenderer{*;}
# protobuf
-dontwarn com.google.**
-keep class com.google.protobuf.** {*;}

-keep class com.sayx.hm_cloud.callback.* { *; }
-keep class com.sayx.hm_cloud.callback.** { *; }
-keep class com.sayx.hm_cloud.constants.* { *; }
-keep class com.sayx.hm_cloud.constants.** { *; }
-keep class com.sayx.hm_cloud.dialog.* { *; }
-keep class com.sayx.hm_cloud.dialog.** { *; }
-keep class com.sayx.hm_cloud.model.* { *; }
-keep class com.sayx.hm_cloud.model.** { *; }
-keep class com.sayx.hm_cloud.utils.* { *; }
-keep class com.sayx.hm_cloud.utils.** { *; }
-keep class com.sayx.hm_cloud.widget.* { *; }
-keep class com.sayx.hm_cloud.widget.** { *; }
-keep class com.sayx.hm_cloud.* { *; }

-keep class * {
    native <methods>;
}

-keepclassmembers class * {
    public static final *;
}