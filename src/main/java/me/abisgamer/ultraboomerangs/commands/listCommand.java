package me.abisgamer.ultraboomerangs.commands;

import me.abisgamer.ultraboomerangs.UltraBoomerangs;
import me.abisgamer.ultraboomerangs.utils.itemBuilder;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class listCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        // Check for permission if needed.
        if (!sender.hasPermission("ultraboomerangs.list")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    UltraBoomerangs.plugin.messages.getString("prefix") + "&cYou do not have permission to use this command."));
            return true;
        }

        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                UltraBoomerangs.plugin.messages.getString("prefix") + "&aAvailable Boomerangs:"));

        // List all boomerang IDs from itemBuilder.boomerangs
        for (String boomerangId : itemBuilder.boomerangs.keySet()) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&e- " + boomerangId));
        }
        return true;
    }
}
