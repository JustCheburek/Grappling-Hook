package com.snowgears.grapplinghook.utils;

import com.snowgears.grapplinghook.GrapplingHook;
import org.bukkit.*;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Level;

public class RecipeLoader {

    private GrapplingHook plugin;
    private File recipesFile;
    private YamlConfiguration localizedRecipes;
    private Map<String, ItemStack> hookItems = new HashMap<>();

    public RecipeLoader(GrapplingHook plugin){
        this.plugin = plugin;
        // Используем локализованное имя файла
        String locale = plugin.getConfig().getString("language", "en");
        this.recipesFile = new File(plugin.getDataFolder(), "recipes_" + locale + ".yml");
        
        // Если файл не существует, пытаемся использовать английскую версию
        if (!recipesFile.exists()) {
            this.recipesFile = new File(plugin.getDataFolder(), "recipes_en.yml");
            plugin.getLogger().log(Level.INFO, "Recipes file for locale " + locale + " not found, using English version");
        }
        
        // Загружаем локализованные рецепты
        loadLocalizedRecipes();
        
        loadRecipes();
    }
    
    private void loadLocalizedRecipes() {
        String locale = plugin.getConfig().getString("language", "en");
        File localizedRecipesFile = new File(plugin.getDataFolder(), "recipes_" + locale + ".yml");
        
        if (localizedRecipesFile.exists()) {
            localizedRecipes = YamlConfiguration.loadConfiguration(localizedRecipesFile);
            plugin.getLogger().log(Level.INFO, "Loaded localized recipes for " + locale);
        } else {
            // Пробуем загрузить из ресурсов
            InputStream defaultStream = plugin.getResource("recipes_" + locale + ".yml");
            if (defaultStream != null) {
                localizedRecipes = YamlConfiguration.loadConfiguration(
                        new InputStreamReader(defaultStream, StandardCharsets.UTF_8));
                plugin.getLogger().log(Level.INFO, "Loaded localized recipes from resources for " + locale);
                
                // Сохраняем файл в директорию плагина для будущего использования
                try {
                    plugin.saveResource("recipes_" + locale + ".yml", false);
                } catch (Exception e) {
                    plugin.getLogger().log(Level.WARNING, "Could not save localized recipes file: " + e.getMessage());
                }
            } else {
                // Если локализованного файла нет, используем стандартный
                localizedRecipes = null;
                plugin.getLogger().log(Level.INFO, "No localized recipes found for " + locale + ", using default");
            }
        }
    }
    
    public void reload() {
        unloadRecipes();
        hookItems.clear();
        
        // Обновляем путь к файлу рецептов
        String locale = plugin.getConfig().getString("language", "en");
        this.recipesFile = new File(plugin.getDataFolder(), "recipes_" + locale + ".yml");
        
        // Если файл не существует, пытаемся использовать английскую версию
        if (!recipesFile.exists()) {
            this.recipesFile = new File(plugin.getDataFolder(), "recipes_en.yml");
            plugin.getLogger().log(Level.INFO, "Recipes file for locale " + locale + " not found, using English version");
        }
        
        loadLocalizedRecipes();
        loadRecipes();
    }

