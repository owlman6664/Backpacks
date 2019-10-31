package me.tade.backpacks.managers;

import me.tade.backpacks.Backpacks;
import me.tade.backpacks.packs.Backpack;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class LoadingManager implements Listener {

    private Backpacks plugin;

    public LoadingManager(Backpacks plugin) {
        this.plugin = plugin;

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onJoinServer(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        for (String configName : plugin.getConfigPacks().keySet()) {
            Backpack backpack = plugin.loadBackpack(player, configName);
            if (backpack != null) {
                if (plugin.getPlayerBackpacks().get(player.getName()) != null)
                    plugin.getPlayerBackpacks().get(player.getName()).add(backpack);
                else
                    plugin.getPlayerBackpacks().put(player.getName(), new ArrayList<>(Collections.singletonList(backpack)));
            }
        }

        if (!plugin.getPluginUpdater().needUpdate())
            return;

        if (player.isOp() || player.hasPermission("backpack.update,info")) {
            plugin.sendUpdateMessage();
        }
    }

    @EventHandler
    public void onQuitServer(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        List<Backpack> backpacks = plugin.getPlayerBackpacks().get(player);

        if (backpacks == null)
            return;

        for (Backpack backpack : backpacks)
            plugin.saveBackpack(player, backpack);
    }
}
