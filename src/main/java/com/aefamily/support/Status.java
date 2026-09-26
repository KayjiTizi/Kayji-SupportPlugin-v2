package com.aefamily.support;

import org.bukkit.ChatColor;

import java.text.Normalizer;

public enum Status {
    OPEN("Mở", ChatColor.YELLOW, 0),
    IN_PROGRESS("Đang xử lý", ChatColor.GOLD, 1),
    RESOLVED("Đã giải quyết", ChatColor.GREEN, 2),
    CLOSED("Đã đóng", ChatColor.GRAY, 3);

    private final String displayName;
    private final ChatColor color;
    private final int sortOrder;

    Status(String displayName, ChatColor color, int sortOrder) {
        this.displayName = displayName;
        this.color = color;
        this.sortOrder = sortOrder;
    }

    public String getDisplayName() {
        return displayName;
    }

    public ChatColor getColor() {
        return color;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public String formatForChat() {
        return color + displayName;
    }

    public static Status fromVietnamese(String input) {
        return fromInput(input);
    }

    public static Status fromInput(String input) {
        if (input == null || input.isEmpty()) {
            return null;
        }
        String clean = normalize(input);
        switch (clean) {
            case "dangcho":
            case "open":
                return OPEN;
            case "dangxuly":
            case "inprogress":
            case "processing":
            case "process":
                return IN_PROGRESS;
            case "dagiaiquyet":
            case "resolved":
            case "done":
                return RESOLVED;
            case "dadong":
            case "closed":
            case "close":
                return CLOSED;
            default:
                return null;
        }
    }

    private static String normalize(String input) {
        String clean = Normalizer.normalize(input, Normalizer.Form.NFD)
                .replace("Đ", "D")
                .replace("đ", "d")
                .replaceAll("\\p{M}", "")
                .replaceAll("\\s+", "")
                .toLowerCase();
        return clean;
    }
}
