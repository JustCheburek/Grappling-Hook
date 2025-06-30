package com.snowgears.grapplinghook;

import com.snowgears.grapplinghook.utils.ConfigUpdater;
import com.snowgears.grapplinghook.utils.MessageManager;
import com.snowgears.grapplinghook.utils.RecipeLoader;
import org.bstats.bukkit.Metrics;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

public class GrapplingHook extends JavaPlugin{
	
	private GrapplingListener grapplingListener = new GrapplingListener(this);
	private AnvilListener anvilListener = new AnvilListener(this);
	private CommandHandler commandHandler;
	private static GrapplingHook plugin;
	protected FileConfiguration config;
	private MessageManager messageManager;

	private boolean usePerms = false;
	private boolean teleportHooked = false;
	private boolean useMetrics = false;
	private boolean consumeUseOnSlowfall = false;
	private String commandAlias;
	private RecipeLoader recipeLoader;


	public void onEnable(){
		plugin = this;
		getServer().getPluginManager().registerEvents(grapplingListener, this);
		getServer().getPluginManager().registerEvents(anvilListener, this);
		
		File configFile = new File(this.getDataFolder() + "/config.yml");
		if(!configFile.exists())
		{
		  this.saveDefaultConfig();
		}
        try {
            ConfigUpdater.update(plugin, "config.yml", configFile, new ArrayList<>());
        } catch (IOException e) {
            e.printStackTrace();
        }
		config = YamlConfiguration.loadConfiguration(configFile);

		// Сохраняем все необходимые ресурсы
		saveResources();

		// Проверяем наличие файла рецептов для текущей локали
		String locale = config.getString("language", "en");
		File recipeConfigFile = new File(getDataFolder(), "recipes_" + locale + ".yml");
		if (!recipeConfigFile.exists()) {
			recipeConfigFile.getParentFile().mkdirs();
			// Пробуем загрузить локализованный файл из ресурсов
			if (getResource("recipes_" + locale + ".yml") != null) {
				this.saveResource("recipes_" + locale + ".yml", false);
			} else {
				// Если локализованного файла нет, используем английский
				if (getResource("recipes_en.yml") != null) {
					this.copy(getResource("recipes_en.yml"), recipeConfigFile);
				}
			}
		}
		recipeLoader = new RecipeLoader(plugin);
		
		usePerms = config.getBoolean("usePermissions");
		teleportHooked = config.getBoolean("teleportToHook");
		useMetrics = config.getBoolean("useMetrics");
		consumeUseOnSlowfall = config.getBoolean("consumeUseOnSlowfall");
		commandAlias = config.getString("command");
		
		// Инициализируем MessageManager
		messageManager = new MessageManager(this);

		if(useMetrics){
			// You can find the plugin ids of your plugins on the page https://bstats.org/what-is-my-plugin-id
			int pluginId = 9957;

			try {
				Metrics metrics = new Metrics(this, pluginId);
			} catch(Exception e) {}

			// Optional: Add custom charts
			//metrics.addCustomChart(new Metrics.SimplePie("chart_id", () -> "My value"));
		}

		commandHandler = new CommandHandler(this, "grapplinghook.operator", commandAlias, "Base command for the GrapplingHook plugin", "/gh", new ArrayList(Arrays.asList(commandAlias)));
	}

	/**
	 * Сохраняет все необходимые ресурсы плагина
	 */
	private void saveResources() {
		// Получаем текущую локаль из конфига
		String locale = config.getString("language", "en");
		
		// Сохраняем основные файлы ресурсов
		saveResourceIfNotExists("recipes_en.yml");
		saveResourceIfNotExists("messages_en.yml");
		
		// Сохраняем локализованные файлы, если они существуют
		if (!locale.equals("en")) {
			saveResourceIfNotExists("messages_" + locale + ".yml");
			saveResourceIfNotExists("recipes_" + locale + ".yml");
		}
	}
	
	/**
	 * Сохраняет ресурс, если он еще не существует в директории плагина
	 * @param resourceName имя ресурса
	 */
	private void saveResourceIfNotExists(String resourceName) {
		File resourceFile = new File(getDataFolder(), resourceName);
		if (!resourceFile.exists()) {
			try {
				saveResource(resourceName, false);
			} catch (Exception e) {
				getLogger().warning("Could not save resource: " + resourceName);
			}
		}
	}

	public void onDisable(){
		recipeLoader.unloadRecipes();
	}

	public void reload(){
		HandlerList.unregisterAll(grapplingListener);
		HandlerList.unregisterAll(anvilListener);
		
		// Перезагружаем MessageManager при перезагрузке плагина
		if (messageManager != null) {
			messageManager.reload();
		}
		
		// Перезагружаем RecipeLoader при перезагрузке плагина
		if (recipeLoader != null) {
			recipeLoader.reload();
		}

		onDisable();
		onEnable();
	}
	
	public MessageManager getMessageManager() {
		return messageManager;
	}

	public RecipeLoader getRecipeLoader(){
		return recipeLoader;
	}

    private static boolean isInteger(String s) {
	    try { 
	        Integer.parseInt(s); 
	    } catch(NumberFormatException e) { 
	        return false; 
	    }
	    return true;
	}

	public static GrapplingHook getPlugin(){
		return plugin;
	}

	public GrapplingListener getGrapplingListener(){
		return grapplingListener;
	}

	public boolean isConsumeUseOnSlowfall(){
		return consumeUseOnSlowfall;
	}

	public boolean usePerms(){
		return usePerms;
	}

	public boolean getTeleportHooked(){
		return teleportHooked;
	}

	private void copy(InputStream in, File file) {
		try {
			OutputStream out = new FileOutputStream(file);
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			out.close();
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}