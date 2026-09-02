package pl.foxcustomitems.listener;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import pl.foxcustomitems.FoxCustomItemsPlugin;
import pl.foxcustomitems.item.CustomItem;

public final class CustomItemListener implements Listener {
    private final FoxCustomItemsPlugin plugin;

    public CustomItemListener(FoxCustomItemsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        for (CustomItem item : plugin.itemManager().all()) {
            if (item.isEquippedBy(player)) {
                item.onDamage(player, event);
            }
        }
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (player.isOnline()) {
                for (CustomItem item : plugin.itemManager().all()) {
                    if (item.isEquippedBy(player)) {
                        item.afterDamage(player, event);
                    }
                }
            }
        });
    }

    @EventHandler(ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        for (CustomItem item : plugin.itemManager().all()) {
            if (item.isEquippedBy(player)) {
                item.onInteract(player, event);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        if (event.getTo() == null) {
            return;
        }
        if (event.getFrom().getX() == event.getTo().getX() && event.getFrom().getZ() == event.getTo().getZ()) {
            return;
        }
        Player player = event.getPlayer();
        for (CustomItem item : plugin.itemManager().all()) {
            item.onMove(player);
        }
    }

    @EventHandler
    public void onGlide(EntityToggleGlideEvent event) {
        if (event.getEntity() instanceof Player player) {
            scheduleArmorUpdate(player);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player player) {
            scheduleArmorUpdate(player);
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getWhoClicked() instanceof Player player) {
            scheduleArmorUpdate(player);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player player) {
            scheduleArmorUpdate(player);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        scheduleArmorUpdate(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        for (CustomItem item : plugin.itemManager().all()) {
            item.onQuit(player);
        }
    }

    private void scheduleArmorUpdate(Player player) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (player.isOnline()) {
                plugin.itemManager().notifyArmorUpdate(player);
            }
        });
    }
}
