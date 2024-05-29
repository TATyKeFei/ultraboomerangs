package me.abisgamer.ultraboomerangs.utils;

import me.abisgamer.ultraboomerangs.UltraBoomerangs;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Set;

public class configUpdater {
    public static void updateConfig() {
        UltraBoomerangs plugin = UltraBoomerangs.plugin;
        FileConfiguration config = plugin.getConfig();

        // Add armorstand rotation config if it doesn't exist
        if (!config.getKeys(false).contains("armorstand")) {
            ConfigurationSection armorstandSection = config.createSection("armorstand");
            armorstandSection.set("x", 0);
            armorstandSection.set("y", 120);
            armorstandSection.set("z", 0);
            UltraBoomerangs.plugin.saveConfig();
            UltraBoomerangs.plugin.reloadConfig();
            plugin.getLogger().info("Updated config to add Armorstand Arm Rotation section.");
        }

        // Add sounds section for each boomerang if it doesn't exist
        ConfigurationSection boomerangSection = config.getConfigurationSection("boomerangs");
        if (boomerangSection != null) {
            Set<String> keys = boomerangSection.getKeys(false); // Get only direct children
            for (String key : keys) {
                ConfigurationSection currentSection = boomerangSection.getConfigurationSection(key);
                if (currentSection != null) {
                    if (!currentSection.isConfigurationSection("sounds")) {
                        ConfigurationSection soundsSection = currentSection.createSection("sounds");
                        soundsSection.set("enabled", true);
                        soundsSection.set("throw-sound", "ENTITY_EXPERIENCE_BOTTLE_THROW");
                        soundsSection.set("receive-sound", "ENTITY_EXPERIENCE_BOTTLE_THROW");
                        soundsSection.set("volume", 0.4);
                        soundsSection.set("pitch", 0.4);
                        UltraBoomerangs.plugin.saveConfig();
                        UltraBoomerangs.plugin.reloadConfig();
                        plugin.getLogger().info("Updated config to add sounds");
                    }
                }
            }
        }

        // Add listener priority config if it doesn't exist
        if (!config.getKeys(false).contains("listener")) {
            ConfigurationSection listenerSection = config.createSection("listener");
            listenerSection.set("priority", "NORMAL");
            plugin.getLogger().info("Updated config to add listener priority section.");
        }

        // Add update-old-boomerangs config if it doesn't exist
        if (!config.getKeys(false).contains("update-old-boomerangs")) {
            config.set("update-old-boomerangs", true); // Default to true
            UltraBoomerangs.plugin.saveConfig();
            UltraBoomerangs.plugin.reloadConfig();
            plugin.getLogger().info("Updated config to add update-old-boomerangs option.");
        }

    }
}
