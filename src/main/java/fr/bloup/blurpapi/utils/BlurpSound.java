package fr.bloup.blurpapi.utils;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class BlurpSound {
    private Sound sound;
    private float volume = 1.0f;
    private float pitch = 1.0f;
    private float radius = 16.0f;
    private float fadeRadius = 1.0f;
    private SoundCategory category = SoundCategory.MASTER;
    private int delayTicks = 0;

    private final Set<UUID> targetPlayers = new HashSet<>();
    private final Set<UUID> excludedPlayers = new HashSet<>();

    public BlurpSound sound(Sound sound) {
        this.sound = sound;
        return this;
    }

    public BlurpSound volume(float  volume) {
        this.volume = volume;
        return this;
    }

    public BlurpSound pitch(float  pitch) {
        this.pitch = pitch;
        return this;
    }

    public BlurpSound radius(float radius) {
        this.radius = radius;
        return this;
    }

    public BlurpSound fadeMultiplier(float multiplier) {
        this.fadeRadius = multiplier;
        return this;
    }

    public BlurpSound category(SoundCategory category) {
        this.category = category;
        return this;
    }

    public BlurpSound delay(int ticks) {
        this.delayTicks = ticks;
        return this;
    }

    public BlurpSound target(Player... players) {
        for (Player p : players) {
            targetPlayers.add(p.getUniqueId());
        }
        return this;
    }

    public BlurpSound exclude(Player... players) {
        for (Player p : players) {
            excludedPlayers.add(p.getUniqueId());
        }
        return this;
    }

    public BlurpSound play(Location location) {
        Runnable playTask = () -> {
            for (Player player : location.getWorld().getPlayers()) {
                if (!targetPlayers.isEmpty() && !targetPlayers.contains(player.getUniqueId())) continue;
                if (excludedPlayers.contains(player.getUniqueId())) continue;

                double distance = player.getLocation().distance(location);
                if (distance > radius) continue;

                float volumeFactor = 1.0f;
                if (radius > 0) {
                    float distanceFactor = Math.min(1f, (float) distance / radius);
                    volumeFactor = 1.0f - distanceFactor * fadeRadius;
                }

                player.playSound(location, sound, category, volume * volumeFactor, pitch);
            }
        };

        if (delayTicks > 0) {
            new BlurpScheduler().after(delayTicks).run(playTask);
        } else {
            playTask.run();
        }

        return this;
    }

    public BlurpSound play(Player player) {
        if (sound != null && player != null) {
            if (excludedPlayers.contains(player.getUniqueId())) return this;
            if (!targetPlayers.isEmpty() && !targetPlayers.contains(player.getUniqueId())) return this;

            Runnable playTask = () -> player.playSound(player.getLocation(), sound, category, volume, pitch);

            if (delayTicks > 0) {
                new BlurpScheduler().after(delayTicks).run(playTask);
            } else {
                playTask.run();
            }
        }

        return this;
    }
}
