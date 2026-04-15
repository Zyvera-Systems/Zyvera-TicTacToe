package de.zyvera.tictactoe.gui;

import de.zyvera.tictactoe.ZyveraTicTacToe;
import de.zyvera.tictactoe.game.TicTacToeGame;
import de.zyvera.tictactoe.game.TicTacToeGame.CellState;
import de.zyvera.tictactoe.util.ItemBuilder;
import de.zyvera.tictactoe.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Kompaktes TicTacToe Spielfeld (36 Slots = 4 Reihen).
 *
 * Layout:
 *   Row 0 (0-8):   [Head_X] [B] [B] [B] [Info] [B] [B] [B] [Head_O]
 *   Row 1 (9-17):  [B] [B] [B] [0] [1] [2] [B] [B] [B]
 *   Row 2 (18-26): [B] [B] [B] [3] [4] [5] [B] [B] [B]
 *   Row 3 (27-35): [B] [B] [B] [6] [7] [8] [B] [B] [Quit]
 *
 * Board 3x3 zentriert: Slots 12,13,14 / 21,22,23 / 30,31,32
 */
public final class GameGui {

    public static final int[] BOARD_SLOTS = {
            12, 13, 14,
            21, 22, 23,
            30, 31, 32
    };

    private static final int[] SLOT_TO_BOARD = new int[36];
    static {
        Arrays.fill(SLOT_TO_BOARD, -1);
        for (int i = 0; i < BOARD_SLOTS.length; i++) {
            SLOT_TO_BOARD[BOARD_SLOTS[i]] = i;
        }
    }

    private static final Set<UUID> guiUpdating = new HashSet<>();

    public static final String GAME_TITLE = "§8§l» §6§lTicTacToe §8§l«";
    public static final String END_TITLE_WIN = "§8§l» §a§lGewonnen! §8§l«";
    public static final String END_TITLE_LOSE = "§8§l» §c§lVerloren! §8§l«";
    public static final String END_TITLE_DRAW = "§8§l» §e§lUnentschieden §8§l«";
    public static final String END_TITLE_CANCEL = "§8§l» §7§lAbgebrochen §8§l«";

    public static final int QUIT_SLOT = 35;
    public static final int HEAD_X_SLOT = 0;
    public static final int HEAD_O_SLOT = 8;
    public static final int INFO_SLOT = 4;

    private GameGui() {}

    public static void openGame(ZyveraTicTacToe plugin, Player player, TicTacToeGame game) {
        FileConfiguration config = plugin.getConfig();
        Inventory inv = Bukkit.createInventory(null, 36, GAME_TITLE);

        Material borderMat = ItemBuilder.safeMaterial(config.getString("gui.border-material", "BLACK_STAINED_GLASS_PANE"));
        Material emptyMat = ItemBuilder.safeMaterial(config.getString("gui.empty-material", "LIGHT_GRAY_STAINED_GLASS_PANE"));
        Material xMat = ItemBuilder.safeMaterial(config.getString("gui.symbol-x-material", "RED_CONCRETE"));
        Material oMat = ItemBuilder.safeMaterial(config.getString("gui.symbol-o-material", "BLUE_CONCRETE"));

        String xName = config.getString("gui.symbol-x-name", "&c&lX");
        String oName = config.getString("gui.symbol-o-name", "&9&lO");
        String emptyName = config.getString("gui.empty-name", "&7Klicke hier!");

        ItemStack border = new ItemBuilder(borderMat).name(config.getString("gui.border-name", " ")).build();

        // Alles mit Border füllen
        for (int i = 0; i < 36; i++) {
            inv.setItem(i, border);
        }

        // Spielfelder setzen
        boolean isPlayerTurn = game.getCurrentPlayer().equals(player.getUniqueId());
        CellState playerSymbol = game.getSymbol(player.getUniqueId());

        for (int i = 0; i < 9; i++) {
            int slot = BOARD_SLOTS[i];
            CellState cell = game.getCell(i);

            if (cell == CellState.EMPTY) {
                if (isPlayerTurn) {
                    inv.setItem(slot, new ItemBuilder(emptyMat).name(emptyName)
                            .lore("&7Setze dein Zeichen!").build());
                } else {
                    inv.setItem(slot, new ItemBuilder(emptyMat).name("&8Warte...").build());
                }
            } else if (cell == CellState.X) {
                inv.setItem(slot, new ItemBuilder(xMat).name(xName).build());
            } else if (cell == CellState.O) {
                inv.setItem(slot, new ItemBuilder(oMat).name(oName).build());
            }
        }

        // Spielerköpfe mit echtem Skin
        Player playerX = Bukkit.getPlayer(game.getPlayerX());
        Player playerO = Bukkit.getPlayer(game.getPlayerO());

        String symbolStr = playerSymbol == CellState.X ? xName : oName;

        // Eigener Kopf (links)
        Material headMat = ItemBuilder.safeMaterial("PLAYER_HEAD");
        inv.setItem(HEAD_X_SLOT, new ItemBuilder(headMat)
                .name("&a" + (playerX != null ? playerX.getName() : "???"))
                .lore("&7Symbol: " + config.getString("gui.symbol-x-name", "&c&lX"))
                .skullOwner(playerX != null ? playerX : player)
                .build());

        // Gegner-Kopf (rechts)
        inv.setItem(HEAD_O_SLOT, new ItemBuilder(headMat)
                .name("&c" + (playerO != null ? playerO.getName() : "???"))
                .lore("&7Symbol: " + config.getString("gui.symbol-o-name", "&9&lO"))
                .skullOwner(playerO != null ? playerO : player)
                .build());

        // Info-Mitte
        String turnInfo = isPlayerTurn ? "&a&lDein Zug!" : "&e&lGegner ist dran...";
        inv.setItem(INFO_SLOT, new ItemBuilder(ItemBuilder.safeMaterial("CLOCK"))
                .name(turnInfo)
                .lore("&7Runde: &f" + game.getRound(),
                        "&7Dein Symbol: " + symbolStr,
                        "&7Deine Züge: &f" + game.getMovesFor(player.getUniqueId()))
                .build());

        // Quit-Button
        inv.setItem(QUIT_SLOT, new ItemBuilder(ItemBuilder.safeMaterial("BARRIER"))
                .name("&c&lSpiel verlassen")
                .lore("&7Klicke oder drücke ESC.")
                .build());

        guiUpdating.add(player.getUniqueId());
        player.openInventory(inv);
        guiUpdating.remove(player.getUniqueId());
    }

