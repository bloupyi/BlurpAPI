package fr.bloup.blurpapi.utils;

import fr.bloup.blurpapi.BlurpAPI;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.function.Consumer;

public class BlurpScheduler {
    private int afterTicks = 0;
    private int repeatTimes = 0;
    private int period = 1;
    private boolean async = false;
    private Runnable onComplete = null;

    private BukkitRunnable runnable = null;

    public BlurpScheduler after(int ticks) {
        afterTicks = ticks;
        return this;
    }

    public BlurpScheduler repeat(int times) {
        this.repeatTimes = times;
        return this;
    }

    public BlurpScheduler period(int ticks) {
        this.period = ticks;
        return this;
    }

    public BlurpScheduler async() {
        this.async = true;
        return this;
    }

    public BlurpScheduler onComplete(Runnable onComplete) {
        this.onComplete = onComplete;
        return this;
    }

    public BlurpScheduler run(Runnable task) {
        return run(scheduler -> task.run());
    }

    public BlurpScheduler run(Consumer<BlurpScheduler> task) {
        if (repeatTimes <= 0) {
            if (period > 0) {
                runnable = new BukkitRunnable() {
                    @Override
                    public void run() {
                        task.accept(BlurpScheduler.this);
                    }
                };
                if (async) {
                    runnable.runTaskTimerAsynchronously(BlurpAPI.getPlugin(), afterTicks, period);
                } else {
                    runnable.runTaskTimer(BlurpAPI.getPlugin(), afterTicks, period);
                }
            } else {
                runnable = new BukkitRunnable() {
                    @Override
                    public void run() {
                        task.accept(BlurpScheduler.this);
                        if (onComplete != null) onComplete.run();
                    }
                };
                if (async) {
                    runnable.runTaskLaterAsynchronously(BlurpAPI.getPlugin(), afterTicks);
                } else {
                    runnable.runTaskLater(BlurpAPI.getPlugin(), afterTicks);
                }
            }
        } else if (period > 0) {
            runnable = new BukkitRunnable() {
                int counter = 0;
                @Override
                public void run() {
                    if (counter++ >= repeatTimes) {
                        cancel();
                        if (onComplete != null) onComplete.run();
                        return;
                    }
                    task.accept(BlurpScheduler.this);
                }
            };
            if (async) {
                runnable.runTaskTimerAsynchronously(BlurpAPI.getPlugin(), afterTicks, period);
            } else {
                runnable.runTaskTimer(BlurpAPI.getPlugin(), afterTicks, period);
            }
        } else {
            runnable = new BukkitRunnable() {
                @Override
                public void run() {
                    task.accept(BlurpScheduler.this);
                    if (onComplete != null) onComplete.run();
                }
            };
            if (async) {
                runnable.runTaskLaterAsynchronously(BlurpAPI.getPlugin(), afterTicks);
            } else {
                runnable.runTaskLater(BlurpAPI.getPlugin(), afterTicks);
            }
        }
        return this;
    }

    public void cancel() {
        if (runnable != null) {
            runnable.cancel();
        }
    }
}
