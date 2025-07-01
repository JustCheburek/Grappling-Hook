package com.snowgears.grapplinghook;

import com.snowgears.grapplinghook.api.HookAPI;
import com.snowgears.grapplinghook.utils.HookSettings;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;


public class GrapplingListener implements Listener{

	private GrapplingHook plugin;

	private HashMap<String, HookSettings> hookSettings = new HashMap<>();

	private HashMap<Integer, Integer> noFallEntities = new HashMap<>(); //entity id, delayed task id
	private HashMap<UUID, Integer> noGrapplePlayers = new HashMap<>(); //uuid, delayed task id
	private HashMap<UUID, FishHook> activeHookEntities = new HashMap<>(); //player uuid, ref to hook entity
	private HashMap<UUID, Location> hookLastLocation = new HashMap<>(); //player uuid, location of hook entity
	private HashSet<UUID> playersConsumedSlowfall = new HashSet<>();

	public GrapplingListener(GrapplingHook instance) {
		plugin = instance;
	}
	
	@EventHandler
	public void onPreCraft(CraftItemEvent event){
		if(plugin.usePerms() == false)
			return;
		if(event.getView().getPlayer() instanceof Player){
			Player player = (Player)event.getView().getPlayer();
			if(player.isOp())
				return;
			if(HookAPI.isGrapplingHook(event.getInventory().getResult())){
				ItemStack resultHook = event.getInventory().getResult();
				String id = HookAPI.getHookID(resultHook);
				if(!player.hasPermission("grapplinghook.craft."+id)) {
					event.setCancelled(true);
					return;
				}
			}
		}
	}

