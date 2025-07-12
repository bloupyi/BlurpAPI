package fr.bloup.blurpapi.Utils;

import org.bukkit.Location;

import java.util.*;

public class BlurpRegion {
    private final Map<String, RegionData> regions = new HashMap<>();

    public enum RegionType {
        CUBOID, SPHERE, CYLINDER, POLYGON
    }

    private static class RegionData {
        public final RegionType type;
        public final List<Location> polygonPoints;
        public final Location loc1;
        public final Location loc2;
        public final Location center;
        public final int radius;
        public final int height;

        public RegionData(RegionType type, Location loc1, Location loc2, Location center, int radius, int height, List<Location> polygonPoints) {
            this.type = type;
            this.loc1 = loc1;
            this.loc2 = loc2;
            this.center = center;
            this.radius = radius;
            this.height = height;
            this.polygonPoints = polygonPoints;
        }
    }

    public BlurpRegion cuboid(String name, Location loc1, Location loc2) {
        regions.put(name, new RegionData(RegionType.CUBOID, loc1, loc2, null, 0, 0, null));
        return this;
    }

    public BlurpRegion sphere(String name, Location center, int radius) {
        regions.put(name, new RegionData(RegionType.SPHERE, null, null, center, radius, 0, null));
        return this;
    }

    public BlurpRegion cylinder(String name, Location center, int radius, int height) {
        regions.put(name, new RegionData(RegionType.CYLINDER, null, null, center, radius, height, null));
        return this;
    }

    public BlurpRegion polygon(String name, List<Location> points) {
        if (points.isEmpty() || points.stream().map(loc -> loc.getWorld().getName()).distinct().count() > 1)
            throw new IllegalArgumentException("All polygon points must be in the same world.");
        regions.put(name, new RegionData(RegionType.POLYGON, null, null, null, 0, 0, points));
        return this;
    }

    public boolean isInRegion(String region, Location loc) {
        RegionData data = regions.get(region);
        if (data == null) return false;

        Location checkLoc = loc.getBlock().getLocation();

        switch (data.type) {
            case CUBOID -> {
                int x = checkLoc.getBlockX();
                int y = checkLoc.getBlockY();
                int z = checkLoc.getBlockZ();

                int minX = Math.min(data.loc1.getBlockX(), data.loc2.getBlockX());
                int maxX = Math.max(data.loc1.getBlockX(), data.loc2.getBlockX());
                int minY = Math.min(data.loc1.getBlockY(), data.loc2.getBlockY());
                int maxY = Math.max(data.loc1.getBlockY(), data.loc2.getBlockY());
                int minZ = Math.min(data.loc1.getBlockZ(), data.loc2.getBlockZ());
                int maxZ = Math.max(data.loc1.getBlockZ(), data.loc2.getBlockZ());

                return x >= minX && x <= maxX &&
                        y >= minY && y <= maxY &&
                        z >= minZ && z <= maxZ;
            }

            case SPHERE -> {
                int dx = checkLoc.getBlockX() - data.center.getBlockX();
                int dy = checkLoc.getBlockY() - data.center.getBlockY();
                int dz = checkLoc.getBlockZ() - data.center.getBlockZ();
                return (dx * dx + dy * dy + dz * dz) <= data.radius * data.radius;
            }

            case CYLINDER -> {
                int dx = checkLoc.getBlockX() - data.center.getBlockX();
                int dz = checkLoc.getBlockZ() - data.center.getBlockZ();
                int dy = checkLoc.getBlockY() - data.center.getBlockY();
                boolean inRadius = (dx * dx + dz * dz) <= data.radius * data.radius;
                boolean inHeight = dy >= 0 && dy < data.height;
                return inRadius && inHeight;
            }

            case POLYGON -> {
                List<Location> pts = data.polygonPoints;
                if (pts.size() < 4) return false;
                Location test = checkLoc;
                int minX = pts.stream().mapToInt(Location::getBlockX).min().orElse(0);
                int maxX = pts.stream().mapToInt(Location::getBlockX).max().orElse(0);
                int minY = pts.stream().mapToInt(Location::getBlockY).min().orElse(0);
                int maxY = pts.stream().mapToInt(Location::getBlockY).max().orElse(0);
                int minZ = pts.stream().mapToInt(Location::getBlockZ).min().orElse(0);
                int maxZ = pts.stream().mapToInt(Location::getBlockZ).max().orElse(0);
                if (test.getBlockX() < minX || test.getBlockX() > maxX ||
                    test.getBlockY() < minY || test.getBlockY() > maxY ||
                    test.getBlockZ() < minZ || test.getBlockZ() > maxZ) {
                    return false;
                }
                // On compte combien de faces sont traversées par un rayon partant du point
                // Ici, on simplifie en projetant sur Y et on considère la projection sur chaque face triangulée
                // Pour une vraie utilisation, il faudrait une structure de faces (triangles)
                // On va approximer avec un test sur chaque triangle formé par pts[0], pts[i], pts[i+1]
                int crossings = 0;
                for (int i = 1; i < pts.size() - 1; i++) {
                    Location a = pts.get(0);
                    Location b = pts.get(i);
                    Location c = pts.get(i + 1);
                    if (isPointInTriangle3D(test, a, b, c)) {
                        crossings++;
                    }
                }
                // Si le nombre de croisements est impair, le point est dedans
                return (crossings % 2) == 1;
            }

            default -> {
                return false;
            }
        }
    }

