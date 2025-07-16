package fr.bloup.blurpapi;

import fr.bloup.blurpapi.regions.BlurpRegion;
import fr.bloup.blurpapi.regions.BlurpRegionListener;
import org.bukkit.plugin.java.JavaPlugin;

public final class BlurpAPI extends JavaPlugin {
    private static JavaPlugin pluginInstance;

    public static void init(JavaPlugin plugin) {
        if (pluginInstance == null) {
            pluginInstance = plugin;
        }

        pluginInstance.getServer().getPluginManager().registerEvents(new BlurpRegionListener(new BlurpRegion()), pluginInstance);
    }

    public static JavaPlugin getPlugin() {
        if (pluginInstance == null) {
            throw new IllegalStateException("BlurpAPI has not been initialized correctly, please use BlurpAPI.init(plugin) to initialize it.");
        }
        return pluginInstance;
    }
}
