package fr.bloup.blurpapi.regions;

import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.List;

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
            }
        }
        for (String region : wasIn) {
            if (!isIn.contains(region)) {
                blurpRegion.triggerLeave(region);
            }
        }
    }
}
