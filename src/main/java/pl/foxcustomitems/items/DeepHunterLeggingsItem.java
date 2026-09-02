package pl.foxcustomitems.items;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import pl.foxcustomitems.FoxCustomItemsPlugin;
import pl.foxcustomitems.item.CustomItem;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class DeepHunterLeggingsItem extends CustomItem {
    private static final int DEFAULT_MAX_AIR = 300;
    private final Map<UUID, BukkitTask> tasks = new HashMap<>();
    private double swimSpeedMultiplier;
    private double maxAirMultiplier;
    private double swimBoostStrength;
    private double maxSwimSpeedPerTick;

    public DeepHunterLeggingsItem(FoxCustomItemsPlugin plugin) {
        super(plugin, "deep_hunter_leggings");
    }

    @Override
    protected void onReload(ConfigurationSection section) {
        swimSpeedMultiplier = section.getDouble("swim-speed-multiplier", 1.30);
        maxAirMultiplier = section.getDouble("max-air-multiplier", 2.00);
        swimBoostStrength = section.getDouble("swim-boost-strength", Math.max(0.0, swimSpeedMultiplier - 1.0) * 0.08);
        maxSwimSpeedPerTick = section.getDouble("max-swim-speed-blocks-per-second", 5.07) / 20.0;
    }

    @Override
    public void onArmorUpdate(Player player) {
        if (isEquippedBy(player)) {
            player.setMaximumAir((int) Math.round(DEFAULT_MAX_AIR * maxAirMultiplier));
            start(player);
        } else {
            stop(player);
        }
    }

    @Override
    public void onInteract(Player player, PlayerInteractEvent event) {
        boostIfSwimming(player);
    }

    @Override
    public void onMove(Player player) {
        boostIfSwimming(player);
    }

    @Override
    public void onQuit(Player player) {
        stop(player);
    }

    private void start(Player player) {
        if (tasks.containsKey(player.getUniqueId())) {
            return;
        }
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline() || !isEquippedBy(player)) {
                    stop(player);
                    return;
                }
                boostIfSwimming(player);
            }
        }.runTaskTimer(plugin, 1L, 1L);
        tasks.put(player.getUniqueId(), task);
    }

    private void stop(Player player) {
        BukkitTask task = tasks.remove(player.getUniqueId());
        if (task != null) {
            task.cancel();
        }
        player.setMaximumAir(DEFAULT_MAX_AIR);
    }

    private void boostIfSwimming(Player player) {
        if (!isEquippedBy(player) || !isInWater(player) || !isTryingToSwimForward(player)) {
            return;
        }
        Vector direction = player.getLocation().getDirection();
        if (direction.lengthSquared() == 0.0) {
            return;
        }
        Vector boosted = player.getVelocity().add(direction.normalize().multiply(swimBoostStrength));
        if (boosted.length() > maxSwimSpeedPerTick) {
            boosted.normalize().multiply(maxSwimSpeedPerTick);
        }
        player.setVelocity(boosted);
    }

    private boolean isInWater(Player player) {
        return player.getLocation().getBlock().getType() == Material.WATER || player.isSwimming();
    }

    private boolean isTryingToSwimForward(Player player) {
        try {
            Object input = player.getClass().getMethod("getCurrentInput").invoke(player);
            Object forward = input.getClass().getMethod("isForward").invoke(input);
            return forward instanceof Boolean pressed && pressed;
        } catch (ReflectiveOperationException exception) {
            Vector direction = player.getLocation().getDirection();
            return player.isSwimming() && player.getVelocity().dot(direction) > 0.02;
        }
    }
}
