package de.zyvera.tictactoe.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.lang.reflect.Method;
import java.util.List;

/**
 * DecentHolograms-Integration via Reflection (keine Compile-Dependency).
 *
 * Erstellt und löscht Hologramme über die DHAPI-Klasse.
 * Falls DecentHolograms nicht installiert ist, werden Hologramme still übersprungen.
 */
public final class HologramUtil {

    private static boolean available = false;
    private static boolean checked = false;
    private static Class<?> dhapiClass;

    private HologramUtil() {}

    /**
     * Prüft ob DecentHolograms auf dem Server verfügbar ist.
     */
    public static boolean isAvailable() {
        if (!checked) {
            checked = true;
            try {
                dhapiClass = Class.forName("eu.decentsoftware.holograms.api.DHAPI");
                available = true;
                Bukkit.getLogger().info("[Zyvera-TicTacToe] DecentHolograms gefunden - Hologramme aktiviert.");
            } catch (ClassNotFoundException e) {
                available = false;
                Bukkit.getLogger().info("[Zyvera-TicTacToe] DecentHolograms nicht gefunden - Hologramme deaktiviert.");
            }
        }
        return available;
    }

    /**
     * Erstellt ein Hologramm über einer Location.
     * @param loc Block-Location der Werkbank
     * @param lines Text-Zeilen (mit Farbcodes)
     * @param offsetY Höhe über dem Block
     */
    public static boolean create(Location loc, String[] lines, double offsetY) {
        if (!isAvailable()) return false;

        String name = hologramName(loc);

        // Vorheriges Hologramm an dieser Stelle löschen
        remove(loc);

        try {
            // DecentHolograms hat eigene Farbcode-Unterstützung (&#RRGGBB, &a, etc.)
            // Daher Zeilen NICHT vorher colorizen — DH macht das selbst.
            List<String> lineList = new java.util.ArrayList<>();
            for (String line : lines) {
                lineList.add(line);
            }

            // Spawn-Location: Mitte des Blocks + Offset
            Location spawnLoc = loc.clone().add(0.5, offsetY, 0.5);

            // DHAPI.createHologram(String name, Location loc, boolean saveToFile, List<String> lines)
            Method createMethod = dhapiClass.getMethod("createHologram",
                    String.class, Location.class, boolean.class, List.class);
            createMethod.invoke(null, name, spawnLoc, true, lineList);

            return true;
        } catch (Exception e) {
            Bukkit.getLogger().warning("[Zyvera-TicTacToe] Hologramm konnte nicht erstellt werden: " + e.getMessage());
            return false;
        }
    }

    /**
     * Entfernt ein Hologramm an einer Location.
     */
    public static void remove(Location loc) {
        if (!isAvailable()) return;

        String name = hologramName(loc);

        try {
            // DHAPI.removeHologram(String name)
            Method removeMethod = dhapiClass.getMethod("removeHologram", String.class);
            removeMethod.invoke(null, name);
        } catch (Exception ignored) {
            // Hologramm existiert möglicherweise nicht — kein Fehler
        }
    }

    /**
     * Entfernt alle TTT-Hologramme.
     */
    public static void removeAll(java.util.Set<String> workbenchKeys) {
        if (!isAvailable()) return;

        for (String key : workbenchKeys) {
            String name = "ttt_" + key.replace(":", "_");
            try {
                Method removeMethod = dhapiClass.getMethod("removeHologram", String.class);
                removeMethod.invoke(null, name);
            } catch (Exception ignored) {}
        }
    }

    /**
     * Generiert einen eindeutigen Hologramm-Namen aus der Block-Location.
     * Format: ttt_world_x_y_z
     */
    private static String hologramName(Location loc) {
        return "ttt_" + loc.getWorld().getName() + "_" + loc.getBlockX() + "_" + loc.getBlockY() + "_" + loc.getBlockZ();
    }
}
