package de.zyvera.tictactoe.util;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public final class HologramUtil {

    // Key: "world:x:y:z" -> Liste von ArmorStands (mehrzeilig)
    private static final Map<String, List<ArmorStand>> holograms = new HashMap<>();

    private HologramUtil() {}

    public static void create(Location loc, String[] lines, double offsetY) {
        String key = locationKey(loc);
        remove(loc); // Vorherige entfernen

        World world = loc.getWorld();
        if (world == null) return;

        List<ArmorStand> stands = new ArrayList<>();
        double lineSpacing = 0.28;

        for (int i = 0; i < lines.length; i++) {
            double y = offsetY - (i * lineSpacing);
            Location spawnLoc = loc.clone().add(0.5, y, 0.5);

            ArmorStand stand = (ArmorStand) world.spawnEntity(spawnLoc, EntityType.ARMOR_STAND);
            stand.setVisible(false);
            stand.setGravity(false);
            stand.setCustomName(MessageUtil.colorize(lines[i]));
            stand.setCustomNameVisible(true);
            stand.setCanPickupItems(false);
            stand.setSmall(true);

            try {
                // 1.16+: setMarker
                stand.getClass().getMethod("setMarker", boolean.class).invoke(stand, true);
            } catch (Exception ignored) {}

            try {
                // Invulnerable (1.9+)
                stand.getClass().getMethod("setInvulnerable", boolean.class).invoke(stand, true);
            } catch (Exception ignored) {}

            stands.add(stand);
        }

        holograms.put(key, stands);
    }

    public static void remove(Location loc) {
        String key = locationKey(loc);
        List<ArmorStand> stands = holograms.remove(key);
        if (stands != null) {
            for (ArmorStand stand : stands) {
                if (stand != null && !stand.isDead()) {
                    stand.remove();
                }
            }
        }
    }

    public static void removeAll() {
        for (List<ArmorStand> stands : holograms.values()) {
            for (ArmorStand stand : stands) {
                if (stand != null && !stand.isDead()) {
                    stand.remove();
                }
            }
        }
        holograms.clear();
    }

    public static boolean exists(Location loc) {
        return holograms.containsKey(locationKey(loc));
    }

    private static String locationKey(Location loc) {
        return loc.getWorld().getName() + ":" + loc.getBlockX() + ":" + loc.getBlockY() + ":" + loc.getBlockZ();
    }
}
