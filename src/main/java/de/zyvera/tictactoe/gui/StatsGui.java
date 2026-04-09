package de.zyvera.tictactoe.gui;

import de.zyvera.tictactoe.ZyveraTicTacToe;
import de.zyvera.tictactoe.data.PlayerStats;
import de.zyvera.tictactoe.util.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public final class StatsGui {

    public static final String STATS_TITLE = "§8§l» §6§lStats §8§l«";
    public static final String TOP_TITLE = "§8§l» §6§lTop Spieler §8§l«";

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy HH:mm");

    private StatsGui() {}

    public static void openStats(ZyveraTicTacToe plugin, Player viewer, PlayerStats stats) {
        Inventory inv = Bukkit.createInventory(null, 45, STATS_TITLE);

        Material border = ItemBuilder.safeMaterial("BLACK_STAINED_GLASS_PANE");
        for (int i = 0; i < 45; i++) {
            inv.setItem(i, new ItemBuilder(border).name(" ").build());
        }

        String name = stats.getLastKnownName();

        // Spieler-Kopf mit echtem Skin
        Material headMat = ItemBuilder.safeMaterial("PLAYER_HEAD");
        Player target = Bukkit.getPlayer(stats.getUuid());
        ItemBuilder headBuilder = new ItemBuilder(headMat)
                .name("&6&l" + name)
                .lore("&7TicTacToe Statistiken");
        if (target != null) {
            headBuilder.skullOwner(target);
        }
        inv.setItem(4, headBuilder.build());

        inv.setItem(19, new ItemBuilder(ItemBuilder.safeMaterial("LIME_CONCRETE"))
                .name("&a&lSiege")
                .lore("&f" + stats.getWins(),
                        "",
                        "&7Siegesserie: &a" + stats.getWinStreak(),
                        "&7Beste Serie: &a" + stats.getBestWinStreak())
                .build());

        inv.setItem(21, new ItemBuilder(ItemBuilder.safeMaterial("RED_CONCRETE"))
                .name("&c&lNiederlagen")
                .lore("&f" + stats.getLosses())
                .build());

        inv.setItem(23, new ItemBuilder(ItemBuilder.safeMaterial("YELLOW_CONCRETE"))
                .name("&e&lUnentschieden")
                .lore("&f" + stats.getDraws())
                .build());

        inv.setItem(25, new ItemBuilder(ItemBuilder.safeMaterial("BOOK"))
                .name("&6&lSpiele Gesamt")
                .lore("&f" + stats.getGamesPlayed())
                .build());

        inv.setItem(29, new ItemBuilder(ItemBuilder.safeMaterial("NETHER_STAR"))
                .name("&6&lWinrate")
                .lore("&f" + stats.getWinRate() + "%")
                .build());

        inv.setItem(31, new ItemBuilder(ItemBuilder.safeMaterial("ARROW"))
                .name("&d&lZüge")
                .lore("&7Gesamt: &f" + stats.getTotalMoves(),
                        "&7Durchschnitt/Spiel: &f" + stats.getAvgMovesPerGame())
                .build());

        String lastPlayed = stats.getLastPlayed() > 0
                ? DATE_FORMAT.format(new Date(stats.getLastPlayed()))
                : "&7Noch nie gespielt";
        inv.setItem(33, new ItemBuilder(ItemBuilder.safeMaterial("CLOCK"))
                .name("&7Letztes Spiel")
                .lore("&f" + lastPlayed)
                .build());

        inv.setItem(40, new ItemBuilder(ItemBuilder.safeMaterial("ARROW"))
                .name("&7Zurück zum Menü")
                .build());

        viewer.openInventory(inv);
    }

    public static void openTopPlayers(ZyveraTicTacToe plugin, Player viewer) {
        Inventory inv = Bukkit.createInventory(null, 54, TOP_TITLE);

        Material border = ItemBuilder.safeMaterial("BLACK_STAINED_GLASS_PANE");
        for (int i = 0; i < 54; i++) {
            inv.setItem(i, new ItemBuilder(border).name(" ").build());
        }

        inv.setItem(4, new ItemBuilder(ItemBuilder.safeMaterial("GOLD_INGOT"))
                .name("&6&lTop 10 Spieler")
                .lore("&7Sortiert nach Siegen")
                .build());

        List<PlayerStats> topPlayers = plugin.getStatsManager().getTopPlayers(10);

        int[] topSlots = {19, 20, 21, 22, 23, 24, 25, 28, 29, 30};
        String[] medals = {"&6&l1.", "&7&l2.", "&c&l3.", "&f4.", "&f5.", "&f6.", "&f7.", "&f8.", "&f9.", "&f10."};

        Material headMat = ItemBuilder.safeMaterial("PLAYER_HEAD");

        for (int i = 0; i < topPlayers.size() && i < topSlots.length; i++) {
            PlayerStats stats = topPlayers.get(i);
            List<String> lore = new ArrayList<>();
            lore.add("&7Siege: &a" + stats.getWins());
            lore.add("&7Niederlagen: &c" + stats.getLosses());
            lore.add("&7Unentschieden: &e" + stats.getDraws());
            lore.add("&7Winrate: &6" + stats.getWinRate() + "%");
            lore.add("&7Beste Serie: &b" + stats.getBestWinStreak());

            org.bukkit.OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(stats.getUuid());
            inv.setItem(topSlots[i], new ItemBuilder(headMat)
                    .name(medals[i] + " &6" + stats.getLastKnownName())
                    .lore(lore)
                    .skullOwner(offlinePlayer)
                    .build());
        }

        inv.setItem(49, new ItemBuilder(ItemBuilder.safeMaterial("ARROW"))
                .name("&7Zurück zum Menü")
                .build());

        viewer.openInventory(inv);
    }
}
