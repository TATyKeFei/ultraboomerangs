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
                if (!currentSection.getKeys(false).contains("sounds")) {
                    currentSection.createSection("sounds");
                    currentSection.set("sounds.enabled", true);
                    currentSection.set("sounds.throw-sound", "ENTITY_EXPERIENCE_BOTTLE_THROW");
                    currentSection.set("sounds.recieve-sound", "ENTITY_EXPERIENCE_BOTTLE_THROW");
                    currentSection.set("sounds.volume", 0.4);
                    currentSection.set("sounds.pitch", 0.4);
                }
                if (!currentSection.getKeys(false).contains("mcmmo_skill")) {
                    currentSection.createSection("mcmmo_skill");
                    currentSection.set("mcmmo_skill", "Archery");
                    UltraBoomerangs.plugin.getLogger().info("Updated config to add mcmmo_skill section");
                }
                if (!currentSection.getKeys(false).contains("mcmmo_skill_amount")) {
                    currentSection.createSection("mcmmo_skill_amount");
                    currentSection.set("mcmmo_skill_amount", "0");
                    UltraBoomerangs.plugin.getLogger().info("Updated config to add mcmmo_skill_amount section");
                }
                if (!currentSection.getKeys(false).contains("auraskills_skill")) {
                    currentSection.createSection("auraskills_skill");
                    currentSection.set("auraskills_skill", "Archery");
                    UltraBoomerangs.plugin.getLogger().info("Updated config to add auraskills_skill section");
                }
                if (!currentSection.getKeys(false).contains("auraskills_skill_amount")) {
                    currentSection.createSection("auraskills_skill_amount");
                    currentSection.set("auraskills_skill_amount", "10");
                    UltraBoomerangs.plugin.getLogger().info("Updated config to add auraskills_skill_amount section");
                }

            }
        }
        if (!config.getKeys(false).contains("listener")) {
            config.createSection("listener");
            config.set("listener.priority", "NORMAL");
            UltraBoomerangs.plugin.getLogger().info("Updated config to add listener priority section");
        }

        // Add update-old-boomerangs config if it doesn't exist
        if (!config.getKeys(false).contains("update-old-boomerangs")) {
            config.set("update-old-boomerangs", true); // Default to true
            UltraBoomerangs.plugin.getLogger().info("Updated config to add update-old-boomerangs option.");
        }
            UltraBoomerangs.plugin.saveConfig();

    }
}