    @EventHandler
    public void onHookHitEntity(ProjectileHitEvent event) {

		if (event.getEntity() instanceof FishHook) {
			FishHook hook = (FishHook) event.getEntity();
			if (!(hook.getShooter() instanceof Player))
				return;
			Player player = (Player) hook.getShooter();

			if (HookAPI.isGrapplingHook(player.getInventory().getItemInMainHand()) == false)
				return;

			if (event.getHitEntity() == null)
				return;

			if (HookAPI.canHookEntityType(player, event.getHitEntity().getType())) {

				if (event.getHitEntity().getType() == EntityType.PLAYER) {
					if (plugin.usePerms() == false || player.hasPermission("grapplinghook.pull.players")) {

						if (event.getHitEntity() instanceof Player) {
							Player hooked = (Player) event.getHitEntity();
							if (hooked.hasPermission("grapplinghook.player.nopull")) {
								//event.setCancelled(true);
								final ItemStack curItemInHand = player.getInventory().getItemInMainHand().clone();
								player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
								//run task 2 ticks later
								Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
									player.getInventory().setItemInMainHand(curItemInHand);
								}, 2);
							} else {
								//hooked.sendMessage(ChatColor.YELLOW+"You have been hooked by "+ ChatColor.RESET+player.getName()+ChatColor.YELLOW+"!");
								//player.sendMessage(ChatColor.GOLD+"You have hooked "+ChatColor.RESET+hooked.getName()+ChatColor.YELLOW+"!");
							}
						} else {
							//String entityName = event.getHitEntity().getType().toString().replace("_", " ").toLowerCase();
							//player.sendMessage(ChatColor.GOLD+"You have hooked a "+entityName+"!");
						}
					} else {
						final ItemStack curItemInHand = player.getInventory().getItemInMainHand().clone();
						player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
						//run task 2 ticks later
						Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
							player.getInventory().setItemInMainHand(curItemInHand);
						}, 2);

						return;
					}
				}
			}
			else{
				event.setCancelled(true);
			}
		}
	}
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamageEvent(EntityDamageEvent event) {
        if (event.getCause() == DamageCause.FALL) {
        	if(plugin.getTeleportHooked()){
        		return;
        	}
        	if(noFallEntities.containsKey(event.getEntity().getEntityId()))
        		event.setCancelled(true);
        }
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void onGrapple(PlayerGrappleEvent event){
    	if(event.isCancelled())
    		return;
    	final Player player = event.getPlayer();
    	
    	// Устанавливаем максимальную прочность удочки (бесконечная)
    	event.getHookItem().setDurability((short)0);
    	// Устанавливаем неразрушимость предмета
    	ItemStack hookItem = event.getHookItem();
    	if (hookItem.getItemMeta() != null) {
    	    org.bukkit.inventory.meta.ItemMeta meta = hookItem.getItemMeta();
    	    meta.setUnbreakable(true);
    	    // Скрываем информацию о рецептах и другие атрибуты
    	    meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_UNBREAKABLE);
    	    meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ATTRIBUTES);
    	    meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
    	    meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_POTION_EFFECTS);
    	    hookItem.setItemMeta(meta);
    	}

		if(activeHookEntities.containsKey(player.getUniqueId())){
			activeHookEntities.remove(player.getUniqueId());
		}

    	if(noGrapplePlayers.containsKey(player.getUniqueId())){
    		if((plugin.usePerms() && !player.hasPermission("grapplinghook.player.nocooldown")) || (!plugin.usePerms() && !player.isOp())){
    			player.sendMessage(plugin.getMessageManager().getHookMessage("cooldown"));
    			return;
    		}
    	}
    	
    	Entity e = event.getPulledEntity();
    	Location loc = event.getPullLocation();
    	
    	if(player.equals(e)){ //the player is pulling themself to a location
	    	if(plugin.getTeleportHooked()){
	    		loc.setPitch(player.getLocation().getPitch());
	    		loc.setYaw(player.getLocation().getYaw());
	        	player.teleport(loc);
	    	}
	    	else{
	    		if(player.getLocation().distance(loc) < 6) //hook is too close to player
	    			pullPlayerSlightly(player, loc);
	    		else
					pullEntityToLocation(player, loc, HookAPI.getHookInHandVelocityPull(player));
	    	}
    	}
    	else{ //the player is pulling an entity to them
    		if(plugin.getTeleportHooked()) {
				e.teleport(loc);
			}
	    	else{
				pullEntityToLocation(e, loc,  HookAPI.getHookInHandVelocityPull(player));

//	    		if(e instanceof Item){
//	    			ItemStack is = ((Item)e).getItemStack();
//	    			String itemName = is.getType().toString().replace("_", " ").toLowerCase();
//	    			player.sendMessage(ChatColor.GOLD+"You have hooked a stack of "+is.getAmount()+" "+itemName+"!");
//	    		}
	    	}
    	}

    	if(HookAPI.addUse(player, event.getHookItem())) {
			HookAPI.playGrappleSound(player.getLocation());
			ItemStack curItemInHand = event.getHookItem().clone();

			int timeBetweenGrapples = HookAPI.getHookInHandTimeBetweenGrapples(player);
			if(timeBetweenGrapples > 0){
				HookAPI.addPlayerCoolDown(player, timeBetweenGrapples);
			}

			player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));

			//run task 2 ticks later
			Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
				player.getInventory().setItemInMainHand(curItemInHand);
			}, 2);
		}
    }

    @EventHandler
	public void onLineBreak(FishingLineBreakEvent event){
		//event.getPlayer().sendMessage("FishingLineBreakEvent called.");
		Player player = event.getPlayer();
		player.getWorld().playSound(player.getLocation(), Sound.BLOCK_TRIPWIRE_DETACH, 1f, 1f);
		//stop falling when line snaps
		if(event.getHookLocation().getY() > player.getLocation().getY()){
			player.setVelocity(player.getVelocity().setY(0));
		}
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event){
		if(activeHookEntities.containsKey(event.getPlayer().getUniqueId())){
			FishHook hook = activeHookEntities.get(event.getPlayer().getUniqueId());
			if(hook != null && !hook.isDead()){
				Block belowHook = hook.getLocation().getBlock().getRelative(BlockFace.DOWN);
				if(belowHook.isPassable())
					return;
			}
			else
				return;

			if(playersConsumedSlowfall.contains(event.getPlayer().getUniqueId())){
				Block belowPlayer = event.getPlayer().getLocation().getBlock().getRelative(BlockFace.DOWN);
				if(!belowPlayer.isPassable()){
					if(HookAPI.isGrapplingHook(event.getPlayer().getInventory().getItemInMainHand())) {
						HookAPI.addUse(event.getPlayer(), event.getPlayer().getInventory().getItemInMainHand());
						playersConsumedSlowfall.remove(event.getPlayer().getUniqueId());
					}
				}
			}

			Location hookLoc = hookLastLocation.get(event.getPlayer().getUniqueId());
			if(hookLoc != null && hookLoc.getY() > event.getPlayer().getLocation().getY()) {
				if(HookAPI.isGrapplingHook(event.getPlayer().getInventory().getItemInMainHand())) {
					if(HookAPI.getHookInHandHasSlowFall(event.getPlayer())) {
						event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 3, 1));
						if(plugin.isConsumeUseOnSlowfall()) {
							Block belowPlayer = event.getPlayer().getLocation().getBlock().getRelative(BlockFace.DOWN);
							if(belowPlayer.isPassable()) {
								playersConsumedSlowfall.add(event.getPlayer().getUniqueId());
							}
						}
					}
				}
			}
		}
	}

	@EventHandler
	public void hookStuck(ProjectileHitEvent event) {
		if (event.getEntity() instanceof FishHook && event.getEntity().getShooter() instanceof Player) {
			if(activeHookEntities.containsKey(((Player) event.getEntity().getShooter()).getUniqueId())) {
				FishHook fishHook = (FishHook) event.getEntity();
				Player player = (Player) fishHook.getShooter();

				if(!HookAPI.getHookInHandHasStickyHook(player))
					return;

				if (event.getHitBlock() != null && !event.getHitBlock().getLocation().getBlock().isEmpty()) {
					Location hitblock = event.getHitBlock().getLocation().add(0.5, 0, 0.5);

					//only secure the sticky hook to the block if that block type can be hit by this hook
					if(HookAPI.canHookMaterial(player, hitblock.getBlock().getType())) {
						ArmorStand armorStand = player.getWorld().spawn(hitblock, ArmorStand.class);
						// Делаем стойку полностью невидимой
						armorStand.setVisible(false);
						armorStand.setInvisible(true); // Добавляем эффект невидимости
						armorStand.setSmall(true);
						armorStand.setArms(false);
						armorStand.setMarker(true);
						armorStand.setBasePlate(false);
						armorStand.setGravity(false);
						armorStand.setSilent(true); // Убираем звуки
						armorStand.setCustomNameVisible(false);
						armorStand.addPassenger(fishHook);
						
						fishHook.setGravity(false);
						fishHook.setBounce(true);
						fishHook.setMetadata("stuckBlock", new FixedMetadataValue(plugin, ""));
					}
				}
			}
		}
	}
    
    @EventHandler (priority = EventPriority.HIGHEST)
    public void fishEvent(PlayerFishEvent event) //called before projectileLaunchEvent
    {
    	try {
			if(event.getState() == PlayerFishEvent.State.IN_GROUND || event.getState() == PlayerFishEvent.State.CAUGHT_ENTITY){
				Player player = event.getPlayer();

				// Проверка на null и корректные типы предметов
				ItemStack mainHand = player.getInventory().getItemInMainHand();
				if (mainHand == null || mainHand.getType() != Material.FISHING_ROD) {
					return;
				}

				// Проверяем, является ли предмет крюком
				if(!HookAPI.isGrapplingHook(mainHand)) {
					return;
				}

				// Проверяем разрешения только если они включены
				if(plugin.usePerms() && !player.hasPermission("grapplinghook.pull.self")) {
					return;
				}

				// Получаем местоположение крюка
				Location hookLoc = null;
				try {
					// Получаем сущность крюка
					FishHook hookEntity = event.getHook();
					if (hookEntity == null) {
						plugin.getLogger().warning("Hook entity was null in fishEvent");
						return;
					}

					// Сохраняем ссылку на сущность крюка для отслеживания
					if(!activeHookEntities.containsKey(player.getUniqueId())){
						activeHookEntities.put(player.getUniqueId(), hookEntity);
					}

					hookLoc = hookEntity.getLocation();
					if (hookLoc == null) {
						plugin.getLogger().warning("Hook location was null in fishEvent");
						return;
					}
				} catch (Exception e) {
					plugin.getLogger().warning("Error getting hook entity or location: " + e.getMessage());
					return;
				}

				// Используем сохраненное местоположение крюка, если оно доступно
				if(hookLastLocation.containsKey(player.getUniqueId())) {
					hookLoc = hookLastLocation.get(player.getUniqueId());
					hookLastLocation.remove(player.getUniqueId());
				}

				if (hookLoc == null) {
					return;
				}

				// Корректируем местоположение крюка
				hookLoc.add(0, 0.2, 0); // Убедимся, что игрок приземлится на верхнюю часть блока

				if(!event.getState().equals(PlayerFishEvent.State.CAUGHT_ENTITY)){
					Block hookBlock = hookLoc.getBlock();
					if(hookBlock == null) {
						return;
					}

					// Проверяем только для воздуха и воды
					if(hookBlock.getType() == Material.AIR || hookBlock.getType() == Material.WATER) {
						// Проверяем блок под крюком
						Block blockBelow = hookBlock.getRelative(BlockFace.DOWN);
						if(blockBelow.getType() != Material.AIR && 
						   blockBelow.getType() != Material.WATER && 
						   blockBelow.getType() != Material.LAVA) {
							// Если под крюком есть твердый блок, опускаем местоположение
							hookLoc.add(0, -1, 0);
						} else {
							// Если крюк в воздухе и нет твердого блока под ним, отменяем
							if(hookBlock.getType() == Material.AIR) {
								return;
							}
						}
					}

					// Если над блоком есть воздух, поднимаем местоположение
					if(hookBlock.getRelative(BlockFace.UP).getType() == Material.AIR) {
						hookLoc.add(0, 1, 0);
					}
				}

				// Обрабатываем пойманную сущность
				Entity caughtEntity = null;
				try {
					caughtEntity = event.getCaught();
				} catch (Exception e) {
					// Игнорируем ошибки, если сущность не поймана
				}

				if(caughtEntity != null) {
					// Проверяем, можно ли зацепить этот тип сущности
					if (!HookAPI.canHookEntityType(player, caughtEntity.getType())) {
						player.sendMessage(plugin.getMessageManager().getHookMessage("cannot_hook_entity"));
						return; 
					}

					// Проверяем особые случаи для игроков
					if(caughtEntity.getType() == EntityType.PLAYER) {
						if(caughtEntity instanceof Player) {
							Player hooked = (Player)caughtEntity;
							if(hooked.hasPermission("grapplinghook.player.nopull")) {
								player.sendMessage(plugin.getMessageManager().getHookMessage("cannot_hook_player"));
								return;
							}
						}
					}

					// Создаем событие для притягивания сущности
					if(!plugin.usePerms() || player.hasPermission("grapplinghook.pull.mobs")) {
						hookLoc = player.getLocation();
						PlayerGrappleEvent grappleEvent = new PlayerGrappleEvent(player, mainHand, caughtEntity, hookLoc);
						Bukkit.getServer().getPluginManager().callEvent(grappleEvent);
					}
				} else {
					// Создаем событие для притягивания игрока
					PlayerGrappleEvent grappleEvent = new PlayerGrappleEvent(player, mainHand, player, hookLoc);
					Bukkit.getServer().getPluginManager().callEvent(grappleEvent);
				}
			}
		} catch (Exception e) {
			plugin.getLogger().severe("Ошибка при обработке события крюка: " + e.getMessage());
			e.printStackTrace();
		}
    }

