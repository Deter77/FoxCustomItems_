package pl.foxcustomitems.item;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import pl.foxcustomitems.FoxCustomItemsPlugin;

public abstract class CustomItem {
    protected final FoxCustomItemsPlugin plugin;
    protected final String configPath;
    protected ItemKey key;

    protected CustomItem(FoxCustomItemsPlugin plugin, String configPath) {
        this.plugin = plugin;
        this.configPath = configPath;
        reload();
    }

    public final void reload() {
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("items." + configPath);
        if (section == null) {
            throw new IllegalStateException("Missing config section: items." + configPath);
        }
        Material material = Material.matchMaterial(section.getString("material", "STONE"));
        if (material == null) {
            throw new IllegalStateException("Invalid material in items." + configPath);
        }
        key = new ItemKey(material, section.getInt("custom-model-data"));
        onReload(section);
    }

    protected void onReload(ConfigurationSection section) {
    }

    public ItemKey key() {
        return key;
    }

    public String configPath() {
        return configPath;
    }

    public boolean matches(ItemStack item) {
        return plugin.itemManager().matches(item, key);
    }

    public boolean isEquippedBy(Player player) {
        return switch (equipmentSlot()) {
            case HEAD -> matches(player.getInventory().getHelmet());
            case CHEST -> matches(player.getInventory().getChestplate());
            case LEGS -> matches(player.getInventory().getLeggings());
            case FEET -> matches(player.getInventory().getBoots());
            case HAND, OFF_HAND -> matches(player.getInventory().getItemInMainHand()) || matches(player.getInventory().getItemInOffHand());
            default -> false;
        };
    }

    public EquipmentSlot equipmentSlot() {
        String name = key.material().name();
        if (name.endsWith("_HELMET") || name.endsWith("_SKULL") || name.endsWith("_HEAD")) {
            return EquipmentSlot.HEAD;
        }
        if (name.endsWith("_CHESTPLATE") || name.equals("ELYTRA")) {
            return EquipmentSlot.CHEST;
        }
        if (name.endsWith("_LEGGINGS")) {
            return EquipmentSlot.LEGS;
        }
        if (name.endsWith("_BOOTS")) {
            return EquipmentSlot.FEET;
        }
        return EquipmentSlot.HAND;
    }

    public void onDamage(Player player, EntityDamageEvent event) {
    }

    public void afterDamage(Player player, EntityDamageEvent event) {
    }

    public void onInteract(Player player, PlayerInteractEvent event) {
    }

    public void onArmorUpdate(Player player) {
    }

    public void onMove(Player player) {
    }

    public void onQuit(Player player) {
    }
}
