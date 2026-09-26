package com.aefamily.support;

import org.bukkit.ChatColor;

import java.text.Normalizer;

public enum Priority {
    LOW("Thấp", ChatColor.DARK_GREEN, 0x2ECC71, 0),
    NORMAL("Bình thường", ChatColor.GREEN, 0x3498DB, 1),
    HIGH("Cao", ChatColor.GOLD, 0xF1C40F, 2),
    CRITICAL("Khẩn cấp", ChatColor.DARK_RED, 0xE74C3C, 3);

    private final String displayName;
    private final ChatColor color;
    private final int discordColor;
    private final int weight;

    Priority(String displayName, ChatColor color, int discordColor, int weight) {
        this.displayName = displayName;
        this.color = color;
        this.discordColor = discordColor;
        this.weight = weight;
    }

    public String getDisplayName() {
        return displayName;
    }

    public ChatColor getColor() {
        return color;
    }

    public int getDiscordColor() {
        return discordColor;
    }

    public int getWeight() {
        return weight;
    }

    public String formatForChat() {
        return color + displayName;
    }

    public static Priority fromString(String input) {
        if (input == null || input.isEmpty()) {
            return null;
        }
        String normalized = normalize(input);
        switch (normalized) {
            case "low":
            case "thap":
                return LOW;
            case "normal":
            case "binhthuong":
            case "thuong":
                return NORMAL;
            case "high":
            case "cao":
                return HIGH;
            case "critical":
            case "khancap":
            case "khanc":
                return CRITICAL;
            default:
                return null;
        }
    }

    public static Priority next(Priority current) {
        Priority[] values = values();
        int idx = current.ordinal() + 1;
        if (idx >= values.length) {
            idx = 0;
        }
        return values[idx];
    }

    private static String normalize(String text) {
        if (text == null) {
            return "";
        }
        String tmp = Normalizer.normalize(text, Normalizer.Form.NFD)
                .replace("Đ", "D")
                .replace("đ", "d")
                .replaceAll("\\p{M}", "")
                .replaceAll("[^A-Za-z]", "")
                .toLowerCase();
        return tmp;
    }
}
