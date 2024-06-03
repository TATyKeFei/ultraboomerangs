package me.abisgamer.ultraboomerangs.commands;

import me.abisgamer.ultraboomerangs.UltraBoomerangs;
import me.abisgamer.ultraboomerangs.utils.itemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class giveCommand implements CommandExecutor {
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        FileConfiguration messages = UltraBoomerangs.plugin.messages;

        if (cmd.getName().equalsIgnoreCase("ultraboomerangs")) {
            if (args.length >= 2) {
                String playerName = args[0];
                String boomerangId = args[1];

                Player targetPlayer = Bukkit.getPlayerExact(playerName);
                if (targetPlayer != null) {
                    ItemStack boomerang = itemBuilder.boomerangs.get(boomerangId);
                    if (boomerang != null) {
                        if (sender.hasPermission("ultraboomerangs.give")) {
                            targetPlayer.getInventory().addItem(boomerang);
                            targetPlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.getString("prefix") + messages.getString("received") + boomerangId));
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.getString("prefix") + "Boomerang given to " + targetPlayer.getName()));
                        } else {
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.getString("prefix") + ChatColor.RED + "You do not have permission to use this command."));
                        }
                    } else {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.getString("prefix") + messages.getString("invalid")));
                    }
                } else {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.getString("prefix") + "Player not found."));
                }
                return true;
            } else if (args.length == 1) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    String boomerangId = args[0];
                    ItemStack boomerang = itemBuilder.boomerangs.get(boomerangId);
                    if (boomerang != null) {
                        if (sender.hasPermission("ultraboomerangs.give")) {
                            player.getInventory().addItem(boomerang);
                            player.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.getString("prefix") + messages.getString("received") + boomerangId));
                        } else {
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.getString("prefix") + ChatColor.RED + "You do not have permission to use this command."));
                        }
                    } else {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.getString("prefix") + messages.getString("invalid")));
                    }
                    return true;
                } else {
                    sender.sendMessage("Usage: /ultraboomerangs give <player> <boomerang_id>");
                    return false;
                }
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.getString("prefix") + messages.getString("id-error")));
                return false;
            }
        }
        return false;
    }
}
