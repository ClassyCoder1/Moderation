package com.arctyll.moderation.commands;

import com.arctyll.moderation.Moderation;
import com.arctyll.moderation.data.models.BanData;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class TempBanCommand implements CommandExecutor {
    private final Moderation plugin;

    public TempBanCommand(Moderation plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("punishment.tempban")) {
            sender.sendMessage(plugin.getMessageUtils().formatMessage(
								   plugin.getLanguageManager().getMessage(sender, "no-permission")));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(plugin.getMessageUtils().formatMessage(
								   plugin.getLanguageManager().getMessage(sender, "tempban-usage")));
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(plugin.getMessageUtils().formatMessage(
								   plugin.getLanguageManager().getMessage(sender, "player-not-found")
								   .replace("{player}", args[0])));
            return true;
        }
		
		if (sender instanceof Player && target.getUniqueId().equals(((Player) sender).getUniqueId())) {
			sender.sendMessage(plugin.getMessageUtils().formatMessage(
								   plugin.getLanguageManager().getMessage(sender, "cannot-ban-self")));
			return true;
		}

		if (!target.isOnline()) {
			sender.sendMessage(plugin.getMessageUtils().formatMessage(
								   plugin.getLanguageManager().getMessage(sender, "player-must-be-online")
								   .replace("{player}", args[0])));
			return true;
		}

        if (plugin.getTempBannedPlayers().containsKey(target.getUniqueId()) || 
            plugin.getPermanentlyBanned().containsKey(target.getUniqueId())) {
            sender.sendMessage(plugin.getMessageUtils().formatMessage(
								   plugin.getLanguageManager().getMessage(sender, "already-banned")
								   .replace("{player}", target.getName())));
            return true;
        }

        long duration = plugin.getPunishmentManager().parseDuration(args[1]);
        if (duration <= 0) {
            sender.sendMessage(plugin.getMessageUtils().formatMessage(
								   plugin.getLanguageManager().getMessage(sender, "invalid-duration")));
            return true;
        }

        String reason = args.length > 2 ? 
            String.join(" ", Arrays.copyOfRange(args, 2, args.length)) : 
            plugin.getLanguageManager().getMessage(sender, "default-reason");

        String banId = plugin.getPunishmentManager().generateBanId();
        long expireTime = System.currentTimeMillis() + duration;

        BanData banData = new BanData(target.getUniqueId(), target.getName(), reason, 
									  System.currentTimeMillis(), expireTime, sender.getName(), banId);

        plugin.getTempBannedPlayers().put(target.getUniqueId(), banData);
        plugin.getDataManager().savePunishmentData();

        plugin.getPunishmentManager().sendBanMessage(target, reason, banId, sender.getName(), true, duration);
        plugin.getPunishmentManager().broadcastBanToStaff(sender, target, reason, banId, true, duration);

        return true;
    }
}
