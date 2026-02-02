package com.arctyll.moderation.commands;

import com.arctyll.moderation.Moderation;
import com.arctyll.moderation.data.models.MuteData;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class UnmuteCommand implements CommandExecutor {
    private final Moderation plugin;

    public UnmuteCommand(Moderation plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("punishment.unmute")) {
            sender.sendMessage(plugin.getMessageUtils().formatMessage(
								   plugin.getLanguageManager().getMessage(sender, "no-permission")));
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(plugin.getMessageUtils().formatMessage(
								   plugin.getLanguageManager().getMessage(sender, "unmute-usage")));
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(plugin.getMessageUtils().formatMessage(
								   plugin.getLanguageManager().getMessage(sender, "player-not-found")
								   .replace("{player}", args[0])));
            return true;
        }

        if (!plugin.getMutedPlayers().containsKey(target.getUniqueId())) {
            sender.sendMessage(plugin.getMessageUtils().formatMessage(
								   plugin.getLanguageManager().getMessage(sender, "not-muted")
								   .replace("{player}", target.getName())));
            return true;
        }

        MuteData muteData = plugin.getMutedPlayers().remove(target.getUniqueId());
        plugin.getDataManager().savePunishmentData();

        target.sendMessage(plugin.getMessageUtils().formatMessage(
							   plugin.getLanguageManager().getMessage(target, "unmute-message")));

        plugin.getPunishmentManager().broadcastUnmuteToStaff(sender, target, 
															 muteData.reason, muteData.punishedBy, muteData.muteId);

        return true;
    }
}
