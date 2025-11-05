package fr.bloup.blurpapi.utils;

import io.netty.channel.Channel;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class BlurpNettyHelper {
    public static Channel getChannel(Player player) {
        try {
            String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];

            Class<?> craftPlayerClass = Class.forName("org.bukkit.craftbukkit." + version + ".entity.CraftPlayer");

            Method getHandle = craftPlayerClass.getMethod("getHandle");
            Object handle = getHandle.invoke(player);

            Field connectionField = handle.getClass().getDeclaredField("connection");
            connectionField.setAccessible(true);
            Object connection = connectionField.get(handle);

            Field networkManagerField = null;
            try {
                networkManagerField = connection.getClass().getDeclaredField("connection");
            } catch (NoSuchFieldException e1) {
                try {
                    networkManagerField = connection.getClass().getDeclaredField("networkManager");
                } catch (NoSuchFieldException e2) {
                    try {
                        networkManagerField = connection.getClass().getDeclaredField("b");
                    } catch (NoSuchFieldException e3) {
                        throw new RuntimeException("Impossible de trouver le NetworkManager field");
                    }
                }
            }

            networkManagerField.setAccessible(true);
            Object networkManager = networkManagerField.get(connection);

            // Récupère le Channel
            Field channelField = networkManager.getClass().getDeclaredField("channel");
            channelField.setAccessible(true);

            return (Channel) channelField.get(networkManager);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
