package com.arctyll.moderation.managers;

import com.arctyll.moderation.Moderation;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.FilenameFilter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class LanguageManager {
    private final Moderation plugin;
    private final Map<String, FileConfiguration> languageFiles = new HashMap<>();
    private String defaultLanguage = "en";

    public LanguageManager(Moderation plugin) {
        this.plugin = plugin;
        loadConfig();
        loadLanguageFiles();
    }

    private void loadConfig() {
        plugin.saveDefaultConfig();
        FileConfiguration config = plugin.getConfig();
        defaultLanguage = config.getString("default-language", "en");
    }

    private void loadLanguageFiles() {
        File langFolder = new File(plugin.getDataFolder(), "languages");
        if (!langFolder.exists()) {
            langFolder.mkdirs();
        }

        createDefaultLanguageFiles(langFolder);

		File[] langFiles = langFolder.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return name.endsWith(".yml");
				}
			});
        if (langFiles != null) {
            for (File file : langFiles) {
                String lang = file.getName().replace(".yml", "");
                FileConfiguration langConfig = YamlConfiguration.loadConfiguration(file);

                loadDefaultsFromResources(langConfig, lang);

                languageFiles.put(lang, langConfig);
                plugin.getLogger().info("Loaded language file: " + lang + ".yml");
            }
        }

        if (languageFiles.isEmpty()) {
            plugin.getLogger().severe("No language files found! Creating emergency English file.");
            createEmergencyEnglishFile(langFolder);
        }

        plugin.getLogger().info("Total language files loaded: " + languageFiles.size() + 
								" [" + String.join(", ", languageFiles.keySet()) + "]");
    }

    private void createDefaultLanguageFiles(File langFolder) {
        String[] languages = {"en"};

        for (String lang : languages) {
            File langFile = new File(langFolder, lang + ".yml");
            if (!langFile.exists()) {
                try {
                    plugin.saveResource("languages/" + lang + ".yml", false);
                    plugin.getLogger().info("Created language file from resources: " + lang + ".yml");
                } catch (Exception e) {
                    plugin.getLogger().warning("Could not find resource for " + lang + ".yml, will be created empty.");
                }
            }
        }
    }

    private void loadDefaultsFromResources(FileConfiguration config, String lang) {
        try {
            InputStream defConfigStream = plugin.getResource("languages/" + lang + ".yml");
            if (defConfigStream != null) {
                FileConfiguration defConfig = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(defConfigStream, StandardCharsets.UTF_8));
                config.setDefaults(defConfig);
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Could not load defaults for " + lang + ".yml from resources", e);
        }
    }

    private void createEmergencyEnglishFile(File langFolder) {
        File enFile = new File(langFolder, "en.yml");
        try {
            enFile.createNewFile();
            FileConfiguration config = YamlConfiguration.loadConfiguration(enFile);

            config.set("default-reason", "No reason specified");

            config.save(enFile);

            languageFiles.put("en", config);
            plugin.getLogger().info("Created emergency English language file");

        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not create emergency language file!", e);
        }
    }

    public String getMessage(CommandSender sender, String key) {
        String lang = defaultLanguage;

        if (sender instanceof Player) {
            Player player = (Player) sender;
            lang = plugin.getPlayerLanguages().getOrDefault(player.getUniqueId(), defaultLanguage);
        }

        FileConfiguration langFile = languageFiles.get(lang);
        if (langFile != null) {
            String message = langFile.getString(key);
            if (message != null && !message.isEmpty()) {
                return message;
            }
        }

        if (!lang.equals(defaultLanguage)) {
            FileConfiguration defaultLangFile = languageFiles.get(defaultLanguage);
            if (defaultLangFile != null) {
                String message = defaultLangFile.getString(key);
                if (message != null && !message.isEmpty()) {
                    plugin.getLogger().warning("Message '" + key + "' not found in " + lang + ".yml, using default language");
                    return message;
                }
            }
        }

        plugin.getLogger().severe("Message '" + key + "' not found in any language file!");
        return "&cMessage '" + key + "' not found";
    }

    public Map<String, FileConfiguration> getLanguageFiles() {
        return languageFiles;
    }

    public String getDefaultLanguage() {
        return defaultLanguage;
    }

    public void reloadLanguageFiles() {
        languageFiles.clear();
        loadLanguageFiles();
        plugin.getLogger().info("Language files reloaded");
    }


    public boolean hasLanguage(String language) {
        return languageFiles.containsKey(language.toLowerCase());
    }

    public String[] getAvailableLanguages() {
        return languageFiles.keySet().toArray(new String[0]);
    }
}
