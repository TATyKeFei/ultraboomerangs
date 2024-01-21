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

public class reloadCommand implements CommandExecutor {
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
            FileConfiguration messages = UltraBoomerangs.plugin.messages;

        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (cmd.getName().equalsIgnoreCase("ultraboomerangs")) {
                if (player.hasPermission("ultraboomerangs.reload")) {
                    UltraBoomerangs.plugin.reloadConfig();
                    itemBuilder.createBoomerangs();
                    UltraBoomerangs.plugin.reloadCustomConfig();
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.getString("prefix") + messages.getString("reload")));
                }
            }
        } else {
            UltraBoomerangs.plugin.getConfig().options().copyDefaults();
            UltraBoomerangs.plugin.saveDefaultConfig();
            itemBuilder.createBoomerangs();
            UltraBoomerangs.plugin.reloadCustomConfig();
            UltraBoomerangs.plugin.getLogger().info("Plugin reloaded successfully");
        }
        return false;
    }
}
