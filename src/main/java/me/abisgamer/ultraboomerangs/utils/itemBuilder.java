package me.abisgamer.ultraboomerangs.utils;

import me.abisgamer.ultraboomerangs.UltraBoomerangs;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class itemBuilder {
    public static HashMap<String, ItemStack> boomerangs = new HashMap<>();
    public static HashMap<String, Integer> boomerDamage = new HashMap<>();
    public static HashMap<String, Integer> travelDistance = new HashMap<>();
    public static HashMap<String, Long> cooldownTime = new HashMap<>();
    public static HashMap<String, String> clickType = new HashMap<>();
    public static HashMap<String, Boolean> supportDurability = new HashMap<>();
    public static HashMap<String, String> mcmmoSkills = new HashMap<>();
    public static HashMap<String, Integer> mcmmoSkillAmounts = new HashMap<>();

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
                boolean supportDurabilityOption = config.getBoolean("boomerangs." + key + ".support-durability", false);
                String mcmmoSkill = config.getString("boomerangs." + key + ".mcmmo_skill", "none");
                int mcmmoSkillAmount = config.getInt("boomerangs." + key + ".mcmmo_skill_amount", 0);

                ItemStack boomerang;
                if (!isItemstack) {
                    boomerang = new ItemStack(Material.getMaterial(material.toUpperCase()));
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
                } else {
                    boomerang = boomerItemStack;
                }

                ItemMeta meta = boomerang.getItemMeta();
                meta.getPersistentDataContainer().set(new NamespacedKey(UltraBoomerangs.plugin, "boomerang_id"), PersistentDataType.STRING, key);
                boomerang.setItemMeta(meta);

                boomerangs.put(key, boomerang);
                boomerDamage.put(key, damage);
                travelDistance.put(key, distance);
                clickType.put(key, BoomClickType);
                cooldownTime.put(key, coolDown);
                supportDurability.put(key, supportDurabilityOption);
                mcmmoSkills.put(key, mcmmoSkill);
                mcmmoSkillAmounts.put(key, mcmmoSkillAmount);

                UltraBoomerangs.plugin.getLogger().info("Loaded Boomerang: " + key);
            }
        }
    }
}
