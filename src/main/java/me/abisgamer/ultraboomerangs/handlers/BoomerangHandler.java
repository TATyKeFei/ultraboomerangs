package me.abisgamer.ultraboomerangs.handlers;

import me.abisgamer.ultraboomerangs.UltraBoomerangs;
import me.abisgamer.ultraboomerangs.utils.ItemUtils;
import me.abisgamer.ultraboomerangs.utils.SoundUtils;
import me.abisgamer.ultraboomerangs.utils.McMMOHelper;
import me.abisgamer.ultraboomerangs.utils.auraSkillsHelper;
import me.abisgamer.ultraboomerangs.utils.itemBuilder;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;

import java.util.*;

import static me.abisgamer.ultraboomerangs.UltraBoomerangs.plugin;

public class BoomerangHandler {
    public static HashMap<Player, ArrayList<Entity>> armorStandEntity = new HashMap<>();
    public static HashMap<Player, HashMap<String, ArrayList<ItemStack>>> playerBoomer = new HashMap<>();
    public static HashMap<String, Long> cooldowns = new HashMap<>();
    public static HashMap<LivingEntity, List<Player>> damageTracker = new HashMap<>();

    private final ConfigurationSection config;
    private final boolean updateOldBoomerangs;
    private final boolean isMcMMO;
    private final boolean isAuraSkills;

    public BoomerangHandler(ConfigurationSection config, boolean updateOldBoomerangs, boolean isMcMMO, boolean isAuraSkills) {
        this.config = config;
        this.updateOldBoomerangs = updateOldBoomerangs;
        this.isMcMMO = isMcMMO;
        this.isAuraSkills = isAuraSkills;
    }

    public void handleInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ConfigurationSection boomerangSection = config.getConfigurationSection("boomerangs");
        FileConfiguration messages = plugin.messages;

