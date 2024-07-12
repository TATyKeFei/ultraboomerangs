package me.abisgamer.ultraboomerangs.handlers;

import me.abisgamer.ultraboomerangs.UltraBoomerangs;
import me.abisgamer.ultraboomerangs.utils.SoundUtils;
import me.abisgamer.ultraboomerangs.utils.itemBuilder;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.util.*;

public class BoomerangReturnTask extends BukkitRunnable {

    private final Player player;
    private final ArmorStand as;
    private final String key;
    private final ArrayList<ItemStack> existingItems;
    private final ConfigurationSection soundSection;
    private Vector vector;
    private int distance;
    private int i = 0;

    public BoomerangReturnTask(Player player, ArmorStand as, String key, ArrayList<ItemStack> existingItems, ConfigurationSection soundSection) {
        this.player = player;
        this.as = as;
        this.key = key;
        this.existingItems = existingItems;
        this.soundSection = soundSection;
        this.vector = player.getEyeLocation().getDirection().normalize();
        this.distance = itemBuilder.travelDistance.get(key);
    }

    @Override
    public void run() {
        EulerAngle rot = as.getRightArmPose();
        EulerAngle rotnew = rot.add(itemBuilder.boomerang_armorstand_x.get(key), itemBuilder.boomerang_armorstand_y.get(key), itemBuilder.boomerang_armorstand_z.get(key));
        as.setRightArmPose(rotnew);
        String rotationType = itemBuilder.rotationType.get(key);

        if (rotationType.equals("curved")) {
            handleCurvedBoomerang();
        } else {
            handleNormalBoomerang();
        }

        i++;

        // Check for entity collisions
        for (Entity entity : as.getLocation().getChunk().getEntities()) {
            if (!as.isDead()) {
                if (as.getLocation().distanceSquared(entity.getLocation()) < 1) {
                    if (entity != player && entity instanceof LivingEntity && !(entity instanceof ArmorStand)) {
                        LivingEntity livingentity = (LivingEntity) entity;
                        double damage = itemBuilder.boomerDamage.get(key);
                        livingentity.damage(damage, player);
                        BoomerangHandler.damageTracker.computeIfAbsent(livingentity, k -> new ArrayList<>()).add(player);
                    }
                }
            }
        }

        // Check for block collisions
        Block targetBlock = as.getLocation().getBlock();
        if (as.getTargetBlockExact(1) != null && !as.getTargetBlockExact(1).isPassable()) {
            if (!as.isDead()) {
                if (rotationType.equals("curved")) {
                    giveBoomerangToPlayer();
                } else {
                    // Change the direction of the boomerang when it hits a block
                    vector = vector.multiply(-1); // Reflect the vector
                }
            }
        }
    }

    private void handleNormalBoomerang() {
        if (i >= distance) {
            as.teleport(as.getLocation().subtract(vector.normalize()));
            if (i >= distance * 2) {
                giveBoomerangToPlayer();
            }
        } else {
            as.teleport(as.getLocation().add(vector.normalize()));
        }
    }

    private void handleCurvedBoomerang() {
        double angle = Math.toRadians(i * 180.0 / (distance / 2));
        Vector initialDirection = player.getEyeLocation().getDirection();
        initialDirection.setY(-initialDirection.getY());
        initialDirection = initialDirection.rotateAroundY(Math.toRadians(-110));
        Vector newVector = new Vector(
                initialDirection.getX() * Math.cos(angle) - initialDirection.getZ() * Math.sin(angle),
                initialDirection.getY(),
                initialDirection.getX() * Math.sin(angle) + initialDirection.getZ() * Math.cos(angle)
        );
        newVector.setY(newVector.getY() * Math.cos(angle));

        if (i >= distance) {
            giveBoomerangToPlayer();
            return;
        } else {
            as.teleport(as.getLocation().subtract(newVector.normalize()));
        }
    }

    private void giveBoomerangToPlayer() {
        if (as != null) {
            as.remove();
        }
        ItemStack latestBoomerang = itemBuilder.boomerangs.get(key).clone();

        int selectedSlot = player.getInventory().getHeldItemSlot();

        if (player.getInventory().getItem(selectedSlot) == null) {
            player.getInventory().setItem(selectedSlot, latestBoomerang);
            SoundUtils.playReceiveSound(player, soundSection);
        } else {
            player.getWorld().dropItemNaturally(player.getLocation(), latestBoomerang);
            SoundUtils.playReceiveSound(player, soundSection);
        }

        Map<String, ArrayList<ItemStack>> playerMap = BoomerangHandler.playerBoomer.get(player);
        if (playerMap != null) {
            playerMap.remove(key);
            if (playerMap.isEmpty()) {
                BoomerangHandler.playerBoomer.remove(player);
            }
        }

        cancel();
    }
}
