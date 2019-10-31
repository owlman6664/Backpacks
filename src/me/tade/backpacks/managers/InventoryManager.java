package me.tade.backpacks.managers;

import me.tade.backpacks.Backpacks;
import me.tade.backpacks.packs.Backpack;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.List;

public class InventoryManager implements Listener {

    private Backpacks plugin;

    public InventoryManager(Backpacks plugin) {
        this.plugin = plugin;

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onInteractBackpack(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (event.getItem() == null)
            return;

        if (!plugin.isBackpack(event.getItem()))
            return;

        Backpack backpack = plugin.getBackpack(player, plugin.getConfigBackpackName(event.getItem()));
        if (backpack == null)
            return;

        event.setCancelled(true);
        player.openInventory(plugin.getBackpackInventories().get(backpack));
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        List<Backpack> backpacks = plugin.getPlayerBackpacks().get(player.getName());

        if (backpacks == null)
            return;

        for (Backpack backpack : backpacks)
            plugin.saveBackpack(player, backpack);
    }
}
