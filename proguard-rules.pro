# Keep the main class
-keep public class com.yourpackage.Main {
    public static void main(java.lang.String[]);
}

# Keep all classes that extend JavaPlugin
-keep class com.yourpackage.** extends org.bukkit.plugin.java.JavaPlugin { *; }

# Keep all event handlers
-keepclassmembers class * {
    @org.bukkit.event.EventHandler *;
}

# Don't warn about missing classes (optional, depends on your project)
-dontwarn com.yourpackage.**