package com.aefamily.support;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class UpdateCommand implements CommandExecutor {
    private final SupportPlugin plugin;
    private final SupportManager manager;

    public UpdateCommand(SupportPlugin plugin, SupportManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("supportplugin.admin")) {
            sender.sendMessage(SupportPlugin.color(plugin.getConfig().getString("messages.no-permission")));
            return true;
        }

        if (args.length != 2) {
            sender.sendMessage(SupportPlugin.color("&cCách sử dụng: /support update <id> <status>"));
            return true;
        }

        int id;
        try {
            id = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            sender.sendMessage(SupportPlugin.color("&cID không hợp lệ!"));
            return true;
        }

        SupportRequest request = manager.get(id);
        if (request == null) {
            sender.sendMessage(SupportPlugin.color("&cKhông tìm thấy yêu cầu hỗ trợ."));
            return true;
        }

        Status status;
        try {
            status = Status.valueOf(args[1].toUpperCase());
        } catch (IllegalArgumentException e) {
            sender.sendMessage(SupportPlugin.color("&cTrạng thái không hợp lệ. Sử dụng một trong: &eMở, Đang xử lý, Đã giải quyết, Đóng"));
            return true;
        }

        request.setStatus(status);
        manager.update(request);
        sender.sendMessage(SupportPlugin.color("&aYêu cầu đã cập nhật #" + id + " đến trạng thái: &e" + status.name()));
        return true;
    }
}
