package pl.foxcustomitems.items;

import org.bukkit.Material;
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
import java.util.Set;
import java.util.UUID;

public final class DesertWandererBootsItem extends CustomItem {
    private static final Set<Material> SPEED_BLOCKS = Set.of(Material.SAND, Material.RED_SAND, Material.GRAVEL);
    private final Map<UUID, BukkitTask> tasks = new HashMap<>();
    private int speedAmplifier;
    private int checkIntervalTicks;
    private int speedDurationTicks;

    public DesertWandererBootsItem(FoxCustomItemsPlugin plugin) {
        super(plugin, "desert_wanderer_boots");
    }

    @Override
    protected void onReload(ConfigurationSection section) {
        speedAmplifier = section.getInt("speed-amplifier", 0);
        checkIntervalTicks = Math.max(1, section.getInt("check-interval-ticks", 1));
        speedDurationTicks = section.getInt("speed-duration-ticks", 80);
    }

    @Override
    public void onArmorUpdate(Player player) {
        if (isEquippedBy(player)) {
            start(player);
        } else {
            stop(player);
        }
    }

    @Override
    public void onMove(Player player) {
        apply(player);
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
                apply(player);
            }
        }.runTaskTimer(plugin, 1L, checkIntervalTicks);
        tasks.put(player.getUniqueId(), task);
    }

    private void stop(Player player) {
        BukkitTask task = tasks.remove(player.getUniqueId());
        if (task != null) {
            task.cancel();
        }
        player.removePotionEffect(PotionEffectType.SPEED);
    }

    private void apply(Player player) {
        if (!isEquippedBy(player)) {
            return;
        }
        Material below = player.getLocation().subtract(0, 0.1, 0).getBlock().getType();
        if (SPEED_BLOCKS.contains(below)) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, speedDurationTicks, speedAmplifier, true, false, true));
        } else if (player.isOnGround()) {
            player.removePotionEffect(PotionEffectType.SPEED);
        }
    }
}
