package de.zyvera.tictactoe.util;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;

public final class MessageUtil {

    private static String prefix = "&8[&6&lTTT&8] ";
    private static FileConfiguration config;
    private static final Map<String, String> DEFAULTS = new HashMap<>();

    static {
        DEFAULTS.put("no-permission", "&cDu hast keine Berechtigung!");
        DEFAULTS.put("player-only", "&cDieser Befehl ist nur für Spieler!");
        DEFAULTS.put("queue-joined", "&aDu bist der Warteschlange beigetreten!");
        DEFAULTS.put("queue-left", "&eDu hast die Warteschlange verlassen.");
        DEFAULTS.put("queue-already", "&eDu bist bereits in der Warteschlange!");
        DEFAULTS.put("game-found", "&aEin Gegner wurde gefunden! Das Spiel beginnt...");
        DEFAULTS.put("game-cancelled", "&eDas Spiel wurde wegen Inaktivität abgebrochen.");
        DEFAULTS.put("challenge-sent", "&aDu hast &e%player% &aherausgefordert!");
        DEFAULTS.put("challenge-received", "&e%player% &ahat dich zu TicTacToe herausgefordert! &7/ttt accept");
        DEFAULTS.put("challenge-accepted", "&aHerausforderung angenommen! Das Spiel beginnt...");
        DEFAULTS.put("challenge-denied", "&eHerausforderung abgelehnt.");
        DEFAULTS.put("challenge-expired", "&eDie Herausforderung ist abgelaufen.");
        DEFAULTS.put("challenge-self", "&cDu kannst dich nicht selbst herausfordern!");
        DEFAULTS.put("player-not-found", "&cSpieler nicht gefunden!");
        DEFAULTS.put("player-ingame", "&cDieser Spieler ist bereits in einem Spiel!");
        DEFAULTS.put("already-ingame", "&cDu bist bereits in einem Spiel!");
        DEFAULTS.put("your-turn", "&aRunde: &e%round% &8| &aDu bist dran! &7(%symbol%)");
        DEFAULTS.put("opponent-turn", "&eRunde: &e%round% &8| &e%player% &eist dran...");
        DEFAULTS.put("game-win", "&a&lGlückwunsch! &aDu hast gegen &e%player% &agewonnen!");
        DEFAULTS.put("game-lose", "&cDu hast gegen &e%player% &cverloren.");
        DEFAULTS.put("game-draw", "&eUnentschieden gegen &e%player%&e!");
        DEFAULTS.put("game-quit", "&eDu hast das Spiel verlassen.");
        DEFAULTS.put("opponent-quit", "&eDein Gegner hat das Spiel verlassen. Du gewinnst!");
        DEFAULTS.put("timeout-win", "&aDein Gegner war zu langsam! Du gewinnst!");
        DEFAULTS.put("timeout-lose", "&cDu warst zu langsam! Du hast verloren.");
        DEFAULTS.put("bind-success", "&aWerkbank erfolgreich als TicTacToe-Station gebunden!");
        DEFAULTS.put("bind-removed", "&eWerkbank-Bindung entfernt.");
        DEFAULTS.put("bind-mode", "&aKlicke auf eine Werkbank, um sie zu binden.");
        DEFAULTS.put("unbind-mode", "&aKlicke auf eine gebundene Werkbank, um die Bindung zu entfernen.");
        DEFAULTS.put("not-a-workbench", "&cDas ist keine Werkbank!");
        DEFAULTS.put("stats-reset", "&aStats wurden zurückgesetzt.");
    }

    private MessageUtil() {}

    public static void init(FileConfiguration cfg) {
        config = cfg;
        prefix = cfg.getString("prefix", "&8[&6&lTTT&8] ");
    }

    public static String colorize(String message) {
        if (message == null) return "";
        // Hex-Farbcodes: &#RRGGBB → §x§R§R§G§G§B§B (1.16+)
        message = translateHexColors(message);
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    /**
     * Konvertiert &#RRGGBB zu §x§R§r§G§g§B§b Format.
     */
    private static String translateHexColors(String message) {
        java.util.regex.Pattern hexPattern = java.util.regex.Pattern.compile("&#([A-Fa-f0-9]{6})");
        java.util.regex.Matcher matcher = hexPattern.matcher(message);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String hex = matcher.group(1);
            StringBuilder replacement = new StringBuilder("§x");
            for (char c : hex.toCharArray()) {
                replacement.append('§').append(c);
            }
            matcher.appendReplacement(sb, replacement.toString());
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    public static void send(CommandSender sender, String message) {
        sender.sendMessage(colorize(prefix + message));
    }

    public static void sendRaw(CommandSender sender, String message) {
        sender.sendMessage(colorize(message));
    }

    public static String getMessage(String key) {
        if (config != null) {
            String val = config.getString("messages." + key);
            if (val != null) return val;
        }
        String def = DEFAULTS.get(key);
        return def != null ? def : key;
    }

    public static void sendMessage(CommandSender sender, String key) {
        send(sender, getMessage(key));
    }

    public static void sendMessage(CommandSender sender, String key, String placeholder, String value) {
        String msg = getMessage(key).replace("%" + placeholder + "%", value);
        send(sender, msg);
    }

    public static void sendMessage(CommandSender sender, String key, String[] placeholders, String[] values) {
        String msg = getMessage(key);
        for (int i = 0; i < placeholders.length && i < values.length; i++) {
            msg = msg.replace("%" + placeholders[i] + "%", values[i]);
        }
        send(sender, msg);
    }

    public static String getPrefix() {
        return prefix;
    }
}
