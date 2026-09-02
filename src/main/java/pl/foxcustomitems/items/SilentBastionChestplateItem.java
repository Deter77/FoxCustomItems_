package pl.foxcustomitems.items;

import org.bukkit.Particle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import pl.foxcustomitems.FoxCustomItemsPlugin;
import pl.foxcustomitems.item.CustomItem;

public final class SilentBastionChestplateItem extends CustomItem {
    private double damageMultiplier;

    public SilentBastionChestplateItem(FoxCustomItemsPlugin plugin) {
        super(plugin, "silent_bastion_chestplate");
    }

    @Override
    protected void onReload(ConfigurationSection section) {
        damageMultiplier = section.getDouble("damage-multiplier", 0.93);
    }

    @Override
    public void onDamage(Player player, EntityDamageEvent event) {
        event.setDamage(event.getDamage() * damageMultiplier);
        player.getWorld().spawnParticle(Particle.SMOKE, player.getLocation().add(0, 1.2, 0), 4, 0.15, 0.1, 0.15, 0.001);
    }
}
