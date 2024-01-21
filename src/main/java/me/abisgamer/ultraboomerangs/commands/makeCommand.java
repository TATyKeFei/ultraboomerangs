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
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (cmd.getName().equalsIgnoreCase("ultraboomerangs")) {
                if (player.hasPermission("ultraboomerangs.make")) {
                    if (args.length == 2) {
                        if (player.getInventory().getItemInMainHand().getType() != Material.AIR) {
                            ConfigurationSection config = UltraBoomerangs.plugin.getConfig();
                            ConfigurationSection boomerangSection = config.getConfigurationSection("boomerangs");
                            ItemStack boomerang = player.getInventory().getItemInMainHand();
                            boomerangSection.set(args[1] + ".itemstack", boomerang);
                            boomerangSection.set(args[1] + ".is-itemstack", true);
                            boomerangSection.set(args[1] + ".damage", 50);
                            boomerangSection.set(args[1] + ".travel-distance", 10);
                            UltraBoomerangs.plugin.saveConfig();
                            player.sendMessage(ChatColor.translateAlternateColorCodes('&',messages.getString("prefix") + messages.getString("make-success") + args[1]));
                            itemBuilder.createBoomerangs();
                        }
                    } else {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&',messages.getString("prefix") + messages.getString("id-error")));
                        return false;
                    }
                }
            }
        } else {
            UltraBoomerangs.plugin.getLogger().info("This command can only be executed by a player");
        }
        return false;
    }
}
