package me.abisgamer.ultraboomerangs.listeners;

import com.gmail.nossr50.datatypes.skills.PrimarySkillType;
import com.gmail.nossr50.events.experience.McMMOPlayerXpGainEvent;
import dev.aurelium.auraskills.api.AuraSkillsApi;
import dev.aurelium.auraskills.api.event.skill.XpGainEvent;
import dev.aurelium.auraskills.api.skill.Skills;
import dev.aurelium.auraskills.api.user.SkillsUser;
import me.abisgamer.ultraboomerangs.UltraBoomerangs;
import me.abisgamer.ultraboomerangs.utils.McMMOHelper;
import me.abisgamer.ultraboomerangs.utils.auraSkillsHelper;
import me.abisgamer.ultraboomerangs.utils.itemBuilder;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
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
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static me.abisgamer.ultraboomerangs.UltraBoomerangs.plugin;

public class throwListener implements Listener {

    public static HashMap<Player, ArrayList<Entity>> armorStandEntity = new HashMap<>();
    public static HashMap<Player, HashMap<String, ArrayList<ItemStack>>> playerBoomer = new HashMap<>();
    public static HashMap<String, Long> cooldowns = new HashMap<>();
    public static HashMap<LivingEntity, List<Player>> damageTracker = new HashMap<>();

    ConfigurationSection config = plugin.getConfig();
    boolean updateOldBoomerangs = config.getBoolean("update-old-boomerangs", false);

    private boolean isMcMMO;
    private boolean isAuraSkills;

    public throwListener(ConfigurationSection config, boolean updateOldBoomerangs, boolean isMcMMO, boolean isAuraSkills) {
        this.config = config;
        this.updateOldBoomerangs = updateOldBoomerangs;
        this.isMcMMO = isMcMMO;
        this.isAuraSkills = isAuraSkills;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
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
                    if (isBoomerang(itemInHand, key)) {
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

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
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
                if (isBoomerang(itemDrop, key)) {
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

    private Player getPlayerForArmorStand(ArmorStand as) {
        for (Player player : armorStandEntity.keySet()) {
            if (armorStandEntity.get(player).contains(as)) {
                return player;
            }
        }
        return null;
    }

    public String getBoomerangKey(Player player) {
        if (playerBoomer.containsKey(player)) {
            for (String key : playerBoomer.get(player).keySet()) {
                return key;
            }
        }
        return null;
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
        if (soundSection != null && soundSection.getBoolean("enabled")) {
            String throwSoundName = soundSection.getString("throw-sound");
            float volume = (float) soundSection.getDouble("volume");
            float pitch = (float) soundSection.getDouble("pitch");
            if (throwSoundName != null) {
                Sound throwSound = Sound.valueOf(throwSoundName);
                player.playSound(player.getLocation(), throwSound, volume, pitch);
            }
        }

        ItemStack thrownBoomerang = itemBuilder.boomerangs.get(key).clone(); // Get the item directly from the config
        //plugin.getLogger().info("Before updating thrown boomerang: " + thrownBoomerang);
        //thrownBoomerang = updateItemMeta(thrownBoomerang, key); // Ensure the cloned item is updated
        //plugin.getLogger().info("After updating thrown boomerang: " + thrownBoomerang);

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
        new BoomerangReturnTask(player, as, key, finalExistingItems, soundSection).runTaskTimer(plugin, 1L, 1L);
    }

    private boolean isBoomerang(ItemStack item, String key) {
        if (item == null) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta != null && meta.getPersistentDataContainer().has(new NamespacedKey(plugin, "boomerang_id"), PersistentDataType.STRING)) {
            boolean matches = key.equals(meta.getPersistentDataContainer().get(new NamespacedKey(plugin, "boomerang_id"), PersistentDataType.STRING));
            if (matches) {
                updateItemMeta(item, key);
            }
            return matches;
        }

        if (updateOldBoomerangs) {
            ItemStack boomerang = itemBuilder.boomerangs.get(key);
            if (item.isSimilar(boomerang)) {
                meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "boomerang_id"), PersistentDataType.STRING, key);
                item.setItemMeta(meta);
                updateItemMeta(item, key);
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

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() == Material.AIR) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        // Check if the item is a boomerang using PDC
        if (meta.getPersistentDataContainer().has(new NamespacedKey(plugin, "boomerang_id"), PersistentDataType.STRING)) {
            String key = meta.getPersistentDataContainer().get(new NamespacedKey(plugin, "boomerang_id"), PersistentDataType.STRING);
            ConfigurationSection boomerangConfig = config.getConfigurationSection("boomerangs." + key);
            if (boomerangConfig != null) {
                updateItemMeta(item, key);
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType() == Material.AIR) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        // Check if the item is a boomerang using PDC
        if (meta.getPersistentDataContainer().has(new NamespacedKey(plugin, "boomerang_id"), PersistentDataType.STRING)) {
            String key = meta.getPersistentDataContainer().get(new NamespacedKey(plugin, "boomerang_id"), PersistentDataType.STRING);
            ConfigurationSection boomerangConfig = config.getConfigurationSection("boomerangs." + key);
            if (boomerangConfig != null) {
                updateItemMeta(item, key);
            }
        }
    }

    private ItemStack updateItemMeta(ItemStack item, String key) {
        if (item == null || key == null) return item;

        ConfigurationSection boomerangConfig = config.getConfigurationSection("boomerangs." + key);
        if (boomerangConfig == null) return item;

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

        return item; // Return the updated item
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
                    ItemStack latestBoomerang = itemBuilder.boomerangs.get(key).clone();
                    //latestBoomerang = updateItemMeta(latestBoomerang, key); // Get the latest boomerang from config

                    // Get the player's selected slot
                    int selectedSlot = player.getInventory().getHeldItemSlot();

                    // If the selected slot is empty, add the boomerang to it
                    if (player.getInventory().getItem(selectedSlot) == null) {
                        player.getInventory().setItem(selectedSlot, latestBoomerang);
                        playReceiveSound(player, soundSection);
                    } else {
                        // If the selected slot is not empty, drop the boomerang at the player's location
                        player.getWorld().dropItemNaturally(player.getLocation(), latestBoomerang);
                        playReceiveSound(player, soundSection);
                    }

                    // Remove the player from playerBoomer when boomerang returns
                    playerBoomer.get(player).remove(key);
                    if (playerBoomer.get(player).isEmpty()) {
                        playerBoomer.remove(player);
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
                            damageTracker.computeIfAbsent(livingentity, k -> new ArrayList<>()).add(player);
                        }
                    }
                }
            }

