package com.snowgears.grapplinghook.utils;

import com.snowgears.grapplinghook.GrapplingHook;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class HookSettings {

    private final String id;
    private final int maxUses;
    private final double velocityThrow;
    private final double velocityPull;
    private final int timeBetweenGrapples;
    private final boolean fallDamage;
    private final boolean slowFall;
    private final boolean lineBreak;
    private final boolean stickyHook;
    private final int customModelData;

    private ItemStack hookItem;

    private final Map<EntityType, Boolean> entityTypes = new EnumMap<>(EntityType.class);
    private final Map<Material, Boolean> materials = new EnumMap<>(Material.class);

    public HookSettings(String id,
        int maxUses,
        double velocityThrow,
        double velocityPull,
        int timeBetweenGrapples,
        boolean fallDamage,
        boolean slowFall,
        boolean lineBreak,
        boolean stickyHook,
        int customModelData){

        this.id = id;
        this.maxUses = maxUses;
        this.velocityThrow = velocityThrow;
        this.velocityPull = velocityPull;
        this.timeBetweenGrapples = timeBetweenGrapples;
        this.fallDamage = fallDamage;
        this.slowFall = slowFall;
        this.lineBreak = lineBreak;
        this.stickyHook = stickyHook;
        this.customModelData = customModelData;
    }

    public void setEntityList(boolean isBlackList, List<EntityType> entityTypeList){
        entityTypes.clear();
        
        if(isBlackList){
            List<EntityType> allEntityTypes = Arrays.asList(EntityType.values());
            for(EntityType entityType : allEntityTypes){
                if(!entityTypeList.contains(entityType)){
                    entityTypes.put(entityType, true);
                }
            }
        } else {
            for(EntityType entityType : entityTypeList){
                entityTypes.put(entityType, true);
            }
        }
    }

    public void setMaterialList(boolean isBlackList, List<Material> materialList){
        materials.clear();
        
        if(isBlackList){
            for(Material material : Material.values()){
                if(!materialList.contains(material)){
                    materials.put(material, true);
                }
            }
        } else {
            for(Material material : materialList){
                materials.put(material, true);
            }
        }
    }

    public ItemStack getHookItem() {
        return hookItem;
    }

    public void setHookItem(ItemStack hookItem) {
        // Устанавливаем CustomModelData, если он задан и включен в конфигурации
        if (this.customModelData > 0 && GrapplingHook.getPlugin().getConfig().getBoolean("custom_models.enabled", false)) {
            if (hookItem != null && hookItem.getItemMeta() != null) {
                ItemMeta meta = hookItem.getItemMeta();
                meta.setCustomModelData(this.customModelData);
                hookItem.setItemMeta(meta);
            }
        }
        this.hookItem = hookItem;
    }

    public String getId() {
        return id;
    }

    public int getMaxUses(){
        return maxUses;
    }

    public double getVelocityThrow() {
        return velocityThrow;
    }

    public double getVelocityPull() {
        return velocityPull;
    }

    public int getTimeBetweenGrapples() {
        return timeBetweenGrapples;
    }

    public boolean isFallDamage() {
        return fallDamage;
    }

    public boolean isSlowFall() {
        return slowFall;
    }

    public boolean isLineBreak() {
        return lineBreak;
    }

    public boolean isStickyHook() {
        return stickyHook;
    }

    public int getCustomModelData() {
        return customModelData;
    }

    public boolean canHookEntityType(EntityType entityType){
        return entityTypes.containsKey(entityType);
    }

    public boolean canHookMaterial(Material material){
        if (material == null || material == Material.AIR) {
            return false;
        }
        
        return materials.containsKey(material);
    }
}
