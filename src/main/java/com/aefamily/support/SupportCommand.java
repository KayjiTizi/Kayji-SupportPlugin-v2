package com.aefamily.support;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SupportCommand implements CommandExecutor {
    private final SupportPlugin plugin;
    private final SupportManager manager;

    public SupportCommand(SupportPlugin plugin, SupportManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) return false;
        Player player = (Player) sender;
        if (!player.hasPermission("supportplugin.use")) {
            player.sendMessage(SupportPlugin.color(
                    plugin.getConfig().getString("messages.no-permission")
            ));
            return true;
        }
        if (args.length == 0) {
            player.sendMessage(SupportPlugin.color(
                    plugin.getConfig().getString("messages.usage-support")
            ));
            return true;
        }
        String message = String.join(" ", args);
        int id = manager.create(player.getName(), message);
        player.sendMessage(SupportPlugin.color(
                plugin.getConfig().getString("messages.support-received")
                        .replace("{id}", String.valueOf(id))
        ));
        return true;
    }
}
