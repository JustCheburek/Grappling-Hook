package com.snowgears.grapplinghook;

import com.snowgears.grapplinghook.utils.HookSettings;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
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

public class AnvilListener implements Listener {

    private GrapplingHook plugin;
    private Map<String, Material> repairMaterials;

    public AnvilListener(GrapplingHook plugin) {
        this.plugin = plugin;
        initRepairMaterials();
    }

    private void initRepairMaterials() {
        repairMaterials = new HashMap<>();
        repairMaterials.put("wood_hook", Material.OAK_PLANKS);
        repairMaterials.put("stone_hook", Material.COBBLESTONE);
        repairMaterials.put("iron_hook", Material.IRON_INGOT);
        repairMaterials.put("gold_hook", Material.GOLD_INGOT);
        repairMaterials.put("emerald_hook", Material.EMERALD);
        repairMaterials.put("diamond_hook", Material.DIAMOND);
        repairMaterials.put("air_hook", Material.FEATHER);
        repairMaterials.put("water_hook", Material.WATER_BUCKET);
    }

    @EventHandler
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        AnvilInventory inventory = event.getInventory();
        ItemStack firstItem = inventory.getItem(0);
        ItemStack secondItem = inventory.getItem(1);

        if (firstItem == null || secondItem == null) {
            return;
        }

        // Проверяем, является ли первый предмет крюком
        if (!isGrapplingHook(firstItem)) {
            return;
        }

        // Получаем ID крюка
        String hookId = getHookId(firstItem);
        if (hookId == null) {
            return;
        }

        // Проверяем, подходит ли материал для ремонта
        Material repairMaterial = repairMaterials.get(hookId);
        if (repairMaterial == null || secondItem.getType() != repairMaterial) {
            return;
        }

        // Получаем настройки крюка
        HookSettings hookSettings = plugin.getGrapplingListener().getHookSettings(hookId);
        if (hookSettings == null) {
            return;
        }

        // Создаем восстановленный крюк
        ItemStack repairedHook = firstItem.clone();
        ItemMeta meta = repairedHook.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        
        // Получаем текущее количество использований
        int currentUses = container.getOrDefault(new NamespacedKey(plugin, "uses"), PersistentDataType.INTEGER, 0);
        int maxUses = hookSettings.getMaxUses();
        
        // Определяем, сколько использований восстановить (1 материал = 25% от максимума)
        int repairAmount = Math.max(1, maxUses / 4);
        int newUses = Math.min(maxUses, currentUses + repairAmount);
        
        // Обновляем количество использований
        container.set(new NamespacedKey(plugin, "uses"), PersistentDataType.INTEGER, newUses);
        
        // Обновляем лор с новым количеством использований
        List<String> lore = meta.getLore();
        if (lore != null && !lore.isEmpty()) {
            List<String> newLore = new ArrayList<>();
            for (String line : lore) {
                newLore.add(line.replace("[uses]", String.valueOf(newUses)));
            }
            meta.setLore(newLore);
        }
        
        repairedHook.setItemMeta(meta);
        
        // Устанавливаем стоимость ремонта
        inventory.setRepairCost(1);
        
        // Устанавливаем результат
        event.setResult(repairedHook);
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory() instanceof AnvilInventory)) {
            return;
        }
        
        if (event.getSlotType().toString().equals("RESULT")) {
            ItemStack result = event.getCurrentItem();
            if (result == null || !isGrapplingHook(result)) {
                return;
            }
            
            // Проверяем, есть ли у игрока достаточно опыта
            AnvilInventory anvilInventory = (AnvilInventory) event.getInventory();
            Player player = (Player) event.getWhoClicked();
            int repairCost = anvilInventory.getRepairCost();
            
            if (player.getLevel() < repairCost && !player.getGameMode().toString().equals("CREATIVE")) {
                event.setCancelled(true);
                player.sendMessage(plugin.getMessageManager().getMessage("anvil.not_enough_xp"));
                return;
            }
            
            // Уменьшаем количество материала для ремонта
            ItemStack secondItem = anvilInventory.getItem(1);
            if (secondItem != null && secondItem.getAmount() > 1) {
                secondItem.setAmount(secondItem.getAmount() - 1);
                anvilInventory.setItem(1, secondItem);
            } else {
                anvilInventory.setItem(1, null);
            }
            
            // Уменьшаем уровень опыта игрока
            if (!player.getGameMode().toString().equals("CREATIVE")) {
                player.setLevel(player.getLevel() - repairCost);
            }
            
            player.sendMessage(plugin.getMessageManager().getMessage("anvil.repair_success"));
        }
    }
    
    private boolean isGrapplingHook(ItemStack item) {
        if (item == null || item.getType() != Material.FISHING_ROD) {
            return false;
        }
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }
        
        PersistentDataContainer container = meta.getPersistentDataContainer();
        return container.has(new NamespacedKey(plugin, "id"), PersistentDataType.STRING);
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