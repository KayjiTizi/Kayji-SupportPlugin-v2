package com.aefamily.support;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SupportGUICommand implements CommandExecutor {

    private final SupportPlugin plugin;
    private final GUIManager gui;

    public SupportGUICommand(SupportPlugin plugin, GUIManager gui) {
        this.plugin = plugin;
        this.gui = gui;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) return false;
        Player p = (Player) sender;
        if (!p.hasPermission("supportplugin.manage")) {
            p.sendMessage(SupportPlugin.color(
                    plugin.getConfig().getString("messages.no-permission")
            ));
            return true;
        }
        gui.openGUI(p, 0);
        return true;
    }
}
