package me.abisgamer.ultraboomerangs;

import me.abisgamer.ultraboomerangs.commands.giveCommand;
import me.abisgamer.ultraboomerangs.commands.helpCommand;
import me.abisgamer.ultraboomerangs.commands.mainCommand;
import me.abisgamer.ultraboomerangs.commands.reloadCommand;
import me.abisgamer.ultraboomerangs.listeners.throwListener;
import me.abisgamer.ultraboomerangs.utils.configUpdater;
import me.abisgamer.ultraboomerangs.utils.itemBuilder;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public final class UltraBoomerangs extends JavaPlugin {

    public static UltraBoomerangs plugin;
    public FileConfiguration messages;

    @Override
    public void onEnable() {
        plugin = this;
        plugin.getLogger().info("---------------------");
        plugin.getLogger().info("UltraBoomerangs");
        plugin.getLogger().info("By - AbisGamer");
        plugin.getLogger().info("---------------------");

        getConfig().options().copyDefaults();
        configUpdater.updateConfig();
        saveDefaultConfig();
        reloadConfig();
        itemBuilder.createBoomerangs();

        // Register commands
        this.getCommand("ultraboomerangs").setExecutor(new mainCommand());

        // Register listeners with priority from the config
        registerListenersWithPriority();

        // Setup custom messages file
        setupMessagesFile();
    }

    private void registerListenersWithPriority() {
        String priorityName = plugin.getConfig().getString("listener.priority", "NORMAL").toUpperCase();
        EventPriority priority;
        try {
            priority = EventPriority.valueOf(priorityName);
        } catch (IllegalArgumentException e) {
            getLogger().warning("Invalid priority '" + priorityName + "' in config.yml. Using NORMAL priority.");
            priority = EventPriority.NORMAL;
        }

        throwListener listener = new throwListener();
        getServer().getPluginManager().registerEvents(listener, this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public void reloadCustomConfig() {
        File f = new File(getDataFolder() + File.separator + "messages.yml");
        messages = YamlConfiguration.loadConfiguration(f);
    }

    public void createMessagesFile() {
        File f = new File(getDataFolder() + File.separator + "messages.yml");
        if (!f.exists()) {
            try {
                f.createNewFile();
                FileConfiguration messages = YamlConfiguration.loadConfiguration(f);
                messages.set("prefix", "&6[UltraBoomerangs] ");
                messages.set("received", "&aYou received the boomerang: ");
                messages.set("make-success", "&aSuccessfully created Boomerang: &6");
                messages.set("id-error", "&cYou must provide a Boomerang ID");
                messages.set("invalid", "&cThis boomerang does not exist!");
                messages.set("limit-error", "&cYou already have a boomerang in the air!");
                messages.set("reload", "&aSuccessfully reloaded config.yml & messages.yml");
                messages.set("cooldown", "&cThis boomerang is on cooldown: &a");
                messages.set("cooldown-2", " &aseconds!");
                messages.save(f);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void setupMessagesFile() {
        File f = new File(getDataFolder() + File.separator + "messages.yml");
        if (!f.exists()) {
            createMessagesFile();
        }
        messages = YamlConfiguration.loadConfiguration(f);
    }
}
