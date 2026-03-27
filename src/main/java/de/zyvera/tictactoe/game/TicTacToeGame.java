package de.zyvera.tictactoe.game;

import java.util.UUID;

public class TicTacToeGame {

    public enum CellState {
        EMPTY, X, O
    }

    public enum GameResult {
        WIN_X, WIN_O, DRAW, IN_PROGRESS, CANCELLED
    }

    private final UUID playerX;
    private final UUID playerO;
    private final CellState[] board;
    private boolean xTurn;
    private int round;
    private int movesX;
    private int movesO;
    private GameResult result;
    private final long startTime;
    private long lastMoveTime;
    private final boolean ranked; // Ob Stats gezählt werden

    public TicTacToeGame(UUID playerX, UUID playerO, boolean ranked) {
        this.playerX = playerX;
        this.playerO = playerO;
        this.board = new CellState[9];
        for (int i = 0; i < 9; i++) {
            board[i] = CellState.EMPTY;
        }
        this.xTurn = true;
        this.round = 1;
        this.movesX = 0;
        this.movesO = 0;
        this.result = GameResult.IN_PROGRESS;
        this.startTime = System.currentTimeMillis();
        this.lastMoveTime = startTime;
        this.ranked = ranked;
    }

    public boolean makeMove(int position, UUID playerUuid) {
        if (result != GameResult.IN_PROGRESS) return false;
        if (position < 0 || position > 8) return false;
        if (board[position] != CellState.EMPTY) return false;

        // Prüfe ob der richtige Spieler dran ist
        if (xTurn && !playerUuid.equals(playerX)) return false;
        if (!xTurn && !playerUuid.equals(playerO)) return false;

        board[position] = xTurn ? CellState.X : CellState.O;

        if (xTurn) movesX++;
        else movesO++;

        lastMoveTime = System.currentTimeMillis();

        // Prüfe Spielergebnis
        result = checkResult();

        if (result == GameResult.IN_PROGRESS) {
            xTurn = !xTurn;
            round++;
        }

        return true;
    }

    private GameResult checkResult() {
        // Alle Gewinn-Kombinationen
        int[][] winLines = {
                {0, 1, 2}, {3, 4, 5}, {6, 7, 8}, // Horizontal
                {0, 3, 6}, {1, 4, 7}, {2, 5, 8}, // Vertikal
                {0, 4, 8}, {2, 4, 6}              // Diagonal
        };

        for (int[] line : winLines) {
            CellState a = board[line[0]];
            CellState b = board[line[1]];
            CellState c = board[line[2]];

            if (a != CellState.EMPTY && a == b && b == c) {
                return a == CellState.X ? GameResult.WIN_X : GameResult.WIN_O;
            }
        }

        // Prüfe auf Unentschieden
        boolean hasEmpty = false;
        for (CellState cell : board) {
            if (cell == CellState.EMPTY) {
                hasEmpty = true;
                break;
            }
        }

        return hasEmpty ? GameResult.IN_PROGRESS : GameResult.DRAW;
    }

    public int[] getWinLine() {
        int[][] winLines = {
                {0, 1, 2}, {3, 4, 5}, {6, 7, 8},
                {0, 3, 6}, {1, 4, 7}, {2, 5, 8},
                {0, 4, 8}, {2, 4, 6}
        };

        for (int[] line : winLines) {
            CellState a = board[line[0]];
            CellState b = board[line[1]];
            CellState c = board[line[2]];

            if (a != CellState.EMPTY && a == b && b == c) {
                return line;
            }
        }
        return null;
    }

    public UUID getPlayerX() { return playerX; }
    public UUID getPlayerO() { return playerO; }
    public CellState[] getBoard() { return board; }
    public CellState getCell(int position) { return board[position]; }
    public boolean isXTurn() { return xTurn; }
    public int getRound() { return round; }
    public int getMovesX() { return movesX; }
    public int getMovesO() { return movesO; }
    public GameResult getResult() { return result; }
    public long getStartTime() { return startTime; }
    public long getLastMoveTime() { return lastMoveTime; }
    public boolean isRanked() { return ranked; }

    public UUID getCurrentPlayer() {
        return xTurn ? playerX : playerO;
    }
    public UUID getOpponent(UUID player) {
        return player.equals(playerX) ? playerO : playerX;
    }
    public boolean isPlayer(UUID uuid) {
        return uuid.equals(playerX) || uuid.equals(playerO);
    }
    public CellState getSymbol(UUID player) {
        return player.equals(playerX) ? CellState.X : CellState.O;
    }
    public void forfeit(UUID quitter) {
        if (result != GameResult.IN_PROGRESS) return;
        result = quitter.equals(playerX) ? GameResult.WIN_O : GameResult.WIN_X;
    }

    public void cancel() {
        if (result != GameResult.IN_PROGRESS) return;
        result = GameResult.CANCELLED;
    }

    public int getTotalMoves() {
        return movesX + movesO;
    }
    public int getMovesFor(UUID player) {
        return player.equals(playerX) ? movesX : movesO;
    }
}
