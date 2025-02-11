-dontwarn **
-renamesourcefileattribute null
-keep class io.netty.** { *; }
-keep class org.cloudburstmc.netty.** { *; }
-keep @io.netty.channel.ChannelHandler$Sharable class *
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keep class coelho.msftauth.api.** { *; }
-keep class com.mucheng.mucute.client.util.** { *; }
-keep class com.mucheng.mucute.client.game.AccountManager { *; }
