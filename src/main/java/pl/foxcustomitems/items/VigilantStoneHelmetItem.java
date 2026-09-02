package pl.foxcustomitems.items;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import pl.foxcustomitems.FoxCustomItemsPlugin;
import pl.foxcustomitems.item.CustomItem;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class VigilantStoneHelmetItem extends CustomItem {
    private final Map<UUID, BukkitTask> tasks = new HashMap<>();
    private final Map<UUID, Boolean> active = new HashMap<>();
    private int stillnessTicksRequired;
    private int particleIntervalTicks;

    public VigilantStoneHelmetItem(FoxCustomItemsPlugin plugin) {
        super(plugin, "vigilant_stone_helmet");
    }

    @Override
    protected void onReload(ConfigurationSection section) {
        stillnessTicksRequired = section.getInt("stillness-ticks-required", 30);
        particleIntervalTicks = section.getInt("particle-interval-ticks", 12);
    }

    @Override
    public void onArmorUpdate(Player player) {
        if (isEquippedBy(player)) {
            start(player);
        } else {
            stop(player, true);
        }
    }

    @Override
    public void onMove(Player player) {
        stop(player, true);
        if (isEquippedBy(player)) {
            start(player);
        }
    }

    @Override
    public void onQuit(Player player) {
        stop(player, true);
    }

    private void start(Player player) {
        if (tasks.containsKey(player.getUniqueId())) {
            return;
        }
        BukkitTask task = new BukkitRunnable() {
            private int stillTicks;
            private int particleTicks;

            @Override
            public void run() {
                if (!player.isOnline() || !isEquippedBy(player)) {
                    stop(player, true);
                    return;
                }
                if (++stillTicks >= stillnessTicksRequired) {
                    if (!active.getOrDefault(player.getUniqueId(), false)) {
                        active.put(player.getUniqueId(), true);
                        player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, PotionEffect.INFINITE_DURATION, 0, true, false, true));
                        player.playSound(player.getLocation(), Sound.BLOCK_STONE_PLACE, 1.0f, 1.0f);
                    }
                    if (++particleTicks >= particleIntervalTicks) {
                        particleTicks = 0;
                        player.getWorld().spawnParticle(Particle.SCRAPE, player.getLocation().add(0, 1.25, 0), 1, 0.35, 0.5, 0.35, 0.1);
                        player.getWorld().spawnParticle(Particle.SCRAPE, player.getLocation().add(0, 0.6, 0), 1, 0.35, 0.6, 0.35, 0.1);
                    }
                }
            }
        }.runTaskTimer(plugin, 1L, 1L);
        tasks.put(player.getUniqueId(), task);
    }

    private void stop(Player player, boolean removeEffect) {
        BukkitTask task = tasks.remove(player.getUniqueId());
        if (task != null) {
            task.cancel();
        }
        active.remove(player.getUniqueId());
        if (removeEffect) {
            player.removePotionEffect(PotionEffectType.NIGHT_VISION);
        }
    }
}
