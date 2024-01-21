package me.abisgamer.ultraboomerangs.utils;

import me.abisgamer.ultraboomerangs.UltraBoomerangs;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class itemBuilder {

    public static HashMap<String, ItemStack> boomerangs = new HashMap<>();
    public static HashMap<String, Integer> boomerDamage = new HashMap<>();

    public static HashMap<String, Integer> travelDistance = new HashMap<>();
    public static HashMap<String, Long> cooldownTime = new HashMap<>();
    public static HashMap<String, String> clickType = new HashMap<>();
    public static HashMap<String, Boolean> autoPickup = new HashMap<>();
    public static void createBoomerangs() {
        ConfigurationSection config = UltraBoomerangs.plugin.getConfig();
        ConfigurationSection boomerangSection = config.getConfigurationSection("boomerangs");

        if (boomerangSection != null) {
            Set<String> keys = boomerangSection.getKeys(false);
            for (String key : keys) {
                String material = config.getString("boomerangs." + key + ".material");
                String name = config.getString("boomerangs." + key + ".name");
                String BoomClickType = config.getString("boomerangs." + key + ".click-type");
                List<String> lore = config.getStringList("boomerangs." + key + ".lore");
                int damage = config.getInt("boomerangs." + key + ".damage");
                int distance = config.getInt("boomerangs." + key + ".travel-distance");
                int customModel = config.getInt("boomerangs." + key + ".custom-model");
                Long coolDown = config.getLong("boomerangs." + key + ".cooldown");
                boolean enchanted = config.getBoolean("boomerangs." + key + ".enchanted");
                boolean isItemstack = config.getBoolean("boomerangs." + key + ".is-itemstack");
                ItemStack boomerItemStack = config.getItemStack("boomerangs." + key + ".itemstack");
                boolean autoPickupDrops = config.getBoolean("boomerangs." + key + ".auto-pickup");

                if (!isItemstack) {
                    ItemStack boomerang = new ItemStack(Material.getMaterial(material.toUpperCase()));
                    ItemMeta meta = boomerang.getItemMeta();
                    meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
                    List<String> coloredLore = new ArrayList<>();
                    for (String s : lore) {
                        coloredLore.add(ChatColor.translateAlternateColorCodes('&', s));
                    }
                    meta.setLore(coloredLore);
                    meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                    meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                    meta.setCustomModelData(customModel);
                    if (enchanted) {
                        meta.addEnchant(Enchantment.DURABILITY, 1, true);
                    }
                    boomerang.setItemMeta(meta);
                    boomerangs.put(key, boomerang);
                    UltraBoomerangs.plugin.getLogger().info("Loaded Boomerang: " + key);
                } else {
                    UltraBoomerangs.plugin.getLogger().info("Loaded Custom Boomerang: " + key);
                    boomerangs.put(key, boomerItemStack);
                }
                boomerDamage.put(key, damage);
                travelDistance.put(key, distance);
                clickType.put(key, BoomClickType);
                cooldownTime.put(key, coolDown);
                autoPickup.put(key, autoPickupDrops);
            }
        }
    }


}


