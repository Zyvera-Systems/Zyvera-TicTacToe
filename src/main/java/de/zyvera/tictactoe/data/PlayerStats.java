package de.zyvera.tictactoe.data;

import java.util.UUID;

/**
 * Speichert die Statistiken eines Spielers.
 */
public class PlayerStats {

    private final UUID uuid;
    private String lastKnownName;
    private int wins;
    private int losses;
    private int draws;
    private int gamesPlayed;
    private int totalMoves;  // Gesamtanzahl gesetzter Züge
    private int winStreak;
    private int bestWinStreak;
    private long lastPlayed;

    public PlayerStats(UUID uuid, String name) {
        this.uuid = uuid;
        this.lastKnownName = name;
        this.wins = 0;
        this.losses = 0;
        this.draws = 0;
        this.gamesPlayed = 0;
        this.totalMoves = 0;
        this.winStreak = 0;
        this.bestWinStreak = 0;
        this.lastPlayed = 0;
    }

    public UUID getUuid() { return uuid; }
    public String getLastKnownName() { return lastKnownName; }
    public void setLastKnownName(String name) { this.lastKnownName = name; }

    public int getWins() { return wins; }
    public void setWins(int wins) { this.wins = wins; }

    public int getLosses() { return losses; }
    public void setLosses(int losses) { this.losses = losses; }

    public int getDraws() { return draws; }
    public void setDraws(int draws) { this.draws = draws; }

    public int getGamesPlayed() { return gamesPlayed; }
    public void setGamesPlayed(int gamesPlayed) { this.gamesPlayed = gamesPlayed; }

    public int getTotalMoves() { return totalMoves; }
    public void setTotalMoves(int totalMoves) { this.totalMoves = totalMoves; }

    public int getWinStreak() { return winStreak; }
    public void setWinStreak(int winStreak) { this.winStreak = winStreak; }

    public int getBestWinStreak() { return bestWinStreak; }
    public void setBestWinStreak(int bestWinStreak) { this.bestWinStreak = bestWinStreak; }

    public long getLastPlayed() { return lastPlayed; }
    public void setLastPlayed(long lastPlayed) { this.lastPlayed = lastPlayed; }

    /**
     * Berechnet die Winrate in Prozent.
     */
    public double getWinRate() {
        if (gamesPlayed == 0) return 0.0;
        return Math.round((double) wins / gamesPlayed * 10000.0) / 100.0;
    }

    /**
     * Durchschnittliche Züge pro Spiel.
     */
    public double getAvgMovesPerGame() {
        if (gamesPlayed == 0) return 0.0;
        return Math.round((double) totalMoves / gamesPlayed * 100.0) / 100.0;
    }

    /**
     * Registriert einen Sieg.
     */
    public void addWin(int moves) {
        wins++;
        gamesPlayed++;
        totalMoves += moves;
        winStreak++;
        if (winStreak > bestWinStreak) bestWinStreak = winStreak;
        lastPlayed = System.currentTimeMillis();
    }

    /**
     * Registriert eine Niederlage.
     */
    public void addLoss(int moves) {
        losses++;
        gamesPlayed++;
        totalMoves += moves;
        winStreak = 0;
        lastPlayed = System.currentTimeMillis();
    }

    /**
     * Registriert ein Unentschieden.
     */
    public void addDraw(int moves) {
        draws++;
        gamesPlayed++;
        totalMoves += moves;
        winStreak = 0;
        lastPlayed = System.currentTimeMillis();
    }
}
