package pl.foxcustomitems.items;

import org.bukkit.Particle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import pl.foxcustomitems.FoxCustomItemsPlugin;
import pl.foxcustomitems.item.CustomItem;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class DragonElytraItem extends CustomItem {
    private final Map<UUID, BukkitTask> tasks = new HashMap<>();
    private double speedThreshold;
    private double pushStrength;
    private int particleCount;

    public DragonElytraItem(FoxCustomItemsPlugin plugin) {
        super(plugin, "dragon_elytra");
    }

    @Override
    protected void onReload(ConfigurationSection section) {
        speedThreshold = section.getDouble("speed-threshold", 2.1);
        pushStrength = section.getDouble("push-strength", 0.04);
        particleCount = section.getInt("particle-count", 10);
    }

    @Override
    public void onArmorUpdate(Player player) {
        if (player.isGliding() && isEquippedBy(player)) {
            start(player);
        } else {
            stop(player);
        }
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
                if (!player.isOnline() || !player.isGliding() || !isEquippedBy(player)) {
                    stop(player);
                    return;
                }
                if (player.getVelocity().length() < speedThreshold) {
                    Vector push = player.getLocation().getDirection().normalize().multiply(pushStrength);
                    player.setVelocity(player.getVelocity().add(push));
                    spawnWingTrails(player);
                }
            }
        }.runTaskTimer(plugin, 1L, 1L);
        tasks.put(player.getUniqueId(), task);
    }

    private void stop(Player player) {
        BukkitTask task = tasks.remove(player.getUniqueId());
        if (task != null) {
            task.cancel();
        }
    }

    private void spawnWingTrails(Player player) {
        Vector direction = player.getLocation().getDirection().normalize();
        Vector side = direction.clone().crossProduct(new Vector(0, 1, 0)).normalize().multiply(0.65);
        Vector back = direction.clone().multiply(-0.45);
        Vector base = player.getLocation().toVector().add(new Vector(0, 1.15, 0)).add(back);
        player.getWorld().spawnParticle(Particle.DRIPPING_OBSIDIAN_TEAR, base.clone().add(side).toLocation(player.getWorld()), particleCount / 2, 0.12, 0.18, 0.12, 0.01);
        player.getWorld().spawnParticle(Particle.DRIPPING_OBSIDIAN_TEAR, base.clone().subtract(side).toLocation(player.getWorld()), particleCount / 2, 0.12, 0.18, 0.12, 0.01);
    }
}
