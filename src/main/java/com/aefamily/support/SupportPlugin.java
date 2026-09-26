package com.aefamily.support;

import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public class SupportPlugin extends JavaPlugin {
    private SupportManager manager;

    @Override
    public void onEnable() {
        try {
            saveDefaultConfig();
            manager = new SupportManager(this);
            GUIManager gui = new GUIManager(this, manager);

            getCommand("support").setExecutor(new SupportCommand(this, manager));
            getCommand("supportupdate").setExecutor(new UpdateCommand(this, manager)); // thêm dòng này
            getCommand("supportstatus").setExecutor(new StatusCommand(this, manager));
            getCommand("supportreload").setExecutor(new ReloadCommand(this));
            getCommand("supportgui").setExecutor(new SupportGUICommand(this, gui));

            getLogger().info("SupportPlugin đã kích hoạt.");
        } catch (Throwable t) {
            getLogger().severe("Lỗi khi khởi động plugin: " + t.getMessage());
            t.printStackTrace();
            // Nếu muốn tắt plugin ngay
            this.getServer().getPluginManager().disablePlugin(this);
        }
    }
    /** Đổi &a, &c,… thành ChatColor */
    public static String color(String msg) {
        return ChatColor.translateAlternateColorCodes('&', msg);
    }
        @Override
    public void onDisable() {
        // chỉ đóng khi manager đã khởi tạo
        if (manager != null) {
            try {
                manager.shutdown();  // đóng DataSource/Hikari pool
            } catch (Exception ignored) {}
        }
        getLogger().info("SupportPlugin đã tắt.");
    }
}
