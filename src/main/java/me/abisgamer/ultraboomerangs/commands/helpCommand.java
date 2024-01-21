package me.abisgamer.ultraboomerangs.commands;

import me.abisgamer.ultraboomerangs.UltraBoomerangs;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Objects;

public class helpCommand implements CommandExecutor {

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (cmd.getName().equalsIgnoreCase("ultraboomerangs")) {
                if (player.hasPermission("ultraboomerangs.give")) {
                            player.sendMessage(ChatColor.GOLD + "--------+ UltraBoomerangs Help +--------");
                            player.sendMessage(ChatColor.GREEN + "/ultraboomberangs give <boomerang_id>" + ChatColor.GOLD + " Give a boomerang to yourself");
                            player.sendMessage(ChatColor.GREEN + "/ultraboomberangs make <boomerang_id>" + ChatColor.GOLD + " Make a new boomerang with the item you're holding");
                            player.sendMessage(ChatColor.GREEN + "/ultraboomberangs reload" + ChatColor.GOLD + " reload the config.yml and messages.yml files");
                            player.sendMessage(ChatColor.GREEN + "/ultraboomberangs help" + ChatColor.GOLD + " Show this help message");
                            player.sendMessage(ChatColor.GOLD + "--------+ By AbisGamer +--------");
                    }
                }
            } else {
            UltraBoomerangs.plugin.getLogger().info("This command can only be executed by a player");
        }
        return false;
    }
    }
