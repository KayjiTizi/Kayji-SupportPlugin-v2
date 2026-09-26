package com.aefamily.support;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class GUIManager implements Listener {
    private static final String INVENTORY_TITLE = "Support Tickets";
    private static final int BOOK_PAGE_LIMIT = 230;

    private final SupportManager manager;
    private final Map<UUID, Integer> pageMap = new HashMap<>();

    public GUIManager(SupportPlugin plugin, SupportManager manager) {
        this.manager = manager;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void openGUI(Player player, int page) {
        List<SupportRequest> list = new ArrayList<>(manager.all());
        int size = 54;
        Inventory gui = Bukkit.createInventory(null, size, INVENTORY_TITLE);
        int start = page * 45;
        for (int i = start; i < Math.min(start + 45, list.size()); i++) {
            SupportRequest req = list.get(i);
            gui.setItem(i - start, createTicketIcon(req));
        }

        ItemStack prev = new ItemStack(Material.ARROW);
        ItemMeta pm = prev.getItemMeta();
        if (pm != null) {
            pm.setDisplayName(ChatColor.RED + "Trang truoc");
            prev.setItemMeta(pm);
        }

        ItemStack next = new ItemStack(Material.ARROW);
        ItemMeta nm = next.getItemMeta();
        if (nm != null) {
            nm.setDisplayName(ChatColor.GREEN + "Trang tiep theo");
            next.setItemMeta(nm);
        }

        if (page > 0) {
            gui.setItem(45, prev);
        }
        if ((page + 1) * 45 < list.size()) {
            gui.setItem(53, next);
        }

        player.openInventory(gui);
        pageMap.put(player.getUniqueId(), page);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        if (!INVENTORY_TITLE.equals(event.getView().getTitle())) {
            return;
        }
        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();
        int slot = event.getRawSlot();
        int page = pageMap.getOrDefault(player.getUniqueId(), 0);
        List<SupportRequest> list = new ArrayList<>(manager.all());

        if (slot == 45 && page > 0) {
            openGUI(player, page - 1);
            return;
        }
        if (slot == 53 && (page + 1) * 45 < list.size()) {
            openGUI(player, page + 1);
            return;
        }

        int index = page * 45 + slot;
        if (index < 0 || index >= list.size()) {
            return;
        }
        SupportRequest req = list.get(index);

        if (event.isRightClick() && !event.isShiftClick()) {
            if (manager.remove(req.getId())) {
                player.sendMessage(SupportPlugin.color("&cTicket #" + req.getId() + " da bi xoa."));
            }
            openGUI(player, page);
            return;
        }

        if (event.isLeftClick() && event.isShiftClick()) {
            Status[] vals = Status.values();
            int nextIdx = (req.getStatus().ordinal() + 1) % vals.length;
            Status next = vals[nextIdx];
            req.setStatus(next);
            manager.updateStatus(req.getId(), next);
            player.sendMessage(SupportPlugin.color(
                    "&aTicket #" + req.getId() + " chuyen sang &e" + next.getDisplayName()
            ));
            openGUI(player, page);
            return;
        }

        if (event.isLeftClick()) {
            player.closeInventory();
            openTicketBook(player, req);
        }
    }

    private ItemStack createTicketIcon(SupportRequest req) {
        ItemStack icon = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta meta = icon.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GREEN + "#" + req.getId() + " " + req.getPlayer());
            meta.setLore(Arrays.asList(
                    ChatColor.YELLOW + "Time: " + req.getTime(),
                    ChatColor.AQUA + "Status: " + req.getStatus().getDisplayName(),
                    ChatColor.GOLD + "Priority: " + req.getPriority().getDisplayName(),
                    "",
                    ChatColor.GRAY + "Left-click: Mo vao so ticket",
                    ChatColor.GRAY + "Shift + Left: Doi trang thai",
                    ChatColor.GRAY + "Right-click: Xoa ticket"
            ));
            icon.setItemMeta(meta);
        }
        return icon;
    }

    private void openTicketBook(Player player, SupportRequest req) {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();
        if (meta == null) {
            player.sendMessage(ChatColor.RED + "Khong the mo ticket nay.");
            return;
        }
        meta.setTitle("Ticket #" + req.getId());
        meta.setAuthor(req.getPlayer());

        List<String> pages = new ArrayList<>();
        StringBuilder header = new StringBuilder();
        header.append(ChatColor.BOLD).append("Ticket #").append(req.getId()).append("\n");
        header.append(ChatColor.RESET).append(ChatColor.GRAY).append("Nguoi choi: ").append(ChatColor.WHITE).append(req.getPlayer()).append("\n");
        header.append(ChatColor.GRAY).append("The loai: ").append(ChatColor.WHITE).append(req.getCategory()).append("\n");
        header.append(ChatColor.GRAY).append("Uu tien: ").append(ChatColor.WHITE).append(req.getPriority().getDisplayName()).append("\n");
        header.append(ChatColor.GRAY).append("Trang thai: ").append(ChatColor.WHITE).append(req.getStatus().getDisplayName()).append("\n");
        header.append(ChatColor.GRAY).append("Tao luc: ").append(ChatColor.WHITE).append(req.getTime()).append("\n");
        header.append(ChatColor.GRAY).append("Cap nhat: ").append(ChatColor.WHITE).append(req.getLastUpdated());
        pages.add(header.toString());

        List<String> contentPages = paginateMessage(req.getMessage());
        for (String content : contentPages) {
            pages.add(ChatColor.DARK_GRAY + "Noi dung:\n" + ChatColor.BLACK + content);
        }
        meta.setPages(pages);
        book.setItemMeta(meta);
        player.openBook(book);
    }

    private List<String> paginateMessage(String message) {
        String text = message == null ? "" : message.replace("\r", "");
        if (text.isEmpty()) {
            return Collections.singletonList(ChatColor.GRAY + "(Khong co noi dung)");
        }
        List<String> pages = new ArrayList<>();
        int length = text.length();
        int cursor = 0;
        while (cursor < length) {
            int end = Math.min(cursor + BOOK_PAGE_LIMIT, length);
            int newline = text.lastIndexOf('\n', end);
            if (newline >= cursor && newline < end) {
                end = newline + 1;
            } else if (end < length) {
                int space = text.lastIndexOf(' ', end);
                if (space >= cursor + 40) {
                    end = space + 1;
                }
            }
            pages.add(text.substring(cursor, end));
            cursor = end;
        }
        return pages;
    }
}
