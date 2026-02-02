package com.arctyll.moderation.listeners;

import com.arctyll.moderation.Moderation;
import com.arctyll.moderation.data.models.BanData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

import java.util.UUID;

public class LoginListener implements Listener {
    private final Moderation plugin;

    public LoginListener(Moderation plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerLogin(PlayerLoginEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();

        BanData permBan = plugin.getPermanentlyBanned().get(playerId);
        if (permBan != null) {
            String banMessage = plugin.getLanguageManager().getMessage(event.getPlayer(), "ban-login-message")
                .replace("{reason}", permBan.reason)
                .replace("{ban-id}", permBan.banId)
                .replace("{staff}", permBan.punishedBy)
                .replace("{appeal-site}", plugin.getLanguageManager().getMessage(event.getPlayer(), "appeal-site"));

            event.disallow(PlayerLoginEvent.Result.KICK_BANNED, 
						   plugin.getMessageUtils().formatMessage(banMessage));
            return;
        }

        BanData tempBan = plugin.getTempBannedPlayers().get(playerId);
        if (tempBan != null) {
            if (!tempBan.isExpired()) {
                long remaining = tempBan.getRemainingTime();
                String banMessage = plugin.getLanguageManager().getMessage(event.getPlayer(), "tempban-login-message")
                    .replace("{reason}", tempBan.reason)
                    .replace("{time-remaining}", plugin.getPunishmentManager().formatDuration(remaining))
                    .replace("{ban-id}", tempBan.banId)
                    .replace("{staff}", tempBan.punishedBy)
                    .replace("{expires}", plugin.getPunishmentManager().formatExpireDate(tempBan.expireTime))
                    .replace("{appeal-site}", plugin.getLanguageManager().getMessage(event.getPlayer(), "appeal-site"));

                event.disallow(PlayerLoginEvent.Result.KICK_BANNED, 
							   plugin.getMessageUtils().formatMessage(banMessage));
            } else {
                plugin.getTempBannedPlayers().remove(playerId);
                plugin.getDataManager().savePunishmentData();

                plugin.getLogger().info("Automatically removed expired temp ban for player: " + 
										event.getPlayer().getName() + " (Ban ID: " + tempBan.banId + ")");
            }
        }
    }
}
