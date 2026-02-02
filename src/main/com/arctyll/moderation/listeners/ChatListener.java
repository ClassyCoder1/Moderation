package com.arctyll.moderation.listeners;

import com.arctyll.moderation.Moderation;
import com.arctyll.moderation.data.models.MuteData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {
    private final Moderation plugin;

    public ChatListener(Moderation plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        MuteData muteData = plugin.getMutedPlayers().get(player.getUniqueId());

        if (muteData != null) {
            if (!muteData.isExpired()) {
                event.setCancelled(true);

                long remaining = muteData.getRemainingTime();
                String muteReminder = plugin.getLanguageManager().getMessage(player, "mute-reminder")
                    .replace("{time-remaining}", plugin.getPunishmentManager().formatDuration(remaining))
                    .replace("{reason}", muteData.reason)
                    .replace("{mute-id}", muteData.muteId)
                    .replace("{appeal-site}", plugin.getLanguageManager().getMessage(player, "appeal-site"));

                player.sendMessage(plugin.getMessageUtils().formatMessage(muteReminder));
            } else {
                plugin.getMutedPlayers().remove(player.getUniqueId());
                plugin.getDataManager().savePunishmentData();

                plugin.getLogger().info("Automatically removed expired mute for player: " + 
										player.getName() + " (Mute ID: " + muteData.muteId + ")");
            }
        }
    }
}