            if (as.getTargetBlockExact(1) != null && !as.getTargetBlockExact(1).isPassable()) {
                if (!as.isDead()) {
                    as.remove();
                    ItemStack latestBoomerang = itemBuilder.boomerangs.get(key).clone();
                    //latestBoomerang = updateItemMeta(latestBoomerang, key); // Get the latest boomerang from config

                    // Get the player's selected slot
                    int selectedSlot = player.getInventory().getHeldItemSlot();

                    // If the selected slot is empty, add the boomerang to it
                    if (player.getInventory().getItem(selectedSlot) == null) {
                        player.getInventory().setItem(selectedSlot, latestBoomerang);
                        playReceiveSound(player, soundSection);
                    } else {
                        // If the selected slot is not empty, drop the boomerang at the player's location
                        player.getWorld().dropItemNaturally(player.getLocation(), latestBoomerang);
                        playReceiveSound(player, soundSection);
                    }

                    // Remove the player from playerBoomer when boomerang returns
                    playerBoomer.get(player).remove(key);
                    if (playerBoomer.get(player).isEmpty()) {
                        playerBoomer.remove(player);
                    }

                    cancel();
                }

            }
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof ArmorStand) {
            ArmorStand as = (ArmorStand) event.getDamager();
            Player player = getPlayerForArmorStand(as);
            if (player != null && event.getEntity() instanceof LivingEntity && !(event.getEntity() instanceof ArmorStand)) {
                LivingEntity entity = (LivingEntity) event.getEntity();
                damageTracker.computeIfAbsent(entity, k -> new ArrayList<>()).add(player);
                double damage = itemBuilder.boomerDamage.get(getBoomerangKey(player));
                entity.damage(damage, player);

                if (isMcMMO) {
                    String skill = config.getString("boomerangs." + getBoomerangKey(player) + ".mcmmo_skill", "none");
                    int xpAmount = config.getInt("boomerangs." + getBoomerangKey(player) + ".mcmmo_skill_amount", 0);
                    String reason = (entity instanceof Player) ? "PVP" : "PVE";
                    McMMOHelper.addMcMMOExperience(player, skill, xpAmount, reason);
                }
                if (isAuraSkills) {
                    String auraSkill = config.getString("boomerangs." + getBoomerangKey(player) + ".auraskills_skill", "none");
                    int auraXpAmount = config.getInt("boomerangs." + getBoomerangKey(player) + ".auraskills_skill_amount", 0);
                    auraSkillsHelper.addAuraSkillsEXP(player, auraSkill, auraXpAmount);
                }
            }
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        boolean wasBoomerangDamage = damageTracker.containsKey(entity);

        // Capture the drops before scheduling the task to avoid issues with cleared drops
        List<ItemStack> drops = new ArrayList<>(event.getDrops());

        // Schedule the task to handle the drops and other logic
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            List<Player> players = damageTracker.get(entity);
            if (players != null && !players.isEmpty()) {
                Player player = players.get(players.size() - 1); // Get the last player who damaged the entity
                String boomerangKey = getBoomerangKey(player);
                if (boomerangKey != null) {
                    ConfigurationSection boomerangConfig = config.getConfigurationSection("boomerangs." + boomerangKey);

                    // Handle auto-pickup for non-player entities
                    if (!(entity instanceof Player) && boomerangConfig.getBoolean("auto-pickup")) {
                        for (ItemStack drop : drops) {
                            if (drop != null && drop.getType() != Material.AIR) {
                                player.getInventory().addItem(drop);
                            }
                        }
                    }

                    // Optionally, play a sound if configured
                    if (boomerangConfig.getConfigurationSection("sounds").getBoolean("enabled", false)) {
                        try {
                            Sound receiveSound = Sound.valueOf(boomerangConfig.getString("sounds.receive-sound", "ENTITY_ITEM_PICKUP"));
                            player.playSound(player.getLocation(), receiveSound,
                                    (float) boomerangConfig.getDouble("sounds.volume", 1.0),
                                    (float) boomerangConfig.getDouble("sounds.pitch", 1.0));
                        } catch (IllegalArgumentException e) {
                            UltraBoomerangs.plugin.getLogger().warning("Invalid sound name: " + boomerangConfig.getString("sounds.receive-sound"));
                        }
                    }

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

        // Clear the drops only for non-player entities
        if (!(entity instanceof Player)) {
            event.getDrops().clear();
        }
    }

}