package com.snowgears.grapplinghook.utils;

import com.snowgears.grapplinghook.GrapplingHook;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class MessageManager {
    private final GrapplingHook plugin;
    private FileConfiguration messages;
    private String locale;
    private final Map<String, String> messageCache = new HashMap<>();

    public MessageManager(GrapplingHook plugin) {
        this.plugin = plugin;
        this.locale = plugin.getConfig().getString("language", "en");
        loadMessages();
    }

    public void reload() {
        this.locale = plugin.getConfig().getString("language", "en");
        messageCache.clear();
        loadMessages();
    }

    private void loadMessages() {
        File messagesFile = new File(plugin.getDataFolder(), "messages_" + locale + ".yml");
        
        // Если файл не существует, создаем его
        if (!messagesFile.exists()) {
            try {
                InputStream defaultStream = plugin.getResource("messages_" + locale + ".yml");
                if (defaultStream == null) {
                    // Если локализация не найдена, используем английский по умолчанию
                    plugin.getLogger().warning("Locale " + locale + " not found, using default (en)");
                    locale = "en";
                    messagesFile = new File(plugin.getDataFolder(), "messages_en.yml");
                    defaultStream = plugin.getResource("messages_en.yml");
                }
                
                if (defaultStream != null) {
                    plugin.saveResource("messages_" + locale + ".yml", false);
                } else {
                    plugin.getLogger().severe("Could not load default messages file!");
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }
        
        messages = YamlConfiguration.loadConfiguration(messagesFile);
        
        // Загружаем дефолтные сообщения из jar для сравнения и дополнения
        InputStream defaultStream = plugin.getResource("messages_" + locale + ".yml");
        if (defaultStream != null) {
            YamlConfiguration defaultMessages = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(defaultStream, StandardCharsets.UTF_8));
            messages.setDefaults(defaultMessages);
        }
    }

    public String getMessage(String path) {
        // Проверяем кэш сначала
        if (messageCache.containsKey(path)) {
            return messageCache.get(path);
        }
        
        String message = messages.getString(path);
        if (message == null) {
            plugin.getLogger().warning("Missing message for key: " + path);
            return "§cMissing message: " + path;
        }
        
        message = ChatColor.translateAlternateColorCodes('&', message);
        messageCache.put(path, message);
        return message;
    }

    public String getMessage(String path, Map<String, String> placeholders) {
        String message = getMessage(path);
        
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            message = message.replace("%" + entry.getKey() + "%", entry.getValue());
        }
        
        return message;
    }
    
    public String getCommandMessage(String path) {
        return getMessage("command." + path);
    }
    
    public String getCommandMessage(String path, Map<String, String> placeholders) {
        return getMessage("command." + path, placeholders);
    }
    
    public String getHookMessage(String path) {
        return getMessage("hook." + path);
    }
    
    public String getHookMessage(String path, Map<String, String> placeholders) {
        return getMessage("hook." + path, placeholders);
    }
} 