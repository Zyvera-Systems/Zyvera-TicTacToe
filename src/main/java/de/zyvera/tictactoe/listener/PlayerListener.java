package de.zyvera.tictactoe.listener;

import de.zyvera.tictactoe.ZyveraTicTacToe;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Listener für Spieler-Events (Join/Quit).
 */
public class PlayerListener implements Listener {

    private final ZyveraTicTacToe plugin;

    public PlayerListener(ZyveraTicTacToe plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        plugin.getGameManager().handleDisconnect(player.getUniqueId());
        plugin.getBlockListener().cancelModes(player.getUniqueId());
    }
}
