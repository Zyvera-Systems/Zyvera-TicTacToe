package de.zyvera.tictactoe.gui;

import de.zyvera.tictactoe.ZyveraTicTacToe;
import de.zyvera.tictactoe.util.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public final class MainMenuGui {

    public static final String MENU_TITLE = "§8§l» §6§lTTT Menü §8§l«";

    public static final int QUEUE_SLOT = 11;
    public static final int STATS_SLOT = 13;
    public static final int TOP_SLOT = 15;
    public static final int CLOSE_SLOT = 22;

    private MainMenuGui() {}

    public static void open(ZyveraTicTacToe plugin, Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, MENU_TITLE);

        Material border = ItemBuilder.safeMaterial("BLACK_STAINED_GLASS_PANE");
        for (int i = 0; i < 27; i++) {
            inv.setItem(i, new ItemBuilder(border).name(" ").build());
        }

        boolean inQueue = plugin.getGameManager().getQueueManager().isInQueue(player.getUniqueId());
        int queueSize = plugin.getGameManager().getQueueManager().getQueueSize();

        if (inQueue) {
            inv.setItem(QUEUE_SLOT, new ItemBuilder(ItemBuilder.safeMaterial("RED_CONCRETE"))
                    .name("&c&lWarteschlange verlassen")
                    .lore("&7Du bist in der Warteschlange.",
                            "&7Spieler in Queue: &f" + queueSize,
                            "",
                            "&eKlicke zum Verlassen!")
                    .build());
        } else {
            inv.setItem(QUEUE_SLOT, new ItemBuilder(ItemBuilder.safeMaterial("LIME_CONCRETE"))
                    .name("&a&lSpielen (Warteschlange)")
                    .lore("&7Tritt der Warteschlange bei",
                            "&7und werde mit einem Gegner gematcht!",
                            "",
                            "&7Spieler in Queue: &f" + queueSize,
                            "&7Stats werden gezählt!",
                            "",
                            "&eKlicke zum Beitreten!")
                    .build());
        }

        inv.setItem(STATS_SLOT, new ItemBuilder(ItemBuilder.safeMaterial("BOOK"))
                .name("&6&lMeine Statistiken")
                .lore("&7Zeige deine TicTacToe-Stats an.",
                        "",
                        "&eKlicke zum Ansehen!")
                .build());

        inv.setItem(TOP_SLOT, new ItemBuilder(ItemBuilder.safeMaterial("GOLD_INGOT"))
                .name("&6&lTop Spieler")
                .lore("&7Zeige die besten Spieler an.",
                        "",
                        "&eKlicke zum Ansehen!")
                .build());

        inv.setItem(CLOSE_SLOT, new ItemBuilder(ItemBuilder.safeMaterial("BARRIER"))
                .name("&c&lSchließen")
                .build());

        player.openInventory(inv);
    }
}
