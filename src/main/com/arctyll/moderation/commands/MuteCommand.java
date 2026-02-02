package com.arctyll.moderation.commands;

import com.arctyll.moderation.Moderation;
import com.arctyll.moderation.data.models.MuteData;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class MuteCommand implements CommandExecutor {
    private final Moderation plugin;

    public MuteCommand(Moderation plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("punishment.mute")) {
            sender.sendMessage(plugin.getMessageUtils().formatMessage(
								   plugin.getLanguageManager().getMessage(sender, "no-permission")));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(plugin.getMessageUtils().formatMessage(
								   plugin.getLanguageManager().getMessage(sender, "mute-usage")));
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(plugin.getMessageUtils().formatMessage(
								   plugin.getLanguageManager().getMessage(sender, "player-not-found")
								   .replace("{player}", args[0])));
            return true;
        }

        if (plugin.getMutedPlayers().containsKey(target.getUniqueId())) {
            sender.sendMessage(plugin.getMessageUtils().formatMessage(
								   plugin.getLanguageManager().getMessage(sender, "already-muted")
								   .replace("{player}", target.getName())));
            return true;
        }
		
		if (sender instanceof Player && target.getUniqueId().equals(((Player) sender).getUniqueId())) {
			sender.sendMessage(plugin.getMessageUtils().formatMessage(
								   plugin.getLanguageManager().getMessage(sender, "cannot-mute-self")));
			return true;
		}
		
		if (!target.isOnline()) {
			sender.sendMessage(plugin.getMessageUtils().formatMessage(
								   plugin.getLanguageManager().getMessage(sender, "player-must-be-online")
								   .replace("{player}", args[0])));
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

        String muteId = plugin.getPunishmentManager().generateMuteId();
        long expireTime = System.currentTimeMillis() + duration;

        MuteData muteData = new MuteData(target.getUniqueId(), target.getName(), reason, 
										 System.currentTimeMillis(), expireTime, sender.getName(), muteId);

        plugin.getMutedPlayers().put(target.getUniqueId(), muteData);
        plugin.getDataManager().savePunishmentData();

        plugin.getPunishmentManager().sendMuteMessage(target, reason, duration, muteId, sender.getName());
        plugin.getPunishmentManager().broadcastMuteToStaff(sender, target, reason, duration, muteId, expireTime);

        return true;
    }
}
