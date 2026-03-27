package de.zyvera.tictactoe.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.lang.reflect.Method;

public final class SchedulerUtil {

    private static boolean foliaDetected = false;
    private static boolean checked = false;

    private SchedulerUtil() {}
    public static boolean isFolia() {
        if (!checked) {
            checked = true;
            try {
                Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
                foliaDetected = true;
            } catch (ClassNotFoundException e) {
                foliaDetected = false;
            }
        }
        return foliaDetected;
    }

    public static void runSync(Plugin plugin, Runnable task) {
        if (isFolia()) {
            try {
                Object globalScheduler = Bukkit.class.getMethod("getGlobalRegionScheduler").invoke(null);
                Method runMethod = globalScheduler.getClass().getMethod("run", Plugin.class, java.util.function.Consumer.class);
                runMethod.invoke(globalScheduler, plugin, (java.util.function.Consumer<Object>) (t) -> task.run());
            } catch (Exception e) {
                // Fallback
                task.run();
            }
        } else {
            Bukkit.getScheduler().runTask(plugin, task);
        }
    }

    public static void runLater(Plugin plugin, Runnable task, long delayTicks) {
        if (isFolia()) {
            try {
                Object globalScheduler = Bukkit.class.getMethod("getGlobalRegionScheduler").invoke(null);
                Method runDelayed = globalScheduler.getClass().getMethod("runDelayed",
                        Plugin.class, java.util.function.Consumer.class, long.class);
                runDelayed.invoke(globalScheduler, plugin, (java.util.function.Consumer<Object>) (t) -> task.run(), delayTicks);
            } catch (Exception e) {
                Bukkit.getScheduler().runTaskLater(plugin, task, delayTicks);
            }
        } else {
            Bukkit.getScheduler().runTaskLater(plugin, task, delayTicks);
        }
    }

    public static CancellableTask runTimer(Plugin plugin, Runnable task, long delayTicks, long periodTicks) {
        if (isFolia()) {
            try {
                Object globalScheduler = Bukkit.class.getMethod("getGlobalRegionScheduler").invoke(null);
                Method runAtFixedRate = globalScheduler.getClass().getMethod("runAtFixedRate",
                        Plugin.class, java.util.function.Consumer.class, long.class, long.class);
                Object scheduledTask = runAtFixedRate.invoke(globalScheduler, plugin,
                        (java.util.function.Consumer<Object>) (t) -> task.run(),
                        Math.max(1, delayTicks), periodTicks);
                return new FoliaCancellableTask(scheduledTask);
            } catch (Exception e) {
                BukkitTask bt = Bukkit.getScheduler().runTaskTimer(plugin, task, delayTicks, periodTicks);
                return new BukkitCancellableTask(bt);
            }
        } else {
            BukkitTask bt = Bukkit.getScheduler().runTaskTimer(plugin, task, delayTicks, periodTicks);
            return new BukkitCancellableTask(bt);
        }
    }

    /**
     * Führt eine Aufgabe asynchron aus.
     */
    public static void runAsync(Plugin plugin, Runnable task) {
        if (isFolia()) {
            try {
                Object asyncScheduler = Bukkit.class.getMethod("getAsyncScheduler").invoke(null);
                Method runNow = asyncScheduler.getClass().getMethod("runNow",
                        Plugin.class, java.util.function.Consumer.class);
                runNow.invoke(asyncScheduler, plugin, (java.util.function.Consumer<Object>) (t) -> task.run());
            } catch (Exception e) {
                Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
            }
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
        }
    }

    /**
     * Führt eine Aufgabe für eine Entity auf dem richtigen Thread aus (Folia).
     */
    public static void runForEntity(Plugin plugin, Entity entity, Runnable task) {
        if (isFolia()) {
            try {
                Method getScheduler = entity.getClass().getMethod("getScheduler");
                Object entityScheduler = getScheduler.invoke(entity);
                Method run = entityScheduler.getClass().getMethod("run",
                        Plugin.class, java.util.function.Consumer.class, Runnable.class);
                run.invoke(entityScheduler, plugin, (java.util.function.Consumer<Object>) (t) -> task.run(), null);
            } catch (Exception e) {
                Bukkit.getScheduler().runTask(plugin, task);
            }
        } else {
            Bukkit.getScheduler().runTask(plugin, task);
        }
    }

    // --- Cancellable Task Abstraktion ---

    public interface CancellableTask {
        void cancel();
    }

    private static class BukkitCancellableTask implements CancellableTask {
        private final BukkitTask task;
        BukkitCancellableTask(BukkitTask task) { this.task = task; }
        @Override
        public void cancel() { task.cancel(); }
    }

    private static class FoliaCancellableTask implements CancellableTask {
        private final Object scheduledTask;
        FoliaCancellableTask(Object scheduledTask) { this.scheduledTask = scheduledTask; }
        @Override
        public void cancel() {
            try {
                scheduledTask.getClass().getMethod("cancel").invoke(scheduledTask);
            } catch (Exception ignored) {}
        }
    }
}