    private void loadRecipes() {
        int loadedCount = 0;
        YamlConfiguration config = YamlConfiguration.loadConfiguration(recipesFile);
        if (config.getConfigurationSection("recipes") == null) {
            plugin.getLogger().log(Level.SEVERE, "Recipes file could not be loaded.");
            return;
        }

        Set<String> allRecipeNumbers = config.getConfigurationSection("recipes").getKeys(false);
        
        // Первый проход: создаем все предметы крюков
        for (String recipeNumber : allRecipeNumbers) {
            boolean enabled = config.getBoolean("recipes." + recipeNumber + ".enabled");
            if (enabled) {
                String id = config.getString("recipes." + recipeNumber + ".id");
                
                // Получаем локализованное имя и описание, если доступно
                String name;
                List<String> lore;
                
                if (localizedRecipes != null && localizedRecipes.contains("recipes." + recipeNumber + ".name")) {
                    name = localizedRecipes.getString("recipes." + recipeNumber + ".name");
                    try {
                        lore = localizedRecipes.getStringList("recipes." + recipeNumber + ".lore");
                    } catch (NullPointerException e) {
                        lore = config.getStringList("recipes." + recipeNumber + ".lore");
                    }
                } else {
                    name = config.getString("recipes." + recipeNumber + ".name");
                    try {
                        lore = config.getStringList("recipes." + recipeNumber + ".lore");
                    } catch (NullPointerException e) {
                        lore = new ArrayList<>();
                    }
                }
                
                int uses = config.getInt("recipes." + recipeNumber + ".uses");
                double velocityThrow = config.getDouble("recipes." + recipeNumber + ".velocityThrow");
                double velocityPull = config.getDouble("recipes." + recipeNumber + ".velocityPull");
                int timeBetweenGrapples = config.getInt("recipes." + recipeNumber + ".timeBetweenGrapples");
                boolean fallDamage = config.getBoolean("recipes." + recipeNumber + ".fallDamage");
                boolean slowFall = config.getBoolean("recipes." + recipeNumber + ".slowFall");
                boolean lineBreak = config.getBoolean("recipes." + recipeNumber + ".lineBreak");
                boolean stickyHook = config.getBoolean("recipes." + recipeNumber + ".stickyHook");
                int customModelData = config.getInt("recipes." + recipeNumber + ".customModelData", 0);

                HookSettings hookSettings = new HookSettings(id, uses, velocityThrow, velocityPull, timeBetweenGrapples, fallDamage, slowFall, lineBreak, stickyHook, customModelData);

                try {
                    boolean isBlackList = false;
                    String blackListString = config.getString("recipes." + recipeNumber + ".entity.listType");
                    List<String> entityTypeStrings = config.getStringList("recipes." + recipeNumber + ".entity.list");

                    List<EntityType> entityTypeList = new ArrayList<>();
                    for(String entityTypeString : entityTypeStrings){
                        try {
                            if(!entityTypeString.isEmpty())
                                entityTypeList.add(EntityType.valueOf(entityTypeString));
                        } catch (IllegalArgumentException e){
                            plugin.getLogger().log(Level.WARNING, "unrecognized entity type in recipe "+recipeNumber+": "+entityTypeString);
                        }
                    }

                    if(blackListString.equalsIgnoreCase("blacklist"))
                        isBlackList = true;
                    hookSettings.setEntityList(isBlackList, entityTypeList);
                } catch (NullPointerException e){
                    hookSettings.setEntityList(true, new ArrayList<>());
                }

                try {
                    boolean isBlackList = false;
                    String blackListString = config.getString("recipes." + recipeNumber + ".entity.listType");
                    List<String> materialStrings = config.getStringList("recipes." + recipeNumber + ".material.list");

                    List<Material> materialList = new ArrayList<>();
                    for(String materialString : materialStrings){
                        try {
                            if(!materialString.isEmpty())
                                materialList.add(Material.valueOf(materialString));
                        } catch (IllegalArgumentException e){
                            plugin.getLogger().log(Level.WARNING, "unrecognized material in recipe "+recipeNumber+": "+materialString);
                        }
                    }

                    if(blackListString.equalsIgnoreCase("blacklist"))
                        isBlackList = true;
                    hookSettings.setMaterialList(isBlackList, materialList);
                } catch (NullPointerException e){
                    hookSettings.setMaterialList(true, new ArrayList<>());
                }

                ItemStack hookItem = new ItemStack(Material.FISHING_ROD);
                ItemMeta hookItemMeta = hookItem.getItemMeta();
                hookItemMeta.setDisplayName(formatString(name, uses));
                if(lore != null && !lore.isEmpty()) {
                    List<String> loreList = new ArrayList<>();
                    for(String loreString : lore) {
                        loreList.add(formatString(loreString, uses));
                    }
                    hookItemMeta.setLore(loreList);
                }

                if(customModelData != 0)
                    hookItemMeta.setCustomModelData(Integer.valueOf(customModelData));

                PersistentDataContainer persistentData = hookItemMeta.getPersistentDataContainer();
                persistentData.set(new NamespacedKey(plugin, "uses"), PersistentDataType.INTEGER, uses);
                persistentData.set(new NamespacedKey(plugin, "id"), PersistentDataType.STRING, id);

                hookItem.setItemMeta(hookItemMeta);

                hookSettings.setHookItem(hookItem);
                //register the hook setting in map
                plugin.getGrapplingListener().addHookSettings(id, hookSettings);
                
                // Сохраняем предмет для использования в рецептах
                hookItems.put(id, hookItem.clone());
            }
        }

        // Второй проход: создаем рецепты
        for (String recipeNumber : allRecipeNumbers) {
            boolean enabled = config.getBoolean("recipes." + recipeNumber + ".enabled");
            if (enabled) {
                String id = config.getString("recipes." + recipeNumber + ".id");
                ItemStack hookItem = hookItems.get(id);
                
                if (hookItem == null) {
                    plugin.getLogger().log(Level.WARNING, "Could not find hook item for ID: " + id);
                    continue;
                }

                NamespacedKey key = new NamespacedKey(plugin, "hook_item_" + id);
                ShapedRecipe recipe = new ShapedRecipe(key, hookItem);

                HashMap<String, Object> materialMap = new HashMap<>();
                Set<String> materialKeys = config.getConfigurationSection("recipes." + recipeNumber + ".recipe.materials").getKeys(false);
                for (String materialKey : materialKeys) {
                    try {
                        String materialValue = config.getString("recipes." + recipeNumber + ".recipe.materials." + materialKey);
                        // Проверяем, является ли материал пользовательским предметом (крюком)
                        if (hookItems.containsKey(materialValue)) {
                            materialMap.put(materialKey, hookItems.get(materialValue));
                        } else {
                            Material material = Material.valueOf(materialValue);
                            materialMap.put(materialKey, material);
                        }
                    } catch (IllegalArgumentException iae) {
                        plugin.getLogger().log(Level.WARNING, "ERROR READING MATERIAL VALUE IN RECIPES.YML FILE");
                    } catch (NullPointerException npe) {
                        plugin.getLogger().log(Level.WARNING, "NULL ERROR READING MATERIAL VALUE IN RECIPES.YML FILE");
                    }
                }

                //System.out.println("recipe #"+recipeNumber);
                String[] threeLettersArray = new String[3];
                List<String> recipeLines = config.getStringList("recipes." + recipeNumber + ".recipe.shape");
                for (int i = 0; i < recipeLines.size(); i++) {
                    String threeLetters = "";
                    String[] splitRecipeLine = recipeLines.get(i).split("\\[");
                    for (String splitRecipeLinePart : splitRecipeLine) {
                        if (splitRecipeLinePart.contains("]")) {
                            String letter = splitRecipeLinePart.replaceAll("]", "");
                            if (letter.isEmpty())
                                letter = "_";
                            threeLetters += letter;
                        }
                    }
                    threeLettersArray[i] = threeLetters;
                }

                if(threeLettersArray[0].startsWith("_") && threeLettersArray[1].startsWith("_") && threeLettersArray[2].startsWith("_")){
                    threeLettersArray[0] = threeLettersArray[0].substring(1);
                    threeLettersArray[1] = threeLettersArray[1].substring(1);
                    threeLettersArray[2] = threeLettersArray[2].substring(1);
                }
                if(threeLettersArray[0].endsWith("_") && threeLettersArray[1].endsWith("_") && threeLettersArray[2].endsWith("_")){
                    threeLettersArray[0] = threeLettersArray[0].substring(0, threeLettersArray[0].length()-1);
                    threeLettersArray[1] = threeLettersArray[0].substring(0, threeLettersArray[1].length()-1);
                    threeLettersArray[2] = threeLettersArray[0].substring(0, threeLettersArray[2].length()-1);
                }

                if(onlyContainsUnderscores(threeLettersArray[0])){
                    recipe.shape(threeLettersArray[1], threeLettersArray[2]);
                    if(threeLettersArray[1].contains("_") || threeLettersArray[2].contains("_")) {
                        materialMap.put("_", Material.AIR);
                    }
                }
                else if(onlyContainsUnderscores(threeLettersArray[2])){
                    recipe.shape(threeLettersArray[0], threeLettersArray[1]);
                    if(threeLettersArray[0].contains("_") || threeLettersArray[1].contains("_")) {
                        materialMap.put("_", Material.AIR);
                    }
                }
                else {
                    try {
                        recipe.shape(threeLettersArray[0], threeLettersArray[1], threeLettersArray[2]);

                        if(threeLettersArray[0].contains("_") || (threeLettersArray[1].contains("_") || threeLettersArray[2].contains("_"))) {
                            materialMap.put("_", Material.AIR);
                        }

                    } catch (IllegalArgumentException e){
                        plugin.getLogger().log(Level.WARNING, "Problem with shaping recipe "+recipeNumber);
                    }
                }

                for (Map.Entry<String, Object> entry : materialMap.entrySet()) {
                    if (entry.getValue() instanceof Material) {
                        Material material = (Material) entry.getValue();
                        if(material.toString().contains("_PLANKS")){
                            recipe.setIngredient(entry.getKey().charAt(0), new RecipeChoice.MaterialChoice(Tag.PLANKS));
                        }
                        else {
                            try {
                                recipe.setIngredient(entry.getKey().charAt(0), material);
                            } catch (IllegalArgumentException e){
                                plugin.getLogger().log(Level.WARNING, "Problem with setting ingredient in recipe "+recipeNumber);
                            }
                        }
                    } else if (entry.getValue() instanceof ItemStack) {
                        // Если материал - это пользовательский предмет (крюк)
                        try {
                            recipe.setIngredient(entry.getKey().charAt(0), new RecipeChoice.ExactChoice((ItemStack) entry.getValue()));
                        } catch (IllegalArgumentException e){
                            plugin.getLogger().log(Level.WARNING, "Problem with setting custom ingredient in recipe "+recipeNumber);
                        }
                    }
                }
                
                try {
                    Bukkit.addRecipe(recipe);
                    loadedCount++;
                } catch (Exception e) {
                    plugin.getLogger().log(Level.WARNING, "Failed to add recipe for " + id + ": " + e.getMessage());
                }
            }
        }

        plugin.getLogger().log(Level.INFO, "Loaded "+loadedCount+" recipes.");
    }

    private boolean onlyContainsUnderscores(String s){
        for (int i =0; i< s.length(); i++){
            char c = s.charAt(i);
            if(c != '_'){
                return false;
            }
        }
        return true;
    }

    public void unloadRecipes(){
        NamespacedKey key;
        for(String hookID : plugin.getGrapplingListener().getHookIDs()){
            key = new NamespacedKey(plugin, "hook_item_" + hookID);
            Bukkit.removeRecipe(key);
        }
    }

    private String formatString(String unformattedString, int uses){
        unformattedString = unformattedString.replace("[uses]", ""+uses);
        unformattedString = ChatColor.translateAlternateColorCodes('&', unformattedString);
        return unformattedString;
    }
}
