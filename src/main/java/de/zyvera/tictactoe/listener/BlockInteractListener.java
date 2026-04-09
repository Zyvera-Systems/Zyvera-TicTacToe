package de.zyvera.tictactoe.listener;

import de.zyvera.tictactoe.ZyveraTicTacToe;
import de.zyvera.tictactoe.util.MessageUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BlockInteractListener implements Listener {

    private final ZyveraTicTacToe plugin;
    private final Set<UUID> bindMode = new HashSet<>();
    private final Set<UUID> unbindMode = new HashSet<>();
    private final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();

    public BlockInteractListener(ZyveraTicTacToe plugin) {
        this.plugin = plugin;
    }

    // HIGHEST + ignoreCancelled=false: Feuert NACH Lobby-Plugins die canceln,
    // sieht das Event trotzdem, und kann für gebundene Werkbänke handeln.
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block block = event.getClickedBlock();
        if (block == null) return;
        if (!isCraftingTable(block.getType())) return;

        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        Location loc = block.getLocation();

        // Bind-Modus
        if (bindMode.contains(uuid)) {
            event.setCancelled(true);
            bindMode.remove(uuid);
            plugin.addBoundWorkbench(loc);
            MessageUtil.sendMessage(player, "bind-success");
            return;
        }

        // Unbind-Modus
        if (unbindMode.contains(uuid)) {
            event.setCancelled(true);
            unbindMode.remove(uuid);
            if (plugin.removeBoundWorkbench(loc)) {
                MessageUtil.sendMessage(player, "bind-removed");
            } else {
                MessageUtil.send(player, "&eDiese Werkbank ist nicht gebunden.");
            }
            return;
        }

        // Gebundene Werkbank → Toggle Queue mit Cooldown
        if (plugin.isBoundWorkbench(loc)) {
            event.setCancelled(true);

            // Cooldown prüfen
            long cooldownMs = plugin.getConfig().getLong("game.workbench-cooldown", 1000);
            long now = System.currentTimeMillis();
            Long lastUse = cooldowns.get(uuid);
            if (lastUse != null && (now - lastUse) < cooldownMs) {
                return; // Stiller Cooldown
            }
            cooldowns.put(uuid, now);

            // Im Spiel → ignorieren
            if (plugin.getGameManager().isInGame(uuid)) {
                MessageUtil.sendMessage(player, "already-ingame");
                return;
            }

            // Toggle: Wenn in Queue → raus, sonst rein
            if (plugin.getGameManager().getQueueManager().isInQueue(uuid)) {
                plugin.getGameManager().getQueueManager().leaveQueue(uuid);
                MessageUtil.sendMessage(player, "queue-left");
            } else {
                if (plugin.getGameManager().getQueueManager().joinQueue(uuid)) {
                    MessageUtil.sendMessage(player, "queue-joined");
                }
            }
        }
    }

    private boolean isCraftingTable(Material material) {
        String name = material.name();
        return name.equals("CRAFTING_TABLE") || name.equals("WORKBENCH");
    }

    public void enableBindMode(UUID player) {
        unbindMode.remove(player);
        bindMode.add(player);
    }

    public void enableUnbindMode(UUID player) {
        bindMode.remove(player);
        unbindMode.add(player);
    }

    public void cancelModes(UUID player) {
        bindMode.remove(player);
        unbindMode.remove(player);
        cooldowns.remove(player);
    }
}
