package fr.bloup.blurpapi.utils;

import io.netty.channel.Channel;
import org.bukkit.craftbukkit.v1_21_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;

public class BlurpNettyHelper {
    public static Channel getChannel(Player player) {
        try {
            Object handle = ((CraftPlayer) player).getHandle();
            Field connectionField = handle.getClass().getField("connection");
            Object connection = connectionField.get(handle);

            Field networkManagerField = connection.getClass().getDeclaredField("connection"); // ou "networkManager"
            networkManagerField.setAccessible(true);
            Object networkManager = networkManagerField.get(connection);

            Field channelField = networkManager.getClass().getDeclaredField("channel");
            channelField.setAccessible(true);
            return (Channel) channelField.get(networkManager);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
