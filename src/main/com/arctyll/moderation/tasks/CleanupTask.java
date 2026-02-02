package com.arctyll.moderation.tasks;

import com.arctyll.moderation.Moderation;
import com.arctyll.moderation.data.models.BanData;
import com.arctyll.moderation.data.models.MuteData;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class CleanupTask {
    private final Moderation plugin;
    private BukkitTask task;

    public CleanupTask(Moderation plugin) {
        this.plugin = plugin;
    }

    public void start() {
        task = Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
			@Override
			public void run() {
				cleanup();
			}
		}, 20L, 20L * 60L);
        plugin.getLogger().info("Cleanup task started - running every minute");
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
            plugin.getLogger().info("Cleanup task stopped");
        }
    }

    private void cleanup() {
        long now = System.currentTimeMillis();
        boolean needsSave = false;
        int removedMutes = 0;
        int removedTempBans = 0;

        Iterator<Map.Entry<UUID, MuteData>> muteIterator = plugin.getMutedPlayers().entrySet().iterator();
        while (muteIterator.hasNext()) {
            Map.Entry<UUID, MuteData> entry = muteIterator.next();
            MuteData muteData = entry.getValue();

            if (muteData.isExpired()) {
                muteIterator.remove();
                removedMutes++;
                needsSave = true;

                plugin.getLogger().info("Automatically removed expired mute for player: " + muteData.playerName + 
										" (Mute ID: " + muteData.muteId + ")");
            }
        }

        Iterator<Map.Entry<UUID, BanData>> banIterator = plugin.getTempBannedPlayers().entrySet().iterator();
        while (banIterator.hasNext()) {
            Map.Entry<UUID, BanData> entry = banIterator.next();
            BanData banData = entry.getValue();

            if (banData.isExpired()) {
                banIterator.remove();
                removedTempBans++;
                needsSave = true;

                plugin.getLogger().info("Automatically removed expired temp ban for player: " + banData.playerName + 
										" (Ban ID: " + banData.banId + ")");
            }
        }

        if (needsSave) {
            plugin.getDataManager().savePunishmentData();

            if (removedMutes > 0 || removedTempBans > 0) {
                plugin.getLogger().info("Cleanup completed: removed " + removedMutes + " expired mutes and " + 
										removedTempBans + " expired temp bans");
            }
        }
    }

    public void runCleanupNow() {
        cleanup();
    }
}
