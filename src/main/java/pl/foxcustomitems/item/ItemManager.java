package pl.foxcustomitems.item;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import pl.foxcustomitems.FoxCustomItemsPlugin;
import pl.foxcustomitems.items.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public final class ItemManager {
    private final FoxCustomItemsPlugin plugin;
    private final Map<ItemKey, CustomItem> items = new HashMap<>();

    public ItemManager(FoxCustomItemsPlugin plugin) {
        this.plugin = plugin;
    }

    public void registerDefaults() {
        register(new DragonElytraItem(plugin));
        register(new LastChanceChestplateItem(plugin));
        register(new RunicGuardianCrownItem(plugin));
        register(new VigilantStoneHelmetItem(plugin));
        register(new SilentBastionChestplateItem(plugin));
        register(new DeepHunterLeggingsItem(plugin));
        register(new DesertWandererBootsItem(plugin));
    }

    public void register(CustomItem item) {
        items.put(item.key(), item);
    }

    public void reloadAll() {
        items.clear();
        registerDefaults();
    }

    public Collection<CustomItem> all() {
        return items.values();
    }

    public CustomItem find(ItemStack stack) {
        ItemKey key = keyOf(stack);
        return key == null ? null : items.get(key);
    }

    public boolean matches(ItemStack stack, ItemKey key) {
        ItemKey actual = keyOf(stack);
        return key.equals(actual);
    }

    public ItemKey keyOf(ItemStack stack) {
        if (stack == null || stack.getType() == Material.AIR || !stack.hasItemMeta()) {
            return null;
        }
        ItemMeta meta = stack.getItemMeta();
        if (meta == null || !meta.hasCustomModelData()) {
            return null;
        }
        return new ItemKey(stack.getType(), meta.getCustomModelData());
    }

    public void notifyArmorUpdate(Player player) {
        for (CustomItem item : items.values()) {
            item.onArmorUpdate(player);
        }
    }
}