        if (boomerangSection != null) {
            Set<String> keys = boomerangSection.getKeys(false);
            for (String key : keys) {
                Action action = event.getAction();
                String ConfigClickType = itemBuilder.clickType.get(key);
                Action clickType;
                Action secondClickType;
                boolean requiresSneaking = false;

                if (Objects.equals(ConfigClickType, "right")) {
                    clickType = Action.RIGHT_CLICK_AIR;
                    secondClickType = Action.RIGHT_CLICK_BLOCK;
                } else if (Objects.equals(ConfigClickType, "left")) {
                    clickType = Action.LEFT_CLICK_AIR;
                    secondClickType = Action.LEFT_CLICK_BLOCK;
                } else if (Objects.equals(ConfigClickType, "shift-right")) {
                    clickType = Action.RIGHT_CLICK_AIR;
                    secondClickType = Action.RIGHT_CLICK_BLOCK;
                    requiresSneaking = true;
                } else if (Objects.equals(ConfigClickType, "shift-left")) {
                    clickType = Action.LEFT_CLICK_AIR;
                    secondClickType = Action.LEFT_CLICK_BLOCK;
                    requiresSneaking = true;
                } else {
                    continue;
                }

                if (requiresSneaking && !player.isSneaking()) {
                    continue;
                }

                if (action == clickType || action == secondClickType) {
                    ItemStack itemInHand = player.getInventory().getItemInMainHand();
                    if (ItemUtils.isBoomerang(itemInHand, key, config, updateOldBoomerangs)) {
                        // Check cooldown before removing the item
                        Long cooldownTime = itemBuilder.cooldownTime.get(key); // Cooldown time in seconds
                        if (cooldowns.containsKey(key)) {
                            long secondsLeft = ((cooldowns.get(key) / 1000) + cooldownTime) - (System.currentTimeMillis() / 1000);
                            if (secondsLeft > 0) {
                                // Inform the player about the remaining cooldown time
                                player.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.getString("cooldown") + secondsLeft + messages.getString("cooldown-2")));
                                event.setCancelled(true);
                                return;
                            }
                        }

                        // Remove one boomerang from the player's inventory
                        if (itemInHand.getAmount() > 1) {
                            itemInHand.setAmount(itemInHand.getAmount() - 1);
                        } else {
                            player.getInventory().setItemInMainHand(null);
                        }
                        player.updateInventory(); // Ensure the inventory is updated

                        handleBoomerangThrow(player, key); // Pass the updated item
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    public void handlePlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        ConfigurationSection boomerangSection = config.getConfigurationSection("boomerangs");
        FileConfiguration messages = plugin.messages;

        if (boomerangSection != null) {
            Set<String> keys = boomerangSection.getKeys(false);
            for (String key : keys) {
                String ConfigClickType = itemBuilder.clickType.get(key);
                if (!Objects.equals(ConfigClickType, "drop")) {
                    continue;
                }
                ItemStack itemDrop = event.getItemDrop().getItemStack();
                if (ItemUtils.isBoomerang(itemDrop, key, config, updateOldBoomerangs)) {
                    // Check cooldown before removing the item
                    Long cooldownTime = itemBuilder.cooldownTime.get(key); // Cooldown time in seconds
                    if (cooldowns.containsKey(key)) {
                        long secondsLeft = ((cooldowns.get(key) / 1000) + cooldownTime) - (System.currentTimeMillis() / 1000);
                        if (secondsLeft > 0) {
                            // Inform the player about the remaining cooldown time
                            player.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.getString("cooldown") + secondsLeft + messages.getString("cooldown-2")));
                            event.setCancelled(true);
                            return;
                        }
                    }

                    // Remove one boomerang from the player's inventory
                    ItemStack itemInHand = itemDrop.clone();
                    if (itemDrop.getAmount() >= 1) {
                        itemDrop.setAmount(itemDrop.getAmount() - 1);
                    } else {
                        event.getItemDrop().remove();
                    }
                    player.updateInventory(); // Ensure the inventory is updated

                    handleBoomerangThrow(player, key); // Pass the updated item
                    event.setCancelled(true);
                    break;
                }
            }
        }
    }

    public void handlePlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        ArrayList<Entity> armorEntity = armorStandEntity.get(player);
        int armorCount = 0;
        while (armorEntity != null && armorEntity.size() > armorCount) {
            Entity as = armorEntity.get(armorCount);
            armorEntity.remove(as);
            as.remove();
            armorCount++;
        }

        HashMap<String, ArrayList<ItemStack>> existingItemsMap = playerBoomer.get(player);
        if (existingItemsMap != null) {
            for (String key : existingItemsMap.keySet()) {
                ArrayList<ItemStack> existingItems = existingItemsMap.get(key);
                int count = 0;
                while (existingItems != null && existingItems.size() > count) {
                    ItemStack boomerang = existingItems.get(count);
                    player.getInventory().addItem(boomerang);
                    existingItems.remove(count);
                    count++;
                }
            }
        }
    }

    public void handlePluginDisable(PluginDisableEvent event) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            ArrayList<Entity> armorEntity = armorStandEntity.get(player);
            int armorCount = 0;
            while (armorEntity != null && armorEntity.size() > armorCount) {
                Entity as = armorEntity.get(armorCount);
                armorEntity.remove(armorCount);
                as.remove();
                armorCount++;
            }
            HashMap<String, ArrayList<ItemStack>> existingItemsMap = playerBoomer.get(player);
            if (existingItemsMap != null) {
                for (String key : existingItemsMap.keySet()) {
                    ArrayList<ItemStack> existingItems = existingItemsMap.get(key);
                    int count = 0;
                    while (existingItems != null && existingItems.size() > count) {
                        ItemStack boomerang = existingItems.get(count);
                        existingItems.remove(count);
                        player.getInventory().addItem(boomerang);
                        count++;
                    }
                }
            }
        }
    }

    public void handleEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof ArmorStand) {
            ArmorStand as = (ArmorStand) event.getDamager();
            Player player = ItemUtils.getPlayerForArmorStand(as, armorStandEntity);
            if (player != null && event.getEntity() instanceof LivingEntity && !(event.getEntity() instanceof ArmorStand)) {
                LivingEntity entity = (LivingEntity) event.getEntity();
                damageTracker.computeIfAbsent(entity, k -> new ArrayList<>()).add(player);
                double damage = itemBuilder.boomerDamage.get(ItemUtils.getBoomerangKey(player, playerBoomer));
                entity.damage(damage, player);

                if (isMcMMO) {
                    String skill = config.getString("boomerangs." + ItemUtils.getBoomerangKey(player, playerBoomer) + ".mcmmo_skill", "none");
                    int xpAmount = config.getInt("boomerangs." + ItemUtils.getBoomerangKey(player, playerBoomer) + ".mcmmo_skill_amount", 0);
                    String reason = (entity instanceof Player) ? "PVP" : "PVE";
                    McMMOHelper.addMcMMOExperience(player, skill, xpAmount, reason);
                }
                if (isAuraSkills) {
                    String auraSkill = config.getString("boomerangs." + ItemUtils.getBoomerangKey(player, playerBoomer) + ".auraskills_skill", "none");
                    int auraXpAmount = config.getInt("boomerangs." + ItemUtils.getBoomerangKey(player, playerBoomer) + ".auraskills_skill_amount", 0);
                    auraSkillsHelper.addAuraSkillsEXP(player, auraSkill, auraXpAmount);
                }
            }
        }
    }

    public void handleEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        boolean wasBoomerangDamage = damageTracker.containsKey(entity);

        // Capture the drops before scheduling the task to avoid issues with cleared drops
        List<ItemStack> drops = new ArrayList<>(event.getDrops());

        // Schedule the task to handle the drops and other logic
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            List<Player> players = damageTracker.get(entity);
            if (players != null && !players.isEmpty()) {
                Player player = players.get(players.size() - 1); // Get the last player who damaged the entity
                String boomerangKey = ItemUtils.getBoomerangKey(player, playerBoomer);
                if (boomerangKey != null) {
                    ConfigurationSection boomerangConfig = config.getConfigurationSection("boomerangs." + boomerangKey);

                    // Handle auto-pickup for non-player entities
                    if (!(entity instanceof Player) && boomerangConfig.getBoolean("auto-pickup")) {
                        for (ItemStack drop : drops) {
                            if (drop != null && drop.getType() != Material.AIR) {
                                player.getInventory().addItem(drop);
                            }
                        }
                        // Clear the drops only for non-player entities
                        if (!(entity instanceof Player)) {
                            event.getDrops().clear();
                        }
                    }

                    // Optionally, play a sound if configured
                    SoundUtils.playReceiveSound(player, boomerangConfig);

                    if (isMcMMO) {
                        String skill = boomerangConfig.getString("mcmmo_skill", "none");
                        int xpAmount = boomerangConfig.getInt("mcmmo_skill_amount", 0);
                        String reason = (entity instanceof Player) ? "PVP" : "PVE";
                        McMMOHelper.addMcMMOExperience(player, skill, xpAmount, reason);
                    }
                    if (isAuraSkills) {
                        String auraSkill = boomerangConfig.getString("auraskills_skill", "none");
                        int auraXpAmount = boomerangConfig.getInt("auraskills_skill_amount", 0);
                        auraSkillsHelper.addAuraSkillsEXP(player, auraSkill, auraXpAmount);
                    }
                }
            }
        }, 1L); // 1 tick delay
    }

    public void handleInventoryClick(InventoryClickEvent event) {
        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType() == Material.AIR) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        // Check if the item is a boomerang using PDC
        if (meta.getPersistentDataContainer().has(new NamespacedKey(plugin, "boomerang_id"), PersistentDataType.STRING)) {
            String key = meta.getPersistentDataContainer().get(new NamespacedKey(plugin, "boomerang_id"), PersistentDataType.STRING);
            ConfigurationSection boomerangConfig = config.getConfigurationSection("boomerangs." + key);
            if (boomerangConfig != null) {
                ItemUtils.updateItemMeta(item, key, config);
            }
        }
    }

    private void handleBoomerangThrow(Player player, String key) {
        FileConfiguration messages = plugin.messages;
        Long cooldownTime = itemBuilder.cooldownTime.get(key); // Cooldown time in seconds
        if (cooldowns.containsKey(key)) {
            long secondsLeft = ((cooldowns.get(key) / 1000) + cooldownTime) - (System.currentTimeMillis() / 1000);
            if (secondsLeft > 0) {
                // Inform the player about the remaining cooldown time
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.getString("cooldown") + secondsLeft + messages.getString("cooldown-2")));
                return;
            }
        }

        ConfigurationSection soundSection = config.getConfigurationSection("boomerangs." + key + ".sounds");
        SoundUtils.playThrowSound(player, soundSection);

        ItemStack thrownBoomerang = itemBuilder.boomerangs.get(key).clone(); // Get the item directly from the config
        String thrownBoomerangName = thrownBoomerang.getItemMeta().getDisplayName();

        ArmorStand as = (ArmorStand) player.getWorld().spawnEntity(player.getEyeLocation().subtract(0, 0.5, 0), EntityType.ARMOR_STAND);
        as.setVisible(false);
        as.setArms(true);
        as.setGravity(false);
        as.setMarker(true);
        as.setItemInHand(thrownBoomerang);
        as.setCustomName(thrownBoomerangName);
        as.setCustomNameVisible(false);
        as.setRightArmPose(new EulerAngle(Math.toRadians(itemBuilder.boomerang_armorstand_x.get(key)), Math.toRadians(itemBuilder.boomerang_armorstand_y.get(key)), Math.toRadians(itemBuilder.boomerang_armorstand_z.get(key))));

        ArrayList<Entity> armorEntity = armorStandEntity.computeIfAbsent(player, k -> new ArrayList<>());
        armorEntity.add(as);

        HashMap<String, ArrayList<ItemStack>> existingItemsMap = playerBoomer.computeIfAbsent(player, k -> new HashMap<>());
        ArrayList<ItemStack> existingItems = existingItemsMap.computeIfAbsent(key, k -> new ArrayList<>());
        existingItems.add(thrownBoomerang);

        // Update the cooldown time
        cooldowns.put(key, System.currentTimeMillis());

        final ArrayList<ItemStack> finalExistingItems = existingItems; // Make final for inner class
        new BoomerangReturnTask(player, as, key, finalExistingItems, soundSection).runTaskTimer(plugin, 1L, 1L);
    }
}
