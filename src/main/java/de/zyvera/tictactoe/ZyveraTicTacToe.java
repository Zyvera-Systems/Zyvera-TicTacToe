package de.zyvera.tictactoe;

import de.zyvera.tictactoe.command.TttCommand;
import de.zyvera.tictactoe.data.StatsManager;
import de.zyvera.tictactoe.game.GameManager;
import de.zyvera.tictactoe.listener.BlockInteractListener;
import de.zyvera.tictactoe.listener.GuiClickListener;
import de.zyvera.tictactoe.listener.PlayerListener;
import de.zyvera.tictactoe.util.HologramUtil;
import de.zyvera.tictactoe.util.MessageUtil;
import de.zyvera.tictactoe.util.SchedulerUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Zyvera-TicTacToe v1.2
 * Autoren: Thomas U. & Zyvera-Systems
 *
 * Kompatibel mit: Bukkit, Spigot, Paper, Purpur, Folia
 * Versionen: 1.13 - 1.21+
 */
public class ZyveraTicTacToe extends JavaPlugin {

    private GameManager gameManager;
    private StatsManager statsManager;
    private BlockInteractListener blockListener;

    private final Set<String> boundWorkbenches = new HashSet<>();
    private File workbenchFile;
    private FileConfiguration workbenchConfig;

    @Override
    public void onEnable() {
        long startTime = System.currentTimeMillis();

        saveDefaultConfig();
        MessageUtil.init(getConfig());

        if (SchedulerUtil.isFolia()) {
            getLogger().info("Folia erkannt! Verwende regionisierten Scheduler.");
        } else {
            getLogger().info("Standard-Server erkannt (Bukkit/Spigot/Paper/Purpur).");
        }

        statsManager = new StatsManager(this);
        gameManager = new GameManager(this);

        loadWorkbenches();

        blockListener = new BlockInteractListener(this);
        Bukkit.getPluginManager().registerEvents(new GuiClickListener(this), this);
        Bukkit.getPluginManager().registerEvents(blockListener, this);
        Bukkit.getPluginManager().registerEvents(new PlayerListener(this), this);

        TttCommand cmdHandler = new TttCommand(this);
        PluginCommand tttCmd = getCommand("ttt");
        if (tttCmd != null) {
            tttCmd.setExecutor(cmdHandler);
            tttCmd.setTabCompleter(cmdHandler);
        }

        gameManager.startTasks();

        // DecentHolograms: Hologramme nach kurzer Verzögerung spawnen
        // (DH muss zuerst geladen sein, daher Delay)
        SchedulerUtil.runLater(this, this::spawnAllHolograms, 60L);

        long elapsed = System.currentTimeMillis() - startTime;
        getLogger().info("=================================");
        getLogger().info(" Zyvera-TicTacToe v" + getDescription().getVersion());
        getLogger().info(" Autoren: Thomas U. & Zyvera-Systems");
        getLogger().info(" Geladen in " + elapsed + "ms");
        getLogger().info("=================================");
    }

    @Override
    public void onDisable() {
        if (gameManager != null) {
            gameManager.stopTasks();
        }

        if (statsManager != null) {
            statsManager.saveStats();
        }

        saveWorkbenches();

        getLogger().info("Zyvera-TicTacToe deaktiviert.");
    }

    // --- Werkbank-Bindungen ---

    private void loadWorkbenches() {
        workbenchFile = new File(getDataFolder(), "workbenches.yml");
        if (!workbenchFile.exists()) {
            try {
                workbenchFile.getParentFile().mkdirs();
                workbenchFile.createNewFile();
            } catch (IOException e) {
                getLogger().severe("Konnte workbenches.yml nicht erstellen: " + e.getMessage());
            }
        }

        workbenchConfig = YamlConfiguration.loadConfiguration(workbenchFile);
        List<String> locations = workbenchConfig.getStringList("bound-workbenches");
        boundWorkbenches.addAll(locations);

        getLogger().info(boundWorkbenches.size() + " gebundene Werkbänke geladen.");
    }

    private void saveWorkbenches() {
        if (workbenchConfig == null) return;
        workbenchConfig.set("bound-workbenches", new ArrayList<>(boundWorkbenches));
        try {
            workbenchConfig.save(workbenchFile);
        } catch (IOException e) {
            getLogger().severe("Konnte workbenches.yml nicht speichern: " + e.getMessage());
        }
    }

    private String locationToKey(Location loc) {
        return loc.getWorld().getName() + ":" + loc.getBlockX() + ":" + loc.getBlockY() + ":" + loc.getBlockZ();
    }

    private Location keyToLocation(String key) {
        String[] parts = key.split(":");
        if (parts.length != 4) return null;
        World world = Bukkit.getWorld(parts[0]);
        if (world == null) return null;
        try {
            int x = Integer.parseInt(parts[1]);
            int y = Integer.parseInt(parts[2]);
            int z = Integer.parseInt(parts[3]);
            return new Location(world, x, y, z);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public boolean addBoundWorkbench(Location loc) {
        String key = locationToKey(loc);
        if (boundWorkbenches.add(key)) {
            saveWorkbenches();
            spawnHologram(loc);
            return true;
        }
        return false;
    }

    public boolean removeBoundWorkbench(Location loc) {
        String key = locationToKey(loc);
        if (boundWorkbenches.remove(key)) {
            saveWorkbenches();
            HologramUtil.remove(loc);
            return true;
        }
        return false;
    }

    public boolean isBoundWorkbench(Location loc) {
        return boundWorkbenches.contains(locationToKey(loc));
    }

    // --- Hologramme (DecentHolograms) ---

    public void spawnHologram(Location loc) {
        if (loc.getWorld() == null) return;

        String line1 = getConfig().getString("hologram.line1", "&6&lTicTacToe");
        String line2 = getConfig().getString("hologram.line2", "&8[&aKlick Mich&8]");
        double offsetY = getConfig().getDouble("hologram.offset-y", 1.5);

        boolean success = HologramUtil.create(loc, new String[]{line1, line2}, offsetY);
        if (!success && HologramUtil.isAvailable()) {
            getLogger().warning("Hologramm konnte nicht erstellt werden bei "
                    + loc.getWorld().getName() + " " + loc.getBlockX() + "/" + loc.getBlockY() + "/" + loc.getBlockZ());
        }
    }

    private void spawnAllHolograms() {
        if (!HologramUtil.isAvailable()) return;

        int count = 0;
        for (String key : boundWorkbenches) {
            Location loc = keyToLocation(key);
            if (loc != null) {
                spawnHologram(loc);
                count++;
            }
        }
        if (count > 0) {
            getLogger().info(count + " DecentHolograms-Hologramme erstellt.");
        }
    }

    public Set<String> getBoundWorkbenchKeys() {
        return Collections.unmodifiableSet(boundWorkbenches);
    }

    // --- Getter ---

    public GameManager getGameManager() { return gameManager; }
    public StatsManager getStatsManager() { return statsManager; }
    public BlockInteractListener getBlockListener() { return blockListener; }
}
