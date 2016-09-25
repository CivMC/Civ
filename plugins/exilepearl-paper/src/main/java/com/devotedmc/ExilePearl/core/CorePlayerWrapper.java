package com.devotedmc.ExilePearl.core;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Achievement;
import org.bukkit.Effect;
import org.bukkit.EntityEffect;
import org.bukkit.GameMode;
import org.bukkit.Instrument;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Note;
import org.bukkit.Particle;
import org.bukkit.Server;
import org.bukkit.Sound;
import org.bukkit.Statistic;
import org.bukkit.WeatherType;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Villager;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.InventoryView.Property;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MainHand;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.map.MapView;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.util.Vector;

import com.devotedmc.ExilePearl.PlayerNameProvider;
import com.devotedmc.ExilePearl.util.Guard;

@SuppressWarnings("deprecation")
public class CorePlayerWrapper implements Player {
	
	private final Player player;
	private final PlayerNameProvider nameProvider;
	
	public CorePlayerWrapper(final Player player, final PlayerNameProvider nameProvider) {
		Guard.ArgumentNotNull(player, "player");
		Guard.ArgumentNotNull(nameProvider, "nameProvider");
		
		this.player = player;
		this.nameProvider = nameProvider;
	}

	@Override
	public void awardAchievement(Achievement arg0) {
		player.awardAchievement(arg0);
	}

	@Override
	public boolean canSee(Player arg0) {
		return player.canSee(arg0);
	}

	@Override
	public void chat(String arg0) {
		player.chat(arg0);
	}

	@Override
	public void decrementStatistic(Statistic arg0) throws IllegalArgumentException {
		player.decrementStatistic(arg0);
	}

	@Override
	public void decrementStatistic(Statistic arg0, int arg1) throws IllegalArgumentException {
		player.decrementStatistic(arg0, arg1);
	}

	@Override
	public void decrementStatistic(Statistic arg0, Material arg1) throws IllegalArgumentException {
		player.decrementStatistic(arg0, arg1);
	}

	@Override
	public void decrementStatistic(Statistic arg0, EntityType arg1) throws IllegalArgumentException {
		player.decrementStatistic(arg0, arg1);
	}

	@Override
	public void decrementStatistic(Statistic arg0, Material arg1, int arg2) throws IllegalArgumentException {
		player.decrementStatistic(arg0, arg1, arg2);
	}

	@Override
	public void decrementStatistic(Statistic arg0, EntityType arg1, int arg2) {
		player.decrementStatistic(arg0, arg1, arg2);
	}

	@Override
	public InetSocketAddress getAddress() {
		return player.getAddress();
	}

	@Override
	public boolean getAllowFlight() {
		return player.getAllowFlight();
	}

	@Override
	public Location getBedSpawnLocation() {
		return player.getBedSpawnLocation();
	}

	@Override
	public Location getCompassTarget() {
		return player.getCompassTarget();
	}

	@Override
	public String getDisplayName() {
		return player.getDisplayName();
	}

	@Override
	public float getExhaustion() {
		return player.getExhaustion();
	}

	@Override
	public float getExp() {
		return player.getExp();
	}

	@Override
	public float getFlySpeed() {
		return player.getFlySpeed();
	}

	@Override
	public int getFoodLevel() {
		return player.getFoodLevel();
	}

	@Override
	public double getHealthScale() {
		return player.getHealthScale();
	}

	@Override
	public int getLevel() {
		return player.getLevel();
	}

	@Override
	public String getPlayerListName() {
		return player.getPlayerListName();
	}

	@Override
	public long getPlayerTime() {
		return player.getPlayerTime();
	}

	@Override
	public long getPlayerTimeOffset() {
		return player.getPlayerTimeOffset();
	}

	@Override
	public WeatherType getPlayerWeather() {
		return player.getPlayerWeather();
	}

	@Override
	public float getSaturation() {
		return player.getSaturation();
	}

	@Override
	public Scoreboard getScoreboard() {
		return player.getScoreboard();
	}

	@Override
	public Entity getSpectatorTarget() {
		return player.getSpectatorTarget();
	}

	@Override
	public int getStatistic(Statistic arg0) throws IllegalArgumentException {
		return player.getStatistic(arg0);
	}

	@Override
	public int getStatistic(Statistic arg0, Material arg1) throws IllegalArgumentException {
		return player.getStatistic(arg0, arg1);
	}

	@Override
	public int getStatistic(Statistic arg0, EntityType arg1) throws IllegalArgumentException {
		return player.getStatistic(arg0, arg1);
	}

