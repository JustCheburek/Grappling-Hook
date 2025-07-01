package com.snowgears.grapplinghook;

import com.snowgears.grapplinghook.utils.HookSettings;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
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

    private final GrapplingHook plugin;
    private final Map<String, Material> repairMaterials;
    private final Map<Material, Integer> repairAmounts;

    public AnvilListener(GrapplingHook plugin) {
        this.plugin = plugin;
        this.repairMaterials = new HashMap<>();
        this.repairAmounts = new HashMap<>();

        // Загружаем материалы для ремонта из конфига
        loadRepairMaterials();
    }

    private void loadRepairMaterials() {
        // Стандартные типы крюков
        repairMaterials.put("grappling_hook", Material.STRING);
        repairMaterials.put("multipull_hook", Material.TRIPWIRE_HOOK);
        repairMaterials.put("rope_hook", Material.LEAD);
        repairMaterials.put("ender_hook", Material.ENDER_PEARL);
        
        // Материальные типы крюков
        repairMaterials.put("wood_hook", Material.OAK_PLANKS);
        repairMaterials.put("stone_hook", Material.COBBLESTONE);
        repairMaterials.put("iron_hook", Material.IRON_INGOT);
        repairMaterials.put("gold_hook", Material.GOLD_INGOT);
        repairMaterials.put("emerald_hook", Material.EMERALD);
        repairMaterials.put("diamond_hook", Material.DIAMOND);
        repairMaterials.put("air_hook", Material.FEATHER);
        repairMaterials.put("water_hook", Material.WATER_BUCKET);
        
        // Количество использований, которые восстанавливаются за один материал
        repairAmounts.put(Material.STRING, 10);
        repairAmounts.put(Material.TRIPWIRE_HOOK, 20);
        repairAmounts.put(Material.LEAD, 16);
        repairAmounts.put(Material.ENDER_PEARL, 30);
        
        repairAmounts.put(Material.OAK_PLANKS, 5);    // деревянный +5
        repairAmounts.put(Material.COBBLESTONE, 10);   // каменный +10
        repairAmounts.put(Material.IRON_INGOT, 15);    // железный +15
        repairAmounts.put(Material.GOLD_INGOT, 15);    // золотой +15
        repairAmounts.put(Material.EMERALD, 30);      // изумрудный +30
        repairAmounts.put(Material.DIAMOND, 100);      // алмазный +100
        repairAmounts.put(Material.FEATHER, 5);       // воздушный +5
        repairAmounts.put(Material.WATER_BUCKET, 15);  // водный +15
        
        plugin.getLogger().info("Загружены материалы для ремонта: " + repairMaterials.keySet());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        AnvilInventory inventory = event.getInventory();
        ItemStack firstItem = inventory.getItem(0);
        ItemStack secondItem = inventory.getItem(1);
        
        // Проверяем, является ли первый предмет крюком
        if (firstItem == null || !isGrapplingHook(firstItem)) {
            return;
        }
        
        // Проверяем, не пустой ли второй слот
        if (secondItem == null || secondItem.getType() == Material.AIR) {
            return;
        }
        
        // Получаем ID крюка и соответствующий материал для ремонта
        String hookId = getHookId(firstItem);
        if (hookId == null) {
            plugin.getLogger().warning("Не удалось получить ID крюка для: " + firstItem);
            return;
        }
        
        plugin.getLogger().info("Подготовка к ремонту крюка типа: " + hookId);
        
        // Получаем материал для ремонта по ID крюка
        Material repairMaterial = repairMaterials.get(hookId);
        
        // Если материал не определен по ID или не соответствует, прерываем
        if (repairMaterial == null) {
            plugin.getLogger().warning("Материал для ремонта не найден для крюка: " + hookId);
            return;
        }
        
        if (secondItem.getType() != repairMaterial) {
            plugin.getLogger().info("Материал не подходит. Нужен: " + repairMaterial + ", Предложен: " + secondItem.getType());
            return;
        }
        
        plugin.getLogger().info("Материал для ремонта соответствует требуемому: " + repairMaterial);
        
        // Получаем текущее количество использований крюка
        int currentUses = 0;
        ItemMeta hookMeta = firstItem.getItemMeta();
        if (hookMeta != null && hookMeta.getPersistentDataContainer().has(
                new NamespacedKey(plugin, "uses"), PersistentDataType.INTEGER)) {
            currentUses = hookMeta.getPersistentDataContainer().get(
                    new NamespacedKey(plugin, "uses"), PersistentDataType.INTEGER);
        }
        
        plugin.getLogger().info("Текущие использования крюка: " + currentUses);
        
        // Получаем максимальное количество использований для данного типа крюка
        HookSettings settings = plugin.getGrapplingListener().getHookSettings(hookId);
        int maxUses = settings != null ? settings.getMaxUses() : 25; // Значение по умолчанию
        
        // Если крюк уже имеет максимальное количество использований, прерываем
        if (currentUses >= maxUses) {
            plugin.getLogger().info("Крюк уже имеет максимальное количество использований: " + currentUses + "/" + maxUses);
            return;
        }
        
        // Рассчитываем сколько использований можно добавить за один материал
        int usesPerMaterial = repairAmounts.getOrDefault(repairMaterial, 1);
        
        // Рассчитываем, сколько материала максимально можно использовать
        int maxMaterialsNeeded = (int) Math.ceil((maxUses - currentUses) / (double) usesPerMaterial);
        int materialsAvailable = secondItem.getAmount();
        int materialsToUse = Math.min(maxMaterialsNeeded, materialsAvailable);
        
        // Рассчитываем новое количество использований
        int newUses = Math.min(currentUses + (materialsToUse * usesPerMaterial), maxUses);
        
        plugin.getLogger().info("Ремонт крюка: материалов используется: " + materialsToUse + 
                ", добавляется использований: " + (newUses - currentUses) + 
                ", новое количество: " + newUses + "/" + maxUses);
        
        // Создаем новый предмет для результата
        ItemStack resultItem = firstItem.clone();
        ItemMeta resultMeta = resultItem.getItemMeta();
        
        // Обновляем количество использований
        resultMeta.getPersistentDataContainer().set(
                new NamespacedKey(plugin, "uses"), PersistentDataType.INTEGER, newUses);
        
        // Обновляем лор с новым количеством использований
        List<String> lore = resultMeta.getLore();
        if (lore != null && !lore.isEmpty()) {
            List<String> newLore = new ArrayList<>();
            boolean foundUsesLine = false;
            
            // Регулярное выражение для поиска строки с количеством использований
            Pattern pattern = Pattern.compile(".*\\[uses\\].*");
            
            // Проверяем наличие надписи "Крюк сломан" в лоре
            boolean hasBrokenHookLine = false;
            for (String line : lore) {
                if (line.contains("Крюк сломан") || line.contains("Hook broken")) {
                    hasBrokenHookLine = true;
                    break;
                }
            }
            
            // Если надпись о сломанном крюке найдена, удаляем её из лора
            if (hasBrokenHookLine) {
                plugin.getLogger().info("Найдена надпись 'Крюк сломан', удаляем её при починке");
            }
            
            for (String line : lore) {
                // Пропускаем строки, содержащие "Крюк сломан" или "Hook broken"
                if (line.contains("Крюк сломан") || line.contains("Hook broken")) {
                    continue;
                }
                
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
            
            resultMeta.setLore(newLore);
        } else {
            // Если лор пустой, создаем новый
            List<String> newLore = new ArrayList<>();
            newLore.add(ChatColor.GRAY + "Uses left: " + ChatColor.GREEN + newUses);
            resultMeta.setLore(newLore);
        }
        
        // Применяем обновленные метаданные
        resultItem.setItemMeta(resultMeta);
        
        // Устанавливаем стоимость ремонта
        try {
            // Устанавливаем фиксированную стоимость ремонта
            if (inventory.getClass().getMethod("setRepairCost", int.class) != null) {
                inventory.setRepairCost(1);
            }
        } catch (NoSuchMethodException | SecurityException e) {
            plugin.getLogger().warning("Could not set repair cost: " + e.getMessage());
        }
        
        // Устанавливаем результат в слот результата
        event.setResult(resultItem);
        
        plugin.getLogger().info("Результат ремонта установлен в слот результата наковальни");
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory() instanceof AnvilInventory)) {
            return;
        }
        
        // Проверяем, кликнул ли игрок по результату
        if (event.getSlotType() != SlotType.RESULT) {
            return;
        }
        
        ItemStack result = event.getCurrentItem();
        if (result == null || !isGrapplingHook(result)) {
            return;
        }
        
        AnvilInventory anvilInventory = (AnvilInventory) event.getInventory();
        Player player = (Player) event.getWhoClicked();
        
        // Получаем оригинальный крюк и материал для ремонта
        ItemStack hookItem = anvilInventory.getItem(0);
        ItemStack materialItem = anvilInventory.getItem(1);
        
        if (hookItem == null || materialItem == null) {
            return;
        }
        
        // Получаем ID крюка и соответствующий материал для ремонта
        String hookId = getHookId(hookItem);
        Material repairMaterial = repairMaterials.get(hookId);
        
        // Если материал не определен по ID, проверяем по типу предмета
        if (repairMaterial == null) {
            for (Material material : repairMaterials.values()) {
                if (materialItem.getType() == material) {
                    repairMaterial = material;
                    break;
                }
            }
        }
        
        // Если материал для ремонта не найден или не соответствует, прерываем
        if (repairMaterial == null || materialItem.getType() != repairMaterial) {
            return;
        }
        
        // Получаем количество uses крюка до и после ремонта
        int currentUses = 0;
        int newUses = 0;
        
        ItemMeta hookMeta = hookItem.getItemMeta();
        ItemMeta resultMeta = result.getItemMeta();
        
        if (hookMeta != null && hookMeta.getPersistentDataContainer().has(
                new NamespacedKey(plugin, "uses"), PersistentDataType.INTEGER)) {
            currentUses = hookMeta.getPersistentDataContainer().get(
                    new NamespacedKey(plugin, "uses"), PersistentDataType.INTEGER);
        }
        
        if (resultMeta != null && resultMeta.getPersistentDataContainer().has(
                new NamespacedKey(plugin, "uses"), PersistentDataType.INTEGER)) {
            newUses = resultMeta.getPersistentDataContainer().get(
                    new NamespacedKey(plugin, "uses"), PersistentDataType.INTEGER);
        }
        
        // Рассчитываем добавленные использования
        int addedUses = newUses - currentUses;
        
        // Получаем максимальное количество uses для данного типа крюка
        HookSettings settings = plugin.getGrapplingListener().getHookSettings(hookId);
        int maxUses = settings != null ? settings.getMaxUses() : 25; // Значение по умолчанию
        
        // Рассчитываем сколько uses можно добавить за один материал
        int usesPerMaterial = repairAmounts.getOrDefault(repairMaterial, 1);
        
        // Рассчитываем сколько материала нужно для добавления использований
        int materialsUsed = (int) Math.ceil(addedUses / (double) usesPerMaterial);
        
        plugin.getLogger().info("Починка крюка: текущие использования=" + currentUses + 
                ", новые использования=" + newUses + ", добавлено=" + addedUses + 
                ", использований за материал=" + usesPerMaterial + 
                ", требуется материалов=" + materialsUsed);
        
        // Проверяем, есть ли у игрока достаточно опыта для ремонта
        int repairCost = 1; // Фиксированная стоимость
        
        if (player.getLevel() < repairCost && player.getGameMode() != GameMode.CREATIVE) {
            event.setCancelled(true);
            player.sendMessage(plugin.getMessageManager().getMessage("anvil.not_enough_xp"));
            return;
        }
        
        // Отменяем стандартное поведение наковальни
        event.setCancelled(true);
        
        // Вычитаем опыт у игрока
        if (player.getGameMode() != GameMode.CREATIVE) {
            player.setLevel(player.getLevel() - repairCost);
        }
        
        // Удаляем крюк из первого слота
        anvilInventory.setItem(0, null);
        
        // Уменьшаем количество материала на точное необходимое количество
        if (materialItem.getAmount() > materialsUsed) {
            materialItem.setAmount(materialItem.getAmount() - materialsUsed);
            anvilInventory.setItem(1, materialItem);
        } else {
            anvilInventory.setItem(1, null);
        }
        
        // Добавляем починенный крюк в инвентарь игрока
        HashMap<Integer, ItemStack> leftovers = player.getInventory().addItem(result.clone());
        
        // Если инвентарь полон, выбрасываем предмет на землю
        if (!leftovers.isEmpty()) {
            for (ItemStack leftover : leftovers.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), leftover);
            }
        }
        
        // Очищаем слот результата
        anvilInventory.setItem(2, null);
        
        // Обновляем инвентарь для игрока
        player.updateInventory();
        
        // Отправляем сообщение об успешном ремонте
        player.sendMessage(plugin.getMessageManager().getMessage("anvil.repair_success") + 
                " (+" + addedUses + ")");
        
        // Воспроизводим звук ремонта
        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1.0f, 1.0f);
        
        plugin.getLogger().info("Player " + player.getName() + " repaired hook with ID: " + hookId + 
                ", added uses: " + addedUses + " using " + materialsUsed + " materials");
    }
    
    /**
     * Проверяет, является ли предмет крюком
     * @param item предмет для проверки
     * @return true, если предмет является крюком
     */
    private boolean isGrapplingHook(ItemStack item) {
        if (item == null) {
            return false;
        }
        
        // Проверяем, что предмет - удочка
        if (item.getType() != Material.FISHING_ROD) {
            return false;
        }
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }
        
        PersistentDataContainer container = meta.getPersistentDataContainer();
        boolean isHook = container.has(new NamespacedKey(plugin, "id"), PersistentDataType.STRING);
        
        if (isHook) {
            plugin.getLogger().info("Обнаружен крюк с ID: " + getHookId(item));
        }
        
        return isHook;
    }
    
    /**
     * Получает ID крюка из предмета
     * @param item предмет, из которого нужно получить ID
     * @return ID крюка или null, если предмет не является крюком
     */
    private String getHookId(ItemStack item) {
        if (item == null) {
            return null;
        }
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return null;
        }
        
        PersistentDataContainer container = meta.getPersistentDataContainer();
        if (!container.has(new NamespacedKey(plugin, "id"), PersistentDataType.STRING)) {
            return null;
        }
        
        return container.get(new NamespacedKey(plugin, "id"), PersistentDataType.STRING);
    }
} 