    public List<Location> getBlocksInRegion(String region) {
        RegionData data = regions.get(region);
        if (data == null) return Collections.emptyList();

        List<Location> blocks = new ArrayList<>();

        switch (data.type) {
            case CUBOID -> {
                int minX = Math.min(data.loc1.getBlockX(), data.loc2.getBlockX());
                int maxX = Math.max(data.loc1.getBlockX(), data.loc2.getBlockX());
                int minY = Math.min(data.loc1.getBlockY(), data.loc2.getBlockY());
                int maxY = Math.max(data.loc1.getBlockY(), data.loc2.getBlockY());
                int minZ = Math.min(data.loc1.getBlockZ(), data.loc2.getBlockZ());
                int maxZ = Math.max(data.loc1.getBlockZ(), data.loc2.getBlockZ());

                for (int x = minX; x <= maxX; x++) {
                    for (int y = minY; y <= maxY; y++) {
                        for (int z = minZ; z <= maxZ; z++) {
                            blocks.add(new Location(data.loc1.getWorld(), x, y, z));
                        }
                    }
                }
            }

            case SPHERE -> {
                int cx = data.center.getBlockX();
                int cy = data.center.getBlockY();
                int cz = data.center.getBlockZ();
                int radiusSquared = data.radius * data.radius;

                for (int x = cx - data.radius; x <= cx + data.radius; x++) {
                    for (int y = cy - data.radius; y <= cy + data.radius; y++) {
                        for (int z = cz - data.radius; z <= cz + data.radius; z++) {
                            int dx = x - cx;
                            int dy = y - cy;
                            int dz = z - cz;
                            if (dx * dx + dy * dy + dz * dz <= radiusSquared) {
                                blocks.add(new Location(data.center.getWorld(), x, y, z));
                            }
                        }
                    }
                }
            }

            case CYLINDER -> {
                int cx = data.center.getBlockX();
                int cy = data.center.getBlockY();
                int cz = data.center.getBlockZ();
                int radiusSquared = data.radius * data.radius;

                for (int y = cy; y < cy + data.height; y++) {
                    for (int x = cx - data.radius; x <= cx + data.radius; x++) {
                        for (int z = cz - data.radius; z <= cz + data.radius; z++) {
                            int dx = x - cx;
                            int dz = z - cz;
                            if (dx * dx + dz * dz <= radiusSquared) {
                                blocks.add(new Location(data.center.getWorld(), x, y, z));
                            }
                        }
                    }
                }
            }

            case POLYGON -> {
                List<Location> pts = data.polygonPoints;
                if (pts.size() < 4) break;
                int minX = pts.stream().mapToInt(Location::getBlockX).min().orElse(0);
                int maxX = pts.stream().mapToInt(Location::getBlockX).max().orElse(0);
                int minY = pts.stream().mapToInt(Location::getBlockY).min().orElse(0);
                int maxY = pts.stream().mapToInt(Location::getBlockY).max().orElse(0);
                int minZ = pts.stream().mapToInt(Location::getBlockZ).min().orElse(0);
                int maxZ = pts.stream().mapToInt(Location::getBlockZ).max().orElse(0);
                for (int x = minX; x <= maxX; x++) {
                    for (int y = minY; y <= maxY; y++) {
                        for (int z = minZ; z <= maxZ; z++) {
                            Location test = new Location(pts.get(0).getWorld(), x, y, z);
                            int crossings = 0;
                            for (int i = 1; i < pts.size() - 1; i++) {
                                Location a = pts.get(0);
                                Location b = pts.get(i);
                                Location c = pts.get(i + 1);
                                if (isPointInTriangle3D(test, a, b, c)) {
                                    crossings++;
                                }
                            }
                            if ((crossings % 2) == 1) {
                                blocks.add(test);
                            }
                        }
                    }
                }
            }
        }

        return blocks;
    }

    public List<String> regionsAt(Location loc) {
        List<String> found = new ArrayList<>();
        for (Map.Entry<String, RegionData> entry : regions.entrySet()) {
            if (isInRegion(entry.getKey(), loc)) {
                found.add(entry.getKey());
            }
        }
        return found;
    }

    public void remove(String name) {
        regions.remove(name);
    }

    public void clear() {
        regions.clear();
    }

    // Test si un point est dans un triangle 3D (approximation, valable pour polyèdre convexe)
    private boolean isPointInTriangle3D(Location p, Location a, Location b, Location c) {
        double[] v0 = {c.getX() - a.getX(), c.getY() - a.getY(), c.getZ() - a.getZ()};
        double[] v1 = {b.getX() - a.getX(), b.getY() - a.getY(), b.getZ() - a.getZ()};
        double[] v2 = {p.getX() - a.getX(), p.getY() - a.getY(), p.getZ() - a.getZ()};
        double dot00 = dot(v0, v0);
        double dot01 = dot(v0, v1);
        double dot02 = dot(v0, v2);
        double dot11 = dot(v1, v1);
        double dot12 = dot(v1, v2);
        double invDenom = 1.0 / (dot00 * dot11 - dot01 * dot01);
        double u = (dot11 * dot02 - dot01 * dot12) * invDenom;
        double v = (dot00 * dot12 - dot01 * dot02) * invDenom;
        return (u >= 0) && (v >= 0) && (u + v <= 1);
    }

    private double dot(double[] a, double[] b) {
        return a[0]*b[0] + a[1]*b[1] + a[2]*b[2];
    }
}
