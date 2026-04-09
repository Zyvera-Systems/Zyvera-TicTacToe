package de.zyvera.tictactoe.data;

import de.zyvera.tictactoe.util.SchedulerUtil;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Verwaltet Spieler-Statistiken mit YAML-Persistenz.
 */
public class StatsManager {

    private final Plugin plugin;
    private final File statsFile;
    private FileConfiguration statsConfig;
    private final Map<UUID, PlayerStats> cache = new ConcurrentHashMap<>();

    public StatsManager(Plugin plugin) {
        this.plugin = plugin;
        this.statsFile = new File(plugin.getDataFolder(), "stats.yml");
        loadStats();
    }

    /**
     * Lädt alle Stats aus der YAML-Datei.
     */
    public void loadStats() {
        if (!statsFile.exists()) {
            try {
                statsFile.getParentFile().mkdirs();
                statsFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Konnte stats.yml nicht erstellen: " + e.getMessage());
            }
        }
        statsConfig = YamlConfiguration.loadConfiguration(statsFile);
        cache.clear();

        if (statsConfig.contains("players")) {
            for (String uuidStr : statsConfig.getConfigurationSection("players").getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(uuidStr);
                    String path = "players." + uuidStr;
                    String name = statsConfig.getString(path + ".name", "Unknown");

                    PlayerStats stats = new PlayerStats(uuid, name);
                    stats.setWins(statsConfig.getInt(path + ".wins", 0));
                    stats.setLosses(statsConfig.getInt(path + ".losses", 0));
                    stats.setDraws(statsConfig.getInt(path + ".draws", 0));
                    stats.setGamesPlayed(statsConfig.getInt(path + ".games-played", 0));
                    stats.setTotalMoves(statsConfig.getInt(path + ".total-moves", 0));
                    stats.setWinStreak(statsConfig.getInt(path + ".win-streak", 0));
                    stats.setBestWinStreak(statsConfig.getInt(path + ".best-win-streak", 0));
                    stats.setLastPlayed(statsConfig.getLong(path + ".last-played", 0));

                    cache.put(uuid, stats);
                } catch (IllegalArgumentException ignored) {}
            }
        }
    }

    /**
     * Speichert alle Stats in die YAML-Datei.
     */
    public void saveStats() {
        for (Map.Entry<UUID, PlayerStats> entry : cache.entrySet()) {
            String path = "players." + entry.getKey().toString();
            PlayerStats s = entry.getValue();

            statsConfig.set(path + ".name", s.getLastKnownName());
            statsConfig.set(path + ".wins", s.getWins());
            statsConfig.set(path + ".losses", s.getLosses());
            statsConfig.set(path + ".draws", s.getDraws());
            statsConfig.set(path + ".games-played", s.getGamesPlayed());
            statsConfig.set(path + ".total-moves", s.getTotalMoves());
            statsConfig.set(path + ".win-streak", s.getWinStreak());
            statsConfig.set(path + ".best-win-streak", s.getBestWinStreak());
            statsConfig.set(path + ".last-played", s.getLastPlayed());
        }

        try {
            statsConfig.save(statsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Konnte stats.yml nicht speichern: " + e.getMessage());
        }
    }

    /**
     * Speichert asynchron.
     */
    public void saveAsync() {
        SchedulerUtil.runAsync(plugin, this::saveStats);
    }

    /**
     * Holt oder erstellt Stats für einen Spieler.
     */
    public PlayerStats getStats(UUID uuid, String name) {
        return cache.computeIfAbsent(uuid, k -> new PlayerStats(uuid, name));
    }

    /**
     * Holt Stats falls vorhanden.
     */
    public PlayerStats getStats(UUID uuid) {
        return cache.get(uuid);
    }

    /**
     * Setzt die Stats eines Spielers zurück.
     */
    public void resetStats(UUID uuid) {
        PlayerStats stats = cache.get(uuid);
        if (stats != null) {
            String name = stats.getLastKnownName();
            cache.put(uuid, new PlayerStats(uuid, name));
            saveAsync();
        }
    }

    /**
     * Gibt die Top-Spieler nach Wins sortiert zurück.
     */
    public List<PlayerStats> getTopPlayers(int limit) {
        List<PlayerStats> all = new ArrayList<>(cache.values());
        all.sort((a, b) -> {
            int cmp = Integer.compare(b.getWins(), a.getWins());
            if (cmp != 0) return cmp;
            return Double.compare(b.getWinRate(), a.getWinRate());
        });
        return all.subList(0, Math.min(limit, all.size()));
    }

    /**
     * Gibt alle gecachten Stats zurück.
     */
    public Collection<PlayerStats> getAllStats() {
        return cache.values();
    }
}
