package de.zyvera.tictactoe.util;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

public final class MessageUtil {

    private static String prefix = "&8[&b&lTTT&8] ";
    private static FileConfiguration config;

    private MessageUtil() {}

    public static void init(FileConfiguration cfg) {
        config = cfg;
        prefix = cfg.getString("prefix", "&8[&b&lTTT&8] ");
    }

    public static String colorize(String message) {
        if (message == null) return "";
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static void send(CommandSender sender, String message) {
        sender.sendMessage(colorize(prefix + message));
    }
    public static void sendRaw(CommandSender sender, String message) {
        sender.sendMessage(colorize(message));
    }
    public static String getMessage(String key) {
        if (config == null) return key;
        return config.getString("messages." + key, key);
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
