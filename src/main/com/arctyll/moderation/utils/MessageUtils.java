package com.arctyll.moderation.utils;

import com.arctyll.moderation.managers.LanguageManager;
import org.bukkit.ChatColor;

public class MessageUtils {
    private final LanguageManager languageManager;

    public MessageUtils(LanguageManager languageManager) {
        this.languageManager = languageManager;
    }

    public String formatMessage(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public String stripColors(String message) {
        return ChatColor.stripColor(formatMessage(message));
    }

    public String centerMessage(String message, int length) {
        if (message.length() >= length) {
            return message;
        }

        int padding = (length - stripColors(message).length()) / 2;
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < padding; i++) {
            builder.append(" ");
        }

        builder.append(message);
        return builder.toString();
    }

    public String createSeparator(char character, int length) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            builder.append(character);
        }
        return builder.toString();
    }

    public String formatList(String[] items, String separator) {
        return String.join(separator, items);
    }
}
