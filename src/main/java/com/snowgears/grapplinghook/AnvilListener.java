package com.snowgears.grapplinghook;

import com.snowgears.grapplinghook.utils.HookSettings;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AnvilListener implements Listener {

    private GrapplingHook plugin;
    private Map<String, Material> repairMaterials;
    private Map<Material, Integer> repairAmounts;

    public AnvilListener(GrapplingHook plugin) {
        this.plugin = plugin;
        this.repairMaterials = new HashMap<>();
        this.repairAmounts = new HashMap<>();
        try {
            // Используем строковые константы для избежания проблем с загрузкой классов
            repairMaterials.put("wood_hook", Material.valueOf("OAK_PLANKS"));
            repairMaterials.put("stone_hook", Material.valueOf("COBBLESTONE"));
            repairMaterials.put("iron_hook", Material.valueOf("IRON_INGOT"));
            repairMaterials.put("gold_hook", Material.valueOf("GOLD_INGOT"));
            repairMaterials.put("emerald_hook", Material.valueOf("EMERALD"));
            repairMaterials.put("diamond_hook", Material.valueOf("DIAMOND"));
            repairMaterials.put("air_hook", Material.valueOf("FEATHER"));
            repairMaterials.put("water_hook", Material.valueOf("WATER_BUCKET"));
            
            // Добавляем количество использований для каждого материала
            repairAmounts.put(Material.valueOf("OAK_PLANKS"), 1);     // деревянный +1
            repairAmounts.put(Material.valueOf("COBBLESTONE"), 3);    // каменный +3
            repairAmounts.put(Material.valueOf("IRON_INGOT"), 5);     // железный +5
            repairAmounts.put(Material.valueOf("GOLD_INGOT"), 5);     // золотой +5
            repairAmounts.put(Material.valueOf("EMERALD"), 10);       // изумрудный +10
            repairAmounts.put(Material.valueOf("DIAMOND"), 50);       // алмазный +50
            repairAmounts.put(Material.valueOf("FEATHER"), 1);        // воздушный +1 (по умолчанию)
            repairAmounts.put(Material.valueOf("WATER_BUCKET"), 5);   // водный +5 (по умолчанию)
            
            plugin.getLogger().info("AnvilListener initialized with repair materials: " + repairMaterials.toString());
            plugin.getLogger().info("AnvilListener initialized with repair amounts: " + repairAmounts.toString());
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to initialize repair materials: " + e.getMessage());
        }
    }

    @EventHandler
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        AnvilInventory inventory = event.getInventory();
        ItemStack firstItem = inventory.getItem(0);
        ItemStack secondItem = inventory.getItem(1);

        if (firstItem == null || secondItem == null) {
            return;
        }

        // Проверяем, является ли первый предмет удочкой
        if (firstItem.getType() != Material.FISHING_ROD) {
            return;
        }
        
        // Проверяем, является ли первый предмет крюком
        if (!isGrapplingHook(firstItem)) {
            plugin.getLogger().info("Item is not a grappling hook: " + firstItem.toString());
            return;
        }

        // Получаем ID крюка
        String hookId = getHookId(firstItem);
        if (hookId == null) {
            plugin.getLogger().info("Could not get hook ID for item: " + firstItem.toString());
            return;
        }
        
        plugin.getLogger().info("Found hook with ID: " + hookId);

        // Проверяем, подходит ли материал для ремонта
        Material repairMaterial = repairMaterials.get(hookId);
        if (repairMaterial == null) {
            plugin.getLogger().info("No specific repair material found for hook ID: " + hookId + ", checking all repair materials");
            
            // Если не найден материал для конкретного типа крюка, проверяем все доступные материалы
            boolean foundMaterial = false;
            for (Material material : repairMaterials.values()) {
                if (secondItem.getType() == material) {
                    repairMaterial = material;
                    foundMaterial = true;
                    plugin.getLogger().info("Found alternative repair material: " + repairMaterial);
                    break;
                }
            }
            
            if (!foundMaterial) {
                plugin.getLogger().info("No repair material found for hook ID: " + hookId);
                return;
            }
        } else if (secondItem.getType() != repairMaterial) {
            plugin.getLogger().info("Repair material does not match. Expected: " + repairMaterial + ", Got: " + secondItem.getType());
            return;
        }
        
        plugin.getLogger().info("Repair material matches: " + repairMaterial);

        // Получаем настройки крюка
        HookSettings hookSettings = plugin.getGrapplingListener().getHookSettings(hookId);
        if (hookSettings == null) {
            plugin.getLogger().info("Could not get hook settings for ID: " + hookId);
            return;
        }
        
        plugin.getLogger().info("Found hook settings for ID: " + hookId);

        // Создаем восстановленный крюк
        ItemStack repairedHook = firstItem.clone();
        ItemMeta meta = repairedHook.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        
        // Получаем текущее количество использований
        Integer currentUses = container.get(new NamespacedKey(plugin, "uses"), PersistentDataType.INTEGER);
        if (currentUses == null) {
            plugin.getLogger().info("Could not get current uses for hook");
            currentUses = 0;
        }
        
        plugin.getLogger().info("Current uses: " + currentUses);
        
        // Получаем максимальное количество использований
        int maxUses = hookSettings.getMaxUses();
        plugin.getLogger().info("Max uses: " + maxUses);
        
        // Проверяем, нужен ли ремонт
        if (currentUses >= maxUses) {
            plugin.getLogger().info("Hook is already at max uses, no repair needed");
            return;
        }
        
        // Получаем базовое количество использований для одного материала
        int repairPerItem = repairAmounts.getOrDefault(repairMaterial, 1);
        
        // Получаем количество материала
        int materialAmount = secondItem.getAmount();
        
        // Вычисляем, сколько материала нужно для полного ремонта
        int usesNeeded = maxUses - currentUses;
        int materialsNeeded = (int) Math.ceil((double) usesNeeded / repairPerItem);
        
        // Ограничиваем количество используемого материала
        int materialsToUse = Math.min(materialAmount, materialsNeeded);
        
        // Вычисляем количество добавляемых использований
        int repairAmount = materialsToUse * repairPerItem;
        
        // Убеждаемся, что не превышаем максимум
        int newUses = Math.min(currentUses + repairAmount, maxUses);
        
        plugin.getLogger().info("Materials to use: " + materialsToUse + ", Repair amount: " + repairAmount + ", New uses: " + newUses);
        
        // Сохраняем количество материалов, которые будут использованы
        meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "repair_materials"), PersistentDataType.INTEGER, materialsToUse);
        
        // Обновляем количество использований
        container.set(new NamespacedKey(plugin, "uses"), PersistentDataType.INTEGER, newUses);
        
        // Обновляем лор с новым количеством использований
        List<String> lore = meta.getLore();
        if (lore != null && !lore.isEmpty()) {
            List<String> newLore = new ArrayList<>();
            boolean foundUsesLine = false;
            
            // Регулярное выражение для поиска строки с количеством использований
            Pattern pattern = Pattern.compile(".*\\[uses\\].*");
            
            for (String line : lore) {
                Matcher matcher = pattern.matcher(line);
                if (matcher.matches()) {
                    // Это строка с плейсхолдером [uses]
                    newLore.add(line.replaceAll("\\[uses\\]", String.valueOf(newUses)));
                    foundUsesLine = true;
                } else if (line.contains("Uses left") || line.contains("uses left")) {
                    // Строка содержит "Uses left" или "uses left"
                    newLore.add(line.replaceAll("\\d+", String.valueOf(newUses)));
                    foundUsesLine = true;
                } else {
                    // Это другая строка, просто добавляем её
                    newLore.add(line);
                }
            }
            
            // Если не нашли строку с использованиями, добавляем новую
            if (!foundUsesLine) {
                newLore.add(ChatColor.GRAY + "Uses left: " + ChatColor.GREEN + newUses);
            }
            
            meta.setLore(newLore);
        } else {
            // Если лор пустой, создаем новый
            List<String> newLore = new ArrayList<>();
            newLore.add(ChatColor.GRAY + "Uses left: " + ChatColor.GREEN + newUses);
            meta.setLore(newLore);
        }
        
        repairedHook.setItemMeta(meta);
        
        // Устанавливаем стоимость ремонта
        inventory.setRepairCost(1);
        
        // Устанавливаем результат
        event.setResult(repairedHook);
        
        plugin.getLogger().info("Prepared repaired hook with new uses: " + newUses);
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory() instanceof AnvilInventory)) {
            return;
        }
        
        if (event.getSlotType() == SlotType.RESULT) {
            ItemStack result = event.getCurrentItem();
            if (result == null || !isGrapplingHook(result)) {
                return;
            }
            
            // Проверяем, есть ли у игрока достаточно опыта
            AnvilInventory anvilInventory = (AnvilInventory) event.getInventory();
            Player player = (Player) event.getWhoClicked();
            int repairCost = anvilInventory.getRepairCost();
            
            if (player.getLevel() < repairCost && player.getGameMode() != GameMode.CREATIVE) {
                event.setCancelled(true);
                player.sendMessage(plugin.getMessageManager().getMessage("anvil.not_enough_xp"));
                return;
            }
            
            // Получаем количество материалов, которые нужно использовать
            ItemMeta meta = result.getItemMeta();
            int materialsToUse = 1; // По умолчанию используем 1 материал
            
            if (meta != null && meta.getPersistentDataContainer().has(new NamespacedKey(plugin, "repair_materials"), PersistentDataType.INTEGER)) {
                materialsToUse = meta.getPersistentDataContainer().get(new NamespacedKey(plugin, "repair_materials"), PersistentDataType.INTEGER);
            }
            
            // Сохраняем оригинальный предмет из первого слота
            ItemStack originalItem = anvilInventory.getItem(0);
            
            // Уменьшаем количество материала для ремонта
            ItemStack secondItem = anvilInventory.getItem(1);
            if (secondItem != null) {
                if (secondItem.getAmount() > materialsToUse) {
                    secondItem.setAmount(secondItem.getAmount() - materialsToUse);
                    anvilInventory.setItem(1, secondItem);
                } else {
                    anvilInventory.setItem(1, null);
                }
            }
            
            // Уменьшаем уровень опыта игрока
            if (player.getGameMode() != GameMode.CREATIVE) {
                player.setLevel(player.getLevel() - repairCost);
            }
            
            // Получаем ID крюка и материал для ремонта
            String hookId = getHookId(result);
            Material repairMaterial = repairMaterials.get(hookId);
            if (repairMaterial == null) {
                // Если материал не найден по ID, ищем по всем доступным материалам
                for (Material material : repairMaterials.values()) {
                    if (secondItem != null && secondItem.getType() == material) {
                        repairMaterial = material;
                        break;
                    }
                }
            }
            
            // Получаем количество добавленных использований на один материал
            int addedUsesPerItem = repairAmounts.getOrDefault(repairMaterial, 1);
            
            // Вычисляем общее количество добавленных использований
            int totalAddedUses = addedUsesPerItem * materialsToUse;
            
            // Отправляем сообщение об успешном ремонте
            player.sendMessage(plugin.getMessageManager().getMessage("anvil.repair_success") + 
                    " (+" + totalAddedUses + ")");
            
            plugin.getLogger().info("Player " + player.getName() + " repaired hook with ID: " + hookId + 
                    ", added uses: " + totalAddedUses + " using " + materialsToUse + " materials");
            
            // Удаляем метаданные о количестве материалов
            if (meta != null && meta.getPersistentDataContainer().has(new NamespacedKey(plugin, "repair_materials"), PersistentDataType.INTEGER)) {
                meta.getPersistentDataContainer().remove(new NamespacedKey(plugin, "repair_materials"));
                result.setItemMeta(meta);
            }
            
            // Важно! Не отменяем событие, чтобы игрок получил отремонтированный предмет
            // Но очищаем первый слот, чтобы предотвратить дублирование предмета
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (originalItem != null && anvilInventory.getItem(0) != null && 
                    anvilInventory.getItem(0).isSimilar(originalItem)) {
                    anvilInventory.setItem(0, null);
                }
            });
        }
    }
    
    private boolean isGrapplingHook(ItemStack item) {
        if (item == null) {
            return false;
        }
        
        try {
            if (item.getType() != Material.valueOf("FISHING_ROD")) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }
        
        // Проверяем наличие PersistentDataContainer
        PersistentDataContainer container = meta.getPersistentDataContainer();
        
        // Проверяем наличие тега id
        if (!container.has(new NamespacedKey(plugin, "id"), PersistentDataType.STRING)) {
            return false;
        }
        
        // Дополнительно проверяем наличие лора, который может содержать информацию об использованиях
        if (meta.hasLore()) {
            List<String> lore = meta.getLore();
            for (String line : lore) {
                if (line.contains("[uses]") || line.contains("Uses left") || line.contains("uses left")) {
                    return true;
                }
            }
        }
        
        // Если есть тег id, считаем что это крюк
        return true;
    }
    
    private String getHookId(ItemStack item) {
        if (!isGrapplingHook(item)) {
            return null;
        }
        
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        return container.get(new NamespacedKey(plugin, "id"), PersistentDataType.STRING);
    }
} 