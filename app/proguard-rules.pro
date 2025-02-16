-dontwarn **
-renamesourcefileattribute null
-keep class io.netty.** { *; }
-keep class org.cloudburstmc.netty.** { *; }
-keep class org.cloudburstmc.protocol.bedrock.codec.** { *; }
-keep @io.netty.channel.ChannelHandler$Sharable class *
-keep class com.google.gson.** { *; }
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keep class net.raphimc.minecraftauth.** { *; }
-keep class net.lenni0451.commons.httpclient.** { *; }
-keep class com.mucheng.mucute.client.game.AccountManager { *; }
-keep class org.jose4j.** { *; }
-keep class io.jsonwebtoken.** { *; }
-keep class com.fasterxml.jackson.** { *; }
-keep class java.security.** { *; }
-keep class javax.** { *; }