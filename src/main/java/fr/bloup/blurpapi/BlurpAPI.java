package fr.bloup.blurpapi;

import org.bukkit.plugin.java.JavaPlugin;

public final class BlurpAPI extends JavaPlugin {
    private static JavaPlugin pluginInstance;

    public static void init(JavaPlugin plugin) {
        if (pluginInstance == null) {
            pluginInstance = plugin;
        }
    }

    public static JavaPlugin getPlugin() {
        if (pluginInstance == null) {
            throw new IllegalStateException("BlurpAPI has not been initialized correctly, please use BlurpAPI.enable(plugin) to initialize it.");
        }
        return pluginInstance;
    }
}
