package com.aefamily.support;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ReloadCommand implements CommandExecutor {
    private final SupportPlugin plugin;

    public ReloadCommand(SupportPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("supportplugin.reload")) {
            sender.sendMessage(SupportPlugin.color(
                    plugin.getConfig().getString("messages.no-permission")
            ));
            return true;
        }
        try {
            plugin.reloadConfig();
            sender.sendMessage(SupportPlugin.color(
                    plugin.getConfig().getString("messages.reload-success")
            ));
        } catch (Exception e) {
            sender.sendMessage(SupportPlugin.color(
                    plugin.getConfig().getString("messages.reload-fail")
                            .replace("{error}", e.getMessage())
            ));

        }
        return true;
    }
}
