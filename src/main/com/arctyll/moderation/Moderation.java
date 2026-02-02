package com.arctyll.moderation;

import com.arctyll.moderation.commands.*;
import com.arctyll.moderation.data.DataManager;
import com.arctyll.moderation.data.models.BanData;
import com.arctyll.moderation.data.models.MuteData;
import com.arctyll.moderation.listeners.ChatListener;
import com.arctyll.moderation.listeners.LoginListener;
import com.arctyll.moderation.managers.LanguageManager;
import com.arctyll.moderation.managers.PunishmentManager;
import com.arctyll.moderation.tasks.CleanupTask;
import com.arctyll.moderation.utils.MessageUtils;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Moderation extends JavaPlugin {

    private static Moderation instance;
    private DataManager dataManager;
    private LanguageManager languageManager;
    private PunishmentManager punishmentManager;
    private MessageUtils messageUtils;
    private CleanupTask cleanupTask;

    private Map<UUID, MuteData> mutedPlayers = new ConcurrentHashMap<>();
    private Map<UUID, BanData> tempBannedPlayers = new ConcurrentHashMap<>();
    private Map<UUID, BanData> permanentlyBanned = new ConcurrentHashMap<>();
    private Map<UUID, String> playerLanguages = new ConcurrentHashMap<>();

    @Override
    public void onEnable() {
        instance = this;

        dataManager = new DataManager(this);
        languageManager = new LanguageManager(this);
        punishmentManager = new PunishmentManager(this);
        messageUtils = new MessageUtils(languageManager);

        dataManager.loadAll();

        registerCommands();

        registerListeners();

        cleanupTask = new CleanupTask(this);
        cleanupTask.start();

        getLogger().info("Moderation Plugin has been enabled!");
    }

    @Override
    public void onDisable() {
        if (cleanupTask != null) {
            cleanupTask.stop();
        }

        dataManager.saveAll();
        getLogger().info("Moderation Plugin has been disabled!");
    }

    private void registerCommands() {
        getCommand("mute").setExecutor(new MuteCommand(this));
        getCommand("unmute").setExecutor(new UnmuteCommand(this));
        getCommand("ban").setExecutor(new BanCommand(this));
        getCommand("unban").setExecutor(new UnbanCommand(this));
        getCommand("tempban").setExecutor(new TempBanCommand(this));
        getCommand("kick").setExecutor(new KickCommand(this));
        getCommand("language").setExecutor(new LanguageCommand(this));
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new ChatListener(this), this);
        getServer().getPluginManager().registerEvents(new LoginListener(this), this);
    }

    public static Moderation getInstance() { return instance; }
    public DataManager getDataManager() { return dataManager; }
    public LanguageManager getLanguageManager() { return languageManager; }
    public PunishmentManager getPunishmentManager() { return punishmentManager; }
    public MessageUtils getMessageUtils() { return messageUtils; }

    public Map<UUID, MuteData> getMutedPlayers() { return mutedPlayers; }
    public Map<UUID, BanData> getTempBannedPlayers() { return tempBannedPlayers; }
    public Map<UUID, BanData> getPermanentlyBanned() { return permanentlyBanned; }
    public Map<UUID, String> getPlayerLanguages() { return playerLanguages; }
}
