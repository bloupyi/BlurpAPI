package fr.bloup.blurpapi.subtick;

import fr.bloup.blurpapi.BlurpAPI;
import fr.bloup.blurpapi.utils.BlurpNettyHelper;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

public class BlurpSubTickEngine {
    private final PriorityQueue<BufferedEvent> buffer =
            new PriorityQueue<>(Comparator.comparingLong(BufferedEvent::getTimestamp));
    private final ConcurrentHashMap<UUID, Channel> injectedPlayers = new ConcurrentHashMap<>();
    private BiConsumer<Player, Object> packetHandler;
    private boolean running = false;

    public void start(BiConsumer<Player, Object> handler) {
        if (running) return;
        running = true;

        this.packetHandler = handler;

        // Process queue every tick
        Bukkit.getScheduler().runTaskTimer(BlurpAPI.getPlugin(), this::processAll, 1L, 1L);

        // Inject all online players
        Bukkit.getOnlinePlayers().forEach(this::injectPlayer);

        // Register join/quit listener
        Bukkit.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onJoin(PlayerJoinEvent event) {
                injectPlayer(event.getPlayer());
            }

            @EventHandler
            public void onQuit(PlayerQuitEvent event) {
                uninjectPlayer(event.getPlayer());
            }
        }, BlurpAPI.getPlugin());
    }

    public void stop() {
        running = false;
        injectedPlayers.forEach((uuid, channel) -> {
            if (channel.pipeline().get("subtick_handler") != null) {
                channel.pipeline().remove("subtick_handler");
            }
        });
        injectedPlayers.clear();
        buffer.clear();
    }

    private void injectPlayer(Player player) {
        Channel channel = BlurpNettyHelper.getChannel(player);

        if (channel.pipeline().get("subtick_handler") != null) return;

        ChannelDuplexHandler handler = new ChannelDuplexHandler() {
            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                long timestamp = System.nanoTime();

                addEvent(() -> {
                    if (packetHandler != null) {
                        packetHandler.accept(player, msg);
                    }
                }, timestamp);

                super.channelRead(ctx, msg);
            }
        };

        channel.pipeline().addBefore("packet_handler", "subtick_handler", handler);
        injectedPlayers.put(player.getUniqueId(), channel);
    }

    private void uninjectPlayer(Player player) {
        Channel channel = injectedPlayers.remove(player.getUniqueId());
        if (channel != null && channel.pipeline().get("subtick_handler") != null) {
            channel.pipeline().remove("subtick_handler");
        }
    }

    private void addEvent(Runnable action, long timestamp) {
        synchronized (buffer) {
            buffer.add(new BufferedEvent(timestamp, action));
        }
    }

    private void processAll() {
        synchronized (buffer) {
            while (!buffer.isEmpty()) {
                buffer.poll().getAction().run();
            }
        }
    }

    @Getter
    private static class BufferedEvent {
        private final long timestamp;
        private final Runnable action;

        public BufferedEvent(long timestamp, Runnable action) {
            this.timestamp = timestamp;
            this.action = action;
        }
    }
}
