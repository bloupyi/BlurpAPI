package fr.bloup.blurpapi.events.regions;

import fr.bloup.blurpapi.regions.BlurpRegion;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

@Getter
public class PlayerLeaveRegionEvent extends PlayerEvent {
    private static final HandlerList handlers = new HandlerList();
    private final BlurpRegion.RegionData regionData;

    public PlayerLeaveRegionEvent(Player player, BlurpRegion.RegionData regionData) {
        super(player);
        this.regionData = regionData;
    }

    @Override
    public HandlerList getHandlers() { return handlers; }

    public static HandlerList getHandlerList() { return handlers; }
}
