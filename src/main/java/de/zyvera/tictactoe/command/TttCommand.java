package de.zyvera.tictactoe.command;

import de.zyvera.tictactoe.ZyveraTicTacToe;
import de.zyvera.tictactoe.data.PlayerStats;
import de.zyvera.tictactoe.gui.MainMenuGui;
import de.zyvera.tictactoe.gui.StatsGui;
import de.zyvera.tictactoe.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class TttCommand implements CommandExecutor, TabCompleter {

    private final ZyveraTicTacToe plugin;

    private static final List<String> SUBCOMMANDS = Arrays.asList(
            "play", "leave", "challenge", "accept", "deny", "stats", "top",
            "bind", "unbind", "quit", "reload", "help"
    );

    public TttCommand(ZyveraTicTacToe plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            MessageUtil.sendMessage(sender, "player-only");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("zyvera.ttt.use")) {
            MessageUtil.sendMessage(player, "no-permission");
            return true;
        }

        if (args.length == 0) {
            MainMenuGui.open(plugin, player);
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "play":
            case "queue":
            case "join":
                handlePlay(player);
                break;
            case "leave":
                handleLeave(player);
                break;
            case "challenge":
            case "duel":
            case "herausfordern":
                handleChallenge(player, args);
                break;
            case "accept":
            case "annehmen":
                handleAccept(player);
                break;
            case "deny":
            case "decline":
            case "ablehnen":
                handleDeny(player);
                break;
            case "stats":
            case "statistiken":
                handleStats(player, args);
                break;
            case "top":
            case "rangliste":
            case "leaderboard":
                StatsGui.openTopPlayers(plugin, player);
                break;
            case "bind":
                handleBind(player);
                break;
            case "unbind":
                handleUnbind(player);
                break;
            case "quit":
            case "aufgeben":
            case "verlassen":
                handleQuit(player);
                break;
            case "reload":
                handleReload(player);
                break;
            case "help":
            case "hilfe":
                showHelp(player);
                break;
            default:
                showHelp(player);
                break;
        }

        return true;
    }

    private void handlePlay(Player player) {
        if (plugin.getGameManager().isInGame(player.getUniqueId())) {
            MessageUtil.sendMessage(player, "already-ingame");
            return;
        }

        if (plugin.getGameManager().getQueueManager().isInQueue(player.getUniqueId())) {
            MessageUtil.sendMessage(player, "queue-already");
            return;
        }

        if (plugin.getGameManager().getQueueManager().joinQueue(player.getUniqueId())) {
            MessageUtil.sendMessage(player, "queue-joined");
        }
    }

    private void handleLeave(Player player) {
        if (plugin.getGameManager().getQueueManager().leaveQueue(player.getUniqueId())) {
            MessageUtil.sendMessage(player, "queue-left");
        } else {
            MessageUtil.send(player, "&eDu bist nicht in der Warteschlange.");
        }
    }

    private void handleChallenge(Player player, String[] args) {
        if (args.length < 2) {
            MessageUtil.send(player, "&eVerwendung: &f/ttt challenge <Spieler>");
            return;
        }

        if (plugin.getGameManager().isInGame(player.getUniqueId())) {
            MessageUtil.sendMessage(player, "already-ingame");
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null || !target.isOnline()) {
            MessageUtil.sendMessage(player, "player-not-found");
            return;
        }

        if (target.getUniqueId().equals(player.getUniqueId())) {
            MessageUtil.sendMessage(player, "challenge-self");
            return;
        }

        if (plugin.getGameManager().isInGame(target.getUniqueId())) {
            MessageUtil.sendMessage(player, "player-ingame");
            return;
        }

        if (plugin.getGameManager().getQueueManager().sendChallenge(player.getUniqueId(), target.getUniqueId())) {
            MessageUtil.sendMessage(player, "challenge-sent", "player", target.getName());
            MessageUtil.sendMessage(target, "challenge-received", "player", player.getName());
        } else {
            MessageUtil.send(player, "&eDu hast bereits eine offene Herausforderung.");
        }
    }

    private void handleAccept(Player player) {
        UUID challenger = plugin.getGameManager().getQueueManager().acceptChallenge(player.getUniqueId());
        if (challenger != null) {
            MessageUtil.sendMessage(player, "challenge-accepted");
            Player challengerPlayer = Bukkit.getPlayer(challenger);
            if (challengerPlayer != null) {
                MessageUtil.sendMessage(challengerPlayer, "challenge-accepted");
            }
            plugin.getGameManager().startGame(challenger, player.getUniqueId(), false);
        } else {
            MessageUtil.send(player, "&eDu hast keine offene Herausforderung.");
        }
    }

    private void handleDeny(Player player) {
        UUID challenger = plugin.getGameManager().getQueueManager().denyChallenge(player.getUniqueId());
        if (challenger != null) {
            MessageUtil.sendMessage(player, "challenge-denied");
            Player challengerPlayer = Bukkit.getPlayer(challenger);
            if (challengerPlayer != null) {
                MessageUtil.sendMessage(challengerPlayer, "challenge-denied");
            }
        } else {
            MessageUtil.send(player, "&eDu hast keine offene Herausforderung.");
        }
    }

    private void handleStats(Player player, String[] args) {
        if (args.length >= 2) {
            if (!player.hasPermission("zyvera.ttt.stats.others")) {
                MessageUtil.sendMessage(player, "no-permission");
                return;
            }

            Player target = Bukkit.getPlayer(args[1]);
            if (target != null) {
                PlayerStats stats = plugin.getStatsManager().getStats(
                        target.getUniqueId(), target.getName());
                StatsGui.openStats(plugin, player, stats);
            } else {
                for (PlayerStats ps : plugin.getStatsManager().getAllStats()) {
                    if (ps.getLastKnownName().equalsIgnoreCase(args[1])) {
                        StatsGui.openStats(plugin, player, ps);
                        return;
                    }
                }
                MessageUtil.sendMessage(player, "player-not-found");
            }
        } else {
            PlayerStats stats = plugin.getStatsManager().getStats(
                    player.getUniqueId(), player.getName());
            StatsGui.openStats(plugin, player, stats);
        }
    }

    private void handleBind(Player player) {
        if (!player.hasPermission("zyvera.ttt.bind")) {
            MessageUtil.sendMessage(player, "no-permission");
            return;
        }
        plugin.getBlockListener().enableBindMode(player.getUniqueId());
        MessageUtil.sendMessage(player, "bind-mode");
    }

    private void handleUnbind(Player player) {
        if (!player.hasPermission("zyvera.ttt.bind")) {
            MessageUtil.sendMessage(player, "no-permission");
            return;
        }
        plugin.getBlockListener().enableUnbindMode(player.getUniqueId());
        MessageUtil.sendMessage(player, "unbind-mode");
    }

    private void handleQuit(Player player) {
        if (plugin.getGameManager().isInGame(player.getUniqueId())) {
            plugin.getGameManager().quitGame(player.getUniqueId());
        } else {
            MessageUtil.send(player, "&eDu bist in keinem Spiel.");
        }
    }

    private void handleReload(Player player) {
        if (!player.hasPermission("zyvera.ttt.admin")) {
            MessageUtil.sendMessage(player, "no-permission");
            return;
        }
        plugin.reloadConfig();
        MessageUtil.init(plugin.getConfig());
        MessageUtil.send(player, "&aKonfiguration wurde neu geladen!");
    }

    private void showHelp(Player player) {
        MessageUtil.sendRaw(player, "");
        MessageUtil.sendRaw(player, "&8&m                                          ");
        MessageUtil.sendRaw(player, " &6&lZyvera-TicTacToe &7- &fHilfe");
        MessageUtil.sendRaw(player, "&8&m                                          ");
        MessageUtil.sendRaw(player, "");
        MessageUtil.sendRaw(player, " &6/ttt &8- &7Hauptmenü öffnen");
        MessageUtil.sendRaw(player, " &6/ttt play &8- &7Warteschlange beitreten");
        MessageUtil.sendRaw(player, " &6/ttt leave &8- &7Warteschlange verlassen");
        MessageUtil.sendRaw(player, " &6/ttt challenge <n> &8- &7Spieler herausfordern");
        MessageUtil.sendRaw(player, " &6/ttt accept &8- &7Herausforderung annehmen");
        MessageUtil.sendRaw(player, " &6/ttt deny &8- &7Herausforderung ablehnen");
        MessageUtil.sendRaw(player, " &6/ttt stats [Name] &8- &7Statistiken anzeigen");
        MessageUtil.sendRaw(player, " &6/ttt top &8- &7Top-Spieler anzeigen");
        MessageUtil.sendRaw(player, " &6/ttt quit &8- &7Aktives Spiel verlassen");

        if (player.hasPermission("zyvera.ttt.bind")) {
            MessageUtil.sendRaw(player, " &6/ttt bind &8- &7Werkbank binden");
            MessageUtil.sendRaw(player, " &6/ttt unbind &8- &7Werkbank-Bindung entfernen");
        }
        if (player.hasPermission("zyvera.ttt.admin")) {
            MessageUtil.sendRaw(player, " &6/ttt reload &8- &7Config neuladen");
        }

        MessageUtil.sendRaw(player, "");
        MessageUtil.sendRaw(player, "&8&m                                          ");
        MessageUtil.sendRaw(player, " &7Autor: &fThomas U. &8& &fZyvera-Systems");
        MessageUtil.sendRaw(player, "&8&m                                          ");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            String input = args[0].toLowerCase();
            for (String sub : SUBCOMMANDS) {
                if (sub.startsWith(input)) {
                    completions.add(sub);
                }
            }
            return completions;
        }

        if (args.length == 2) {
            String sub = args[0].toLowerCase();
            if (sub.equals("challenge") || sub.equals("duel") || sub.equals("stats")) {
                List<String> players = new ArrayList<>();
                String input = args[1].toLowerCase();
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (p.getName().toLowerCase().startsWith(input) && !p.equals(sender)) {
                        players.add(p.getName());
                    }
                }
                return players;
            }
        }

        return new ArrayList<>();
    }
}
