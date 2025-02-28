package me.abisgamer.ultraboomerangs.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class mainCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length > 0) {
            String subCommand = args[0];
            String[] subArgs = new String[args.length - 1];
            System.arraycopy(args, 1, subArgs, 0, args.length - 1);

            if (subCommand.equalsIgnoreCase("give")) {
                return new giveCommand().onCommand(sender, command, label, subArgs);
            } else if (subCommand.equalsIgnoreCase("help")) {
                return new helpCommand().onCommand(sender, command, label, subArgs);
            } else if (subCommand.equalsIgnoreCase("make")) {
                return new makeCommand().onCommand(sender, command, label, subArgs);
            } else if (subCommand.equalsIgnoreCase("reload")) {
                return new reloadCommand().onCommand(sender, command, label, subArgs);
            } else if (subCommand.equalsIgnoreCase("list")) {
                return new listCommand().onCommand(sender, command, label, subArgs);
            }
        } else {
            return new helpCommand().onCommand(sender, command, label, args);
        }
        return false;
    }
}