    public static void openGameEnd(ZyveraTicTacToe plugin, Player player, TicTacToeGame game) {
        FileConfiguration config = plugin.getConfig();

        TicTacToeGame.GameResult result = game.getResult();
        CellState playerSymbol = game.getSymbol(player.getUniqueId());
        boolean isWinner = (result == TicTacToeGame.GameResult.WIN_X && playerSymbol == CellState.X)
                || (result == TicTacToeGame.GameResult.WIN_O && playerSymbol == CellState.O);

        String title;
        if (result == TicTacToeGame.GameResult.DRAW) {
            title = END_TITLE_DRAW;
        } else if (result == TicTacToeGame.GameResult.CANCELLED) {
            title = END_TITLE_CANCEL;
        } else if (isWinner) {
            title = END_TITLE_WIN;
        } else {
            title = END_TITLE_LOSE;
        }

        Inventory inv = Bukkit.createInventory(null, 36, title);

        Material borderMat = ItemBuilder.safeMaterial(config.getString("gui.border-material", "BLACK_STAINED_GLASS_PANE"));
        Material xMat = ItemBuilder.safeMaterial(config.getString("gui.symbol-x-material", "RED_CONCRETE"));
        Material oMat = ItemBuilder.safeMaterial(config.getString("gui.symbol-o-material", "BLUE_CONCRETE"));
        String xName = config.getString("gui.symbol-x-name", "&c&lX");
        String oName = config.getString("gui.symbol-o-name", "&9&lO");

        ItemStack border = new ItemBuilder(borderMat).name(" ").build();
        for (int i = 0; i < 36; i++) inv.setItem(i, border);

        int[] winLine = game.getWinLine();
        Set<Integer> winPositions = new HashSet<>();
        if (winLine != null) {
            for (int pos : winLine) winPositions.add(pos);
        }

        for (int i = 0; i < 9; i++) {
            int slot = BOARD_SLOTS[i];
            CellState cell = game.getCell(i);

            if (cell == CellState.X) {
                if (winPositions.contains(i)) {
                    inv.setItem(slot, new ItemBuilder(ItemBuilder.safeMaterial("LIME_CONCRETE"))
                            .name(xName + " &a&l✓").build());
                } else {
                    inv.setItem(slot, new ItemBuilder(xMat).name(xName).build());
                }
            } else if (cell == CellState.O) {
                if (winPositions.contains(i)) {
                    inv.setItem(slot, new ItemBuilder(ItemBuilder.safeMaterial("LIME_CONCRETE"))
                            .name(oName + " &a&l✓").build());
                } else {
                    inv.setItem(slot, new ItemBuilder(oMat).name(oName).build());
                }
            } else {
                inv.setItem(slot, new ItemBuilder(borderMat).name(" ").build());
            }
        }

        Player opponent = Bukkit.getPlayer(game.getOpponent(player.getUniqueId()));
        String oppName = opponent != null ? opponent.getName() : "???";

        String resultText;
        Material resultMat;
        if (result == TicTacToeGame.GameResult.CANCELLED) {
            resultText = "&7&lAbgebrochen";
            resultMat = ItemBuilder.safeMaterial("BARRIER");
        } else if (result == TicTacToeGame.GameResult.DRAW) {
            resultText = "&e&lUnentschieden!";
            resultMat = ItemBuilder.safeMaterial("YELLOW_CONCRETE");
        } else if (isWinner) {
            resultText = "&a&lSieg!";
            resultMat = ItemBuilder.safeMaterial("LIME_CONCRETE");
        } else {
            resultText = "&c&lNiederlage!";
            resultMat = ItemBuilder.safeMaterial("RED_CONCRETE");
        }

        inv.setItem(INFO_SLOT, new ItemBuilder(resultMat)
                .name(resultText)
                .lore("&7Gegen: &f" + oppName,
                        "&7Züge: &f" + game.getMovesFor(player.getUniqueId()),
                        "&7Runden: &f" + game.getRound(),
                        "",
                        "&7Schließe das Inventar zum Beenden.")
                .build());

        guiUpdating.add(player.getUniqueId());
        player.openInventory(inv);
        guiUpdating.remove(player.getUniqueId());
    }

    /**
     * Prüft ob ein GUI-Update gerade läuft (programmatischer Close, kein ESC).
     */
    public static boolean isUpdating(UUID uuid) {
        return guiUpdating.contains(uuid);
    }

    public static int slotToBoard(int slot) {
        if (slot < 0 || slot >= SLOT_TO_BOARD.length) return -1;
        return SLOT_TO_BOARD[slot];
    }
}
