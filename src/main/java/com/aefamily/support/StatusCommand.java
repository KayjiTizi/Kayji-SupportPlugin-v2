package com.aefamily.support;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StatusCommand implements CommandExecutor {
    private final SupportPlugin plugin;
    private final SupportManager manager;

    public StatusCommand(SupportPlugin plugin, SupportManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) return false;
        Player player = (Player) sender;
        if (args.length == 0) {
            player.sendMessage(SupportPlugin.color(
                    plugin.getConfig().getString("messages.usage-support")
            ));
            return true;
        }

        int id;
        try {
            id = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            player.sendMessage(SupportPlugin.color(
                    plugin.getConfig().getString("messages.support-no-found")
            ));
            return true;
        }

        SupportRequest req = manager.get(id);
        if (req == null || !req.getPlayer().equals(player.getName())) {
            player.sendMessage(SupportPlugin.color(
                    plugin.getConfig().getString("messages.support-no-found")
            ));
            return true;
        }

        player.sendMessage(SupportPlugin.color(
                plugin.getConfig().getString("messages.support-status")
                        .replace("{id}", String.valueOf(req.getId()))
                        .replace("{status}", req.getStatus().getDisplayName())
        ));
        return true;
    }
}
