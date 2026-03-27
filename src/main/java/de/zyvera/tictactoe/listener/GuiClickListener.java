package de.zyvera.tictactoe.listener;

import de.zyvera.tictactoe.ZyveraTicTacToe;
import de.zyvera.tictactoe.data.PlayerStats;
import de.zyvera.tictactoe.game.TicTacToeGame;
import de.zyvera.tictactoe.gui.GameGui;
import de.zyvera.tictactoe.gui.MainMenuGui;
import de.zyvera.tictactoe.gui.StatsGui;
import de.zyvera.tictactoe.util.MessageUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

import java.util.HashSet;
import java.util.Set;

public class GuiClickListener implements Listener {

    private final ZyveraTicTacToe plugin;

    // Alle Plugin-GUI-Titel
    private static final Set<String> PLUGIN_TITLES = new HashSet<>();
    static {
        PLUGIN_TITLES.add(GameGui.GAME_TITLE);
        PLUGIN_TITLES.add(GameGui.END_TITLE_WIN);
        PLUGIN_TITLES.add(GameGui.END_TITLE_LOSE);
        PLUGIN_TITLES.add(GameGui.END_TITLE_DRAW);
        PLUGIN_TITLES.add(GameGui.END_TITLE_CANCEL);
        PLUGIN_TITLES.add(MainMenuGui.MENU_TITLE);
        PLUGIN_TITLES.add(StatsGui.STATS_TITLE);
        PLUGIN_TITLES.add(StatsGui.TOP_TITLE);
    }

    public GuiClickListener(ZyveraTicTacToe plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = false)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();

        if (!PLUGIN_TITLES.contains(title)) return;
        event.setCancelled(true);

        // Hauptmenü
        if (title.equals(MainMenuGui.MENU_TITLE)) {
            handleMainMenu(player, event.getRawSlot());
            return;
        }

        // Spiel-GUI
        if (title.equals(GameGui.GAME_TITLE)) {
            handleGameClick(player, event.getRawSlot());
            return;
        }

        // End-GUIs (kein Klick nötig)
        if (title.equals(GameGui.END_TITLE_WIN) || title.equals(GameGui.END_TITLE_LOSE)
                || title.equals(GameGui.END_TITLE_DRAW) || title.equals(GameGui.END_TITLE_CANCEL)) {
            return;
        }

        // Stats-GUI
        if (title.equals(StatsGui.STATS_TITLE)) {
            if (event.getRawSlot() == 40) {
                MainMenuGui.open(plugin, player);
            }
            return;
        }

        // Top-Spieler GUI
        if (title.equals(StatsGui.TOP_TITLE)) {
            if (event.getRawSlot() == 49) {
                MainMenuGui.open(plugin, player);
            }
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        String title = event.getView().getTitle();
        if (PLUGIN_TITLES.contains(title)) {
            event.setCancelled(true);
        }
    }

    private void handleMainMenu(Player player, int slot) {
        switch (slot) {
            case MainMenuGui.QUEUE_SLOT:
                handleQueueToggle(player);
                break;
            case MainMenuGui.STATS_SLOT:
                PlayerStats stats = plugin.getStatsManager().getStats(
                        player.getUniqueId(), player.getName());
                StatsGui.openStats(plugin, player, stats);
                break;
            case MainMenuGui.TOP_SLOT:
                StatsGui.openTopPlayers(plugin, player);
                break;
            case MainMenuGui.CLOSE_SLOT:
                player.closeInventory();
                break;
        }
    }

    private void handleQueueToggle(Player player) {
        if (plugin.getGameManager().isInGame(player.getUniqueId())) {
            MessageUtil.sendMessage(player, "already-ingame");
            return;
        }

        if (plugin.getGameManager().getQueueManager().isInQueue(player.getUniqueId())) {
            plugin.getGameManager().getQueueManager().leaveQueue(player.getUniqueId());
            MessageUtil.sendMessage(player, "queue-left");
        } else {
            if (plugin.getGameManager().getQueueManager().joinQueue(player.getUniqueId())) {
                MessageUtil.sendMessage(player, "queue-joined");
            }
        }

        MainMenuGui.open(plugin, player);
    }

    private void handleGameClick(Player player, int slot) {
        if (slot == GameGui.QUIT_SLOT) {
            plugin.getGameManager().quitGame(player.getUniqueId());
            return;
        }

        int boardPos = GameGui.slotToBoard(slot);
        if (boardPos >= 0) {
            plugin.getGameManager().handleMove(player.getUniqueId(), boardPos);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        Player player = (Player) event.getPlayer();
        String title = event.getView().getTitle();

        if (title.equals(GameGui.GAME_TITLE)) {
            TicTacToeGame game = plugin.getGameManager().getGame(player.getUniqueId());
            if (game != null && game.getResult() == TicTacToeGame.GameResult.IN_PROGRESS) {
                de.zyvera.tictactoe.util.SchedulerUtil.runLater(plugin, () -> {
                    if (player.isOnline() && plugin.getGameManager().isInGame(player.getUniqueId())) {
                        GameGui.openGame(plugin, player, game);
                    }
                }, 1L);
            }
        }
    }
}
