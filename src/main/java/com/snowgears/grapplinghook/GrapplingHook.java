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
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ArmorStand;

public class GrapplingHook extends JavaPlugin{
	
	private GrapplingListener grapplingListener;
	private AnvilListener anvilListener;
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
		try {
			plugin = this;
			
			// Инициализируем конфигурацию
			File configFile = new File(this.getDataFolder() + "/config.yml");
			if(!configFile.exists()) {
				this.saveDefaultConfig();
			}
			
			try {
				ConfigUpdater.update(plugin, "config.yml", configFile, new ArrayList<>());
			} catch (IOException e) {
				getLogger().severe("Error updating config: " + e.getMessage());
			}
			
			config = YamlConfiguration.loadConfiguration(configFile);
			
			// Загружаем настройки из конфига
			usePerms = config.getBoolean("usePermissions");
			teleportHooked = config.getBoolean("teleportToHook");
			useMetrics = config.getBoolean("useMetrics");
			consumeUseOnSlowfall = config.getBoolean("consumeUseOnSlowfall");
			commandAlias = config.getString("command");
			
			// Загружаем менеджер сообщений
			messageManager = new MessageManager(this);
			getLogger().info("MessageManager initialized");
			
			// Инициализируем слушателей
			grapplingListener = new GrapplingListener(this);
			anvilListener = new AnvilListener(this);
			
			// Регистрируем события
			getServer().getPluginManager().registerEvents(grapplingListener, this);
			getServer().getPluginManager().registerEvents(anvilListener, this);
			getLogger().info("Event listeners registered");
			
			// Загружаем рецепты
			File recipeConfigFile = new File(getDataFolder(), "recipes.yml");
			if (!recipeConfigFile.exists()) {
				recipeConfigFile.getParentFile().mkdirs();
				this.copy(getResource("recipes.yml"), recipeConfigFile);
			}
			recipeLoader = new RecipeLoader(plugin);
			getLogger().info("RecipeLoader initialized");
			
			// Регистрируем команды через plugin.yml
			commandHandler = new CommandHandler(this);
			getCommand(commandAlias).setExecutor(commandHandler);
			getCommand(commandAlias).setTabCompleter(commandHandler);
			getLogger().info("Command handler initialized");
			
			// Инициализируем метрики
			if(useMetrics){
				try {
					int pluginId = 9957;
					new Metrics(this, pluginId);
					getLogger().info("Metrics initialized");
				} catch(Exception e) {
					getLogger().warning("Failed to initialize metrics: " + e.getMessage());
					e.printStackTrace();
				}
			}
			
			getLogger().info("GrapplingHook successfully enabled!");
			
		} catch (Exception e) {
			getLogger().severe("Error enabling GrapplingHook: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public void onDisable(){
		try {
			// Отменяем все задачи плагина
			Bukkit.getScheduler().cancelTasks(this);
			
			// Очищаем все метаданные, установленные плагином
			for (World world : Bukkit.getWorlds()) {
				for (Entity entity : world.getEntities()) {
					if (entity.hasMetadata("stuckBlock") || entity.hasMetadata("grappling_hook_stand")) {
						// Удаляем ArmorStand, созданные плагином
						if (entity instanceof ArmorStand && entity.hasMetadata("grappling_hook_stand")) {
							entity.remove();
						}
						// Очищаем метаданные
						entity.removeMetadata("stuckBlock", this);
						entity.removeMetadata("grappling_hook_stand", this);
					}
				}
			}
			
			if (recipeLoader != null) {
				recipeLoader.unloadRecipes();
				getLogger().info("Recipes unloaded");
			}
			
			getLogger().info("GrapplingHook successfully disabled!");
		} catch (Exception e) {
			getLogger().severe("Error disabling GrapplingHook: " + e.getMessage());
		}
	}

	public void reload(){
		try {
			getLogger().info("Reloading GrapplingHook...");
			
			// Отменяем регистрацию текущих слушателей
			HandlerList.unregisterAll(grapplingListener);
			HandlerList.unregisterAll(anvilListener);
			
			// Перезагружаем MessageManager при перезагрузке плагина
			if (messageManager != null) {
				messageManager.reload();
				getLogger().info("MessageManager reloaded");
			}
			
			// Перезагружаем RecipeLoader при перезагрузке плагина
			if (recipeLoader != null) {
				recipeLoader.reload();
				getLogger().info("RecipeLoader reloaded");
			}

			// Перезагружаем все компоненты
			onDisable();
			onEnable();
			
			getLogger().info("GrapplingHook successfully reloaded!");
		} catch (Exception e) {
			getLogger().severe("Error reloading GrapplingHook: " + e.getMessage());
			e.printStackTrace();
		}
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
			if (in == null) {
				getLogger().warning("Could not find resource to copy: " + file.getName());
				return;
			}
			
			OutputStream out = new FileOutputStream(file);
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			out.close();
			in.close();
		} catch (Exception e) {
			getLogger().severe("Error copying resource: " + e.getMessage());
		}
	}
}