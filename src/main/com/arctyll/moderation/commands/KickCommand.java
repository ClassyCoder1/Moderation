package com.arctyll.moderation.commands;

import com.arctyll.moderation.Moderation;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class KickCommand implements CommandExecutor {
    private final Moderation plugin;

    public KickCommand(Moderation plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("punishment.kick")) {
            sender.sendMessage(plugin.getMessageUtils().formatMessage(
								   plugin.getLanguageManager().getMessage(sender, "no-permission")));
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(plugin.getMessageUtils().formatMessage(
								   plugin.getLanguageManager().getMessage(sender, "kick-usage")));
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
								   plugin.getLanguageManager().getMessage(sender, "cannot-kick-self")));
			return true;
		}

		if (!target.isOnline()) {
			sender.sendMessage(plugin.getMessageUtils().formatMessage(
								   plugin.getLanguageManager().getMessage(sender, "player-must-be-online")
								   .replace("{player}", args[0])));
			return true;
		}

        String reason = args.length > 1 ? 
            String.join(" ", Arrays.copyOfRange(args, 1, args.length)) : 
            plugin.getLanguageManager().getMessage(sender, "default-reason");

        plugin.getPunishmentManager().sendKickMessage(target, reason, sender.getName());
        plugin.getPunishmentManager().broadcastKickToStaff(sender, target, reason);

        return true;
    }
}
