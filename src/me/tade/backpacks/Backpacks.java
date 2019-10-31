package me.tade.backpacks;

import me.tade.backpacks.commands.BackpacksCommand;
import me.tade.backpacks.managers.CraftManager;
import me.tade.backpacks.managers.InventoryManager;
import me.tade.backpacks.managers.LoadingManager;
import me.tade.backpacks.packs.Backpack;
import me.tade.backpacks.packs.ConfigPack;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.*;
import java.util.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class Backpacks extends JavaPlugin {

    private HashMap<String, ConfigPack> configPacks = new HashMap<>();
    private HashMap<String, List<Backpack>> playerBackpacks = new HashMap<>();
    private HashMap<Backpack, Inventory> backpackInventories = new HashMap<>();
    private PluginUpdater pluginUpdater;

    @Override
    public void onEnable() {
        super.onEnable();

        getConfig().options().copyDefaults(true);
        saveConfig();

        new File(this.getDataFolder().getAbsolutePath() + "/saves").mkdir();

        new LoadingManager(this);
        new InventoryManager(this);
        new CraftManager(this);

        pluginUpdater = new PluginUpdater(this);

        getCommand("backpack").setExecutor(new BackpacksCommand(this));

        reloadBackpacks();

        getLogger().info("Loading bStats... https://bstats.org/plugin/bukkit/Backpack");
        Metrics mcs = new Metrics(this);
        mcs.addCustomChart(new Metrics.SingleLineChart("backpacks") {
            @Override
            public int getValue() {
                return getConfigPacks().size();
            }
        });

        for (Player player : Bukkit.getOnlinePlayers())
            for (String configName : getConfigPacks().keySet()) {
                Backpack backpack = loadBackpack(player, configName);
                if (backpack != null) {
                    if (getPlayerBackpacks().get(player.getName()) != null)
                        getPlayerBackpacks().get(player.getName()).add(backpack);
                    else
                        getPlayerBackpacks().put(player.getName(), new ArrayList<>(Collections.singletonList(backpack)));
                }
            }
    }

    @Override
    public void onDisable() {
        super.onDisable();

        for (Player player : Bukkit.getOnlinePlayers()) {
            List<Backpack> backpacks = getPlayerBackpacks().get(player.getName());
            if (backpacks != null)
                for (Backpack backpack : backpacks)
                    saveBackpack(player, backpack);
        }
    }

    public void reloadBackpacks() {
        configPacks.clear();

        for (String name : getConfig().getConfigurationSection("backpacks").getKeys(false)) {
            int size = getConfig().getInt("backpacks." + name + ".size");
            List<String> recipe = getConfig().getStringList("backpacks." + name + ".recipe");

            //Item
            Material material = Material.matchMaterial(getConfig().getString("backpacks." + name + ".item.material"));
            byte data = (byte) getConfig().getInt("backpacks." + name + ".item.data");
            int amount = getConfig().getInt("backpacks." + name + ".item.amount");
            String itemName = getConfig().getString("backpacks." + name + ".item.name");
            List<String> configLore = getConfig().getStringList("backpacks." + name + ".item.lore");
            List<String> lore = new ArrayList<>();
            for (String line : configLore) {
                lore.add(ChatColor.translateAlternateColorCodes('&', line));
            }

            ItemStack item = new ItemStack(material, amount, data);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', itemName));
            meta.setLore(lore);
            item.setItemMeta(meta);

            configPacks.put(name, new ConfigPack(name, size, recipe, item));
        }
    }

    public HashMap<String, ConfigPack> getConfigPacks() {
        return configPacks;
    }

    public HashMap<String, List<Backpack>> getPlayerBackpacks() {
        return playerBackpacks;
    }

    public HashMap<Backpack, Inventory> getBackpackInventories() {
        return backpackInventories;
    }

    public boolean isBackpack(ItemStack itemStack) {
        for (ConfigPack pack : getConfigPacks().values())
            if (pack.getItemStack().equals(itemStack))
                return true;
        return false;
    }

    public String getConfigBackpackName(ItemStack itemStack) {
        for (ConfigPack pack : getConfigPacks().values())
            if (pack.getItemStack().equals(itemStack))
                return pack.getName();
        return null;
    }

    public Backpack getBackpack(Player player, String configName) {
        if (getPlayerBackpacks().containsKey(player.getName())) {
            for (Backpack backpack : getPlayerBackpacks().get(player.getName()))
                if (backpack.getConfigName().equalsIgnoreCase(configName))
                    return backpack;
        }

        Backpack backpack = new Backpack(player.getName(), getConfigPacks().get(configName).getSize(), configName, new ArrayList<HashMap<Map<String, Object>, Map<String, Object>>>());
        if (getPlayerBackpacks().containsKey(player.getName()))
            getPlayerBackpacks().get(player.getName()).add(backpack);
        else
            getPlayerBackpacks().put(player.getName(), new ArrayList<>(Collections.singletonList(backpack)));

        getBackpackInventories().put(backpack, Bukkit.createInventory(player, getConfigPacks().get(configName).getSize(), getConfigPacks().get(configName).getItemStack().getItemMeta().getDisplayName()));
        return backpack;
    }

    public Backpack loadBackpack(Player player, String configName) {
        File dir = new File(getDataFolder(), "/saves/" + player.getUniqueId().toString() + "=" + configName + ".backpack");
        if (!dir.exists())
            return null;

        FileInputStream fis = null;
        try {
            fis = new FileInputStream(dir.getAbsolutePath());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        GZIPInputStream gs = null;
        ObjectInputStream ois = null;
        try {
            gs = new GZIPInputStream(fis);
            ois = new ObjectInputStream(gs);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Backpack backpack = null;
        try {
            backpack = (Backpack) ois.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        try {
            ois.close();
            gs.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Inventory inventory = Bukkit.createInventory(player, getConfigPacks().get(backpack.getConfigName()).getSize(), getConfigPacks().get(backpack.getConfigName()).getItemStack().getItemMeta().getDisplayName());
        inventory.setContents(deserializeItemStackList(backpack.getItems()));
        getBackpackInventories().put(backpack, inventory);

        return backpack;
    }

    public void saveBackpack(Player player, Backpack backpack) {
        File dir = new File(getDataFolder(), "/saves/" + player.getUniqueId().toString() + "=" + backpack.getConfigName() + ".backpack");
        if (!dir.exists()) {
            try {
                dir.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        backpack.setItems(serializeItemStackList(getBackpackInventories().get(backpack).getContents()));

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(dir.getAbsoluteFile());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        GZIPOutputStream gz = null;
        ObjectOutputStream oos = null;
        try {
            gz = new GZIPOutputStream(fos);
            oos = new
                    ObjectOutputStream(gz);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            oos.writeObject(backpack);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            oos.flush();
            oos.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public final List<HashMap<Map<String, Object>, Map<String, Object>>> serializeItemStackList(final ItemStack[] itemStackList) {
        final List<HashMap<Map<String, Object>, Map<String, Object>>> serializedItemStackList = new ArrayList<HashMap<Map<String, Object>, Map<String, Object>>>();

        for (ItemStack originalItemStack : itemStackList) {
            ItemStack itemStack = originalItemStack == null ? new ItemStack(Material.AIR) : originalItemStack.clone();
            Map<String, Object> serializedItemStack, serializedItemMeta;
            HashMap<Map<String, Object>, Map<String, Object>> serializedMap = new HashMap<Map<String, Object>, Map<String, Object>>();

            serializedItemMeta = (itemStack.hasItemMeta())
                    ? itemStack.getItemMeta().serialize()
                    : null;
            itemStack.setItemMeta(null);
            serializedItemStack = itemStack.serialize();

            serializedMap.put(serializedItemStack, serializedItemMeta);
            serializedItemStackList.add(serializedMap);
        }
        return serializedItemStackList;
    }

    public final ItemStack[] deserializeItemStackList(final List<HashMap<Map<String, Object>, Map<String, Object>>> serializedItemStackList) {
        final ItemStack[] itemStackList = new ItemStack[serializedItemStackList.size()];

        int i = 0;
        for (HashMap<Map<String, Object>, Map<String, Object>> serializedItemStackMap : serializedItemStackList) {
            Map.Entry<Map<String, Object>, Map<String, Object>> serializedItemStack = serializedItemStackMap.entrySet().iterator().next();

            ItemStack itemStack = ItemStack.deserialize(serializedItemStack.getKey());
            if (serializedItemStack.getValue() != null) {
                ItemMeta itemMeta = (ItemMeta) ConfigurationSerialization.deserializeObject(serializedItemStack.getValue(), ConfigurationSerialization.getClassByAlias("ItemMeta"));
                itemStack.setItemMeta(itemMeta);
            }

            itemStackList[i++] = itemStack;
        }
        return itemStackList;
    }

    public void sendUpdateMessage() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.isOp() || player.hasPermission("backpack.update,info")) {
                        player.sendMessage(" ");
                        player.sendMessage("§e§lBackpack §aA new update has come! Released on §e" + pluginUpdater.getUpdateInfo()[1]);
                        player.sendMessage("§e§lBackpack §aNew version number/your current version §e" + pluginUpdater.getUpdateInfo()[0] + "§7/§c" + getDescription().getVersion());
                        player.sendMessage("§e§lBackpack §aDownload update here: §ehttps://www.spigotmc.org/resources/17192/");
                    }
                }
            }
        }.runTaskLater(this, 20);
    }

    public PluginUpdater getPluginUpdater() {
        return pluginUpdater;
    }
}
