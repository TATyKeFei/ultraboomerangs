package me.abisgamer.ultraboomerangs.commands;

import me.abisgamer.ultraboomerangs.UltraBoomerangs;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class helpCommand implements CommandExecutor {

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("ultraboomerangs")) {
            sender.sendMessage(ChatColor.GOLD + "--------+ UltraBoomerangs Help +--------");
            sender.sendMessage(ChatColor.GREEN + "/ultraboomberangs give <player> <boomerang_id>" + ChatColor.GOLD + " Give a boomerang to a player");
            sender.sendMessage(ChatColor.GREEN + "/ultraboomberangs give <boomerang_id>" + ChatColor.GOLD + " Give a boomerang to yourself");
            sender.sendMessage(ChatColor.GREEN + "/ultraboomberangs make <boomerang_id>" + ChatColor.GOLD + " Make a new boomerang with the item you're holding");
            sender.sendMessage(ChatColor.GREEN + "/ultraboomberangs reload" + ChatColor.GOLD + " Reload the config.yml and messages.yml files");
            sender.sendMessage(ChatColor.GREEN + "/ultraboomberangs help" + ChatColor.GOLD + " Show this help message");
            sender.sendMessage(ChatColor.GOLD + "--------+ By AbisGamer +--------");
        }
        return true;
    }
}
