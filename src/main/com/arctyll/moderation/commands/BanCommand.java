package com.arctyll.moderation.commands;

import com.arctyll.moderation.Moderation;
import com.arctyll.moderation.data.models.BanData;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class BanCommand implements CommandExecutor {
    private final Moderation plugin;

    public BanCommand(Moderation plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("punishment.ban")) {
            sender.sendMessage(plugin.getMessageUtils().formatMessage(
								   plugin.getLanguageManager().getMessage(sender, "no-permission")));
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(plugin.getMessageUtils().formatMessage(
								   plugin.getLanguageManager().getMessage(sender, "ban-usage")));
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

        if (plugin.getPermanentlyBanned().containsKey(target.getUniqueId()) || 
            plugin.getTempBannedPlayers().containsKey(target.getUniqueId())) {
            sender.sendMessage(plugin.getMessageUtils().formatMessage(
								   plugin.getLanguageManager().getMessage(sender, "already-banned")
								   .replace("{player}", target.getName())));
            return true;
        }

        String reason = args.length > 1 ? 
            String.join(" ", Arrays.copyOfRange(args, 1, args.length)) : 
            plugin.getLanguageManager().getMessage(sender, "default-reason");

        String banId = plugin.getPunishmentManager().generateBanId();

        BanData banData = new BanData(target.getUniqueId(), target.getName(), reason, 
									  System.currentTimeMillis(), -1, sender.getName(), banId);

        plugin.getPermanentlyBanned().put(target.getUniqueId(), banData);
        plugin.getDataManager().savePunishmentData();

        plugin.getPunishmentManager().sendBanMessage(target, reason, banId, sender.getName(), false, 0);
        plugin.getPunishmentManager().broadcastBanToStaff(sender, target, reason, banId, false, 0);

        return true;
    }
}
