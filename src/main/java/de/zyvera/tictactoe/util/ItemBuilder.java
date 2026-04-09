package de.zyvera.tictactoe.util;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class ItemBuilder {

    private final ItemStack item;
    private final ItemMeta meta;

    public ItemBuilder(Material material) {
        this.item = new ItemStack(material);
        this.meta = item.getItemMeta();
    }

    public ItemBuilder(Material material, int amount) {
        this.item = new ItemStack(material, amount);
        this.meta = item.getItemMeta();
    }

    public ItemBuilder name(String name) {
        if (meta != null) {
            meta.setDisplayName(MessageUtil.colorize(name));
        }
        return this;
    }

    public ItemBuilder lore(String... lines) {
        if (meta != null) {
            List<String> lore = new ArrayList<>();
            for (String line : lines) {
                lore.add(MessageUtil.colorize(line));
            }
            meta.setLore(lore);
        }
        return this;
    }

    public ItemBuilder lore(List<String> lines) {
        if (meta != null) {
            List<String> lore = new ArrayList<>();
            for (String line : lines) {
                lore.add(MessageUtil.colorize(line));
            }
            meta.setLore(lore);
        }
        return this;
    }

    public ItemBuilder addLore(String line) {
        if (meta != null) {
            List<String> lore = meta.getLore();
            if (lore == null) lore = new ArrayList<>();
            lore.add(MessageUtil.colorize(line));
            meta.setLore(lore);
        }
        return this;
    }

    /**
     * Setzt den Besitzer eines Spielerkopfes (echtes Skin).
     */
    public ItemBuilder skullOwner(OfflinePlayer player) {
        if (meta instanceof SkullMeta) {
            SkullMeta skull = (SkullMeta) meta;
            try {
                // 1.12.1+: setOwningPlayer
                Method m = SkullMeta.class.getMethod("setOwningPlayer", OfflinePlayer.class);
                m.invoke(skull, player);
            } catch (Exception e) {
                try {
                    // Legacy: setOwner
                    skull.setOwner(player.getName());
                } catch (Exception ignored) {}
            }
        }
        return this;
    }

    public ItemStack build() {
        if (meta != null) {
            item.setItemMeta(meta);
        }
        return item;
    }

    public static Material safeMaterial(String name) {
        try {
            return Material.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return fallbackMaterial(name);
        }
    }

    private static Material fallbackMaterial(String name) {
        switch (name.toUpperCase()) {
            case "RED_CONCRETE":
                return tryMaterials("RED_CONCRETE", "STAINED_CLAY");
            case "BLUE_CONCRETE":
                return tryMaterials("BLUE_CONCRETE", "STAINED_CLAY");
            case "LIGHT_GRAY_STAINED_GLASS_PANE":
                return tryMaterials("LIGHT_GRAY_STAINED_GLASS_PANE", "STAINED_GLASS_PANE", "THIN_GLASS");
            case "BLACK_STAINED_GLASS_PANE":
                return tryMaterials("BLACK_STAINED_GLASS_PANE", "STAINED_GLASS_PANE", "THIN_GLASS");
            case "WHITE_STAINED_GLASS_PANE":
                return tryMaterials("WHITE_STAINED_GLASS_PANE", "STAINED_GLASS_PANE", "THIN_GLASS");
            case "LIME_CONCRETE":
                return tryMaterials("LIME_CONCRETE", "STAINED_CLAY");
            case "YELLOW_CONCRETE":
                return tryMaterials("YELLOW_CONCRETE", "STAINED_CLAY");
            case "PLAYER_HEAD":
                return tryMaterials("PLAYER_HEAD", "SKULL_ITEM");
            case "CRAFTING_TABLE":
                return tryMaterials("CRAFTING_TABLE", "WORKBENCH");
            case "NETHER_STAR":
                return tryMaterials("NETHER_STAR");
            case "BOOK":
                return tryMaterials("BOOK");
            case "BARRIER":
                return tryMaterials("BARRIER", "BEDROCK");
            case "CLOCK":
                return tryMaterials("CLOCK", "WATCH");
            case "ARROW":
                return tryMaterials("ARROW");
            case "GOLD_INGOT":
                return tryMaterials("GOLD_INGOT");
            case "IRON_INGOT":
                return tryMaterials("IRON_INGOT");
            case "BRICK":
                return tryMaterials("BRICK", "CLAY_BRICK");
            case "PAPER":
                return tryMaterials("PAPER");
            default:
                return Material.STONE;
        }
    }

    private static Material tryMaterials(String... names) {
        for (String n : names) {
            try {
                return Material.valueOf(n);
            } catch (IllegalArgumentException ignored) {}
        }
        return Material.STONE;
    }
}