//	//FOR HOOKING AN ITEM AND PULLING TOWARD YOU
//	public void pullItemToLocation(Item i, Location loc){
//		ItemStack is = i.getItemStack();
//		i.getWorld().dropItemNaturally(loc, is);
//		i.remove();
//	}
//
//	//FOR HOOKING AN ITEM AND PULLING TOWARD YOU
//	public void pullItemToLocation(Entity e, Location loc){
//		Location oLoc = e.getLocation().add(0, 1, 0);
//		Location pLoc = loc;
//
//		// Velocity from Minecraft Source.
//		double d1 = pLoc.getX() - oLoc.getX();
//		double d3 = pLoc.getY() - oLoc.getY();
//		double d5 = pLoc.getZ() - oLoc.getZ();
//		double d7 = ((float) Math
//				.sqrt((d1 * d1 + d3 * d3 + d5 * d5)));
//		double d9 = 0.10000000000000001D;
//		double motionX = d1 * d9;
//		double motionY = d3 * d9 + (double) ((float) Math.sqrt(d7))
//				* 0.080000000000000002D;
//		double motionZ = d5 * d9;
//		e.setVelocity(new Vector(motionX, motionY, motionZ));
//	}

//	//FOR HOOKING AN ENTITY AND PULLING TOWARD YOU
//	private void pullEntityToLocation(Entity e, Location loc){
//		Location entityLoc = e.getLocation();
//
//		double dX = entityLoc.getX() - loc.getX();
//		double dY = entityLoc.getY() - loc.getY();
//		double dZ = entityLoc.getZ() - loc.getZ();
//
//		double yaw = Math.atan2(dZ, dX);
//		double pitch = Math.atan2(Math.sqrt(dZ * dZ + dX * dX), dY) + Math.PI;
//
//		double X = Math.sin(pitch) * Math.cos(yaw);
//		double Y = Math.sin(pitch) * Math.sin(yaw);
//		double Z = Math.cos(pitch);
//
//		Vector vector = new Vector(X, Z, Y);
//		e.setVelocity(vector.multiply(8));
//	}

	//For pulling a player slightly
	private void pullPlayerSlightly(Player p, Location loc){
		if(loc.getY() > p.getLocation().getY()){
			p.setVelocity(new Vector(0,0.25,0));
			return;
		}
		
		Location playerLoc = p.getLocation();
		
		Vector vector = loc.toVector().subtract(playerLoc.toVector());
		p.setVelocity(vector);
	}
	
	//better method for pulling
	private void pullEntityToLocation(final Entity e, Location loc, double multiply){
		Location entityLoc = e.getLocation();
		
		Vector boost = e.getVelocity();
		boost.setY(0.3);
		e.setVelocity(boost);
		
		Bukkit.getScheduler().scheduleSyncDelayedTask(GrapplingHook.getPlugin(), () -> {
			double g = -0.08;
			double d = loc.distance(entityLoc);
			double t = d;
			double v_x = (1.0+0.07*t) * (loc.getX()-entityLoc.getX())/t;
			double v_y = (1.0+0.03*t) * (loc.getY()-entityLoc.getY())/t -0.5*g*t;
			double v_z = (1.0+0.07*t) * (loc.getZ()-entityLoc.getZ())/t;

			Vector v = e.getVelocity();
			v.setX(v_x);
			v.setY(v_y);
			v.setZ(v_z);
			v.multiply(multiply);
			e.setVelocity(v);
		}, 1L);

		if(e instanceof Player){
			Player player = (Player)e;
			if(HookAPI.isGrapplingHook(player.getInventory().getItemInMainHand())){
				boolean fallDamage = HookAPI.getHookInHandHasFallDamage(player);
				if(!fallDamage){
					addNoFall(player, 100);
				}
			}
		}
		else {
			addNoFall(e, 100);
		}
	}
	
	public void addNoFall(final Entity e, int ticks) {
		if(noFallEntities.containsKey(e.getEntityId()))
			Bukkit.getServer().getScheduler().cancelTask(noFallEntities.get(e.getEntityId()));
		
		int taskId = plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin,new Runnable() {
			 @Override
			public void run(){
				  if(noFallEntities.containsKey(e.getEntityId()))
					 noFallEntities.remove(e.getEntityId());
			  }
	  	}, ticks);
		
		noFallEntities.put(e.getEntityId(), taskId);
	}

	public void removePlayerCoolDown(Player player){
		if(noGrapplePlayers.containsKey(player.getUniqueId()))
			noGrapplePlayers.remove(player.getUniqueId());
	}

	public boolean isPlayerOnCoolDown(Player player) {
		return noGrapplePlayers.containsKey(player.getUniqueId());
	}

	public void addPlayerCoolDown(final Player player, int seconds) {
		if(noGrapplePlayers.containsKey(player.getUniqueId()))
			plugin.getServer().getScheduler().cancelTask(noGrapplePlayers.get(player.getUniqueId()));

		int taskId = plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin,new Runnable() {
			public void run(){
				removePlayerCoolDown(player);
			}
		}, (seconds*20));

		noGrapplePlayers.put(player.getUniqueId(), taskId);
	}

	public HookSettings getHookSettings(String id){
		if(hookSettings.containsKey(id))
			return hookSettings.get(id);
		return null;
	}

	public void addHookSettings(String id, HookSettings hookSettings){
		this.hookSettings.put(id, hookSettings);
	}

	public List<String> getHookIDs(){
		ArrayList<String> hookIDs = new ArrayList<>();
		for(String id : hookSettings.keySet()){
			hookIDs.add(id);
		}
		Collections.sort(hookIDs);
		return hookIDs;
	}
}
