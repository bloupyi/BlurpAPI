package fr.bloup.blurpapi.events.regions;

import fr.bloup.blurpapi.regions.BlurpRegion;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@RequiredArgsConstructor
public class PlayerLeaveRegionEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    @Getter private final Player player;
    @Getter private final BlurpRegion.RegionData regionData;

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
