package com.arctyll.moderation.data;

import com.arctyll.moderation.Moderation;
import com.arctyll.moderation.data.models.BanData;
import com.arctyll.moderation.data.models.MuteData;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class DataManager {
    private final Moderation plugin;
    private final Gson gson;
    private final File dataFolder;

    public DataManager(Moderation plugin) {
        this.plugin = plugin;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.dataFolder = plugin.getDataFolder();

        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
    }

    public void loadAll() {
        loadPlayerLanguages();
        loadPunishmentData();
    }

    public void saveAll() {
        savePlayerLanguages();
        savePunishmentData();
    }

    private void loadPlayerLanguages() {
        File playerLangFile = new File(dataFolder, "player-languages.json");
        if (!playerLangFile.exists()) {
            plugin.getLogger().info("Player languages file not found, creating new one.");
            return;
        }

        try {
			try (FileReader reader = new FileReader(playerLangFile)) {
				Type type = new TypeToken<Map<UUID, String>>(){}.getType();
				Map<UUID, String> loaded = gson.fromJson(reader, type);
				if (loaded != null) {
					plugin.getPlayerLanguages().clear();
					plugin.getPlayerLanguages().putAll(loaded);
					plugin.getLogger().info("Loaded " + loaded.size() + " player language preferences.");
				}
			}
		} catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load player languages: " + e.getMessage(), e);
        }
    }

    public void savePlayerLanguages() {
        File playerLangFile = new File(dataFolder, "player-languages.json");
        try {
			try (FileWriter writer = new FileWriter(playerLangFile)) {
				gson.toJson(plugin.getPlayerLanguages(), writer);
			}
		} catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save player languages: " + e.getMessage(), e);
        }
    }

    private void loadPunishmentData() {
        loadMutes();
        loadTempBans();
        loadPermanentBans();

        plugin.getLogger().info("Loaded punishment data: " + 
								plugin.getMutedPlayers().size() + " mutes, " + 
								plugin.getTempBannedPlayers().size() + " temp bans, " + 
								plugin.getPermanentlyBanned().size() + " permanent bans");
    }

    private void loadMutes() {
        File muteFile = new File(dataFolder, "mutes.json");
        if (!muteFile.exists()) return;

        try {
			try (FileReader reader = new FileReader(muteFile)) {
				Type type = new TypeToken<Map<UUID, MuteData>>(){}.getType();
				Map<UUID, MuteData> loaded = gson.fromJson(reader, type);
				if (loaded != null) {
					plugin.getMutedPlayers().clear();
					plugin.getMutedPlayers().putAll(loaded);
				}
			}
		} catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load mutes: " + e.getMessage(), e);
        }
    }

    private void loadTempBans() {
        File tempBanFile = new File(dataFolder, "tempbans.json");
        if (!tempBanFile.exists()) return;

        try {
			try (FileReader reader = new FileReader(tempBanFile)) {
				Type type = new TypeToken<Map<UUID, BanData>>(){}.getType();
				Map<UUID, BanData> loaded = gson.fromJson(reader, type);
				if (loaded != null) {
					plugin.getTempBannedPlayers().clear();
					plugin.getTempBannedPlayers().putAll(loaded);
				}
			}
		} catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load temp bans: " + e.getMessage(), e);
        }
    }

    private void loadPermanentBans() {
        File permBanFile = new File(dataFolder, "bans.json");
        if (!permBanFile.exists()) return;

        try {
			try (FileReader reader = new FileReader(permBanFile)) {
				Type type = new TypeToken<Map<UUID, BanData>>(){}.getType();
				Map<UUID, BanData> loaded = gson.fromJson(reader, type);
				if (loaded != null) {
					plugin.getPermanentlyBanned().clear();
					plugin.getPermanentlyBanned().putAll(loaded);
				}
			}
		} catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load permanent bans: " + e.getMessage(), e);
        }
    }

    public void savePunishmentData() {
        saveMutes();
        saveTempBans();
        savePermanentBans();
    }

    private void saveMutes() {
        File muteFile = new File(dataFolder, "mutes.json");
        try {
			try (FileWriter writer = new FileWriter(muteFile)) {
				gson.toJson(plugin.getMutedPlayers(), writer);
			}
		} catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save mutes: " + e.getMessage(), e);
        }
    }

    private void saveTempBans() {
        File tempBanFile = new File(dataFolder, "tempbans.json");
        try {
			try (FileWriter writer = new FileWriter(tempBanFile)) {
				gson.toJson(plugin.getTempBannedPlayers(), writer);
			}
		} catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save temp bans: " + e.getMessage(), e);
        }
    }

    private void savePermanentBans() {
        File permBanFile = new File(dataFolder, "bans.json");
        try {
			try (FileWriter writer = new FileWriter(permBanFile)) {
				gson.toJson(plugin.getPermanentlyBanned(), writer);
			}
		} catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save permanent bans: " + e.getMessage(), e);
        }
    }
}
