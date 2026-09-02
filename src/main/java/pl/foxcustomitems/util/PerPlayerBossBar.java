package pl.foxcustomitems.util;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import pl.foxcustomitems.FoxCustomItemsPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.BooleanSupplier;
import java.util.function.IntConsumer;

public final class PerPlayerBossBar {
    private final FoxCustomItemsPlugin plugin;
    private final Map<UUID, BossBar> bars = new HashMap<>();
    private final Map<UUID, BukkitTask> tasks = new HashMap<>();

    public PerPlayerBossBar(FoxCustomItemsPlugin plugin) {
        this.plugin = plugin;
    }

    public void showTimed(Player player, String title, BarColor color, BarStyle style, long durationTicks, BooleanSupplier keepVisible) {
        clear(player);
        BossBar bar = Bukkit.createBossBar(Text.color(title), color, style);
        bar.setProgress(1.0);
        bar.addPlayer(player);
        bars.put(player.getUniqueId(), bar);
        BukkitTask task = new BukkitRunnable() {
            private long elapsed;

            @Override
            public void run() {
                if (!player.isOnline() || !keepVisible.getAsBoolean()) {
                    clear(player);
                    return;
                }
                elapsed++;
                double progress = Math.max(0.0, 1.0 - (elapsed / (double) durationTicks));
                bar.setProgress(progress);
                if (elapsed >= durationTicks) {
                    clear(player);
                }
            }
        }.runTaskTimer(plugin, 1L, 1L);
        tasks.put(player.getUniqueId(), task);
    }

    public void showStatic(Player player, String title, BarColor color, BarStyle style, int ticks, BooleanSupplier keepVisible) {
        showTimed(player, title, color, style, Math.max(1, ticks), keepVisible);
    }

    public void showCooldown(Player player, String itemName, long endMillis, int totalSeconds, BooleanSupplier keepVisible, Runnable onFinished) {
        clear(player);
        BossBar bar = Bukkit.createBossBar("", BarColor.WHITE, BarStyle.SOLID);
        bar.addPlayer(player);
        bars.put(player.getUniqueId(), bar);
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline() || !keepVisible.getAsBoolean()) {
                    clear(player);
                    return;
                }
                long remainingMillis = Math.max(0L, endMillis - System.currentTimeMillis());
                int seconds = (int) Math.ceil(remainingMillis / 1000.0);
                if (seconds <= 0) {
                    clear(player);
                    onFinished.run();
                    return;
                }
                bar.setTitle(Text.color(itemName + " &7- &bCooldown: " + seconds + "s"));
                bar.setProgress(Math.max(0.0, Math.min(1.0, remainingMillis / (double) (totalSeconds * 1000L))));
            }
        }.runTaskTimer(plugin, 0L, 20L);
        tasks.put(player.getUniqueId(), task);
    }

    public void showManaged(Player player, BossBar bar, BukkitTask task) {
        clear(player);
        bars.put(player.getUniqueId(), bar);
        tasks.put(player.getUniqueId(), task);
    }

    public void clear(Player player) {
        UUID id = player.getUniqueId();
        BukkitTask task = tasks.remove(id);
        if (task != null) {
            task.cancel();
        }
        BossBar bar = bars.remove(id);
        if (bar != null) {
            bar.removeAll();
        }
    }

    public void clearAll() {
        for (BukkitTask task : tasks.values()) {
            task.cancel();
        }
        tasks.clear();
        for (BossBar bar : bars.values()) {
            bar.removeAll();
        }
        bars.clear();
    }
}
