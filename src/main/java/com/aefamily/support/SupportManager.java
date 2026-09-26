package com.aefamily.support;

import org.bukkit.plugin.java.JavaPlugin;

import com.aefamily.support.db.DBManager;
import com.aefamily.support.db.JdbcSupportRequestDAO;
import com.aefamily.support.db.SupportRequestDAO;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SupportManager {
    private static final int WEBHOOK_MESSAGE_LIMIT = 1800;

    private final JavaPlugin plugin;
    private final SupportRequestDAO dao;
    private final SimpleDateFormat dateFormat;
    private final String webhookUrl;
    private final DBManager dbManager;
    private final List<String> categories;
    private final String defaultCategory;
    private final Priority defaultPriority;
    private final boolean notifyConsole;
    private final boolean notifyUpdates;
    private final int minMessageLength;
    private final int maxMessageLength;

    public SupportManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.dbManager = new DBManager(plugin);
        this.dao = new JdbcSupportRequestDAO(dbManager.getDataSource());
        this.dateFormat = new SimpleDateFormat(plugin.getConfig().getString("support.date-format", "dd/MM/yyyy HH:mm:ss"));
        this.webhookUrl = plugin.getConfig().getString("support.webhook-url");
        List<String> configuredCategories = new ArrayList<>(plugin.getConfig().getStringList("support.categories"));
        if (configuredCategories.isEmpty()) {
            configuredCategories.add("GENERAL");
            configuredCategories.add("BUG");
            configuredCategories.add("PAYMENT");
            configuredCategories.add("TECH");
        }
        this.categories = new ArrayList<>();
        for (String entry : configuredCategories) {
            categories.add(entry.trim().toUpperCase(Locale.ENGLISH));
        }
        this.defaultCategory = sanitizeCategory(plugin.getConfig().getString("support.default-category", categories.get(0)));
        Priority parsedPriority = Priority.fromString(plugin.getConfig().getString("support.default-priority", "NORMAL"));
        this.defaultPriority = parsedPriority == null ? Priority.NORMAL : parsedPriority;
        this.notifyConsole = plugin.getConfig().getBoolean("support.notify-console", true);
        this.notifyUpdates = plugin.getConfig().getBoolean("support.notify-on-update", true);
        this.minMessageLength = plugin.getConfig().getInt("support.min-message-length", 5);
        this.maxMessageLength = plugin.getConfig().getInt("support.max-message-length", 400);
    }

    public int create(String player, String message) {
        return create(player, message, defaultCategory, defaultPriority);
    }

    public int create(String player, String message, String category, Priority priority) {
        String normalizedCategory = sanitizeCategory(category);
        Priority appliedPriority = priority == null ? defaultPriority : priority;
        String time = dateFormat.format(new Date());
        SupportRequest req = new SupportRequest(0, player, message, time, Status.OPEN,
                normalizedCategory, appliedPriority, null, time);
        try {
            int id = dao.create(req);
            req.setId(id);
            if (notifyConsole) {
                plugin.getLogger().info(String.format(Locale.ENGLISH,
                        "[Support] #%d %s (%s/%s)", id, player, normalizedCategory, appliedPriority.name()));
            }
            notifyDiscordCreate(req);
            return id;
        } catch (Exception e) {
            plugin.getLogger().severe("Loi khi tao ticket support: " + e.getMessage());
            return -1;
        }
    }

    public SupportRequest get(int id) {
        try {
            return dao.findById(id);
        } catch (Exception e) {
            plugin.getLogger().severe("Loi khi lay ticket: " + e.getMessage());
            return null;
        }
    }

    public List<SupportRequest> all() {
        try {
            List<SupportRequest> data = dao.findAll();
            data.sort(Comparator
                    .comparingInt((SupportRequest req) -> req.getStatus().getSortOrder())
                    .thenComparingInt(req -> -req.getPriority().getWeight())
                    .thenComparing((SupportRequest req) -> req.getId(), Comparator.reverseOrder()));
            return data;
        } catch (Exception e) {
            plugin.getLogger().severe("Loi khi lay danh sach ticket: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public List<SupportRequest> all(Status filter) {
        List<SupportRequest> data = all();
        if (filter == null) {
            return data;
        }
        List<SupportRequest> filtered = new ArrayList<>();
        for (SupportRequest req : data) {
            if (req.getStatus() == filter) {
                filtered.add(req);
            }
        }
        return filtered;
    }

    public List<SupportRequest> byPlayer(String player) {
        try {
            return dao.findByPlayer(player);
        } catch (Exception e) {
            plugin.getLogger().severe("Loi khi lay ticket cua nguoi choi: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public boolean save(SupportRequest req) {
        req.setLastUpdated(dateFormat.format(new Date()));
        try {
            return dao.update(req);
        } catch (Exception e) {
            plugin.getLogger().severe("Loi khi luu ticket #" + req.getId() + ": " + e.getMessage());
            return false;
        }
    }

    public boolean updateStatus(int id, Status status) {
        SupportRequest req = get(id);
        if (req == null) {
            return false;
        }
        req.setStatus(status);
        return save(req);
    }

    public boolean update(SupportRequest request) {
        return save(request);
    }

    public boolean remove(int id) {
        SupportRequest req = get(id);
        if (req == null) {
            return false;
        }
        try {
            boolean result = dao.delete(id);
            if (result && notifyUpdates) {
                req.setStatus(Status.CLOSED);
                notifyTicketUpdate(req, "Ticket da bi xoa", "System");
            }
            return result;
        } catch (Exception e) {
            plugin.getLogger().severe("Loi khi xoa ticket: " + e.getMessage());
            return false;
        }
    }

    public void shutdown() {
        dbManager.close();
    }

    public List<String> getCategories() {
        return Collections.unmodifiableList(categories);
    }

    public String getDefaultCategory() {
        return defaultCategory;
    }

    public Priority getDefaultPriority() {
        return defaultPriority;
    }

    public boolean isValidCategory(String category) {
        if (category == null || category.isEmpty()) {
            return false;
        }
        return categories.contains(category.trim().toUpperCase(Locale.ENGLISH));
    }

    public String sanitizeCategory(String input) {
        if (input == null || input.isEmpty()) {
            return defaultCategory;
        }
        String normalized = input.trim().toUpperCase(Locale.ENGLISH);
        if (!categories.contains(normalized)) {
            return defaultCategory;
        }
        return normalized;
    }

    public int getMinMessageLength() {
        return minMessageLength;
    }

    public int getMaxMessageLength() {
        return maxMessageLength;
    }

    public void notifyTicketUpdate(SupportRequest req, String action, String actor) {
        if (!notifyUpdates) {
            return;
        }
        String description = "**" + action + "** boi " + actor + "\n\n" + formatMessageBlock(req.getMessage());
        sendWebhook(req, "Ticket #" + req.getId() + " cap nhat", description,
                req.getPriority().getDiscordColor());
    }

    private void notifyDiscordCreate(SupportRequest req) {
        String description = "Noi dung:\n" + formatMessageBlock(req.getMessage());
        sendWebhook(req, "Ticket #" + req.getId() + " moi", description,
                req.getPriority().getDiscordColor());
    }

    private void sendWebhook(SupportRequest req, String title, String description, int color) {
        if (webhookUrl == null || webhookUrl.isEmpty()) {
            return;
        }
        Map<String, String> fields = new LinkedHashMap<>();
        fields.put("Nguoi choi", req.getPlayer());
        fields.put("The loai", req.getCategory());
        fields.put("Uu tien", req.getPriority().getDisplayName());
        fields.put("Trang thai", req.getStatus().getDisplayName());
        fields.put("Assigned cho", req.isAssigned() ? req.getAssignedStaff() : "-");
        fields.put("Tao luc", req.getTime());
        fields.put("Cap nhat", req.getLastUpdated());
        new Thread(() -> {
            try {
                DiscordWebhook.send(webhookUrl, title, description, color, fields);
            } catch (IOException e) {
                plugin.getLogger().severe("Khong the gui webhook Discord: " + e.getMessage());
            }
        }).start();
    }

    private String formatMessageBlock(String message) {
        if (message == null || message.trim().isEmpty()) {
            return "`(Khong co noi dung)`";
        }
        String sanitized = message.trim().replace("```", "` ` `");
        if (sanitized.length() > WEBHOOK_MESSAGE_LIMIT) {
            sanitized = sanitized.substring(0, WEBHOOK_MESSAGE_LIMIT) + "...";
        }
        return "```" + sanitized + "```";
    }
}
