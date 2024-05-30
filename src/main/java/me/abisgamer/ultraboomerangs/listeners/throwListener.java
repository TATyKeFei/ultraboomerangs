package me.abisgamer.ultraboomerangs.listeners;

import me.abisgamer.ultraboomerangs.UltraBoomerangs;
import me.abisgamer.ultraboomerangs.utils.McMMOHelper;
import me.abisgamer.ultraboomerangs.utils.itemBuilder;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class throwListener implements Listener {

    public static HashMap<Player, ArrayList<Entity>> armorStandEntity = new HashMap<>();
    public static HashMap<Player, HashMap<String, ArrayList<ItemStack>>> playerBoomer = new HashMap<>();
    public static HashMap<String, Long> cooldowns = new HashMap<>();
    public static HashMap<LivingEntity, List<Player>> damageTracker = new HashMap<>();

    ConfigurationSection config = UltraBoomerangs.plugin.getConfig();
    boolean updateOldBoomerangs = config.getBoolean("update-old-boomerangs", false);

    public throwListener(ConfigurationSection config, boolean updateOldBoomerangs) {
        this.config = config;
        this.updateOldBoomerangs = updateOldBoomerangs;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ConfigurationSection boomerangSection = config.getConfigurationSection("boomerangs");
        FileConfiguration messages = UltraBoomerangs.plugin.messages;

        if (boomerangSection != null) {
            Set<String> keys = boomerangSection.getKeys(false);
            for (String key : keys) {
                Action action = event.getAction();
                String ConfigClickType = itemBuilder.clickType.get(key);
                Action clickType;
                Action secondClickType;
                if (Objects.equals(ConfigClickType, "right")) {
                    clickType = Action.RIGHT_CLICK_AIR;
                    secondClickType = Action.RIGHT_CLICK_BLOCK;
                } else if (Objects.equals(ConfigClickType, "left")) {
                    clickType = Action.LEFT_CLICK_AIR;
                    secondClickType = Action.LEFT_CLICK_BLOCK;
                } else {
                    continue;
                }
                ItemStack boomerang = itemBuilder.boomerangs.get(key);
                if (action == clickType || action == secondClickType) {
                    ItemStack itemInHand = player.getInventory().getItemInMainHand();
                    if (isBoomerang(itemInHand, boomerang, key)) {
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

                        handleBoomerangThrow(player, itemInHand, key); // Pass the actual item in hand
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    private Player getPlayerForArmorStand(ArmorStand as) {
        for (Player player : armorStandEntity.keySet()) {
            if (armorStandEntity.get(player).contains(as)) {
                return player;
            }
        }
        return null;
    }

    private String getBoomerangKey(Player player) {
        if (playerBoomer.containsKey(player)) {
            for (String key : playerBoomer.get(player).keySet()) {
                return key;
            }
        }
        return null;
    }

    private void handleBoomerangThrow(Player player, ItemStack itemInHand, String key) {
        FileConfiguration messages = UltraBoomerangs.plugin.messages;
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
        if (soundSection != null && soundSection.getBoolean("enabled")) {
            String throwSoundName = soundSection.getString("throw-sound");
            float volume = (float) soundSection.getDouble("volume");
            float pitch = (float) soundSection.getDouble("pitch");
            if (throwSoundName != null) {
                Sound throwSound = Sound.valueOf(throwSoundName);
                player.playSound(player.getLocation(), throwSound, volume, pitch);
            }
        }

        ItemStack thrownBoomerang = itemInHand.clone(); // Clone the item to preserve its state
        ArmorStand as = (ArmorStand) player.getWorld().spawnEntity(player.getEyeLocation().subtract(0, 0.5, 0), EntityType.ARMOR_STAND);
        as.setVisible(false);
        as.setArms(true);
        as.setGravity(false);
        as.setMarker(true);
        as.setItemInHand(thrownBoomerang);
        as.setRightArmPose(new EulerAngle(Math.toRadians(config.getInt("armorstand.x")), Math.toRadians(config.getInt("armorstand.y")), Math.toRadians(config.getInt("armorstand.z"))));

        ArrayList<Entity> armorEntity = armorStandEntity.computeIfAbsent(player, k -> new ArrayList<>());
        armorEntity.add(as);

        HashMap<String, ArrayList<ItemStack>> existingItemsMap = playerBoomer.computeIfAbsent(player, k -> new HashMap<>());
        ArrayList<ItemStack> existingItems = existingItemsMap.computeIfAbsent(key, k -> new ArrayList<>());
        existingItems.add(thrownBoomerang);

        // Update the cooldown time
        cooldowns.put(key, System.currentTimeMillis());

        final ArrayList<ItemStack> finalExistingItems = existingItems; // Make final for inner class
        new BoomerangReturnTask(player, as, key, finalExistingItems, soundSection).runTaskTimer(UltraBoomerangs.plugin, 1L, 1L);
    }

    private boolean isBoomerang(ItemStack item, ItemStack boomerang, String key) {
        if (item == null || boomerang == null) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta != null && meta.getPersistentDataContainer().has(new NamespacedKey(UltraBoomerangs.plugin, "boomerang_id"), PersistentDataType.STRING)) {
            return key.equals(meta.getPersistentDataContainer().get(new NamespacedKey(UltraBoomerangs.plugin, "boomerang_id"), PersistentDataType.STRING));
        }

        if (updateOldBoomerangs) {
            if (item.isSimilar(boomerang)) {
                meta.getPersistentDataContainer().set(new NamespacedKey(UltraBoomerangs.plugin, "boomerang_id"), PersistentDataType.STRING, key);
                item.setItemMeta(meta);
                return true;
            }
        }

        return false;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
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

    @EventHandler
    public void onPluginDisable(PluginDisableEvent event) {
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

    private void playReceiveSound(Player player, ConfigurationSection soundSection) {
        if (soundSection != null && soundSection.getBoolean("enabled")) {
            String receiveSoundName = soundSection.getString("receive-sound");
            float volume = (float) soundSection.getDouble("volume");
            float pitch = (float) soundSection.getDouble("pitch");
            if (receiveSoundName != null) {
                Sound receiveSound = Sound.valueOf(receiveSoundName);
                player.playSound(player.getLocation(), receiveSound, volume, pitch);
            }
        }
    }

    private class BoomerangReturnTask extends BukkitRunnable {

        private final Player player;
        private final ArmorStand as;
        private final String key;
        private final ArrayList<ItemStack> existingItems;
        private final ConfigurationSection soundSection;
        private final Vector vector;
        private int distance;
        private int i = 0;

        public BoomerangReturnTask(Player player, ArmorStand as, String key, ArrayList<ItemStack> existingItems, ConfigurationSection soundSection) {
            this.player = player;
            this.as = as;
            this.key = key;
            this.existingItems = existingItems;
            this.soundSection = soundSection;
            this.vector = player.getEyeLocation().add(player.getEyeLocation().getDirection().multiply(10)).subtract(player.getEyeLocation().subtract(0, 0.5, 0)).toVector();
            this.distance = itemBuilder.travelDistance.get(key);
        }

        @Override
        public void run() {
            EulerAngle rot = as.getRightArmPose();
            EulerAngle rotnew = rot.add(0, 20, 0);
            as.setRightArmPose(rotnew);

            if (i >= distance) {
                as.teleport(as.getLocation().subtract(vector.normalize()));
                if (i >= distance * 2) {
                    as.remove();
                    if (player.getInventory().firstEmpty() != -1) {
                        int count = 0;
                        while (existingItems.size() > count) {
                            ItemStack boomerang = existingItems.get(count);
                            player.getInventory().addItem(boomerang);
                            existingItems.remove(count);
                            playReceiveSound(player, soundSection);
                            count++;
                        }
                    } else {
                        int count = 0;
                        while (existingItems.size() > count) {
                            ItemStack boomerang = existingItems.get(count);
                            player.getWorld().dropItemNaturally(player.getLocation(), boomerang);
                            playReceiveSound(player, soundSection);
                            existingItems.remove(count);
                            count++;
                        }
                    }
                    cancel();
                }
            } else {
                as.teleport(as.getLocation().add(vector.normalize()));
            }

            i++;

            for (Entity entity : as.getLocation().getChunk().getEntities()) {
                if (!as.isDead()) {
                    if (as.getLocation().distanceSquared(entity.getLocation()) < 1) {
                        if (entity != player && entity instanceof LivingEntity && !(entity instanceof ArmorStand)) {
                            LivingEntity livingentity = (LivingEntity) entity;
                            double damage = itemBuilder.boomerDamage.get(key);
                            livingentity.damage(damage, player);
                            UltraBoomerangs.plugin.getLogger().info("Tracking damage to: " + livingentity.getType().name() + " by player: " + player.getName());
                            damageTracker.computeIfAbsent(livingentity, k -> new ArrayList<>()).add(player);
                            UltraBoomerangs.plugin.getLogger().info("Updated damageTracker: " + damageTracker);
                        }
                    }
                }
            }

            if (as.getTargetBlockExact(1) != null && !as.getTargetBlockExact(1).isPassable()) {
                if (!as.isDead()) {
                    as.remove();
                    if (player.getInventory().firstEmpty() != -1) {
                        int count = 0;
                        while (existingItems.size() > count) {
                            ItemStack boomerang = existingItems.get(count);
                            player.getInventory().addItem(boomerang);
                            playReceiveSound(player, soundSection);
                            existingItems.remove(count);
                            count++;
                        }
                    } else {
                        int count = 0;
                        while (existingItems.size() > count) {
                            ItemStack boomerang = existingItems.get(count);
                            player.getWorld().dropItemNaturally(player.getLocation(), boomerang);
                            playReceiveSound(player, soundSection);
                            existingItems.remove(count);
                            count++;
                        }
                    }
                    cancel();
                }
            }
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        UltraBoomerangs.plugin.getLogger().info("onEntityDamageByEntity triggered");
        if (event.getDamager() instanceof ArmorStand) {
            ArmorStand as = (ArmorStand) event.getDamager();
            Player player = getPlayerForArmorStand(as);
            if (player != null && event.getEntity() instanceof LivingEntity && !(event.getEntity() instanceof ArmorStand)) {
                LivingEntity entity = (LivingEntity) event.getEntity();
                UltraBoomerangs.plugin.getLogger().info("Damage dealt by: " + player.getName() + " to entity: " + entity.getType().name());
                damageTracker.computeIfAbsent(entity, k -> new ArrayList<>()).add(player);
                UltraBoomerangs.plugin.getLogger().info("Updated damageTracker: " + damageTracker);
                double damage = itemBuilder.boomerDamage.get(getBoomerangKey(player));
                entity.damage(damage, player);

                // Add experience on damage
                String skill = config.getString("boomerangs." + getBoomerangKey(player) + ".mcmmo_skill", "none");
                int xpAmount = config.getInt("boomerangs." + getBoomerangKey(player) + ".mcmmo_skill_amount", 0);
                String reason = (entity instanceof Player) ? "PVP" : "PVE";
                UltraBoomerangs.plugin.getLogger().info("Passing skill in onEntityDamageByEntity: " + skill);
                UltraBoomerangs.plugin.getLogger().info("XP Amount: " + xpAmount + ", Reason: " + reason);
                McMMOHelper.addMcMMOExperience(player, skill, xpAmount, reason);
            }
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        UltraBoomerangs.plugin.getLogger().info("onEntityDeath triggered for entity: " + entity.getType().name());
        UltraBoomerangs.plugin.getLogger().info("Current damageTracker: " + damageTracker);

        Bukkit.getScheduler().runTaskLater(UltraBoomerangs.plugin, () -> {
            List<Player> players = damageTracker.get(entity);
            if (players != null && !players.isEmpty()) {
                Player player = players.get(players.size() - 1); // Get the last player who damaged the entity
                String skill = config.getString("boomerangs." + getBoomerangKey(player) + ".mcmmo_skill", "none");
                int xpAmount = config.getInt("boomerangs." + getBoomerangKey(player) + ".mcmmo_skill_amount", 0);
                String reason = (entity instanceof Player) ? "PVP" : "PVE";
                UltraBoomerangs.plugin.getLogger().info("Passing skill in onEntityDeath: " + skill);
                UltraBoomerangs.plugin.getLogger().info("XP Amount: " + xpAmount + ", Reason: " + reason);
                McMMOHelper.addMcMMOExperience(player, skill, xpAmount, reason);
            } else {
                UltraBoomerangs.plugin.getLogger().info("Player is null in onEntityDeath");
            }
        }, 5L); // 1 tick delay
    }

}
