package me.abisgamer.ultraboomerangs.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class mainCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("give")) {
                return new giveCommand().onCommand(sender, command, label, args);
            } else if (args[0].equalsIgnoreCase("help")) {
                return new helpCommand().onCommand(sender, command, label, args);
            } else if (args[0].equalsIgnoreCase("make")) {
                return new makeCommand().onCommand(sender, command, label, args);
            }
            else if (args[0].equalsIgnoreCase("reload")) {
                return new reloadCommand().onCommand(sender, command, label, args);
            }
        } else {
            return new helpCommand().onCommand(sender, command, label, args);
        }
        return false;
    }
}