	@Override
	public int getTotalExperience() {
		return player.getTotalExperience();
	}

	@Override
	public float getWalkSpeed() {
		return player.getWalkSpeed();
	}

	@Override
	public void giveExp(int arg0) {
		player.giveExp(arg0);
	}

	@Override
	public void giveExpLevels(int arg0) {
		player.giveExpLevels(arg0);
	}

	@Override
	public boolean hasAchievement(Achievement arg0) {
		return player.hasAchievement(arg0);
	}

	@Override
	public void hidePlayer(Player arg0) {
		player.hidePlayer(arg0);
	}

	@Override
	public void incrementStatistic(Statistic arg0) throws IllegalArgumentException {
		player.incrementStatistic(arg0);
	}

	@Override
	public void incrementStatistic(Statistic arg0, int arg1) throws IllegalArgumentException {
		player.incrementStatistic(arg0, arg1);
	}

	@Override
	public void incrementStatistic(Statistic arg0, Material arg1) throws IllegalArgumentException {
		player.incrementStatistic(arg0, arg1);
	}

	@Override
	public void incrementStatistic(Statistic arg0, EntityType arg1) throws IllegalArgumentException {
		player.incrementStatistic(arg0, arg1);
	}

	@Override
	public void incrementStatistic(Statistic arg0, Material arg1, int arg2) throws IllegalArgumentException {
		player.incrementStatistic(arg0, arg1, arg2);
	}

	@Override
	public void incrementStatistic(Statistic arg0, EntityType arg1, int arg2) throws IllegalArgumentException {
		player.incrementStatistic(arg0, arg1, arg2);
	}

	@Override
	public boolean isFlying() {
		return player.isFlying();
	}

	@Override
	public boolean isHealthScaled() {
		return player.isHealthScaled();
	}

	@Override
	public boolean isOnGround() {
		return player.isOnGround();
	}

	@Override
	public boolean isPlayerTimeRelative() {
		return player.isPlayerTimeRelative();
	}

	@Override
	public boolean isSleepingIgnored() {
		return player.isSleepingIgnored();
	}

	@Override
	public boolean isSneaking() {
		return player.isSneaking();
	}

	@Override
	public boolean isSprinting() {
		return player.isSprinting();
	}

	@Override
	public void kickPlayer(String arg0) {
		player.kickPlayer(arg0);
	}

	@Override
	public void loadData() {
		player.loadData();
	}

	@Override
	public boolean performCommand(String arg0) {
		return player.performCommand(arg0);
	}

	@Override
	public void playEffect(Location arg0, Effect arg1, int arg2) {
		player.playEffect(arg0, arg1, arg2);
	}

	@Override
	public <T> void playEffect(Location arg0, Effect arg1, T arg2) {
		player.playEffect(arg0, arg1, arg2);
	}

	@Override
	public void playNote(Location arg0, byte arg1, byte arg2) {
		player.playNote(arg0, arg1, arg2);
	}

	@Override
	public void playNote(Location arg0, Instrument arg1, Note arg2) {
		player.playNote(arg0, arg1, arg2);
	}

	@Override
	public void playSound(Location arg0, Sound arg1, float arg2, float arg3) {
		player.playSound(arg0, arg1, arg2, arg3);
	}

	@Override
	public void playSound(Location arg0, String arg1, float arg2, float arg3) {
		player.playSound(arg0, arg1, arg2, arg3);		
	}

	@Override
	public void removeAchievement(Achievement arg0) {
		player.removeAchievement(arg0);
	}

	@Override
	public void resetPlayerTime() {
		player.resetPlayerTime();
	}

	@Override
	public void resetPlayerWeather() {
		player.resetPlayerWeather();
	}

	@Override
	public void resetTitle() {
		player.resetTitle();
	}

	@Override
	public void saveData() {
		player.saveData();
	}

	@Override
	public void sendBlockChange(Location arg0, Material arg1, byte arg2) {
		player.sendBlockChange(arg0, arg1, arg2);
	}

	@Override
	public void sendBlockChange(Location arg0, int arg1, byte arg2) {
		player.sendBlockChange(arg0, arg1, arg2);
	}

	@Override
	public boolean sendChunkChange(Location arg0, int arg1, int arg2, int arg3, byte[] arg4) {
		return player.sendChunkChange(arg0, arg1, arg2, arg3, arg4);
	}

	@Override
	public void sendMap(MapView arg0) {
		player.sendMap(arg0);
	}

