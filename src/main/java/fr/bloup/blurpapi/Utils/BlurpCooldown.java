package fr.bloup.blurpapi.Utils;

public class BlurpCooldown {
    private int cooldownTicks = 0;
    private Runnable onStart = null;
    private Runnable onComplete = null;

    public BlurpCooldown cooldown(int ticks) {
        this.cooldownTicks = ticks;
        return this;
    }

    public BlurpCooldown onStart(Runnable onStart) {
        this.onStart = onStart;
        return this;
    }

    public BlurpCooldown onComplete(Runnable onComplete) {
        this.onComplete = onComplete;
        return this;
    }

    public void start() {
        if (onStart != null) {
            onStart.run();
        }
        if (onComplete != null) {
            new BlurpScheduler().after(cooldownTicks).run(() -> onComplete.run());
        }
    }
}
