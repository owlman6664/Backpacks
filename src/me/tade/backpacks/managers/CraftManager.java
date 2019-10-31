package me.tade.backpacks.managers;

import me.tade.backpacks.Backpacks;
import me.tade.backpacks.packs.ConfigPack;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;

public class CraftManager implements Listener {

    private Backpacks plugin;

    public CraftManager(Backpacks plugin) {
        this.plugin = plugin;

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerCraftItem(CraftItemEvent event) {
        Player player = (Player) event.getWhoClicked();

        for (ConfigPack configPack : plugin.getConfigPacks().values()) {
            if (configPack.getItemStack().equals(event.getInventory().getResult())) {
                if (!player.hasPermission("backpack.craft." + configPack.getName())) {
                    event.setCancelled(true);
                    event.setResult(Event.Result.DENY);
                    player.sendMessage("§e§lBackpack §cNo permissions!");
                    return;
                }
            }
        }
    }
}