	@Override
	public void sendRawMessage(String arg0) {
		player.sendRawMessage(arg0);
	}

	@Override
	public void sendSignChange(Location arg0, String[] arg1) throws IllegalArgumentException {
		player.sendSignChange(arg0, arg1);
	}

	@Override
	public void sendTitle(String arg0, String arg1) {
		player.sendTitle(arg0, arg1);
	}

	@Override
	public void setAllowFlight(boolean arg0) {
		player.setAllowFlight(arg0);
	}

	@Override
	public void setBedSpawnLocation(Location arg0) {
		player.setBedSpawnLocation(arg0);
	}

	@Override
	public void setBedSpawnLocation(Location arg0, boolean arg1) {
		player.setBedSpawnLocation(arg0, arg1);
	}

	@Override
	public void setCompassTarget(Location arg0) {
		player.setCompassTarget(arg0);
	}

	@Override
	public void setDisplayName(String arg0) {
		player.setDisplayName(arg0);
	}

	@Override
	public void setExhaustion(float arg0) {
		player.setExhaustion(arg0);
	}

	@Override
	public void setExp(float arg0) {
		player.setExp(arg0);
	}

	@Override
	public void setFlySpeed(float arg0) throws IllegalArgumentException {
		player.setFlySpeed(arg0);
	}

	@Override
	public void setFlying(boolean arg0) {
		player.setFlying(arg0);
	}

	@Override
	public void setFoodLevel(int arg0) {
		player.setFoodLevel(arg0);
	}

	@Override
	public void setHealthScale(double arg0) throws IllegalArgumentException {
		player.setHealthScale(arg0);
	}

	@Override
	public void setHealthScaled(boolean arg0) {
		player.setHealthScaled(arg0);
	}

	@Override
	public void setLevel(int arg0) {
		player.setLevel(arg0);
	}

	@Override
	public void setPlayerListName(String arg0) {
		player.setPlayerListName(arg0);
	}

	@Override
	public void setPlayerTime(long arg0, boolean arg1) {
		player.setPlayerTime(arg0, arg1);
	}

	@Override
	public void setPlayerWeather(WeatherType arg0) {
		player.setPlayerWeather(arg0);
	}

	@Override
	public void setResourcePack(String arg0) {
		player.setResourcePack(arg0);
	}

	@Override
	public void setSaturation(float arg0) {
		player.setSaturation(arg0);
	}

	@Override
	public void setScoreboard(Scoreboard arg0) throws IllegalArgumentException, IllegalStateException {
		player.setScoreboard(arg0);
	}

	@Override
	public void setSleepingIgnored(boolean arg0) {
		player.setSleepingIgnored(arg0);
	}

	@Override
	public void setSneaking(boolean arg0) {
		player.setSneaking(arg0);
	}

	@Override
	public void setSpectatorTarget(Entity arg0) {
		player.setSpectatorTarget(arg0);
	}

	@Override
	public void setSprinting(boolean arg0) {
		player.setSprinting(arg0);
	}

	@Override
	public void setStatistic(Statistic arg0, int arg1) throws IllegalArgumentException {
		player.setStatistic(arg0, arg1);
	}

	@Override
	public void setStatistic(Statistic arg0, Material arg1, int arg2) throws IllegalArgumentException {
		player.setStatistic(arg0, arg1, arg2);
	}

	@Override
	public void setStatistic(Statistic arg0, EntityType arg1, int arg2) {
		player.setStatistic(arg0, arg1, arg2);
	}

	@Override
	public void setTexturePack(String arg0) {
		player.setTexturePack(arg0);
	}

	@Override
	public void setTotalExperience(int arg0) {
		player.setTotalExperience(arg0);
	}

	@Override
	public void setWalkSpeed(float arg0) throws IllegalArgumentException {
		player.setWalkSpeed(arg0);
	}

	@Override
	public void showPlayer(Player arg0) {
		player.showPlayer(arg0);
	}

	@Override
	public void spawnParticle(Particle arg0, Location arg1, int arg2) {
		player.spawnParticle(arg0, arg1, arg2);
	}

	@Override
	public <T> void spawnParticle(Particle arg0, Location arg1, int arg2, T arg3) {
		player.spawnParticle(arg0, arg1, arg2, arg3);
	}

	@Override
	public void spawnParticle(Particle arg0, double arg1, double arg2, double arg3, int arg4) {
		player.spawnParticle(arg0, arg1, arg2, arg3, arg4);
	}

