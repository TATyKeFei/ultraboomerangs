package me.abisgamer.ultraboomerangs.handlers;

import me.abisgamer.ultraboomerangs.UltraBoomerangs;
import me.abisgamer.ultraboomerangs.utils.SoundUtils;
import me.abisgamer.ultraboomerangs.utils.itemBuilder;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Map;

public class BoomerangReturnTask extends BukkitRunnable {

    private final Player player;
    private final ArmorStand as;
    private final String key;
    private final ArrayList<ItemStack> existingItems;
    private final ConfigurationSection soundSection;
    private Vector vector;
    private final int distance;
    private int i = 0;
    // Retrieve the speed value from the itemBuilder for this boomerang.
    private final double speed;
    // Store the damage value from when the boomerang was thrown
    private final int storedDamage;

    public BoomerangReturnTask(Player player, ArmorStand as, String key, ArrayList<ItemStack> existingItems,
                               ConfigurationSection soundSection, int storedDamage) {
        this.player = player;
        this.as = as;
        this.key = key;
        this.existingItems = existingItems;
        this.soundSection = soundSection;
        this.vector = player.getEyeLocation().getDirection().normalize();
        this.distance = itemBuilder.travelDistance.get(key);
        // Get speed; if null, default to 1.0
        this.speed = itemBuilder.speedValue.getOrDefault(key, 1.0);
        // Store the initial damage
        this.storedDamage = storedDamage;
    }

    @Override
    public void run() {
        // Rotate the armor stand's right arm gradually
        EulerAngle rot = as.getRightArmPose();
        EulerAngle rotNew = rot.add(
                itemBuilder.boomerang_armorstand_x.get(key),
                itemBuilder.boomerang_armorstand_y.get(key),
                itemBuilder.boomerang_armorstand_z.get(key)
        );
        as.setRightArmPose(rotNew);

        String rotationType = itemBuilder.rotationType.get(key);

        // Handle boomerang motion based on rotation type
        if (rotationType.equals("curved")) {
            handleCurvedBoomerang();
        } else {
            handleNormalBoomerang();
        }

        i++;

        // Check for entity collisions
        for (Entity entity : as.getLocation().getChunk().getEntities()) {
            if (!as.isDead() && entity != player && entity instanceof LivingEntity && !(entity instanceof ArmorStand)) {
                if (as.getLocation().distanceSquared(entity.getLocation()) < 1) {
                    LivingEntity livingEntity = (LivingEntity) entity;
                    double damage = itemBuilder.boomerDamage.get(key);
                    livingEntity.damage(damage, player);
                    BoomerangHandler.damageTracker.computeIfAbsent(livingEntity, k -> new ArrayList<>()).add(player);
                }
            }
        }

        // Check for block collisions and compute a proper reflection vector
        Block targetBlock = as.getTargetBlockExact(1);
        if (targetBlock != null && !targetBlock.isPassable()) {
            if (!as.isDead()) {
                if (rotationType.equals("curved")) {
                    giveBoomerangToPlayer();
                } else {
                    // Calculate a reflection vector based on the collision normal.
                    Vector blockCenter = targetBlock.getLocation().toVector().add(new Vector(0.5, 0.5, 0.5));
                    Vector collisionPoint = as.getLocation().toVector();
                    Vector normal = collisionPoint.subtract(blockCenter).normalize();
                    // Reflection formula: R = D - 2*(D·N)*N
                    vector = vector.subtract(normal.multiply(2 * vector.dot(normal)));
                }
            }
        }
    }

    private void handleNormalBoomerang() {
        // Multiply the normalized vector by the speed value
        Vector move = vector.clone().normalize().multiply(speed);
        if (i >= distance) {
            as.teleport(as.getLocation().subtract(move));
            if (i >= distance * 2) {
                giveBoomerangToPlayer();
            }
        } else {
            as.teleport(as.getLocation().add(move));
        }
    }

    private void handleCurvedBoomerang() {
        // Calculate a curved trajectory using a gradually changing angle.
        double angle = Math.toRadians(i * 180.0 / (distance / 2));
        Vector initialDirection = player.getEyeLocation().getDirection();
        // Invert the Y-component to simulate a curved return.
        initialDirection.setY(-initialDirection.getY());
        // Rotate the direction for a more dramatic curve.
        initialDirection = initialDirection.rotateAroundY(Math.toRadians(-110));
        // Apply rotation in the XZ plane
        Vector newVector = new Vector(
                initialDirection.getX() * Math.cos(angle) - initialDirection.getZ() * Math.sin(angle),
                initialDirection.getY(),
                initialDirection.getX() * Math.sin(angle) + initialDirection.getZ() * Math.cos(angle)
        );
        newVector.setY(newVector.getY() * Math.cos(angle));

        // Multiply by speed for consistency
        Vector move = newVector.clone().normalize().multiply(speed);

        if (i >= distance) {
            giveBoomerangToPlayer();
        } else {
            as.teleport(as.getLocation().subtract(move));
        }
    }

    private void giveBoomerangToPlayer() {
        // Remove the ArmorStand
        if (as != null) {
            as.remove();
        }

        // Clone the boomerang from your config.
        ItemStack returnedBoomerang = itemBuilder.boomerangs.get(key).clone();

        // Use the stored damage value from when the boomerang was thrown
        if (itemBuilder.supportDurability.get(key)) {
            ItemMeta meta = returnedBoomerang.getItemMeta();
            if (meta instanceof Damageable) {
                ((Damageable) meta).setDamage(storedDamage);
                returnedBoomerang.setItemMeta(meta);
            }
        }

        int selectedSlot = player.getInventory().getHeldItemSlot();
        if (player.getInventory().getItem(selectedSlot) == null) {
            player.getInventory().setItem(selectedSlot, returnedBoomerang);
            SoundUtils.playReceiveSound(player, soundSection);
        } else {
            player.getWorld().dropItemNaturally(player.getLocation(), returnedBoomerang);
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