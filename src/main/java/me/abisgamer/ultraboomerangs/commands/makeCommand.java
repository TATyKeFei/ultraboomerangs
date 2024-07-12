package me.abisgamer.ultraboomerangs.commands;

import me.abisgamer.ultraboomerangs.UltraBoomerangs;
import me.abisgamer.ultraboomerangs.utils.itemBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class makeCommand implements CommandExecutor {
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        FileConfiguration messages = UltraBoomerangs.plugin.messages;

        if (cmd.getName().equalsIgnoreCase("ultraboomerangs")) {
            if (args.length == 1) {
                String boomerangId = args[0];

                if (sender instanceof Player) {
                    Player player = (Player) sender;

                    if (player.hasPermission("ultraboomerangs.make")) {
                        ItemStack boomerang = player.getInventory().getItemInMainHand();
                        if (boomerang.getType() != Material.AIR) {
                            ConfigurationSection config = UltraBoomerangs.plugin.getConfig();
                            ConfigurationSection boomerangSection = config.createSection("boomerangs." + boomerangId);
                            boomerangSection.set("itemstack", boomerang);
                            boomerangSection.set("is-itemstack", true);
                            boomerangSection.set("damage", 50);
                            boomerangSection.set("travel-distance", 10);
                            boomerangSection.set("click-type", "right");
                            boomerangSection.set("cooldown", 3);
                            boomerangSection.set("mcmmo_skill", "Archery");
                            boomerangSection.set("mcmmo_skill_amount", 10);
                            boomerangSection.set("auraskills_skill", "Archery");
                            boomerangSection.set("auraskills_skill_amount", 10);
                            boomerangSection.set("auto-pickup", true);

                            ConfigurationSection soundsSection = boomerangSection.createSection("sounds");
                            soundsSection.set("enabled", true);
                            soundsSection.set("throw-sound", "ENTITY_ENDER_DRAGON_FLAP");
                            soundsSection.set("receive-sound", "ENTITY_ITEM_PICKUP");
                            soundsSection.set("volume", 1.0);
                            soundsSection.set("pitch", 1.0);

                            ConfigurationSection armorStandSection = boomerangSection.createSection("armorstand_arm");
                            armorStandSection.set("x_rotation", 0);
                            armorStandSection.set("y_rotation", 20);
                            armorStandSection.set("z_rotation", 0);
                            ConfigurationSection launchOffsetSection = boomerangSection.createSection("launch_offset");
                            launchOffsetSection.set("x", 0);
                            launchOffsetSection.set("y", 0);
                            launchOffsetSection.set("z", 0);
                            boomerangSection.set("rotation_type", "normal");

                            UltraBoomerangs.plugin.saveConfig();
                            player.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.getString("prefix") + messages.getString("make-success") + boomerangId));
                            itemBuilder.createBoomerangs();
                        } else {
                            player.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.getString("prefix") + ChatColor.RED + "You must be holding an item to make a boomerang."));
                        }
                    } else {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.getString("prefix") + ChatColor.RED + "You do not have permission to use this command."));
                    }
                } else {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.getString("prefix") + ChatColor.RED + "This command can only be executed by a player."));
                }
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.getString("prefix") + messages.getString("id-error")));
                return false;
            }
        }
        return true;
    }
}