	@Override
	public <T> void spawnParticle(Particle arg0, double arg1, double arg2, double arg3, int arg4, T arg5) {
		player.spawnParticle(arg0, arg1, arg2, arg3, arg4, arg5);
	}

	@Override
	public void spawnParticle(Particle arg0, Location arg1, int arg2, double arg3, double arg4, double arg5) {
		player.spawnParticle(arg0, arg1, arg2, arg3, arg4, arg5);
	}

	@Override
	public <T> void spawnParticle(Particle arg0, Location arg1, int arg2, double arg3, double arg4, double arg5,
			T arg6) {
		player.spawnParticle(arg0, arg1, arg2, arg3, arg4, arg5, arg6);
	}

	@Override
	public void spawnParticle(Particle arg0, Location arg1, int arg2, double arg3, double arg4, double arg5,
			double arg6) {
		player.spawnParticle(arg0, arg1, arg2, arg3, arg4, arg5, arg6);
	}

	@Override
	public void spawnParticle(Particle arg0, double arg1, double arg2, double arg3, int arg4, double arg5, double arg6,
			double arg7) {
		player.spawnParticle(arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7);
	}

	@Override
	public <T> void spawnParticle(Particle arg0, Location arg1, int arg2, double arg3, double arg4, double arg5,
			double arg6, T arg7) {
		player.spawnParticle(arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7);
	}

	@Override
	public <T> void spawnParticle(Particle arg0, double arg1, double arg2, double arg3, int arg4, double arg5,
			double arg6, double arg7, T arg8) {
		player.spawnParticle(arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8);
	}

	@Override
	public void spawnParticle(Particle arg0, double arg1, double arg2, double arg3, int arg4, double arg5, double arg6,
			double arg7, double arg8) {
		player.spawnParticle(arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8);
	}

	@Override
	public <T> void spawnParticle(Particle arg0, double arg1, double arg2, double arg3, int arg4, double arg5,
			double arg6, double arg7, double arg8, T arg9) {
		player.spawnParticle(arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9);
	}

	@Override
	public Spigot spigot() {
		return player.spigot();
	}

	@Override
	public void stopSound(Sound arg0) {
		player.stopSound(arg0);
	}

	@Override
	public void stopSound(String arg0) {
		player.stopSound(arg0);
	}

	@Override
	public void updateInventory() {
		player.updateInventory();
	}

	@Override
	public void closeInventory() {
		player.updateInventory();
	}

	@Override
	public Inventory getEnderChest() {
		return player.getEnderChest();
	}

	@Override
	public int getExpToLevel() {
		return player.getExpToLevel();
	}

	@Override
	public GameMode getGameMode() {
		return player.getGameMode();
	}

	@Override
	public PlayerInventory getInventory() {
		return player.getInventory();
	}

	@Override
	public ItemStack getItemInHand() {
		return player.getItemInHand();
	}

	@Override
	public ItemStack getItemOnCursor() {
		return player.getItemOnCursor();
	}

	@Override
	public MainHand getMainHand() {
		return player.getMainHand();
	}

	@Override
	public String getName() {
		return nameProvider.getName(player.getUniqueId());
	}

	@Override
	public InventoryView getOpenInventory() {
		return player.getOpenInventory();
	}

	@Override
	public int getSleepTicks() {
		return player.getSleepTicks();
	}

	@Override
	public boolean isBlocking() {
		return player.isBlocking();
	}

	@Override
	public boolean isSleeping() {
		return player.isSleeping();
	}

	@Override
	public InventoryView openEnchanting(Location arg0, boolean arg1) {
		return player.openEnchanting(arg0, arg1);
	}

