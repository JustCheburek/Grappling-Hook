package com.snowgears.grapplinghook.api;

import com.snowgears.grapplinghook.GrapplingHook;
import com.snowgears.grapplinghook.utils.HookSettings;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public final class HookAPI {
	
	public static boolean isGrapplingHook(ItemStack is) {
		if (is == null) return false;
		
		try {
			// Проверяем, что это удочка
			if(is.getType() != Material.FISHING_ROD) {
				return false;
			}
			
			ItemMeta im = is.getItemMeta();
			if (im == null) return false;
			
			// Проверяем наличие PersistentDataContainer
			PersistentDataContainer persistentData = im.getPersistentDataContainer();
			if (persistentData == null) return false;
			
			// Проверяем наличие тега uses и что uses > 0
			try {
				Integer uses = persistentData.get(new NamespacedKey(GrapplingHook.getPlugin(), "uses"), PersistentDataType.INTEGER);
				if (uses != null && uses > 0) {
					return true;
				} else if (uses != null && uses <= 0) {
					// Если uses <= 0, крюк считается сломанным
					return false;
				}
			} catch (Exception e) {
				// Игнорируем ошибку
			}
			
			// Проверяем наличие тега id
			try {
				String id = persistentData.get(new NamespacedKey(GrapplingHook.getPlugin(), "id"), PersistentDataType.STRING);
				if (id != null) {
					// Дополнительно проверяем, не сломан ли крюк
					if (im.hasLore()) {
						List<String> lore = im.getLore();
						for (String line : lore) {
							if (line.contains("Крюк сломан") || line.contains("Hook broken")) {
								return false;
							}
						}
					}
					return true;
				}
			} catch (Exception e) {
				// Игнорируем ошибку
			}
			
			// Проверяем по лору
			if (im.hasLore()) {
				List<String> lore = im.getLore();
				for (String line : lore) {
					// Проверяем, не сломан ли крюк
					if (line.contains("Крюк сломан") || line.contains("Hook broken")) {
						return false;
					}
					
					if (line.contains("Uses left") || line.contains("uses left")) {
						// Проверяем, что количество использований > 0
						if (line.contains("0")) {
							return false;
						}
						return true;
					}
				}
			}
			
			// Проверяем по имени
			if (im.hasDisplayName()) {
				String name = im.getDisplayName();
				if (name.contains("Grappling Hook") || name.contains("Hook")) {
					// Дополнительно проверяем, не сломан ли крюк по лору
					if (im.hasLore()) {
						List<String> lore = im.getLore();
						for (String line : lore) {
							if (line.contains("Крюк сломан") || line.contains("Hook broken")) {
								return false;
							}
						}
					}
					return true;
				}
			}
		} catch (Exception e) {
			// В случае ошибки при получении типа материала или других исключений
			GrapplingHook.getPlugin().getLogger().warning("Error checking if item is grappling hook: " + e.getMessage());
		}
		
		return false;
	}

	public static ItemStack createGrapplingHook(String hookID) {
		HookSettings hookSettings = GrapplingHook.getPlugin().getGrapplingListener().getHookSettings(hookID);
		if(hookSettings == null)
			return null;

		return hookSettings.getHookItem();
	}

	public static HookSettings getHookSettingsForHookInHand(Player player){
		return getHookSettingsForHook(player.getInventory().getItemInMainHand());
	}

	public static HookSettings getHookSettingsForHook(ItemStack hook){
		try {
			ItemMeta im = hook.getItemMeta();
			PersistentDataContainer persistentData = im.getPersistentDataContainer();

			String hookID = persistentData.get(new NamespacedKey(GrapplingHook.getPlugin(), "id"), PersistentDataType.STRING);
			return GrapplingHook.getPlugin().getGrapplingListener().getHookSettings(hookID);
		} catch (Exception e) {
			return null;
		}
	}

	public static boolean getHookInHandHasFallDamage(Player player){

		HookSettings hookSettings = getHookSettingsForHookInHand(player);
		if(hookSettings == null)
			return false;
		return hookSettings.isFallDamage();
	}

	public static boolean getHookInHandHasSlowFall(Player player){

		HookSettings hookSettings = getHookSettingsForHookInHand(player);
		if(hookSettings == null)
			return false;
		return hookSettings.isSlowFall();
	}

	public static boolean getHookInHandHasLineBreak(Player player){

		HookSettings hookSettings = getHookSettingsForHookInHand(player);
		if(hookSettings == null)
			return false;
		return hookSettings.isLineBreak();
	}

	public static boolean getHookInHandHasStickyHook(Player player){

		HookSettings hookSettings = getHookSettingsForHookInHand(player);
		if(hookSettings == null)
			return false;
		return hookSettings.isStickyHook();
	}

	public static double getHookInHandVelocityThrow(Player player){

		HookSettings hookSettings = getHookSettingsForHookInHand(player);
		if(hookSettings == null)
			return 0;
		return hookSettings.getVelocityThrow();
	}

	public static double getHookInHandVelocityPull(Player player){

		HookSettings hookSettings = getHookSettingsForHookInHand(player);
		if(hookSettings == null)
			return 0;
		return hookSettings.getVelocityPull();
	}

	public static int getHookInHandTimeBetweenGrapples(Player player){

		HookSettings hookSettings = getHookSettingsForHookInHand(player);
		if(hookSettings == null)
			return 0;
		return hookSettings.getTimeBetweenGrapples();
	}

	public static boolean canHookEntityType(Player player, EntityType entityType){

		HookSettings hookSettings = getHookSettingsForHookInHand(player);
		if(hookSettings == null)
			return false;
		return hookSettings.canHookEntityType(entityType);
	}

	public static boolean canHookMaterial(Player player, Material material){
		// Если это воздух, всегда запрещаем зацепление
		if (material == Material.AIR) {
			return false;
		}
		
		// Для всех остальных материалов разрешаем зацепление
		return true;
	}


	//returns the recipe # of the hook (from the recipes.yml file)
	public static String getHookID(ItemStack hook){

		HookSettings hookSettings = getHookSettingsForHook(hook);
		if(hookSettings == null)
			return null;
		return hookSettings.getId();
	}
	
	public static boolean isPlayerOnCoolDown(Player player) {
		return GrapplingHook.getPlugin().getGrapplingListener().isPlayerOnCoolDown(player);
	}
	
	public static void removePlayerCoolDown(Player player) {
		GrapplingHook.getPlugin().getGrapplingListener().removePlayerCoolDown(player);
	}
	
	public static void addPlayerCoolDown(final Player player, int seconds) {
		GrapplingHook.getPlugin().getGrapplingListener().addPlayerCoolDown(player, seconds);
	}
	
//	public static void setUses(ItemStack is, int uses) {
//		ItemMeta im = is.getItemMeta();
//		List<String> lore = new ArrayList<String>();
//
//		lore.add(ChatColor.GRAY+"Uses left: "+ChatColor.GREEN+uses);
//
//		im.setLore(lore);
//		is.setItemMeta(im);
//	}

	public static void breakHookInHand(Player player){
		try {
			player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
			player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 10f, 1f);
		} catch (Exception e) {
			GrapplingHook.getPlugin().getLogger().warning("Error breaking hook in hand: " + e.getMessage());
		}
	}
	
	public static boolean addUse(Player player, ItemStack hook){
		if (player == null || hook == null) return false;
		
		if(player.getGameMode() == GameMode.CREATIVE)
			return true;
			
		try {
			ItemMeta im = hook.getItemMeta();
			if (im == null) return false;

			PersistentDataContainer persistentData = im.getPersistentDataContainer();
			if (persistentData == null) return false;
			
			try {
				Integer uses = persistentData.get(new NamespacedKey(GrapplingHook.getPlugin(), "uses"), PersistentDataType.INTEGER);
				
				if (uses == null) {
					return false;
				}

				if((uses - 1) <= 0) { 
					// Крюк должен "сломаться" (стать неактивным), но предмет остается
					// Устанавливаем uses = 0
					List<String> oldLore = im.getLore();
					List<String> newLore = new ArrayList<>();
					
					if (oldLore != null) {
						for(String loreLine : oldLore){
							// Пропускаем строки, содержащие "Крюк сломан" или "Hook broken"
							if (loreLine.contains("Крюк сломан") || loreLine.contains("Hook broken")) {
								continue;
							}
							
							// Заменяем все упоминания использований на 0
							String modifiedLine = loreLine;
							// Заменяем [uses] на 0
							modifiedLine = modifiedLine.replace("[uses]", String.valueOf(0));
							// Заменяем числа в строках, содержащих "Uses left" или "uses left"
							if (modifiedLine.contains("Uses left") || modifiedLine.contains("uses left")) {
								// Изменяем цвет текста на красный, а цифры на темно-красный
								modifiedLine = ChatColor.RED + "Uses left: " + ChatColor.DARK_RED + "0";
							}
							newLore.add(modifiedLine);
						}
					} else {
						newLore = new ArrayList<>();
					}
					
					// Добавляем строку о том, что крюк сломан только если uses = 0
					newLore.add(ChatColor.RED + "Крюк сломан!");
					im.setLore(newLore);

					persistentData.set(new NamespacedKey(GrapplingHook.getPlugin(), "uses"), PersistentDataType.INTEGER, 0);
					hook.setItemMeta(im);
					
					// Оповещаем игрока о том, что крюк сломан
					player.sendMessage(ChatColor.RED + "Ваш крюк сломался!");
					player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
					
					return false; // Возвращаем false, чтобы крюк не сработал
				}
				else {
					// Обновляем лор и количество использований
					List<String> oldLore = im.getLore();
					List<String> newLore = new ArrayList<>();
					
					int newUses = uses - 1;
					
					if (oldLore != null) {
						for(String loreLine : oldLore){
							// Пропускаем строки, содержащие "Крюк сломан" или "Hook broken"
							if (loreLine.contains("Крюк сломан") || loreLine.contains("Hook broken")) {
								continue;
							}
							
							// Заменяем [uses] на новое значение
							String modifiedLine = loreLine.replace("[uses]", String.valueOf(newUses));
							
							// Заменяем числа в строках, содержащих "Uses left" или "uses left"
							if (modifiedLine.contains("Uses left") || modifiedLine.contains("uses left")) {
								modifiedLine = modifiedLine.replaceAll("\\d+", String.valueOf(newUses));
							}
							
							newLore.add(modifiedLine);
						}
						im.setLore(newLore);
					}

					// Обновляем количество использований
					persistentData.set(new NamespacedKey(GrapplingHook.getPlugin(), "uses"), PersistentDataType.INTEGER, newUses);
					hook.setItemMeta(im);

					return true;
				}
			} catch (Exception e) {
				GrapplingHook.getPlugin().getLogger().warning("Error updating hook uses: " + e.getMessage());
				e.printStackTrace();
				return false;
			}
		} catch (Exception e) {
			GrapplingHook.getPlugin().getLogger().warning("General error in addUse: " + e.getMessage());
			e.printStackTrace();
			return false;
		}
	}
	
	public static void playGrappleSound(Location loc) {
		try {
			loc.getWorld().playSound(loc, Sound.ENTITY_BAT_TAKEOFF, 10f, 1f);
		} catch (Exception e) {
			GrapplingHook.getPlugin().getLogger().warning("Error playing grapple sound: " + e.getMessage());
		}
	}
	
    private static boolean isInteger(String s) {
	    try { 
	        Integer.parseInt(s); 
	    } catch(NumberFormatException e) { 
	        return false; 
	    }
	    return true;
	}
}
