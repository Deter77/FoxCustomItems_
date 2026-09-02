package pl.foxcustomitems.items;

import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Trident;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import pl.foxcustomitems.FoxCustomItemsPlugin;
import pl.foxcustomitems.item.CustomItem;
import pl.foxcustomitems.util.PerPlayerBossBar;
import pl.foxcustomitems.util.Text;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class RunicGuardianCrownItem extends CustomItem {
    private static final String COOLDOWN_KEY = "runic_guardian_crown";
    private static final String DISPLAY_NAME = "&x&F&F&B&9&1&F&lK&x&F&F&B&A&2&2&lo&x&F&F&B&B&2&5&lr&x&F&F&B&B&2&9&lo&x&F&F&B&C&2&C&ln&x&F&F&B&D&2&F&la &x&F&F&B&E&3&5&lR&x&F&F&B&F&3&9&lu&x&F&F&C&0&3&C&ln&x&F&F&C&1&3&F&li&x&F&F&C&1&4&2&lc&x&F&F&C&2&4&5&lz&x&F&F&C&3&4&9&ln&x&F&F&C&4&4&C&le&x&F&F&C&5&4&F&lg&x&F&F&C&5&5&2&lo &x&F&F&C&7&5&8&lS&x&F&F&C&8&5&C&lt&x&F&F&C&8&5&F&lr&x&F&F&C&9&6&2&la&x&F&F&C&A&6&5&lż&x&F&F&C&B&6&8&ln&x&F&F&C&B&6&C&li&x&F&F&C&C&6&F&lk&x&F&F&C&D&7&2&la";

    private final PerPlayerBossBar activeBars;
    private final Map<UUID, Integer> shields = new HashMap<>();
    private final Map<UUID, BukkitTask> particles = new HashMap<>();
    private double projectileMultiplier;
    private double triggerHealth;
    private int shieldUses;
    private int cooldownSeconds;

    public RunicGuardianCrownItem(FoxCustomItemsPlugin plugin) {
        super(plugin, "runic_guardian_crown");
        activeBars = new PerPlayerBossBar(plugin);
    }

    @Override
    protected void onReload(ConfigurationSection section) {
        projectileMultiplier = section.getDouble("projectile-damage-multiplier", 0.60);
        triggerHealth = section.getDouble("trigger-health", 6.0);
        shieldUses = section.getInt("shield-uses", 6);
        cooldownSeconds = section.getInt("cooldown-seconds", 120);
    }

    @Override
    public void onDamage(Player player, EntityDamageEvent event) {
        if (shields.containsKey(player.getUniqueId())) {
            absorb(player, event);
            return;
        }
        if (isRangedDamage(event)) {
            event.setDamage(event.getDamage() * projectileMultiplier);
        }
    }

    @Override
    public void afterDamage(Player player, EntityDamageEvent event) {
        if (!event.isCancelled() && player.getHealth() < triggerHealth && plugin.cooldowns().isReady(COOLDOWN_KEY, player)) {
            activate(player);
        }
    }

    @Override
    public void onArmorUpdate(Player player) {
        if (!isEquippedBy(player)) {
            removeShield(player, false);
            plugin.cooldowns().clearBar(COOLDOWN_KEY, player);
            return;
        }
        if (shields.containsKey(player.getUniqueId())) {
            showActiveBar(player);
        } else if (!plugin.cooldowns().isReady(COOLDOWN_KEY, player)) {
            plugin.cooldowns().syncBar(COOLDOWN_KEY, player, DISPLAY_NAME, cooldownSeconds, () -> player.isOnline() && isEquippedBy(player));
        }
    }

    @Override
    public void onQuit(Player player) {
        removeShield(player, false);
        plugin.cooldowns().clearBar(COOLDOWN_KEY, player);
    }

    private void activate(Player player) {
        if (!isEquippedBy(player) || shields.containsKey(player.getUniqueId())) {
            return;
        }
        shields.put(player.getUniqueId(), shieldUses);
        showActiveBar(player);
        startParticles(player);
    }

    private void absorb(Player player, EntityDamageEvent event) {
        event.setCancelled(true);
        player.playSound(player.getLocation(), Sound.ITEM_SHIELD_BLOCK, 1.0f, 1.0f);
        int remaining = shields.getOrDefault(player.getUniqueId(), 0) - 1;
        if (remaining <= 0) {
            removeShield(player, true);
            plugin.cooldowns().start(COOLDOWN_KEY, player, cooldownSeconds);
            if (isEquippedBy(player)) {
                plugin.cooldowns().syncBar(COOLDOWN_KEY, player, DISPLAY_NAME, cooldownSeconds, () -> player.isOnline() && isEquippedBy(player));
            }
        } else {
            shields.put(player.getUniqueId(), remaining);
            showActiveBar(player);
        }
    }

    private void removeShield(Player player, boolean broken) {
        if (shields.remove(player.getUniqueId()) != null && broken) {
            player.playSound(player.getLocation(), Sound.BLOCK_GLASS_BREAK, 1.0f, 1.0f);
        }
        activeBars.clear(player);
        stopParticles(player);
    }

    private void showActiveBar(Player player) {
        int remaining = shields.getOrDefault(player.getUniqueId(), shieldUses);
        BossBar bar = Bukkit.createBossBar(Text.color("&fTarcza &aAktywna &7- &fPozostało &b" + remaining + " &fużyć"), BarColor.BLUE, BarStyle.SEGMENTED_6);
        bar.setProgress(Math.max(0.0, Math.min(1.0, remaining / (double) shieldUses)));
        bar.addPlayer(player);
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline() || !isEquippedBy(player) || !shields.containsKey(player.getUniqueId())) {
                    removeShield(player, false);
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
        activeBars.showManaged(player, bar, task);
    }

    private void startParticles(Player player) {
        stopParticles(player);
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline() || !isEquippedBy(player) || !shields.containsKey(player.getUniqueId())) {
                    removeShield(player, false);
                    return;
                }
                var base = player.getLocation();
                player.getWorld().spawnParticle(Particle.GLOW_SQUID_INK, base, 1, 0.04, 0.4, 0.04, 0.000000000001);
                player.getWorld().spawnParticle(Particle.GLOW_SQUID_INK, base.clone().add(0, 1.25, 0), 1, 0.04, 0.4, 0.04, 0.000000000001);
                player.getWorld().spawnParticle(Particle.GLOW, base, 1, 0.001, 0.4, 0.001, 0.000000000001);
                player.getWorld().spawnParticle(Particle.GLOW, base.clone().add(0, 1.25, 0), 1, 0.001, 0.4, 0.001, 0.000000000001);
            }
        }.runTaskTimer(plugin, 0L, 1L);
        particles.put(player.getUniqueId(), task);
    }

    private void stopParticles(Player player) {
        BukkitTask task = particles.remove(player.getUniqueId());
        if (task != null) {
            task.cancel();
        }
    }

    private boolean isRangedDamage(EntityDamageEvent event) {
        if (!(event instanceof EntityDamageByEntityEvent byEntity)) {
            return false;
        }
        return byEntity.getDamager() instanceof Projectile || byEntity.getDamager() instanceof Trident || byEntity.getDamager() instanceof Firework;
    }
}