	@Override
	public InventoryView openInventory(Inventory arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void openInventory(InventoryView arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public InventoryView openMerchant(Villager arg0, boolean arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public InventoryView openWorkbench(Location arg0, boolean arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setGameMode(GameMode arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setItemInHand(ItemStack arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setItemOnCursor(ItemStack arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean setWindowProperty(Property arg0, int arg1) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int _INVALID_getLastDamage() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void _INVALID_setLastDamage(int arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean addPotionEffect(PotionEffect arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean addPotionEffect(PotionEffect arg0, boolean arg1) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean addPotionEffects(Collection<PotionEffect> arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Collection<PotionEffect> getActivePotionEffects() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean getCanPickupItems() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public EntityEquipment getEquipment() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getEyeHeight() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getEyeHeight(boolean arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Location getEyeLocation() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Player getKiller() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getLastDamage() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List<Block> getLastTwoTargetBlocks(HashSet<Byte> arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Block> getLastTwoTargetBlocks(Set<Material> arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Entity getLeashHolder() throws IllegalStateException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Block> getLineOfSight(HashSet<Byte> arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Block> getLineOfSight(Set<Material> arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getMaximumAir() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getMaximumNoDamageTicks() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getNoDamageTicks() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public PotionEffect getPotionEffect(PotionEffectType arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getRemainingAir() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean getRemoveWhenFarAway() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Block getTargetBlock(HashSet<Byte> arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Block getTargetBlock(Set<Material> arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasAI() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean hasLineOfSight(Entity arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean hasPotionEffect(PotionEffectType arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isCollidable() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isGliding() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isLeashed() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void removePotionEffect(PotionEffectType arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setAI(boolean arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setCanPickupItems(boolean arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setCollidable(boolean arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setGliding(boolean arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setLastDamage(double arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean setLeashHolder(Entity arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setMaximumAir(int arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setMaximumNoDamageTicks(int arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setNoDamageTicks(int arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setRemainingAir(int arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setRemoveWhenFarAway(boolean arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public AttributeInstance getAttribute(Attribute arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean eject() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getCustomName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getEntityId() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public float getFallDistance() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getFireTicks() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public EntityDamageEvent getLastDamageCause() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Location getLocation() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Location getLocation(Location arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getMaxFireTicks() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List<Entity> getNearbyEntities(double arg0, double arg1, double arg2) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Entity getPassenger() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Server getServer() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getTicksLived() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public EntityType getType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UUID getUniqueId() {
		return player.getUniqueId();
	}

	@Override
	public Entity getVehicle() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Vector getVelocity() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public World getWorld() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasGravity() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isCustomNameVisible() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isDead() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isGlowing() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isInsideVehicle() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isInvulnerable() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isSilent() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isValid() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean leaveVehicle() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void playEffect(EntityEffect arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void remove() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setCustomName(String arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setCustomNameVisible(boolean arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setFallDistance(float arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setFireTicks(int arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setGlowing(boolean arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setGravity(boolean arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setInvulnerable(boolean arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setLastDamageCause(EntityDamageEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean setPassenger(Entity arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setSilent(boolean arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setTicksLived(int arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setVelocity(Vector arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean teleport(Location arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean teleport(Entity arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean teleport(Location arg0, TeleportCause arg1) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean teleport(Entity arg0, TeleportCause arg1) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<MetadataValue> getMetadata(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasMetadata(String arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void removeMetadata(String arg0, Plugin arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setMetadata(String arg0, MetadataValue arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sendMessage(String arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sendMessage(String[] arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public PermissionAttachment addAttachment(Plugin arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PermissionAttachment addAttachment(Plugin arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PermissionAttachment addAttachment(Plugin arg0, String arg1, boolean arg2) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PermissionAttachment addAttachment(Plugin arg0, String arg1, boolean arg2, int arg3) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<PermissionAttachmentInfo> getEffectivePermissions() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasPermission(String arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean hasPermission(Permission arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isPermissionSet(String arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isPermissionSet(Permission arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void recalculatePermissions() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeAttachment(PermissionAttachment arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isOp() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setOp(boolean arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void _INVALID_damage(int arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void _INVALID_damage(int arg0, Entity arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int _INVALID_getHealth() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int _INVALID_getMaxHealth() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void _INVALID_setHealth(int arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void _INVALID_setMaxHealth(int arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void damage(double arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void damage(double arg0, Entity arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public double getHealth() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getMaxHealth() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void resetMaxHealth() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setHealth(double arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setMaxHealth(double arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public <T extends Projectile> T launchProjectile(Class<? extends T> arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T extends Projectile> T launchProjectile(Class<? extends T> arg0, Vector arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void abandonConversation(Conversation arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void abandonConversation(Conversation arg0, ConversationAbandonedEvent arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void acceptConversationInput(String arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean beginConversation(Conversation arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isConversing() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public long getFirstPlayed() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getLastPlayed() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Player getPlayer() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasPlayedBefore() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isBanned() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isOnline() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isWhitelisted() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setBanned(boolean arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setWhitelisted(boolean arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Map<String, Object> serialize() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<String> getListeningPluginChannels() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void sendPluginMessage(Plugin arg0, String arg1, byte[] arg2) {
		// TODO Auto-generated method stub
		
	}

}
