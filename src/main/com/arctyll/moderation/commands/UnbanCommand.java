package com.arctyll.moderation.commands;

import com.arctyll.moderation.Moderation;
import com.arctyll.moderation.data.models.BanData;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class UnbanCommand implements CommandExecutor {
    private final Moderation plugin;

    public UnbanCommand(Moderation plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("punishment.unban")) {
            sender.sendMessage(plugin.getMessageUtils().formatMessage(
								   plugin.getLanguageManager().getMessage(sender, "no-permission")));
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(plugin.getMessageUtils().formatMessage(
								   plugin.getLanguageManager().getMessage(sender, "unban-usage")));
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        UUID targetUUID = null;
        String targetName = args[0];

        if (target != null) {
            targetUUID = target.getUniqueId();
            targetName = target.getName();
        } else {
            for (BanData ban : plugin.getPermanentlyBanned().values()) {
                if (ban.playerName.equalsIgnoreCase(args[0])) {
                    targetUUID = ban.playerId;
                    targetName = ban.playerName;
                    break;
                }
            }

            if (targetUUID == null) {
                for (BanData ban : plugin.getTempBannedPlayers().values()) {
                    if (ban.playerName.equalsIgnoreCase(args[0])) {
                        targetUUID = ban.playerId;
                        targetName = ban.playerName;
                        break;
                    }
                }
            }
        }

        if (targetUUID == null) {
            sender.sendMessage(plugin.getMessageUtils().formatMessage(
								   plugin.getLanguageManager().getMessage(sender, "player-not-found")
								   .replace("{player}", args[0])));
            return true;
        }

        BanData banData = null;
        boolean wasPermBan = false;

        if (plugin.getPermanentlyBanned().containsKey(targetUUID)) {
            banData = plugin.getPermanentlyBanned().remove(targetUUID);
            wasPermBan = true;
        } else if (plugin.getTempBannedPlayers().containsKey(targetUUID)) {
            banData = plugin.getTempBannedPlayers().remove(targetUUID);
            wasPermBan = false;
        } else {
            sender.sendMessage(plugin.getMessageUtils().formatMessage(
								   plugin.getLanguageManager().getMessage(sender, "not-banned")
								   .replace("{player}", targetName)));
            return true;
        }

        plugin.getDataManager().savePunishmentData();

        plugin.getPunishmentManager().broadcastUnbanToStaff(sender, targetName, targetUUID.toString(), 
															wasPermBan, banData.reason, banData.punishedBy, banData.banId);

        sender.sendMessage(plugin.getMessageUtils().formatMessage(
							   plugin.getLanguageManager().getMessage(sender, "unban-success")
							   .replace("{player}", targetName)));

        return true;
    }
}
