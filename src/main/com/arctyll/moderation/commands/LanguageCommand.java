package com.arctyll.moderation.commands;

import com.arctyll.moderation.Moderation;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LanguageCommand implements CommandExecutor {
    private final Moderation plugin;

    public LanguageCommand(Moderation plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getMessageUtils().formatMessage(
								   plugin.getLanguageManager().getMessage(sender, "players-only")));
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            String currentLang = plugin.getPlayerLanguages().getOrDefault(
                player.getUniqueId(), plugin.getLanguageManager().getDefaultLanguage());
            String availableLanguages = String.join(", ", 
													plugin.getLanguageManager().getLanguageFiles().keySet());

            sender.sendMessage(plugin.getMessageUtils().formatMessage(
								   plugin.getLanguageManager().getMessage(sender, "language-info")
								   .replace("{current}", currentLang)
								   .replace("{available}", availableLanguages)));
            return true;
        }

        String newLanguage = args[0].toLowerCase();

        if (!plugin.getLanguageManager().getLanguageFiles().containsKey(newLanguage)) {
            String availableLanguages = String.join(", ", 
													plugin.getLanguageManager().getLanguageFiles().keySet());
            sender.sendMessage(plugin.getMessageUtils().formatMessage(
								   plugin.getLanguageManager().getMessage(sender, "invalid-language")
								   .replace("{language}", newLanguage)
								   .replace("{available}", availableLanguages)));
            return true;
        }

        plugin.getPlayerLanguages().put(player.getUniqueId(), newLanguage);
        plugin.getDataManager().savePlayerLanguages();

        sender.sendMessage(plugin.getMessageUtils().formatMessage(
							   plugin.getLanguageManager().getMessage(sender, "language-changed")
							   .replace("{language}", newLanguage)));

        return true;
    }
}
