package me.abisgamer.ultraboomerangs.commands;

import me.abisgamer.ultraboomerangs.UltraBoomerangs;
import me.abisgamer.ultraboomerangs.utils.itemBuilder;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;


public class giveCommand implements CommandExecutor {
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            FileConfiguration messages = UltraBoomerangs.plugin.messages;
            Player player = (Player) sender;
            if (cmd.getName().equalsIgnoreCase("ultraboomerangs")) {
                if (player.hasPermission("ultraboomerangs.give")) {
                    if (args.length == 2) {
                        ItemStack boomerang = itemBuilder.boomerangs.get(args[1]);
                        if (boomerang != null) {
                            player.getInventory().addItem(boomerang);
                            player.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.getString("prefix") + messages.getString("received") + args[1]));
                        } else {
                            player.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.getString("prefix") + messages.getString("invalid")));
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