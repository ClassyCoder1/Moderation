package com.arctyll.moderation.managers;

import com.arctyll.moderation.Moderation;
import com.arctyll.moderation.utils.TimeUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.arctyll.moderation.data.models.*;

public class PunishmentManager {
    private final Moderation plugin;

    public PunishmentManager(Moderation plugin) {
        this.plugin = plugin;
    }

    public long parseDuration(String duration) {
        return TimeUtils.parseDuration(duration);
    }

    public String formatDuration(long millis) {
        return TimeUtils.formatDuration(millis);
    }

    public String formatExpireDate(long timestamp) {
        return TimeUtils.formatExpireDate(timestamp);
    }

    public String generateMuteId() {
        return "M" + String.valueOf(System.currentTimeMillis()).substring(7);
    }

    public String generateBanId() {
        return "B" + String.valueOf(System.currentTimeMillis()).substring(7);
    }

    public void sendMuteMessage(Player target, String reason, long duration, String muteId, String staffName) {
        String muteMessage = plugin.getLanguageManager().getMessage(target, "mute-message")
            .replace("{reason}", reason)
            .replace("{duration}", formatDuration(duration))
            .replace("{mute-id}", muteId)
            .replace("{staff}", staffName)
            .replace("{appeal-site}", plugin.getLanguageManager().getMessage(target, "appeal-site"));

        target.sendMessage(plugin.getMessageUtils().formatMessage(muteMessage));
    }

    public void broadcastMuteToStaff(CommandSender sender, Player target, String reason, long duration, String muteId, long expireTime) {
        String staffMsg = plugin.getLanguageManager().getMessage(sender, "mute-broadcast")
            .replace("{player}", target.getName())
            .replace("{uuid}", target.getUniqueId().toString())
            .replace("{staff}", sender.getName())
            .replace("{duration}", formatDuration(duration))
            .replace("{reason}", reason)
            .replace("{mute-id}", muteId)
            .replace("{expires}", formatExpireDate(expireTime));
        broadcastToStaff(staffMsg);
    }

    public void broadcastUnmuteToStaff(CommandSender sender, Player target, String originalReason, String originalStaff, String muteId) {
        String staffMsg = plugin.getLanguageManager().getMessage(sender, "unmute-broadcast")
            .replace("{player}", target.getName())
            .replace("{uuid}", target.getUniqueId().toString())
            .replace("{staff}", sender.getName())
            .replace("{original-reason}", originalReason)
            .replace("{original-staff}", originalStaff)
            .replace("{mute-id}", muteId);
        broadcastToStaff(staffMsg);
    }

    public void sendBanMessage(Player target, String reason, String banId, String staffName, boolean isTemp, long duration) {
        String messageKey = isTemp ? "tempban-message" : "ban-message";
        String banMessage = plugin.getLanguageManager().getMessage(target, messageKey)
            .replace("{reason}", reason)
            .replace("{ban-id}", banId)
            .replace("{staff}", staffName)
            .replace("{appeal-site}", plugin.getLanguageManager().getMessage(target, "appeal-site"));

        if (isTemp) {
            long expireTime = System.currentTimeMillis() + duration;
            String formattedDuration = formatDuration(duration);
            String formattedExpireDate = formatExpireDate(expireTime);

            banMessage = banMessage
                .replace("{duration}", formattedDuration)
                .replace("{expires}", formattedExpireDate);

            plugin.getLogger().info("DEBUG: Formatted duration: " + formattedDuration);
            plugin.getLogger().info("DEBUG: Message before formatting: " + banMessage);
        }

        target.kickPlayer(plugin.getMessageUtils().formatMessage(banMessage));
    }

    public void broadcastBanToStaff(CommandSender sender, Player target, String reason, String banId, boolean isTemp, long duration) {
        String messageKey = isTemp ? "tempban-broadcast" : "ban-broadcast";
        String staffMsg = plugin.getLanguageManager().getMessage(sender, messageKey)
            .replace("{player}", target.getName())
            .replace("{uuid}", target.getUniqueId().toString())
            .replace("{staff}", sender.getName())
            .replace("{reason}", reason)
            .replace("{ban-id}", banId)
            .replace("{ip}", target.getAddress().getAddress().getHostAddress());

        if (isTemp) {
            long expireTime = System.currentTimeMillis() + duration;
            String formattedDuration = formatDuration(duration);
            String formattedExpireDate = formatExpireDate(expireTime);

            staffMsg = staffMsg
                .replace("{duration}", formattedDuration)
                .replace("{expires}", formattedExpireDate);
        }

        broadcastToStaff(staffMsg);
    }

    public void broadcastUnbanToStaff(CommandSender sender, String targetName, String targetUUID, boolean wasPermBan, String originalReason, String originalStaff, String banId) {
        String staffMsg = plugin.getLanguageManager().getMessage(sender, "unban-broadcast")
            .replace("{player}", targetName)
            .replace("{uuid}", targetUUID)
            .replace("{staff}", sender.getName())
            .replace("{ban-type}", wasPermBan ? "Permanent" : "Temporary")
            .replace("{original-reason}", originalReason)
            .replace("{original-staff}", originalStaff)
            .replace("{ban-id}", banId);
        broadcastToStaff(staffMsg);
    }

    public void sendKickMessage(Player target, String reason, String staffName) {
        String kickMessage = plugin.getLanguageManager().getMessage(target, "kick-message")
            .replace("{reason}", reason)
            .replace("{staff}", staffName);
        target.kickPlayer(plugin.getMessageUtils().formatMessage(kickMessage));
    }

    public void broadcastKickToStaff(CommandSender sender, Player target, String reason) {
        String staffMsg = plugin.getLanguageManager().getMessage(sender, "kick-broadcast")
            .replace("{player}", target.getName())
            .replace("{uuid}", target.getUniqueId().toString())
            .replace("{staff}", sender.getName())
            .replace("{reason}", reason)
            .replace("{ip}", target.getAddress().getAddress().getHostAddress());
        broadcastToStaff(staffMsg);
    }

    private void broadcastToStaff(String message) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("punishment.notify")) {
                player.sendMessage(plugin.getMessageUtils().formatMessage(message));
            }
        }
        plugin.getLogger().info(plugin.getMessageUtils().stripColors(message));
    }

    public boolean isValidDuration(String duration) {
        return parseDuration(duration) > 0;
    }

    public boolean isPlayerBanned(String playerName) {
        for (BanData ban : plugin.getPermanentlyBanned().values()) {
            if (ban.playerName.equalsIgnoreCase(playerName)) {
                return true;
            }
        }
        for (BanData ban : plugin.getTempBannedPlayers().values()) {
            if (ban.playerName.equalsIgnoreCase(playerName)) {
                return true;
            }
        }
        return false;
    }

    public boolean isPlayerMuted(String playerName) {
        for (MuteData mute : plugin.getMutedPlayers().values()) {
            if (mute.playerName.equalsIgnoreCase(playerName)) {
                return true;
            }
        }
        return false;
    }

    public String getPunishmentStats() {
        int activeMutes = plugin.getMutedPlayers().size();
        int activeTempBans = plugin.getTempBannedPlayers().size();
        int activePermBans = plugin.getPermanentlyBanned().size();

        return String.format("Active Punishments - Mutes: %d, Temp Bans: %d, Perm Bans: %d", 
							 activeMutes, activeTempBans, activePermBans);
    }
}
