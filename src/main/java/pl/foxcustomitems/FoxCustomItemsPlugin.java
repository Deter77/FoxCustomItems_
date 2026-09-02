package pl.foxcustomitems;

import org.bukkit.plugin.java.JavaPlugin;
import pl.foxcustomitems.item.ItemManager;
import pl.foxcustomitems.listener.CustomItemListener;
import pl.foxcustomitems.util.Cooldowns;

public final class FoxCustomItemsPlugin extends JavaPlugin {
    private ItemManager itemManager;
    private Cooldowns cooldowns;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        cooldowns = new Cooldowns(this);
        itemManager = new ItemManager(this);
        itemManager.registerDefaults();
        getServer().getPluginManager().registerEvents(new CustomItemListener(this), this);
        getServer().getOnlinePlayers().forEach(itemManager::notifyArmorUpdate);
    }

    @Override
    public void onDisable() {
        if (itemManager != null) {
            itemManager.all().forEach(item -> getServer().getOnlinePlayers().forEach(item::onQuit));
        }
        if (cooldowns != null) {
            cooldowns.clearAll();
        }
        getServer().getScheduler().cancelTasks(this);
    }

    public ItemManager itemManager() {
        return itemManager;
    }

    public Cooldowns cooldowns() {
        return cooldowns;
    }
}
