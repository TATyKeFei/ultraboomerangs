package me.abisgamer.ultraboomerangs.utils;

import me.abisgamer.ultraboomerangs.UltraBoomerangs;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Set;

public class configUpdater {
    public static void updateConfig() {
        ConfigurationSection config = UltraBoomerangs.plugin.getConfig();
        //Add rotation config
        if (!config.getKeys(false).contains("armorstand")) {
            config.createSection("armorstand");
            config.set("armorstand.x", 0);
            config.set("armorstand.y", 120);
            config.set("armorstand.z", 0);
            UltraBoomerangs.plugin.saveConfig();
            UltraBoomerangs.plugin.reloadConfig();
            UltraBoomerangs.plugin.getLogger().info("Updated config to add Armorstand Arm Rotation section.");
        }

        ConfigurationSection boomerangSection = config.getConfigurationSection("boomerangs");
        if (boomerangSection != null) {
            Set<String> keys = boomerangSection.getKeys(false); // Get only direct children
            for (String key : keys) {
                ConfigurationSection currentSection = boomerangSection.getConfigurationSection(key);
                if (currentSection != null && !currentSection.isConfigurationSection("sounds")) {
                    currentSection.createSection("sounds");
                    currentSection.set("sounds.enabled", true);
                    currentSection.set("sounds.throw-sound", "ENTITY_EXPERIENCE_BOTTLE_THROW");
                    currentSection.set("sounds.recieve-sound", "ENTITY_EXPERIENCE_BOTTLE_THROW");
                    currentSection.set("sounds.volume", 0.4);
                    currentSection.set("sounds.pitch", 0.4);
                }
            }
            UltraBoomerangs.plugin.saveConfig();
            UltraBoomerangs.plugin.reloadConfig();
            UltraBoomerangs.plugin.getLogger().info("Updated config to add sounds.");
        }
        if (!config.getKeys(false).contains("listener")) {
            config.createSection("listener");
            config.set("listener.priority", "NORMAL");
            UltraBoomerangs.plugin.saveConfig();
            UltraBoomerangs.plugin.reloadConfig();
            UltraBoomerangs.plugin.getLogger().info("Updated config to add listener priority section");
        }
    }

}