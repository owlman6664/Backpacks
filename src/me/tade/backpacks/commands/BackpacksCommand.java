package me.tade.backpacks.commands;

import me.tade.backpacks.Backpacks;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class BackpacksCommand implements CommandExecutor {

    private Backpacks plugin;

    public BackpacksCommand(Backpacks plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!cmd.getName().equalsIgnoreCase("backpack"))
            return false;
        if (args.length == 0) {
            sender.sendMessage(" ");
            sender.sendMessage("§e§lBackpack §aPlugin by §cThe_TadeSK");
            sender.sendMessage("§e§lBackpack §aVersion §c" + plugin.getDescription().getVersion() + " §ahttps://www.spigotmc.org/resources/17192/");
        } else if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("backpack.reload"))
                return false;

            sender.sendMessage("§e§lBackpack §aReloading..");
            plugin.saveConfig();
            plugin.reloadConfig();
            plugin.reloadBackpacks();
            sender.sendMessage("§e§lBackpack §aReloaded succesfully");
        }
        return true;
    }
}
