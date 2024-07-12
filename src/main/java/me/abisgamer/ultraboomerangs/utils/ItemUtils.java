package me.abisgamer.ultraboomerangs.utils;

import me.abisgamer.ultraboomerangs.UltraBoomerangs;
import me.abisgamer.ultraboomerangs.handlers.BoomerangHandler;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static me.abisgamer.ultraboomerangs.UltraBoomerangs.plugin;

public class ItemUtils {
    public static boolean isBoomerang(ItemStack item, String key, ConfigurationSection config, boolean updateOldBoomerangs) {
        if (item == null) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta != null && meta.getPersistentDataContainer().has(new NamespacedKey(plugin, "boomerang_id"), PersistentDataType.STRING)) {
            boolean matches = key.equals(meta.getPersistentDataContainer().get(new NamespacedKey(plugin, "boomerang_id"), PersistentDataType.STRING));
            if (matches) {
                updateItemMeta(item, key, config);
            }
            return matches;
        }

        if (updateOldBoomerangs) {
            ItemStack boomerang = itemBuilder.boomerangs.get(key);
            if (item.isSimilar(boomerang)) {
                meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "boomerang_id"), PersistentDataType.STRING, key);
                item.setItemMeta(meta);
                updateItemMeta(item, key, config);
                return true;
            }
        }

        return false;
    }

    public static void updateItemMeta(ItemStack item, String key, ConfigurationSection config) {
        if (item == null || key == null) return;

        ConfigurationSection boomerangConfig = config.getConfigurationSection("boomerangs." + key);
        if (boomerangConfig == null) return;

        boolean isItemstack = boomerangConfig.getBoolean("is-itemstack");
        ItemStack configuredItemStack = boomerangConfig.getItemStack("itemstack");

        if (isItemstack && configuredItemStack != null) {
            // Use the entire item stack from the config
            item.setType(configuredItemStack.getType());
            item.setAmount(configuredItemStack.getAmount());
            ItemMeta meta = configuredItemStack.getItemMeta();
            if (meta != null) {
                meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "boomerang_id"), PersistentDataType.STRING, key);
                item.setItemMeta(meta);
            }
        } else {
            String material = boomerangConfig.getString("material");
            if (material != null) {
                item.setType(Material.getMaterial(material.toUpperCase()));
            }
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                // Update item meta based on configuration
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', boomerangConfig.getString("name")));

                List<String> lore = new ArrayList<>();
                for (String loreLine : boomerangConfig.getStringList("lore")) {
                    lore.add(ChatColor.translateAlternateColorCodes('&', loreLine));
                }
                meta.setLore(lore);

                if (boomerangConfig.getBoolean("enchanted", false)) {
                    meta.addEnchant(Enchantment.DURABILITY, 1, true); // Example enchantment
                } else {
                    meta.getEnchants().forEach((enchantment, level) -> meta.removeEnchant(enchantment));
                }

                if (boomerangConfig.contains("custom-model")) {
                    meta.setCustomModelData(boomerangConfig.getInt("custom-model"));
                }

                meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "boomerang_id"), PersistentDataType.STRING, key);
                item.setItemMeta(meta);
            }
        }
    }

    public static String getBoomerangKey(Player player, HashMap<Player, HashMap<String, ArrayList<ItemStack>>> playerBoomer) {
        if (playerBoomer.containsKey(player)) {
            for (String key : playerBoomer.get(player).keySet()) {
                return key;
            }
        }
        return null;
    }
    public String getBoomerangKeyWithPlayer(Player player) {
        if (BoomerangHandler.playerBoomer.containsKey(player)) {
            for (String key : BoomerangHandler.playerBoomer.get(player).keySet()) {
                return key;
            }
        }
        return null;
    }
    public static Player getPlayerForArmorStand(ArmorStand as, HashMap<Player, ArrayList<Entity>> armorStandEntity) {
        for (Player player : armorStandEntity.keySet()) {
            if (armorStandEntity.get(player).contains(as)) {
                return player;
            }
        }
        return null;
    }
}
