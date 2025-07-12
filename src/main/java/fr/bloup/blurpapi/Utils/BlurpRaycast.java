package fr.bloup.blurpapi.Utils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.function.Predicate;

public class BlurpRaycast {
    private Location location;
    private Vector vector;
    private float maxDistance = 10;
    private float precision = 0.25F;
    private boolean ignoreEntities = false;
    private boolean ignoreFuilds = false;
    private boolean ignoreBlocks = false;
    private Predicate<Entity> entityFilters = e -> true;
    private Predicate<Block> blockFilters = b -> true;

    public BlurpRaycast from(Location location) {
        this.location = location;
        return this;
    }

    public BlurpRaycast vector(Vector vector) {
        this.vector = vector;
        return this;
    }

    public BlurpRaycast maxDistance(float distance) {
        this.maxDistance = distance;
        return this;
    }

    public BlurpRaycast precision(float precision) {
        if (precision <= 0) throw new IllegalArgumentException("Precision must be > 0");
        this.precision = precision;
        return this;
    }

    public BlurpRaycast ignoreEntities() {
        this.ignoreEntities = true;
        return this;
    }

    public BlurpRaycast ignoreFluids() {
        this.ignoreFuilds = true;
        return this;
    }

    public BlurpRaycast ignoreBlocks() {
        this.ignoreBlocks = true;
        return this;
    }

    public <T> BlurpRaycast filter(Predicate<T> filter, Class<T> type) {
        if (type == Entity.class) {
            this.entityFilters = (Predicate<Entity>) filter;
        } else if (type == Block.class) {
            this.blockFilters = (Predicate<Block>) filter;
        }
        return this;
    }

    public RaycastResult cast() {
        if (location == null || vector == null) return null;

        if (!ignoreEntities) {
            List<Entity> entities = location.getWorld().getNearbyEntities(location, maxDistance, maxDistance, maxDistance)
                    .stream()
                    .filter(entity -> !entity.getLocation().equals(location))
                    .filter(entityFilters != null ? entityFilters : e -> true)
                    .toList();

            for (Entity entity : entities) {
                RayTraceResult result = entity.getBoundingBox().rayTrace(location.toVector(), vector, maxDistance);
                if (result != null) {
                    Location hitLocation = result.getHitPosition().toLocation(location.getWorld());
                    return new RaycastResult(hitLocation, entity, null);
                }
            }
        }

        if (ignoreBlocks) {
            return null;
        }

        Vector direction = vector.clone().normalize();
        Location current = location.clone();

        for (float d = 0; d < maxDistance; d += precision) {
            current.add(direction.clone().multiply(precision));
            Block block = current.getBlock();

            if (block.getType().isAir()) continue;

            if (ignoreFuilds && block.isLiquid()) continue;

            if (blockFilters != null && !blockFilters.test(block)) {
                continue;
            }

            return new RaycastResult(current.clone(), null, block);
        }

        return null;
    }



    private boolean blockFilter(Block block) {
        return blockFilters == null || blockFilters.test(block);
    }

    @Getter
    @RequiredArgsConstructor
    private static class RaycastResult {
        private final Location hitLocation;
        private final Entity hitEntity;
        private final Block hitBlock;

        public boolean hitEntity() {
            return hitEntity != null;
        }

        public boolean hitBlock() {
            return hitBlock != null;
        }
    }
}
