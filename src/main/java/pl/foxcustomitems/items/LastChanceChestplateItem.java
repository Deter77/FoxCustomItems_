package pl.foxcustomitems.items;

import org.bukkit.Particle;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import pl.foxcustomitems.FoxCustomItemsPlugin;
import pl.foxcustomitems.item.CustomItem;
import pl.foxcustomitems.util.PerPlayerBossBar;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class LastChanceChestplateItem extends CustomItem {
    private static final String COOLDOWN_KEY = "last_chance_chestplate";
    private static final String DISPLAY_NAME = "&x&F&F&0&0&0&0&lK&x&F&F&0&5&0&5&ll&x&F&F&0&A&0&A&la&x&F&F&0&F&0&F&lt&x&F&F&1&3&1&3&la &x&F&F&1&D&1&D&lO&x&F&F&2&2&2&2&ls&x&F&F&2&7&2&7&lt&x&F&F&2&C&2&C&la&x&F&F&3&1&3&1&lt&x&F&F&3&5&3&5&ln&x&F&F&3&A&3&A&li&x&F&F&3&F&3&F&le&x&F&F&4&4&4&4&lj &x&F&F&4&E&4&E&lS&x&F&F&5&3&5&3&lz&x&F&F&5&7&5&7&la&x&F&F&5&C&5&C&ln&x&F&F&6&1&6&1&ls&x&F&F&6&6&6&6&ly";

    private final PerPlayerBossBar activeBars;
    private final Map<UUID, BukkitTask> particleTasks = new HashMap<>();
    private final Map<UUID, BukkitTask> cooldownWatchers = new HashMap<>();
    private double triggerHealth;
    private int durationSeconds;
    private int amplifier;
    private int cooldownSeconds;
    private int heartParticles;

    public LastChanceChestplateItem(FoxCustomItemsPlugin plugin) {
        super(plugin, "last_chance_chestplate");
        activeBars = new PerPlayerBossBar(plugin);
    }

    @Override
    protected void onReload(ConfigurationSection section) {
        triggerHealth = section.getDouble("trigger-health", 6.0);
        durationSeconds = section.getInt("regeneration-duration-seconds", 5);
        amplifier = section.getInt("regeneration-amplifier", 3);
        cooldownSeconds = section.getInt("cooldown-seconds", 180);
        heartParticles = section.getInt("heart-particles-per-tick", 12);
    }

    @Override
    public void afterDamage(Player player, EntityDamageEvent event) {
        if (!event.isCancelled() && player.getHealth() < triggerHealth) {
            activateIfReady(player);
        }
    }

    @Override
    public void onArmorUpdate(Player player) {
        if (!isEquippedBy(player)) {
            plugin.cooldowns().clearBar(COOLDOWN_KEY, player);
            stopWatcher(player);
            return;
        }
        if (plugin.cooldowns().isReady(COOLDOWN_KEY, player)) {
            if (player.getHealth() < triggerHealth) {
                activateIfReady(player);
            }
        } else {
            plugin.cooldowns().syncBar(COOLDOWN_KEY, player, DISPLAY_NAME, cooldownSeconds, () -> player.isOnline() && isEquippedBy(player));
            startWatcher(player);
        }
    }

    @Override
    public void onQuit(Player player) {
        activeBars.clear(player);
        plugin.cooldowns().clearBar(COOLDOWN_KEY, player);
        stopParticles(player);
        stopWatcher(player);
    }

    private void activateIfReady(Player player) {
        if (!isEquippedBy(player) || !plugin.cooldowns().isReady(COOLDOWN_KEY, player)) {
            return;
        }
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, durationSeconds * 20, amplifier, true, true, true));
        activeBars.showTimed(player, "&4Ostatnia Szansa &7(Regeneracja IV)", BarColor.RED, BarStyle.SOLID, durationSeconds * 20L, () -> player.isOnline() && isEquippedBy(player));
        startParticles(player);
        plugin.cooldowns().start(COOLDOWN_KEY, player, cooldownSeconds);
        startWatcher(player);
        plugin.cooldowns().syncBar(COOLDOWN_KEY, player, DISPLAY_NAME, cooldownSeconds, () -> player.isOnline() && isEquippedBy(player));
    }

    private void startParticles(Player player) {
        stopParticles(player);
        BukkitTask task = new BukkitRunnable() {
            private int ticks;

            @Override
            public void run() {
                if (!player.isOnline() || ticks++ >= durationSeconds * 20 || !isEquippedBy(player)) {
                    stopParticles(player);
                    return;
                }
                player.getWorld().spawnParticle(Particle.HEART, player.getLocation().add(0, 1.15, 0), heartParticles, 0.45, 0.35, 0.45, 0.02);
            }
        }.runTaskTimer(plugin, 0L, 1L);
        particleTasks.put(player.getUniqueId(), task);
    }

    private void stopParticles(Player player) {
        BukkitTask task = particleTasks.remove(player.getUniqueId());
        if (task != null) {
            task.cancel();
        }
    }

    private void startWatcher(Player player) {
        if (cooldownWatchers.containsKey(player.getUniqueId())) {
            return;
        }
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline() || !isEquippedBy(player)) {
                    stopWatcher(player);
                    return;
                }
                if (plugin.cooldowns().isReady(COOLDOWN_KEY, player)) {
                    stopWatcher(player);
                    if (player.getHealth() < triggerHealth) {
                        activateIfReady(player);
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
        cooldownWatchers.put(player.getUniqueId(), task);
    }

    private void stopWatcher(Player player) {
        BukkitTask task = cooldownWatchers.remove(player.getUniqueId());
        if (task != null) {
            task.cancel();
        }
    }
}
