package fr.bloup.blurpapi.utils;

import org.bukkit.*;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

public class BlurpParticle {

    public enum ShapeType {
        SINGLE,
        CUBOID,
        PYRAMID,
        SPHERE,
        CIRCLE,
        CYLINDER
    }

    public enum Axis {
        X,
        Y,
        Z
    }

    public enum ColorMode {
        SINGLE,
        RANDOM_PER_POINT,
        GRADIENT,
        RAINBOW
    }

    private Particle particle = Particle.FLAME;
    private int count = 1;
    private double offsetX = 0;
    private double offsetY = 0;
    private double offsetZ = 0;
    private double speed = 0;
    private boolean force = false;

    private Color color;
    private List<Color> colors;
    private ColorMode colorMode = ColorMode.SINGLE;
    private Color gradientFrom;
    private Color gradientTo;
    private List<Color> gradientColors;
    private Color transitionTo;
    private float dustSize = 1.0f;

    private Object data;

    private ShapeType shapeType = ShapeType.SINGLE;

    private boolean filled = true;
    private double surfaceThickness = -1;

    private Axis axis = Axis.Y;
    private double yawRad = 0;
    private double pitchRad = 0;
    private double rollRad = 0;

    private double radius = 1.0;
    private double height = 2.0;
    private Vector size = new Vector(1, 1, 1);
    private double step = 0.25;

    private Integer circlePoints;

    private double pyramidBase = 2.0;
    private double pyramidHeight = 2.0;

    public BlurpParticle particle(Particle particle) {
        this.particle = Objects.requireNonNull(particle, "particle");
        return this;
    }

    public BlurpParticle count(int count) {
        if (count < 0) {
            throw new IllegalArgumentException("count must be >= 0");
        }
        this.count = count;
        return this;
    }

