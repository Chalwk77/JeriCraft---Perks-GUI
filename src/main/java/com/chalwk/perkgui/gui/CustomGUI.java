/* Copyright (c) 2023, JeriCraftPerks. Jericho Crosby <jericho.crosby227@gmail.com> */
package com.chalwk.perkgui.gui;

import com.chalwk.perkgui.Main;
import com.chalwk.perkgui.data.PlayerDataManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.chalwk.perkgui.Main.*;
import static com.chalwk.perkgui.gui.MainMenu.showMenu;

public class CustomGUI {

    private final List<GUIButton> buttons = new ArrayList<>();
    private Inventory inventory;

    public CustomGUI(String title, int rows) {
        if (rows > 6) {
            Main.getInstance().getLogger().warning("Too many rows!");
            return;
        }
        this.inventory = Bukkit.createInventory(null, rows * 9, title);
    }

    public void setItem(GUIButton button, int slot) {
        buttons.add(button);
        inventory.setItem(slot, button.getStack());
    }

    public void handleButton(ItemStack stack) {
        buttons.stream().filter(p -> p.getStack().isSimilar(stack)).findAny().ifPresent(button -> button.getAction().run());
    }

    public void show(Player player) {
        PlayerDataManager.getData(player).setOpenGUI(this);
        player.openInventory(inventory);
    }

    public void close(Player player) {
        player.closeInventory();
    }

    public void fillEmptySlots(int maxSlots) {
        String emptySlots = config.getString("GUI_EMPTY_SLOTS");
        ItemStack item = this.createItem(emptySlots, "", new ArrayList<>(), false);
        GUIButton button = new GUIButton(item);
        for (int i = 0; i < maxSlots; i++) {
            if (!this.getItem(i)) {
                this.setItem(button, i);
            }
        }
        button.setAction(() -> {
        });
    }

    public void showCloseButton(Player sender, int slot, boolean Enchant) {
        String closeIcon = config.getString("GUI_CLOSE_BUTTON");
        String closeIconText = config.getString("GUI_CLOSE_BUTTON_TEXT");
        ItemStack item = this.createItem(closeIcon, closeIconText, new ArrayList<>(), Enchant);
        GUIButton button = new GUIButton(item);
        this.setItem(button, slot);
        button.setAction(sender::closeInventory);
    }

    public void showProfileButton(Player sender, int slot, boolean show) {
        if (!show) {
            return;
        }

        int moneySpent = PlayerDataManager.getData(sender).getMoneySpent();
        List<String> profileLore = new ArrayList<>();
        profileLore.add(formatMSG("&aClick to view your profile."));
        profileLore.add(formatMSG("&aYou have spent a total of &b$" + moneySpent + "&a."));

        ItemStack item = this.createItem("PLAYER_HEAD", config.getString("GUI_PROFILE_BUTTON"), profileLore, true);
        GUIButton button = new GUIButton(item);
        this.setItem(button, slot);

        button.setAction(() -> {
            this.close(sender);
            sound(sender, "block.note_block.pling");
            showMenu(sender, config.getString("PROFILE-MENU-TITLE"), 3, false);
        });
    }

    public ItemStack createItem(String icon, String name, List<String> lore, boolean Enchant) {

        ItemStack item = new ItemStack(Material.valueOf(icon));
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(formatMSG(name));
        lore.replaceAll(Main::formatMSG);
        meta.setLore(lore);

        if (Enchant) {
            meta.addEnchant(Enchantment.DURABILITY, 1, true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
        }
        item.setItemMeta(meta);

        return item;
    }

    public static void renderPerkButton(String name, String icon, List<String> lore, CustomGUI menu, int slot) {
        ItemStack item = menu.createItem(icon, name, lore, true);
        GUIButton button = new GUIButton(item);
        button.setAction(() -> {
        });
        menu.setItem(button, slot);
    }

    @NotNull
    public static Categories RenderCategories(Map<?, ?> opt, CustomGUI menu, int slot) {
        List<String> type = new ArrayList<>((Collection<? extends String>) opt.keySet());
        String category = type.get(0);
        Map<?, ?> data = (Map<?, ?>) opt.get(category);

        String name = (String) data.get("title");               // category title
        String icon = (String) data.get("icon");                // category icon
        List<String> lore = (List<String>) data.get("lore");    // category lore

        ItemStack item = menu.createItem(icon, name, lore, true);
        GUIButton button = new GUIButton(item);
        menu.setItem(button, slot);
        return new Categories(data, button);
    }

    public static class Categories {
        public final Map<?, ?> data;
        public final GUIButton button;

        public String title;

        public Categories(Map<?, ?> data, GUIButton button) {
            this.data = data;
            this.button = button;
            this.title = (String) data.get("title");
        }
    }

    public boolean getItem(int i) {
        return inventory.getItem(i) != null;
    }
}
