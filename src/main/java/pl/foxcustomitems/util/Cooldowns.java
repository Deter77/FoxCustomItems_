package pl.foxcustomitems.util;

import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.entity.Player;
import pl.foxcustomitems.FoxCustomItemsPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.BooleanSupplier;

public final class Cooldowns {
    private final FoxCustomItemsPlugin plugin;
    private final Map<String, Map<UUID, Long>> endTimes = new HashMap<>();
    private final Map<String, PerPlayerBossBar> bars = new HashMap<>();

    public Cooldowns(FoxCustomItemsPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean isReady(String key, Player player) {
        return remainingMillis(key, player) <= 0L;
    }

    public long remainingMillis(String key, Player player) {
        return Math.max(0L, endTimes.getOrDefault(key, Map.of()).getOrDefault(player.getUniqueId(), 0L) - System.currentTimeMillis());
    }

    public int remainingSeconds(String key, Player player) {
        return (int) Math.ceil(remainingMillis(key, player) / 1000.0);
    }

    public void start(String key, Player player, int seconds) {
        endTimes.computeIfAbsent(key, ignored -> new HashMap<>()).put(player.getUniqueId(), System.currentTimeMillis() + seconds * 1000L);
    }

    public void syncBar(String key, Player player, String itemDisplayName, int totalSeconds, BooleanSupplier stillEquipped) {
        long remaining = remainingMillis(key, player);
        if (remaining <= 0L) {
            bar(key).clear(player);
            return;
        }
        bar(key).showCooldown(player, itemDisplayName, System.currentTimeMillis() + remaining, totalSeconds, stillEquipped, () -> showReady(key, player, itemDisplayName, stillEquipped));
    }

    public void showReady(String key, Player player, String itemDisplayName, BooleanSupplier stillEquipped) {
        bar(key).showStatic(player, itemDisplayName + " &7- &aGotowa", BarColor.GREEN, BarStyle.SOLID, 100, stillEquipped);
    }

    public void clearBar(String key, Player player) {
        bar(key).clear(player);
    }

    public void clearAll() {
        bars.values().forEach(PerPlayerBossBar::clearAll);
        bars.clear();
    }

    private PerPlayerBossBar bar(String key) {
        return bars.computeIfAbsent(key, ignored -> new PerPlayerBossBar(plugin));
    }
}