    public BlurpParticle velocity(double offsetX, double offsetY, double offsetZ, double speed) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.offsetZ = offsetZ;
        this.speed = speed;
        return this;
    }

    public BlurpParticle force(boolean force) {
        this.force = force;
        return this;
    }

    public BlurpParticle color(Color color) {
        this.color = color;
        this.colorMode = ColorMode.SINGLE;
        return this;
    }

    public BlurpParticle colors(Color... colors) {
        Objects.requireNonNull(colors, "colors");
        List<Color> list = new ArrayList<>();
        for (Color c : colors) {
            if (c != null) {
                list.add(c);
            }
        }
        this.colors = list.isEmpty() ? null : list;
        return this;
    }

    public BlurpParticle colorMode(ColorMode colorMode) {
        this.colorMode = Objects.requireNonNull(colorMode, "colorMode");
        return this;
    }

    public BlurpParticle gradient(Color from, Color to) {
        this.gradientFrom = Objects.requireNonNull(from, "from");
        this.gradientTo = Objects.requireNonNull(to, "to");
        this.gradientColors = null;
        this.colorMode = ColorMode.GRADIENT;
        return this;
    }

    public BlurpParticle gradient(Color... colors) {
        Objects.requireNonNull(colors, "colors");
        List<Color> list = new ArrayList<>();
        for (Color c : colors) {
            if (c != null) {
                list.add(c);
            }
        }
        if (list.size() < 2) {
            throw new IllegalArgumentException("gradient requires at least 2 colors");
        }
        this.gradientColors = list;
        this.gradientFrom = null;
        this.gradientTo = null;
        this.colorMode = ColorMode.GRADIENT;
        return this;
    }

    public BlurpParticle rainbow() {
        this.colorMode = ColorMode.RAINBOW;
        return this;
    }

    public BlurpParticle dustTransition(Color from, Color to, float size) {
        this.gradientFrom = Objects.requireNonNull(from, "from");
        this.transitionTo = Objects.requireNonNull(to, "to");
        dustSize(size);
        return this;
    }

    public BlurpParticle dustSize(float dustSize) {
        if (dustSize <= 0) {
            throw new IllegalArgumentException("dustSize must be > 0");
        }
        this.dustSize = dustSize;
        return this;
    }

    public BlurpParticle data(Object data) {
        this.data = data;
        return this;
    }

    public BlurpParticle blockData(BlockData blockData) {
        return data(Objects.requireNonNull(blockData, "blockData"));
    }

    public BlurpParticle blockData(Material material) {
        Objects.requireNonNull(material, "material");
        return data(material.createBlockData());
    }

    public BlurpParticle itemData(ItemStack itemStack) {
        return data(Objects.requireNonNull(itemStack, "itemStack"));
    }

    public BlurpParticle filled() {
        this.filled = true;
        return this;
    }

    public BlurpParticle hollow() {
        this.filled = false;
        return this;
    }

    public BlurpParticle surfaceThickness(double thickness) {
        this.surfaceThickness = thickness;
        return this;
    }

    public BlurpParticle axis(Axis axis) {
        this.axis = Objects.requireNonNull(axis, "axis");
        return this;
    }

    public BlurpParticle rotate(double yawDegrees, double pitchDegrees, double rollDegrees) {
        this.yawRad = Math.toRadians(yawDegrees);
        this.pitchRad = Math.toRadians(pitchDegrees);
        this.rollRad = Math.toRadians(rollDegrees);
        return this;
    }

    public BlurpParticle density(double pointsPerBlock) {
        if (pointsPerBlock <= 0) {
            throw new IllegalArgumentException("pointsPerBlock must be > 0");
        }
        this.step = 1.0 / pointsPerBlock;
        return this;
    }

    public BlurpParticle points(int points) {
        if (points <= 0) {
            throw new IllegalArgumentException("points must be > 0");
        }
        this.circlePoints = points;
        return this;
    }

    public BlurpParticle single() {
        this.shapeType = ShapeType.SINGLE;
        return this;
    }

    public BlurpParticle sphere(double radius, double step) {
        this.shapeType = ShapeType.SPHERE;
        this.radius = radius;
        this.step = step;
        return this;
    }

    public BlurpParticle circle(double radius, double step) {
        this.shapeType = ShapeType.CIRCLE;
        this.radius = radius;
        this.step = step;
        return this;
    }

    public BlurpParticle cylinder(double radius, double height, double step) {
        this.shapeType = ShapeType.CYLINDER;
        this.radius = radius;
        this.height = height;
        this.step = step;
        return this;
    }

    public BlurpParticle cuboid(double sizeX, double sizeY, double sizeZ, double step) {
        this.shapeType = ShapeType.CUBOID;
        this.size = new Vector(sizeX, sizeY, sizeZ);
        this.step = step;
        return this;
    }

    public BlurpParticle pyramid(double baseSize, double height, double step) {
        this.shapeType = ShapeType.PYRAMID;
        this.pyramidBase = baseSize;
        this.pyramidHeight = height;
        this.step = step;
        return this;
    }

    public void spawn(Location center) {
        Objects.requireNonNull(center, "center");
        World world = Objects.requireNonNull(center.getWorld(), "center.world");
        spawnInternal(world, null, center);
    }

    public void spawn(Player player, Location center) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(center, "center");
        World world = Objects.requireNonNull(center.getWorld(), "center.world");
        spawnInternal(world, player, center);
    }

    public void spawn(Collection<? extends Player> players, Location center) {
        Objects.requireNonNull(players, "players");
        Objects.requireNonNull(center, "center");
        World world = Objects.requireNonNull(center.getWorld(), "center.world");
        for (Player player : players) {
            if (player == null) {
                continue;
            }
            spawnInternal(world, player, center);
        }
    }

    private void spawnInternal(World world, Player player, Location center) {
        if (step <= 0) {
            throw new IllegalArgumentException("step must be > 0");
        }

        switch (shapeType) {
            case SINGLE -> spawnParticle(world, player, center, dataForProgress(0));
            case CUBOID -> spawnCuboid(world, player, center);
            case PYRAMID -> spawnPyramid(world, player, center);
            case SPHERE -> spawnSphere(world, player, center);
            case CIRCLE -> spawnCircle(world, player, center);
            case CYLINDER -> spawnCylinder(world, player, center);
        }
    }

    private Object dataForProgress(double t) {
        if (data != null) {
            return data;
        }

        if (particle == Particle.DUST) {
            Color c = colorForProgress(t);
            if (c == null) {
                return null;
            }
            return new Particle.DustOptions(c, dustSize);
        }

        if (particle == Particle.DUST_COLOR_TRANSITION) {
            Color from = gradientFrom != null ? gradientFrom : (gradientColors != null && !gradientColors.isEmpty() ? gradientColors.get(0) : color);
            if (from == null) {
                return null;
            }
            Color to = transitionTo != null
                    ? transitionTo
                    : (gradientTo != null ? gradientTo : (gradientColors != null && !gradientColors.isEmpty() ? gradientColors.get(gradientColors.size() - 1) : null));
            if (to == null) {
                return null;
            }
            return new Particle.DustTransition(from, to, dustSize);
        }

        return null;
    }

    private Color colorForProgress(double t) {
        t = clamp01(t);
        if (colors != null && !colors.isEmpty()) {
            if (colorMode == ColorMode.RANDOM_PER_POINT) {
                return colors.get(ThreadLocalRandom.current().nextInt(colors.size()));
            }
        }

        return switch (colorMode) {
            case SINGLE -> color;
            case RANDOM_PER_POINT -> {
                if (colors != null && !colors.isEmpty()) {
                    yield colors.get(ThreadLocalRandom.current().nextInt(colors.size()));
                }
                yield color;
            }
            case GRADIENT -> {
                if (gradientColors != null && gradientColors.size() >= 2) {
                    yield gradientColor(gradientColors, t);
                }

                Color from = gradientFrom != null ? gradientFrom : color;
                Color to = gradientTo;
                if (from == null || to == null) {
                    yield color;
                }
                yield lerpColor(from, to, t);
            }
            case RAINBOW -> hsvToColor(t);
        };
    }

    private static Color gradientColor(List<Color> colors, double t) {
        if (colors == null || colors.size() < 2) {
            return null;
        }

        t = clamp01(t);
        int segments = colors.size() - 1;
        if (t >= 1.0) {
            return colors.get(colors.size() - 1);
        }

        double scaled = t * segments;
        int idx = (int) Math.floor(scaled);
        double localT = scaled - idx;

        Color a = colors.get(Math.max(0, Math.min(idx, colors.size() - 2)));
        Color b = colors.get(Math.max(1, Math.min(idx + 1, colors.size() - 1)));
        return lerpColor(a, b, localT);
    }

    private static double clamp01(double v) {
        if (v < 0) {
            return 0;
        }
        if (v > 1) {
            return 1;
        }
        return v;
    }

    private static Color lerpColor(Color a, Color b, double t) {
        int r = (int) Math.round(a.getRed() + (b.getRed() - a.getRed()) * t);
        int g = (int) Math.round(a.getGreen() + (b.getGreen() - a.getGreen()) * t);
        int bl = (int) Math.round(a.getBlue() + (b.getBlue() - a.getBlue()) * t);
        return Color.fromRGB(clamp255(r), clamp255(g), clamp255(bl));
    }

    private static int clamp255(int v) {
        return Math.max(0, Math.min(255, v));
    }

    private static Color hsvToColor(double t) {
        double h = (t % 1.0) * 6.0;
        int i = (int) Math.floor(h);
        double f = h - i;
        double q = 1.0 - f;

        double r;
        double g;
        double b;
        switch (i) {
            case 0 -> {
                r = 1;
                g = f;
                b = 0;
            }
            case 1 -> {
                r = q;
                g = 1;
                b = 0;
            }
            case 2 -> {
                r = 0;
                g = 1;
                b = f;
            }
            case 3 -> {
                r = 0;
                g = q;
                b = 1;
            }
            case 4 -> {
                r = f;
                g = 0;
                b = 1;
            }
            default -> {
                r = 1;
                g = 0;
                b = q;
            }
        }

        return Color.fromRGB((int) Math.round(r * 255.0), (int) Math.round(g * 255.0), (int) Math.round(b * 255.0));
    }

    private void spawnParticle(World world, Player player, Location loc, Object data) {
        if (player != null) {
            if (data != null) {
                player.spawnParticle(particle, loc, count, offsetX, offsetY, offsetZ, speed, data);
            } else {
                player.spawnParticle(particle, loc, count, offsetX, offsetY, offsetZ, speed);
            }
            return;
        }

        if (data != null) {
            world.spawnParticle(particle, loc, count, offsetX, offsetY, offsetZ, speed, data, force);
        } else {
            world.spawnParticle(particle, loc, count, offsetX, offsetY, offsetZ, speed, null, force);
        }
    }

    private void spawnCuboid(World world, Player player, Location center) {
        double hx = size.getX() / 2.0;
        double hy = size.getY() / 2.0;
        double hz = size.getZ() / 2.0;

        double thickness = surfaceThickness > 0 ? surfaceThickness : step;
        double eps = Math.max(thickness, step) / 2.0;

        double total = 0;
        if (hx > 0) {
            total += (2.0 * hx) / step;
        }
        if (hy > 0) {
            total += (2.0 * hy) / step;
        }
        if (hz > 0) {
            total += (2.0 * hz) / step;
        }
        total = Math.max(1.0, total);

        double index = 0;

        for (double x = -hx; x <= hx; x += step) {
            for (double y = -hy; y <= hy; y += step) {
                for (double z = -hz; z <= hz; z += step) {
                    if (!filled) {
                        boolean onSurface = Math.abs(x) >= (hx - eps) || Math.abs(y) >= (hy - eps) || Math.abs(z) >= (hz - eps);
                        if (!onSurface) {
                            continue;
                        }
                    }

                    Vector offset = rotateOffset(new Vector(x, y, z));
                    Location loc = center.clone().add(offset);
                    spawnParticle(world, player, loc, dataForProgress(index / total));
                    index++;
                }
            }
        }
    }

    private void spawnSphere(World world, Player player, Location center) {
        if (radius <= 0) {
            throw new IllegalArgumentException("radius must be > 0");
        }

        double thickness = surfaceThickness > 0 ? surfaceThickness : step;
        double inner = Math.max(0, radius - thickness);
        double inner2 = inner * inner;
        double r2 = radius * radius;

        double total = Math.max(1.0, (2.0 * radius / step) * (2.0 * radius / step) * (2.0 * radius / step));
        double index = 0;
        for (double x = -radius; x <= radius; x += step) {
            for (double y = -radius; y <= radius; y += step) {
                for (double z = -radius; z <= radius; z += step) {
                    double d2 = (x * x) + (y * y) + (z * z);
                    if (d2 <= r2) {
                        if (!filled && d2 < inner2) {
                            continue;
                        }
                        Vector offset = rotateOffset(new Vector(x, y, z));
                        Location loc = center.clone().add(offset);
                        spawnParticle(world, player, loc, dataForProgress(index / total));
                        index++;
                    }
                }
            }
        }
    }

    private void spawnCircle(World world, Player player, Location center) {
        if (radius <= 0) {
            throw new IllegalArgumentException("radius must be > 0");
        }

        int points;
        if (circlePoints != null) {
            points = circlePoints;
        } else {
            double circumference = 2.0 * Math.PI * radius;
            points = Math.max(1, (int) Math.ceil(circumference / step));
        }

        for (int i = 0; i < points; i++) {
            double a = (2.0 * Math.PI * i) / points;

            Vector base = switch (axis) {
                case Y -> new Vector(Math.cos(a) * radius, 0, Math.sin(a) * radius);
                case X -> new Vector(0, Math.cos(a) * radius, Math.sin(a) * radius);
                case Z -> new Vector(Math.cos(a) * radius, Math.sin(a) * radius, 0);
            };

            Vector offset = rotateOffset(base);
            Location loc = center.clone().add(offset);
            spawnParticle(world, player, loc, dataForProgress((double) i / (double) Math.max(1, points - 1)));
        }
    }

    private void spawnCylinder(World world, Player player, Location center) {
        if (radius <= 0) {
            throw new IllegalArgumentException("radius must be > 0");
        }
        if (height <= 0) {
            throw new IllegalArgumentException("height must be > 0");
        }

        double circumference = 2.0 * Math.PI * radius;
        int points = Math.max(1, (int) Math.ceil(circumference / step));
        int layers = Math.max(1, (int) Math.ceil(height / step));

        double thickness = surfaceThickness > 0 ? surfaceThickness : step;
        double inner = Math.max(0, radius - thickness);
        double inner2 = inner * inner;

        double total = Math.max(1.0, (double) (layers + 1) * (double) points);
        double index = 0;

        for (int y = 0; y <= layers; y++) {
            double yy = (y * height) / layers;
            for (int i = 0; i < points; i++) {
                double a = (2.0 * Math.PI * i) / points;

                Vector circle = switch (axis) {
                    case Y -> new Vector(Math.cos(a) * radius, 0, Math.sin(a) * radius);
                    case X -> new Vector(0, Math.cos(a) * radius, Math.sin(a) * radius);
                    case Z -> new Vector(Math.cos(a) * radius, Math.sin(a) * radius, 0);
                };

                Vector along = switch (axis) {
                    case Y -> new Vector(0, yy, 0);
                    case X -> new Vector(yy, 0, 0);
                    case Z -> new Vector(0, 0, yy);
                };

                Vector base = circle.clone().add(along);
                if (!filled) {
                    double radial2 = circle.getX() * circle.getX() + circle.getY() * circle.getY() + circle.getZ() * circle.getZ();
                    if (radial2 < inner2) {
                        continue;
                    }
                }

                Vector offset = rotateOffset(base);
                Location loc = center.clone().add(offset);
                spawnParticle(world, player, loc, dataForProgress(index / total));
                index++;
            }
        }
    }

    private void spawnPyramid(World world, Player player, Location center) {
        if (pyramidBase <= 0) {
            throw new IllegalArgumentException("baseSize must be > 0");
        }
        if (pyramidHeight <= 0) {
            throw new IllegalArgumentException("height must be > 0");
        }

        double thickness = surfaceThickness > 0 ? surfaceThickness : step;
        double eps = Math.max(thickness, step) / 2.0;

        int layers = Math.max(1, (int) Math.ceil(pyramidHeight / step));
        for (int layer = 0; layer <= layers; layer++) {
            double t = (double) layer / (double) layers;
            double y = t * pyramidHeight;
            double layerSize = pyramidBase * (1.0 - t);
            double half = layerSize / 2.0;

            for (double x = -half; x <= half; x += step) {
                for (double z = -half; z <= half; z += step) {
                    if (!filled) {
                        boolean onSurface = Math.abs(x) >= (half - eps) || Math.abs(z) >= (half - eps);
                        if (!onSurface) {
                            continue;
                        }
                    }

                    Vector offset = rotateOffset(new Vector(x, y, z));
                    Location loc = center.clone().add(offset);
                    spawnParticle(world, player, loc, dataForProgress(t));
                }
            }
        }
    }

    private Vector rotateOffset(Vector v) {
        double x = v.getX();
        double y = v.getY();
        double z = v.getZ();

        if (rollRad != 0) {
            double c = Math.cos(rollRad);
            double s = Math.sin(rollRad);
            double nx = x * c - y * s;
            double ny = x * s + y * c;
            x = nx;
            y = ny;
        }

        if (pitchRad != 0) {
            double c = Math.cos(pitchRad);
            double s = Math.sin(pitchRad);
            double ny = y * c - z * s;
            double nz = y * s + z * c;
            y = ny;
            z = nz;
        }

        if (yawRad != 0) {
            double c = Math.cos(yawRad);
            double s = Math.sin(yawRad);
            double nx = x * c + z * s;
            double nz = -x * s + z * c;
            x = nx;
            z = nz;
        }

        return new Vector(x, y, z);
    }
}
