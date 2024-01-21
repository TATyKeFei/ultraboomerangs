package me.abisgamer.ultraboomerangs.listeners;

import me.abisgamer.ultraboomerangs.UltraBoomerangs;
import me.abisgamer.ultraboomerangs.utils.itemBuilder;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.Set;

public class throwListener implements Listener {

    public static HashMap<Player, ArrayList<Entity>> armorStandEntity = new HashMap<>();
    public static HashMap<Player, ArrayList<ItemStack>> playerBoomer = new HashMap<>();


    public static HashMap<String, Long> cooldowns = new HashMap<>();
    ConfigurationSection config = UltraBoomerangs.plugin.getConfig();

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
                } else {
                    clickType = Action.LEFT_CLICK_AIR;
                    secondClickType = Action.LEFT_CLICK_BLOCK;
                }
                ItemStack boomerang = itemBuilder.boomerangs.get(key);
                if (action == clickType || action == secondClickType) {
                    if (player.getInventory().getItemInMainHand().equals(boomerang)) {

                        Long cooldownTime = itemBuilder.cooldownTime.get(key); // Cooldown time in seconds
                        if (cooldowns.containsKey(key)) {
                            long secondsLeft = ((cooldowns.get(key)/1000)+cooldownTime) - (System.currentTimeMillis()/1000);
                            if (secondsLeft > 0) {
                                // Inform the player about the remaining cooldown time
                                player.sendMessage(ChatColor.translateAlternateColorCodes('&',messages.getString("cooldown") + secondsLeft + messages.getString("cooldown-2")));
                                return;
                            }
                        }

                        ConfigurationSection soundSection = boomerangSection.getConfigurationSection(".sounds");
                        if (soundSection != null && soundSection.getBoolean(".enabled")) {
                            String throwSoundName = soundSection.getString(".throw-sound");
                            float volume = (float) soundSection.getDouble(".volume");
                            float pitch = (float) soundSection.getDouble(".pitch");
                            if (throwSoundName != null) {
                                Sound throwSound = Sound.valueOf(throwSoundName);
                                player.playSound(player.getLocation(), throwSound, volume, pitch);
                            }
                        }

                        player.getInventory().getItemInMainHand().setAmount(player.getInventory().getItemInMainHand().getAmount() - 1);


                        ArmorStand as = (ArmorStand) player.getWorld().spawnEntity(player.getEyeLocation().subtract(0, 0.5, 0), EntityType.ARMOR_STAND);
                        as.setVisible(false);
                        as.setArms(true);
                        as.setGravity(false);
                        as.setMarker(true);
                        Location destination = player.getEyeLocation().add(player.getEyeLocation().getDirection().multiply(10));
                        as.setItemInHand(boomerang);
                        as.setRightArmPose(new EulerAngle(Math.toRadians(config.getInt("armorstand.x")), Math.toRadians(config.getInt("armorstand.y")), Math.toRadians(config.getInt("armorstand.z"))));

                        ArrayList<Entity> armorEntity = armorStandEntity.get(player);
                        if (armorEntity == null) {
                            armorEntity = new ArrayList<>();
                            armorStandEntity.put(player, armorEntity);
                        }
                        armorEntity.add(as);
                        ArrayList<ItemStack> existingItems = playerBoomer.get(player);
                        if (existingItems == null) {
                            existingItems = new ArrayList<>();
                            playerBoomer.put(player, existingItems);
                        }
                        existingItems.add(boomerang);

                        // Update the cooldown time
                        cooldowns.put(key, System.currentTimeMillis());

                        Vector vector = destination.subtract(player.getEyeLocation().subtract(0, 0.5, 0)).toVector();

                        new BukkitRunnable() {

                            int distance = itemBuilder.travelDistance.get(key);
                            int i = 0;

                            public void run() {

                                EulerAngle rot = as.getRightArmPose();
                                EulerAngle rotnew = rot.add(0, 20, 0);
                                as.setRightArmPose(rotnew);

                                if (i >= distance) {
                                    as.teleport(as.getLocation().subtract(vector.normalize()));
                                    if (i >= distance * 2) {
                                        as.remove();
                                        if (player.getInventory().firstEmpty() != -1) {
                                            ArrayList<ItemStack> existingItems = playerBoomer.get(player);
                                            int count = 0;
                                            while (existingItems.size() > count) {
                                                ItemStack boomerang = existingItems.get(count);
                                                player.getInventory().addItem(boomerang);
                                                existingItems.remove(count);
                                                if (soundSection != null && soundSection.getBoolean("enabled")) {
                                                    String throwSoundName = soundSection.getString("recieve-sound");
                                                    float volume = (float) soundSection.getDouble("volume");
                                                    float pitch = (float) soundSection.getDouble("pitch");
                                                    if (throwSoundName != null) {
                                                        Sound throwSound = Sound.valueOf(throwSoundName);
                                                        player.playSound(player.getLocation(), throwSound, volume, pitch);
                                                    }
                                                }
                                                count++;
                                            }
                                        } else {
                                            ArrayList<ItemStack> existingItems = playerBoomer.get(player);
                                            int count = 0;
                                            while (existingItems.size() > count) {
                                                ItemStack boomerang = existingItems.get(count);
                                                player.getWorld().dropItemNaturally(player.getLocation(), boomerang);
                                                existingItems.remove(count);
                                                if (soundSection != null && soundSection.getBoolean("enabled")) {
                                                    String throwSoundName = soundSection.getString("recieve-sound");
                                                    float volume = (float) soundSection.getDouble("volume");
                                                    float pitch = (float) soundSection.getDouble("pitch");
                                                    if (throwSoundName != null) {
                                                        Sound throwSound = Sound.valueOf(throwSoundName);
                                                        player.playSound(player.getLocation(), throwSound, volume, pitch);
                                                    }
                                                }
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
                                            if (entity != player && entity instanceof LivingEntity) {
                                                LivingEntity livingentity = (LivingEntity) entity;
                                                livingentity.damage(itemBuilder.boomerDamage.get(key), player);
                                            }
                                        }
                                    }
                                }

                                if (as.getTargetBlockExact(1) != null && !as.getTargetBlockExact(1).isPassable()) {
                                    if (!as.isDead()) {
                                        as.remove();
                                        if (player.getInventory().firstEmpty() != -1) {
                                            ArrayList<ItemStack> existingItems = playerBoomer.get(player);
                                            int count = 0;
                                            while (existingItems.size() > count) {
                                                ItemStack boomerang = existingItems.get(count);
                                                player.getInventory().addItem(boomerang);
                                                if (soundSection != null && soundSection.getBoolean("enabled")) {
                                                    String throwSoundName = soundSection.getString("recieve-sound");
                                                    float volume = (float) soundSection.getDouble("volume");
                                                    float pitch = (float) soundSection.getDouble("pitch");
                                                    if (throwSoundName != null) {
                                                        Sound throwSound = Sound.valueOf(throwSoundName);
                                                        player.playSound(player.getLocation(), throwSound, volume, pitch);
                                                    }
                                                }
                                                existingItems.remove(count);
                                                count++;
                                            }
                                        } else {
                                            ArrayList<ItemStack> existingItems = playerBoomer.get(player);
                                            int count = 0;
                                            while (existingItems.size() > count) {
                                                ItemStack boomerang = existingItems.get(count);
                                                player.getWorld().dropItemNaturally(player.getLocation(), boomerang);
                                                if (soundSection != null && soundSection.getBoolean("enabled")) {
                                                    String throwSoundName = soundSection.getString("recieve-sound");
                                                    float volume = (float) soundSection.getDouble("volume");
                                                    float pitch = (float) soundSection.getDouble("pitch");
                                                    if (throwSoundName != null) {
                                                        Sound throwSound = Sound.valueOf(throwSoundName);
                                                        player.playSound(player.getLocation(), throwSound, volume, pitch);
                                                    }
                                                }
                                                existingItems.remove(count);
                                                count++;
                                            }
                                        }
                                        cancel();
                                    }
                                }
                            }
                        }.runTaskTimer(UltraBoomerangs.plugin, 1L, 1L);
                        event.setCancelled(true);
                    }

                }
            }
        }
    }

    @EventHandler
    public void onPlayerDisconnect(PlayerQuitEvent event) {
        Player player = event.getPlayer();

            ArrayList<Entity> armorEntity = armorStandEntity.get(player);
            int armorCount = 0;
            while (armorEntity.size() > armorCount) {
                Entity as = armorEntity.get(armorCount);
                armorEntity.remove(as);
                as.remove();
                armorCount++;
            }

            ArrayList<ItemStack> existingItems = playerBoomer.get(player);
            int count = 0;
            while (existingItems.size() > count) {
                ItemStack boomerang = existingItems.get(count);
                player.getInventory().addItem(boomerang);
                existingItems.remove(count);
                count++;
            }
        }


    @EventHandler
    public void onPluginDisable(PluginDisableEvent event) {
        for (Player player : Bukkit.getOnlinePlayers()) {
                ArrayList<Entity> armorEntity = armorStandEntity.get(player);
                int armorCount = 0;
                while (armorEntity.size() > armorCount) {
                    Entity as = armorEntity.get(armorCount);
                    armorEntity.remove(armorCount);
                    as.remove();
                    armorCount++;
                }
                ArrayList<ItemStack> existingItems = playerBoomer.get(player);
                int count = 0;
                while (existingItems.size() > count) {
                    ItemStack boomerang = existingItems.get(count);
                    existingItems.remove(count);
                    player.getInventory().addItem(boomerang);
                    count++;
                }
            }
    }

}