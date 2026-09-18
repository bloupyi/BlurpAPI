package fr.bloup.blurpapi.utils;

import fr.bloup.blurpapi.BlurpAPI;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class BlurpScheduler {
    private int afterTicks = 0;
    private int repeatTimes = 0;
    private int period = 1;

    private boolean periodExplicit = false;
    private boolean async = false;
    private Runnable onComplete = null;

    private final AtomicBoolean completed = new AtomicBoolean(false);

    private BukkitRunnable runnable = null;

    /** Delai avant la premiere execution, en ticks. */
    public BlurpScheduler after(int ticks) {
        afterTicks = Math.max(0, ticks);
        return this;
    }

    /** Nombre d'executions. Zero ou moins signifie "sans limite". */
    public BlurpScheduler repeat(int times) {
        this.repeatTimes = times;
        return this;
    }

    /** Intervalle entre deux executions, en ticks. Rend la tache repetitive. */
    public BlurpScheduler period(int ticks) {
        this.period = Math.max(1, ticks);
        this.periodExplicit = true;
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
        boolean repeating = repeatTimes > 0 || periodExplicit;

        if (!repeating) {
            runnable = new BukkitRunnable() {
                @Override
                public void run() {
                    task.accept(BlurpScheduler.this);
                    completeOnce();
                }
            };
            schedule(false);
            return this;
        }

        if (repeatTimes <= 0) {
            runnable = new BukkitRunnable() {
                @Override
                public void run() {
                    task.accept(BlurpScheduler.this);
                }
            };
            schedule(true);
            return this;
        }

        runnable = new BukkitRunnable() {
            int counter = 0;

            @Override
            public void run() {
                if (counter++ >= repeatTimes) {
                    cancel();
                    completeOnce();
                    return;
                }
                task.accept(BlurpScheduler.this);
            }
        };
        schedule(true);
        return this;
    }

    private void schedule(boolean timer) {
        if (timer) {
            if (async) {
                runnable.runTaskTimerAsynchronously(BlurpAPI.getPlugin(), afterTicks, period);
            } else {
                runnable.runTaskTimer(BlurpAPI.getPlugin(), afterTicks, period);
            }
            return;
        }

        if (async) {
            runnable.runTaskLaterAsynchronously(BlurpAPI.getPlugin(), afterTicks);
        } else {
            runnable.runTaskLater(BlurpAPI.getPlugin(), afterTicks);
        }
    }

    public void cancel() {
        if (runnable != null && !runnable.isCancelled()) {
            runnable.cancel();
        }
        completeOnce();
    }

    private void completeOnce() {
        if (onComplete == null) return;
        if (!completed.compareAndSet(false, true)) return;

        if (async) {
            Bukkit.getScheduler().runTaskAsynchronously(BlurpAPI.getPlugin(), onComplete);
        } else {
            Bukkit.getScheduler().runTask(BlurpAPI.getPlugin(), onComplete);
        }
    }
}
