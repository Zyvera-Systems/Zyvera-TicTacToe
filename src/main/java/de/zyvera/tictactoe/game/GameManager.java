package de.zyvera.tictactoe.game;

import de.zyvera.tictactoe.ZyveraTicTacToe;
import de.zyvera.tictactoe.data.PlayerStats;
import de.zyvera.tictactoe.gui.GameGui;
import de.zyvera.tictactoe.util.MessageUtil;
import de.zyvera.tictactoe.util.SchedulerUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class GameManager {

    private final ZyveraTicTacToe plugin;
    private final QueueManager queueManager;
    private final Map<UUID, TicTacToeGame> activeGames = new ConcurrentHashMap<>();
    private SchedulerUtil.CancellableTask queueTask;
    private SchedulerUtil.CancellableTask timeoutTask;

    public GameManager(ZyveraTicTacToe plugin) {
        this.plugin = plugin;
        this.queueManager = new QueueManager();
    }

    public void startTasks() {
        int queueInterval = plugin.getConfig().getInt("game.queue-check-interval", 40);

        queueTask = SchedulerUtil.runTimer(plugin, () -> {
            UUID[] match = queueManager.findMatch();
            if (match != null) {
                startGame(match[0], match[1], true);
            }
        }, queueInterval, queueInterval);

        timeoutTask = SchedulerUtil.runTimer(plugin, () -> {
            long challengeTimeout = plugin.getConfig().getInt("game.challenge-timeout", 60) * 1000L;
            List<UUID> expired = queueManager.cleanupExpiredChallenges(challengeTimeout);
            for (UUID challenger : expired) {
                Player p = Bukkit.getPlayer(challenger);
                if (p != null && p.isOnline()) {
                    MessageUtil.sendMessage(p, "challenge-expired");
                }
            }

            long turnTimeout = plugin.getConfig().getInt("game.turn-timeout", 120) * 1000L;
            long now = System.currentTimeMillis();

            Set<UUID> processed = new HashSet<>();
            for (Map.Entry<UUID, TicTacToeGame> entry : activeGames.entrySet()) {
                TicTacToeGame game = entry.getValue();
                UUID key = entry.getKey();

                if (game.getResult() != TicTacToeGame.GameResult.IN_PROGRESS) continue;
                if (processed.contains(game.getPlayerX()) || processed.contains(game.getPlayerO())) continue;

                if (now - game.getLastMoveTime() > turnTimeout) {
                    processed.add(game.getPlayerX());
                    processed.add(game.getPlayerO());

                    if (game.getTotalMoves() == 0) {
                        // Noch kein Zug gesetzt → Spiel abbrechen (keine Stats)
                        handleCancelledGame(game);
                    } else if (game.isRanked()) {
                        // Ranked: Aktueller Spieler verliert wegen Timeout
                        UUID timeoutPlayer = game.getCurrentPlayer();
                        UUID winner = game.getOpponent(timeoutPlayer);

                        game.forfeit(timeoutPlayer);

                        Player wp = Bukkit.getPlayer(winner);
                        Player lp = Bukkit.getPlayer(timeoutPlayer);

                        if (wp != null) MessageUtil.sendMessage(wp, "timeout-win");
                        if (lp != null) MessageUtil.sendMessage(lp, "timeout-lose");

                        endGame(game);
                    }
                    // Unranked (Challenge): Timeout egal, kein Auto-Sieg
                }
            }
        }, 20L, 20L);
    }

    public void stopTasks() {
        if (queueTask != null) queueTask.cancel();
        if (timeoutTask != null) timeoutTask.cancel();
    }

    public void startGame(UUID playerXUuid, UUID playerOUuid, boolean ranked) {
        Player playerX = Bukkit.getPlayer(playerXUuid);
        Player playerO = Bukkit.getPlayer(playerOUuid);

        if (playerX == null || playerO == null || !playerX.isOnline() || !playerO.isOnline()) {
            if (playerX != null) queueManager.removePlayer(playerXUuid);
            if (playerO != null) queueManager.removePlayer(playerOUuid);
            return;
        }

        TicTacToeGame game = new TicTacToeGame(playerXUuid, playerOUuid, ranked);
        activeGames.put(playerXUuid, game);
        activeGames.put(playerOUuid, game);

        MessageUtil.sendMessage(playerX, "game-found");
        MessageUtil.sendMessage(playerO, "game-found");

        SchedulerUtil.runSync(plugin, () -> {
            GameGui.openGame(plugin, playerX, game);
            GameGui.openGame(plugin, playerO, game);
        });
    }

    public void handleMove(UUID playerUuid, int position) {
        TicTacToeGame game = activeGames.get(playerUuid);
        if (game == null) return;

        if (game.makeMove(position, playerUuid)) {
            if (game.getResult() != TicTacToeGame.GameResult.IN_PROGRESS) {
                endGame(game);
            } else {
                updateGameGui(game);
            }
        }
    }


    private void handleCancelledGame(TicTacToeGame game) {
        game.cancel();

        Player playerX = Bukkit.getPlayer(game.getPlayerX());
        Player playerO = Bukkit.getPlayer(game.getPlayerO());

        if (playerX != null) MessageUtil.sendMessage(playerX, "game-cancelled");
        if (playerO != null) MessageUtil.sendMessage(playerO, "game-cancelled");

        SchedulerUtil.runSync(plugin, () -> {
            if (playerX != null && playerX.isOnline()) {
                GameGui.openGameEnd(plugin, playerX, game);
            }
            if (playerO != null && playerO.isOnline()) {
                GameGui.openGameEnd(plugin, playerO, game);
            }
        });

        SchedulerUtil.runLater(plugin, () -> {
            activeGames.remove(game.getPlayerX());
            activeGames.remove(game.getPlayerO());
        }, 60L);
    }

    public void endGame(TicTacToeGame game) {
        UUID xUuid = game.getPlayerX();
        UUID oUuid = game.getPlayerO();

        Player playerX = Bukkit.getPlayer(xUuid);
        Player playerO = Bukkit.getPlayer(oUuid);

        if (game.isRanked() && game.getResult() != TicTacToeGame.GameResult.CANCELLED) {
            PlayerStats statsX = plugin.getStatsManager().getStats(xUuid,
                    playerX != null ? playerX.getName() : "Unknown");
            PlayerStats statsO = plugin.getStatsManager().getStats(oUuid,
                    playerO != null ? playerO.getName() : "Unknown");

            switch (game.getResult()) {
                case WIN_X:
                    statsX.addWin(game.getMovesX());
                    statsO.addLoss(game.getMovesO());
                    break;
                case WIN_O:
                    statsO.addWin(game.getMovesO());
                    statsX.addLoss(game.getMovesX());
                    break;
                case DRAW:
                    statsX.addDraw(game.getMovesX());
                    statsO.addDraw(game.getMovesO());
                    break;
                default:
                    break;
            }

            plugin.getStatsManager().saveAsync();
        }

        sendGameEndMessages(game, playerX, playerO);

        SchedulerUtil.runSync(plugin, () -> {
            if (playerX != null && playerX.isOnline()) {
                GameGui.openGameEnd(plugin, playerX, game);
            }
            if (playerO != null && playerO.isOnline()) {
                GameGui.openGameEnd(plugin, playerO, game);
            }
        });

        SchedulerUtil.runLater(plugin, () -> {
            activeGames.remove(xUuid);
            activeGames.remove(oUuid);
        }, 60L);
    }

    private void sendGameEndMessages(TicTacToeGame game, Player playerX, Player playerO) {
        switch (game.getResult()) {
            case WIN_X:
                if (playerX != null)
                    MessageUtil.sendMessage(playerX, "game-win", "player",
                            playerO != null ? playerO.getName() : "Gegner");
                if (playerO != null)
                    MessageUtil.sendMessage(playerO, "game-lose", "player",
                            playerX != null ? playerX.getName() : "Gegner");
                break;
            case WIN_O:
                if (playerO != null)
                    MessageUtil.sendMessage(playerO, "game-win", "player",
                            playerX != null ? playerX.getName() : "Gegner");
                if (playerX != null)
                    MessageUtil.sendMessage(playerX, "game-lose", "player",
                            playerO != null ? playerO.getName() : "Gegner");
                break;
            case DRAW:
                if (playerX != null)
                    MessageUtil.sendMessage(playerX, "game-draw", "player",
                            playerO != null ? playerO.getName() : "Gegner");
                if (playerO != null)
                    MessageUtil.sendMessage(playerO, "game-draw", "player",
                            playerX != null ? playerX.getName() : "Gegner");
                break;
            default:
                break;
        }
    }

    private void updateGameGui(TicTacToeGame game) {
        Player playerX = Bukkit.getPlayer(game.getPlayerX());
        Player playerO = Bukkit.getPlayer(game.getPlayerO());

        SchedulerUtil.runSync(plugin, () -> {
            if (playerX != null && playerX.isOnline()) {
                GameGui.openGame(plugin, playerX, game);
            }
            if (playerO != null && playerO.isOnline()) {
                GameGui.openGame(plugin, playerO, game);
            }
        });
    }

    public void quitGame(UUID player) {
        TicTacToeGame game = activeGames.get(player);
        if (game == null) return;

        game.forfeit(player);

        Player quitter = Bukkit.getPlayer(player);
        Player opponent = Bukkit.getPlayer(game.getOpponent(player));

        if (quitter != null) {
            MessageUtil.sendMessage(quitter, "game-quit");
            quitter.closeInventory();
        }

        if (opponent != null) {
            MessageUtil.sendMessage(opponent, "opponent-quit");
        }

        endGame(game);
    }

    public boolean isInGame(UUID player) {
        TicTacToeGame game = activeGames.get(player);
        return game != null && game.getResult() == TicTacToeGame.GameResult.IN_PROGRESS;
    }

    public TicTacToeGame getGame(UUID player) {
        return activeGames.get(player);
    }

    public QueueManager getQueueManager() {
        return queueManager;
    }

    public void handleDisconnect(UUID player) {
        queueManager.removePlayer(player);
        if (isInGame(player)) {
            quitGame(player);
        }
    }
}
