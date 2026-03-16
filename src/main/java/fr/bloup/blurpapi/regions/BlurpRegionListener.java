package fr.bloup.blurpapi.regions;

import fr.bloup.blurpapi.events.regions.PlayerEnterRegionEvent;
import fr.bloup.blurpapi.events.regions.PlayerLeaveRegionEvent;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.List;

import static fr.bloup.blurpapi.regions.BlurpRegion.regions;

@RequiredArgsConstructor
public class BlurpRegionListener implements Listener {
    private final BlurpRegion blurpRegion;

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();
        if (from == null || to == null) return;

        List<String> wasIn = blurpRegion.regionsAt(from);
        List<String> isIn = blurpRegion.regionsAt(to);

        for (String region : isIn) {
            if (!wasIn.contains(region)) {
                blurpRegion.triggerEnter(region);
                Bukkit.getPluginManager().callEvent(new PlayerEnterRegionEvent(event.getPlayer(), regions.get(region)));
            }
        }
        for (String region : wasIn) {
            if (!isIn.contains(region)) {
                blurpRegion.triggerLeave(region);
                Bukkit.getPluginManager().callEvent(new PlayerLeaveRegionEvent(event.getPlayer(), regions.get(region)));
            }
        }
    }
}
