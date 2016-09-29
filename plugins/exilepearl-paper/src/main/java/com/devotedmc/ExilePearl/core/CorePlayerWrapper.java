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

/**
 * Wrapper class for a Bukkit Player instance that will fetch the player
 * name from a given player provider instead of the player instance.
 * @author Gordon
 *
 */
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
		return player.openInventory(arg0);
	}

	@Override
	public void openInventory(InventoryView arg0) {
		player.openInventory(arg0);
	}

	@Override
	public InventoryView openMerchant(Villager arg0, boolean arg1) {
		return player.openMerchant(arg0, arg1);
	}

	@Override
	public InventoryView openWorkbench(Location arg0, boolean arg1) {
		return player.openWorkbench(arg0, arg1);
	}

	@Override
	public void setGameMode(GameMode arg0) {
		player.setGameMode(arg0);
	}

	@Override
	public void setItemInHand(ItemStack arg0) {
		player.setItemInHand(arg0);
	}

	@Override
	public void setItemOnCursor(ItemStack arg0) {
		player.setItemOnCursor(arg0);
	}

	@Override
	public boolean setWindowProperty(Property arg0, int arg1) {
		return player.setWindowProperty(arg0, arg1);
	}

	@Override
	public int _INVALID_getLastDamage() {
		return player._INVALID_getLastDamage();
	}

	@Override
	public void _INVALID_setLastDamage(int arg0) {
		player._INVALID_setLastDamage(arg0);
	}

	@Override
	public boolean addPotionEffect(PotionEffect arg0) {
		return player.addPotionEffect(arg0);
	}

	@Override
	public boolean addPotionEffect(PotionEffect arg0, boolean arg1) {
		return player.addPotionEffect(arg0, arg1);
	}

	@Override
	public boolean addPotionEffects(Collection<PotionEffect> arg0) {
		return player.addPotionEffects(arg0);
	}

	@Override
	public Collection<PotionEffect> getActivePotionEffects() {
		return player.getActivePotionEffects();
	}

	@Override
	public boolean getCanPickupItems() {
		return player.getCanPickupItems();
	}

	@Override
	public EntityEquipment getEquipment() {
		return player.getEquipment();
	}

	@Override
	public double getEyeHeight() {
		return player.getEyeHeight();
	}

	@Override
	public double getEyeHeight(boolean arg0) {
		return player.getEyeHeight();
	}

	@Override
	public Location getEyeLocation() {
		return player.getEyeLocation();
	}

	@Override
	public Player getKiller() {
		return player.getKiller();
	}

	@Override
	public double getLastDamage() {
		return player.getLastDamage();
	}

	@Override
	public List<Block> getLastTwoTargetBlocks(HashSet<Byte> arg0, int arg1) {
		return player.getLastTwoTargetBlocks(arg0, arg1);
	}

	@Override
	public List<Block> getLastTwoTargetBlocks(Set<Material> arg0, int arg1) {
		return player.getLastTwoTargetBlocks(arg0, arg1);
	}

	@Override
	public Entity getLeashHolder() throws IllegalStateException {
		return player.getLeashHolder();
	}

	@Override
	public List<Block> getLineOfSight(HashSet<Byte> arg0, int arg1) {
		return player.getLineOfSight(arg0, arg1);
	}

	@Override
	public List<Block> getLineOfSight(Set<Material> arg0, int arg1) {
		return player.getLineOfSight(arg0, arg1);
	}

	@Override
	public int getMaximumAir() {
		return player.getMaximumAir();
	}

	@Override
	public int getMaximumNoDamageTicks() {
		return player.getMaximumNoDamageTicks();
	}

	@Override
	public int getNoDamageTicks() {
		return player.getNoDamageTicks();
	}

	@Override
	public PotionEffect getPotionEffect(PotionEffectType arg0) {
		return player.getPotionEffect(arg0);
	}

	@Override
	public int getRemainingAir() {
		return player.getRemainingAir();
	}

	@Override
	public boolean getRemoveWhenFarAway() {
		return player.getRemoveWhenFarAway();
	}

	@Override
	public Block getTargetBlock(HashSet<Byte> arg0, int arg1) {
		return player.getTargetBlock(arg0, arg1);
	}

	@Override
	public Block getTargetBlock(Set<Material> arg0, int arg1) {
		return player.getTargetBlock(arg0, arg1);
	}

	@Override
	public boolean hasAI() {
		return player.hasAI();
	}

	@Override
	public boolean hasLineOfSight(Entity arg0) {
		return player.hasLineOfSight(arg0);
	}

	@Override
	public boolean hasPotionEffect(PotionEffectType arg0) {
		return player.hasPotionEffect(arg0);
	}

	@Override
	public boolean isCollidable() {
		return player.isCollidable();
	}

	@Override
	public boolean isGliding() {
		return player.isGliding();
	}

	@Override
	public boolean isLeashed() {
		return player.isLeashed();
	}

	@Override
	public void removePotionEffect(PotionEffectType arg0) {
		player.removePotionEffect(arg0);
	}

	@Override
	public void setAI(boolean arg0) {
		player.setAI(arg0);
	}

	@Override
	public void setCanPickupItems(boolean arg0) {
		player.setCanPickupItems(arg0);
	}

	@Override
	public void setCollidable(boolean arg0) {
		player.setCollidable(arg0);
	}

	@Override
	public void setGliding(boolean arg0) {
		player.setGliding(arg0);
	}

	@Override
	public void setLastDamage(double arg0) {
		player.setLastDamage(arg0);
	}

	@Override
	public boolean setLeashHolder(Entity arg0) {
		return player.setLeashHolder(arg0);
	}

	@Override
	public void setMaximumAir(int arg0) {
		player.setMaximumAir(arg0);
	}

	@Override
	public void setMaximumNoDamageTicks(int arg0) {
		player.setMaximumNoDamageTicks(arg0);
	}

	@Override
	public void setNoDamageTicks(int arg0) {
		player.setNoDamageTicks(arg0);
	}

	@Override
	public void setRemainingAir(int arg0) {
		player.setRemainingAir(arg0);
	}

	@Override
	public void setRemoveWhenFarAway(boolean arg0) {
		player.setRemoveWhenFarAway(arg0);
	}

	@Override
	public AttributeInstance getAttribute(Attribute arg0) {
		return player.getAttribute(arg0);
	}

	@Override
	public boolean eject() {
		return player.eject();
	}

	@Override
	public String getCustomName() {
		return player.getCustomName();
	}

	@Override
	public int getEntityId() {
		return player.getEntityId();
	}

	@Override
	public float getFallDistance() {
		return player.getFallDistance();
	}

	@Override
	public int getFireTicks() {
		return player.getFireTicks();
	}

	@Override
	public EntityDamageEvent getLastDamageCause() {
		return player.getLastDamageCause();
	}

	@Override
	public Location getLocation() {
		return player.getLocation();
	}

	@Override
	public Location getLocation(Location arg0) {
		return player.getLocation(arg0);
	}

	@Override
	public int getMaxFireTicks() {
		return player.getMaxFireTicks();
	}

	@Override
	public List<Entity> getNearbyEntities(double arg0, double arg1, double arg2) {
		return player.getNearbyEntities(arg0, arg1, arg2);
	}

	@Override
	public Entity getPassenger() {
		return player.getPassenger();
	}

	@Override
	public Server getServer() {
		return player.getServer();
	}

	@Override
	public int getTicksLived() {
		return player.getTicksLived();
	}

	@Override
	public EntityType getType() {
		return player.getType();
	}

	@Override
	public UUID getUniqueId() {
		return player.getUniqueId();
	}

	@Override
	public Entity getVehicle() {
		return player.getVehicle();
	}

	@Override
	public Vector getVelocity() {
		return player.getVelocity();
	}

	@Override
	public World getWorld() {
		return player.getWorld();
	}

	@Override
	public boolean hasGravity() {
		return player.hasGravity();
	}

	@Override
	public boolean isCustomNameVisible() {
		return player.isCustomNameVisible();
	}

	@Override
	public boolean isDead() {
		return player.isDead();
	}

	@Override
	public boolean isEmpty() {
		return player.isEmpty();
	}

	@Override
	public boolean isGlowing() {
		return player.isGlowing();
	}

	@Override
	public boolean isInsideVehicle() {
		return player.isInsideVehicle();
	}

	@Override
	public boolean isInvulnerable() {
		return player.isInvulnerable();
	}

	@Override
	public boolean isSilent() {
		return player.isSilent();
	}

	@Override
	public boolean isValid() {
		return player.isValid();
	}

	@Override
	public boolean leaveVehicle() {
		return player.leaveVehicle();
	}

	@Override
	public void playEffect(EntityEffect arg0) {
		player.playEffect(arg0);
	}

	@Override
	public void remove() {
		player.remove();
	}

	@Override
	public void setCustomName(String arg0) {
		player.setCustomName(arg0);
	}

	@Override
	public void setCustomNameVisible(boolean arg0) {
		player.setCustomNameVisible(arg0);
	}

	@Override
	public void setFallDistance(float arg0) {
		player.setFallDistance(arg0);
	}

	@Override
	public void setFireTicks(int arg0) {
		player.setFireTicks(arg0);
	}

	@Override
	public void setGlowing(boolean arg0) {
		player.setGlowing(arg0);
	}

	@Override
	public void setGravity(boolean arg0) {
		player.setGravity(arg0);
	}

	@Override
	public void setInvulnerable(boolean arg0) {
		player.setInvulnerable(arg0);
	}

	@Override
	public void setLastDamageCause(EntityDamageEvent arg0) {
		player.setLastDamageCause(arg0);
	}

	@Override
	public boolean setPassenger(Entity arg0) {
		return player.setPassenger(arg0);
	}

	@Override
	public void setSilent(boolean arg0) {
		player.setSilent(arg0);
	}

	@Override
	public void setTicksLived(int arg0) {
		player.setTicksLived(arg0);
	}

	@Override
	public void setVelocity(Vector arg0) {
		player.setVelocity(arg0);
	}

	@Override
	public boolean teleport(Location arg0) {
		return player.teleport(arg0);
	}

	@Override
	public boolean teleport(Entity arg0) {
		return player.teleport(arg0);
	}

	@Override
	public boolean teleport(Location arg0, TeleportCause arg1) {
		return player.teleport(arg0, arg1);
	}

	@Override
	public boolean teleport(Entity arg0, TeleportCause arg1) {
		return player.teleport(arg0, arg1);
	}

	@Override
	public List<MetadataValue> getMetadata(String arg0) {
		return player.getMetadata(arg0);
	}

	@Override
	public boolean hasMetadata(String arg0) {
		return player.hasMetadata(arg0);
	}

	@Override
	public void removeMetadata(String arg0, Plugin arg1) {
		player.removeMetadata(arg0, arg1);
	}

	@Override
	public void setMetadata(String arg0, MetadataValue arg1) {
		player.setMetadata(arg0, arg1);
	}

	@Override
	public void sendMessage(String arg0) {
		player.sendMessage(arg0);
	}

	@Override
	public void sendMessage(String[] arg0) {
		player.sendMessage(arg0);
	}

	@Override
	public PermissionAttachment addAttachment(Plugin arg0) {
		return player.addAttachment(arg0);
	}

	@Override
	public PermissionAttachment addAttachment(Plugin arg0, int arg1) {
		return player.addAttachment(arg0, arg1);
	}

	@Override
	public PermissionAttachment addAttachment(Plugin arg0, String arg1, boolean arg2) {
		return player.addAttachment(arg0, arg1, arg2);
	}

	@Override
	public PermissionAttachment addAttachment(Plugin arg0, String arg1, boolean arg2, int arg3) {
		return player.addAttachment(arg0, arg1, arg2, arg3);
	}

	@Override
	public Set<PermissionAttachmentInfo> getEffectivePermissions() {
		return player.getEffectivePermissions();
	}

	@Override
	public boolean hasPermission(String arg0) {
		return player.hasPermission(arg0);
	}

	@Override
	public boolean hasPermission(Permission arg0) {
		return player.hasPermission(arg0);
	}

	@Override
	public boolean isPermissionSet(String arg0) {
		return player.isPermissionSet(arg0);
	}

	@Override
	public boolean isPermissionSet(Permission arg0) {
		return player.isPermissionSet(arg0);
	}

	@Override
	public void recalculatePermissions() {
		player.recalculatePermissions();
	}

	@Override
	public void removeAttachment(PermissionAttachment arg0) {
		player.removeAttachment(arg0);
	}

	@Override
	public boolean isOp() {
		return player.isOp();
	}

	@Override
	public void setOp(boolean arg0) {
		player.setOp(arg0);
	}

	@Override
	public void _INVALID_damage(int arg0) {
		player._INVALID_damage(arg0);
	}

	@Override
	public void _INVALID_damage(int arg0, Entity arg1) {
		player._INVALID_damage(arg0, arg1);
	}

	@Override
	public int _INVALID_getHealth() {
		return player._INVALID_getHealth();
	}

	@Override
	public int _INVALID_getMaxHealth() {
		return player._INVALID_getMaxHealth();
	}

	@Override
	public void _INVALID_setHealth(int arg0) {
		player._INVALID_setHealth(arg0);
	}

	@Override
	public void _INVALID_setMaxHealth(int arg0) {
		player._INVALID_setMaxHealth(arg0);
	}

	@Override
	public void damage(double arg0) {
		player.damage(arg0);
	}

	@Override
	public void damage(double arg0, Entity arg1) {
		player.damage(arg0, arg1);
	}

	@Override
	public double getHealth() {
		return player.getHealth();
	}

	@Override
	public double getMaxHealth() {
		return player.getMaxHealth();
	}

	@Override
	public void resetMaxHealth() {
		player.resetMaxHealth();
	}

	@Override
	public void setHealth(double arg0) {
		player.setHealth(arg0);
	}

	@Override
	public void setMaxHealth(double arg0) {
		player.setMaxHealth(arg0);
	}

	@Override
	public <T extends Projectile> T launchProjectile(Class<? extends T> arg0) {
		return player.launchProjectile(arg0);
	}

	@Override
	public <T extends Projectile> T launchProjectile(Class<? extends T> arg0, Vector arg1) {
		return player.launchProjectile(arg0, arg1);
	}

	@Override
	public void abandonConversation(Conversation arg0) {
		player.abandonConversation(arg0);
	}

	@Override
	public void abandonConversation(Conversation arg0, ConversationAbandonedEvent arg1) {
		player.abandonConversation(arg0, arg1);
	}

	@Override
	public void acceptConversationInput(String arg0) {
		player.acceptConversationInput(arg0);
	}

	@Override
	public boolean beginConversation(Conversation arg0) {
		return player.beginConversation(arg0);
	}

	@Override
	public boolean isConversing() {
		return player.isConversing();
	}

	@Override
	public long getFirstPlayed() {
		return player.getFirstPlayed();
	}

	@Override
	public long getLastPlayed() {
		return player.getLastPlayed();
	}

	@Override
	public Player getPlayer() {
		return player.getPlayer();
	}

	@Override
	public boolean hasPlayedBefore() {
		return player.hasPlayedBefore();
	}

	@Override
	public boolean isBanned() {
		return player.isBanned();
	}

	@Override
	public boolean isOnline() {
		return player.isOnline();
	}

	@Override
	public boolean isWhitelisted() {
		return player.isWhitelisted();
	}

	@Override
	public void setBanned(boolean arg0) {
		player.setBanned(arg0);
	}

	@Override
	public void setWhitelisted(boolean arg0) {
		player.setWhitelisted(arg0);
	}

	@Override
	public Map<String, Object> serialize() {
		return player.serialize();
	}

	@Override
	public Set<String> getListeningPluginChannels() {
		return player.getListeningPluginChannels();
	}

	@Override
	public void sendPluginMessage(Plugin arg0, String arg1, byte[] arg2) {
		player.sendPluginMessage(arg0, arg1, arg2);
	}
}
