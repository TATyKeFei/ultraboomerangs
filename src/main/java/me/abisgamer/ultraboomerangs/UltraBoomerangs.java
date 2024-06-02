package me.abisgamer.ultraboomerangs;

import me.abisgamer.ultraboomerangs.commands.mainCommand;
import me.abisgamer.ultraboomerangs.listeners.mcMMOListener;
import me.abisgamer.ultraboomerangs.listeners.throwListener;
import me.abisgamer.ultraboomerangs.listeners.auraSkillsListener;
import me.abisgamer.ultraboomerangs.utils.configUpdater;
import me.abisgamer.ultraboomerangs.utils.itemBuilder;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventPriority;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public final class UltraBoomerangs extends JavaPlugin {

    public static UltraBoomerangs plugin;
    public FileConfiguration messages;

    public static boolean isMcMMO = false;
    public static boolean isAuraSkills = false;
    private throwListener throwListenerInstance; // Store the throwListener instance


    @Override
    public void onEnable() {
        plugin = this;
        plugin.getLogger().info("---------------------");
        plugin.getLogger().info("UltraBoomerangs");
        plugin.getLogger().info("By - AbisGamer");
        plugin.getLogger().info("---------------------");
        getConfig().options().copyDefaults();
        saveDefaultConfig();
        reloadConfig();
        configUpdater.updateConfig();
        reloadConfig();
        itemBuilder.createBoomerangs();
        this.getCommand("ultraboomerangs").setExecutor(new mainCommand());

        // Set plugin presence flags
        if (getServer().getPluginManager().getPlugin("mcMMO") != null) {
            plugin.getLogger().info("mcMMO detected, Enabled support for mcMMO.");
            isMcMMO = true;
        }

        if (getServer().getPluginManager().getPlugin("AuraSkills") != null) {
            plugin.getLogger().info("AuraSkills detected, Enabled support for AuraSkills.");
            isAuraSkills = true;
        }

        // Register all listeners
        registerAllListeners();

        // Load messages file
        File f = new File(getDataFolder() + File.separator + "messages.yml");
        if (!f.exists()) {
            createMessagesFile();
        }
        FileConfiguration fmessages = YamlConfiguration.loadConfiguration(f);
        messages = fmessages;
    }


    public void registerAllListeners() {
        // Create throwListener with correct flags
        throwListenerInstance = new throwListener(plugin.getConfig(), plugin.getConfig().getBoolean("update-old-boomerangs", false), isMcMMO, isAuraSkills);

        // Register listeners with priority from the config
        registerListenersWithPriority(throwListenerInstance);
        registerUpdateListenersWithPriority(throwListenerInstance);

        // Register mcMMOListener if mcMMO is present
        if (isMcMMO) {
            getServer().getPluginManager().registerEvents(new mcMMOListener(this, throwListenerInstance), this);
        }

        // Register auraSkillsListener if AuraSkills is present
        if (isAuraSkills) {
            getServer().getPluginManager().registerEvents(new auraSkillsListener(this, throwListenerInstance), this);
        }
    }

    public void registerAllUpdateListeners() {
        // Create throwListener with correct flags
        throwListenerInstance = new throwListener(plugin.getConfig(), plugin.getConfig().getBoolean("update-old-boomerangs", false), isMcMMO, isAuraSkills);

        registerUpdateListenersWithPriority(throwListenerInstance);
    }

    private void registerUpdateListenersWithPriority(throwListener listener) {
        String priorityName = plugin.getConfig().getString("listener.priority", "NORMAL").toUpperCase();
        EventPriority priority;
        try {
            priority = EventPriority.valueOf(priorityName);
        } catch (IllegalArgumentException e) {
            getLogger().warning("Invalid priority '" + priorityName + "' in config.yml. Using NORMAL priority.");
            priority = EventPriority.NORMAL;
        }

        PluginManager pluginManager = getServer().getPluginManager();
        EventExecutor executor = (listener1, event) -> {
            if (event instanceof org.bukkit.event.player.PlayerInteractEvent) {
                listener.onPlayerInteract((org.bukkit.event.player.PlayerInteractEvent) event);
            }
            if (event instanceof org.bukkit.event.inventory.InventoryClickEvent) { // Register InventoryClickEvent
                listener.onInventoryClick((org.bukkit.event.inventory.InventoryClickEvent) event);
            }

        };

        pluginManager.registerEvent(org.bukkit.event.inventory.InventoryClickEvent.class, listener, priority, executor, this); // Add InventoryClickEvent registration
        pluginManager.registerEvent(org.bukkit.event.player.PlayerInteractEvent.class, listener, priority, executor, this); // Add InventoryClickEvent registration

    }

    private void registerListenersWithPriority(throwListener listener) {
        String priorityName = plugin.getConfig().getString("listener.priority", "NORMAL").toUpperCase();
        EventPriority priority;
        try {
            priority = EventPriority.valueOf(priorityName);
        } catch (IllegalArgumentException e) {
            getLogger().warning("Invalid priority '" + priorityName + "' in config.yml. Using NORMAL priority.");
            priority = EventPriority.NORMAL;
        }

        PluginManager pluginManager = getServer().getPluginManager();
        EventExecutor executor = (listener1, event) -> {
            if (event instanceof org.bukkit.event.player.PlayerInteractEvent) {
                listener.onInteract((org.bukkit.event.player.PlayerInteractEvent) event);
            }
            if (event instanceof org.bukkit.event.player.PlayerDropItemEvent) {
                listener.onPlayerDropItem((org.bukkit.event.player.PlayerDropItemEvent) event);
            }
            if (event instanceof org.bukkit.event.player.PlayerQuitEvent) {
                listener.onPlayerQuit((org.bukkit.event.player.PlayerQuitEvent) event);
            }
            if (event instanceof org.bukkit.event.server.PluginDisableEvent) {
                listener.onPluginDisable((org.bukkit.event.server.PluginDisableEvent) event);
            }
            if (event instanceof org.bukkit.event.entity.EntityDamageByEntityEvent) {
                listener.onEntityDamageByEntity((org.bukkit.event.entity.EntityDamageByEntityEvent) event);
            }
            if (event instanceof org.bukkit.event.entity.EntityDeathEvent) {
                listener.onEntityDeath((org.bukkit.event.entity.EntityDeathEvent) event);
            }


        };

        pluginManager.registerEvent(org.bukkit.event.player.PlayerInteractEvent.class, listener, priority, executor, this);
        pluginManager.registerEvent(org.bukkit.event.player.PlayerDropItemEvent.class, listener, priority, executor, this);
        pluginManager.registerEvent(org.bukkit.event.player.PlayerQuitEvent.class, listener, priority, executor, this);
        pluginManager.registerEvent(org.bukkit.event.server.PluginDisableEvent.class, listener, priority, executor, this);
        pluginManager.registerEvent(org.bukkit.event.entity.EntityDamageByEntityEvent.class, listener, priority, executor, this);
        pluginManager.registerEvent(org.bukkit.event.entity.EntityDeathEvent.class, listener, priority, executor, this);

    }
    @Override
    public void onDisable() {
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
}
