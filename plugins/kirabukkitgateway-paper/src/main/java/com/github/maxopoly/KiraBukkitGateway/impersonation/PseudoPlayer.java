package com.github.maxopoly.KiraBukkitGateway.impersonation;

import com.github.maxopoly.KiraBukkitGateway.KiraBukkitGatewayPlugin;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.EntityEffect;
import org.bukkit.GameMode;
import org.bukkit.Instrument;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Note;
import org.bukkit.OfflinePlayer;
import org.bukkit.Particle;
import org.bukkit.Server;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.Statistic;
import org.bukkit.WeatherType;
import org.bukkit.World;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.block.PistonMoveReaction;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.craftbukkit.v1_18_R1.CraftServer;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_18_R1.scoreboard.CraftScoreboard;
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
import org.bukkit.inventory.Merchant;
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

public class PseudoPlayer extends CraftPlayer {

	private String name;
	private UUID uuid;
	private OfflinePlayer offlinePlayer;
	private List<String> replies;
	private long discordChannelId;
	private PseudoSpigotPlayer spigotPlayer;

	public PseudoPlayer(UUID uuid, long channelId) {
		super((CraftServer) Bukkit.getServer(), PseudoPlayerIdentity.generate(uuid, ""));
		if (uuid == null) {
			throw new IllegalArgumentException("No null uuid allowed");
		}
		offlinePlayer = Bukkit.getOfflinePlayer(uuid);
		if (offlinePlayer == null) {
			throw new IllegalArgumentException("No such player known: " + uuid.toString());
		}
		name = offlinePlayer.getName();
		this.discordChannelId = channelId;
		this.uuid = uuid;
		this.spigotPlayer = new PseudoSpigotPlayer(this);
		replies = new LinkedList<>();
	}

	public synchronized List<String> collectReplies() {
		List<String> replyCopy = replies;
		replies = null;
		return replyCopy;
	}

	public OfflinePlayer getOfflinePlayer() {
		return offlinePlayer;
	}

	public void closeInventory() {
		throw new InvalidCommandAttemptException();
	}

	public int getCooldown(Material arg0) {
		throw new InvalidCommandAttemptException();
	}

	public Inventory getEnderChest() {
		throw new InvalidCommandAttemptException();
	}

	public int getExpToLevel() {
		throw new InvalidCommandAttemptException();
	}

	public GameMode getGameMode() {
		throw new InvalidCommandAttemptException();
	}

	public PlayerInventory getInventory() {
		throw new InvalidCommandAttemptException();
	}

	public ItemStack getItemInHand() {
		throw new InvalidCommandAttemptException();
	}

	public ItemStack getItemOnCursor() {
		throw new InvalidCommandAttemptException();
	}

	public MainHand getMainHand() {
		throw new InvalidCommandAttemptException();
	}

	public String getName() {
		return name;
	}

	public InventoryView getOpenInventory() {
		throw new InvalidCommandAttemptException();
	}

	public Entity getShoulderEntityLeft() {
		throw new InvalidCommandAttemptException();
	}

	public Entity getShoulderEntityRight() {
		throw new InvalidCommandAttemptException();
	}

	public int getSleepTicks() {
		throw new InvalidCommandAttemptException();
	}

	public boolean hasCooldown(Material arg0) {
		throw new InvalidCommandAttemptException();
	}

	public boolean isBlocking() {
		throw new InvalidCommandAttemptException();
	}

	public boolean isHandRaised() {
		throw new InvalidCommandAttemptException();
	}

	public boolean isSleeping() {
		throw new InvalidCommandAttemptException();
	}

	public InventoryView openEnchanting(Location arg0, boolean arg1) {
		throw new InvalidCommandAttemptException();
	}

	public InventoryView openInventory(Inventory arg0) {
		throw new InvalidCommandAttemptException();
	}

	public void openInventory(InventoryView arg0) {
		throw new InvalidCommandAttemptException();
	}

	public InventoryView openMerchant(Villager arg0, boolean arg1) {
		throw new InvalidCommandAttemptException();
	}

	public InventoryView openMerchant(Merchant arg0, boolean arg1) {
		throw new InvalidCommandAttemptException();
	}

	public InventoryView openWorkbench(Location arg0, boolean arg1) {
		throw new InvalidCommandAttemptException();
	}

	public void setCooldown(Material arg0, int arg1) {
		throw new InvalidCommandAttemptException();
	}

	public void setGameMode(GameMode arg0) {
		throw new InvalidCommandAttemptException();
	}

	public void setItemInHand(ItemStack arg0) {
		throw new InvalidCommandAttemptException();
	}

	public void setItemOnCursor(ItemStack arg0) {
		throw new InvalidCommandAttemptException();
	}

	public void setShoulderEntityLeft(Entity arg0) {
		throw new InvalidCommandAttemptException();
	}

	public void setShoulderEntityRight(Entity arg0) {
		throw new InvalidCommandAttemptException();
	}

	public boolean setWindowProperty(Property arg0, int arg1) {
		throw new InvalidCommandAttemptException();
	}

	public boolean addPotionEffect(PotionEffect arg0) {
		throw new InvalidCommandAttemptException();
	}

	public boolean addPotionEffect(PotionEffect arg0, boolean arg1) {
		throw new InvalidCommandAttemptException();
	}

	public boolean addPotionEffects(Collection<PotionEffect> arg0) {
		throw new InvalidCommandAttemptException();
	}

	public Collection<PotionEffect> getActivePotionEffects() {
		throw new InvalidCommandAttemptException();
	}

	public boolean getCanPickupItems() {
		throw new InvalidCommandAttemptException();
	}

	public EntityEquipment getEquipment() {
		throw new InvalidCommandAttemptException();
	}

	public double getEyeHeight() {
		throw new InvalidCommandAttemptException();
	}

	public double getEyeHeight(boolean arg0) {
		throw new InvalidCommandAttemptException();
	}

	public Location getEyeLocation() {
		throw new InvalidCommandAttemptException();
	}

	public Player getKiller() {
		throw new InvalidCommandAttemptException();
	}

	public double getLastDamage() {
		throw new InvalidCommandAttemptException();
	}

	public List<Block> getLastTwoTargetBlocks(Set<Material> arg0, int arg1) {
		throw new InvalidCommandAttemptException();
	}

	public Entity getLeashHolder() throws IllegalStateException {
		throw new InvalidCommandAttemptException();
	}

	public List<Block> getLineOfSight(Set<Material> arg0, int arg1) {
		throw new InvalidCommandAttemptException();
	}

	public int getMaximumAir() {
		throw new InvalidCommandAttemptException();
	}

	public int getMaximumNoDamageTicks() {
		throw new InvalidCommandAttemptException();
	}

	public int getNoDamageTicks() {
		throw new InvalidCommandAttemptException();
	}

	public PotionEffect getPotionEffect(PotionEffectType arg0) {
		throw new InvalidCommandAttemptException();
	}

	public int getRemainingAir() {
		throw new InvalidCommandAttemptException();
	}

	public boolean getRemoveWhenFarAway() {
		throw new InvalidCommandAttemptException();
	}

	public Block getTargetBlock(Set<Material> arg0, int arg1) {
		throw new InvalidCommandAttemptException();
	}

	public boolean hasAI() {
		throw new InvalidCommandAttemptException();
	}

	public boolean hasLineOfSight(Entity arg0) {
		throw new InvalidCommandAttemptException();
	}

	public boolean hasPotionEffect(PotionEffectType arg0) {
		throw new InvalidCommandAttemptException();
	}

	public boolean isCollidable() {
		throw new InvalidCommandAttemptException();
	}

	public boolean isGliding() {
		throw new InvalidCommandAttemptException();
	}

	public boolean isLeashed() {
		throw new InvalidCommandAttemptException();
	}

	public void removePotionEffect(PotionEffectType arg0) {
		throw new InvalidCommandAttemptException();
	}

	public void setAI(boolean arg0) {
		throw new InvalidCommandAttemptException();
	}

	public void setCanPickupItems(boolean arg0) {
		throw new InvalidCommandAttemptException();
	}

	public void setCollidable(boolean arg0) {
		throw new InvalidCommandAttemptException();
	}

	public void setGliding(boolean arg0) {
		throw new InvalidCommandAttemptException();
	}

	public void setLastDamage(double arg0) {
		throw new InvalidCommandAttemptException();
	}

	public boolean setLeashHolder(Entity arg0) {
		throw new InvalidCommandAttemptException();
	}

	public void setMaximumAir(int arg0) {
		throw new InvalidCommandAttemptException();
	}

	public void setMaximumNoDamageTicks(int arg0) {
		throw new InvalidCommandAttemptException();
	}

	public void setNoDamageTicks(int arg0) {
		throw new InvalidCommandAttemptException();
	}

	public void setRemainingAir(int arg0) {
		throw new InvalidCommandAttemptException();
	}

	public void setRemoveWhenFarAway(boolean arg0) {
		throw new InvalidCommandAttemptException();
	}

	public AttributeInstance getAttribute(Attribute arg0) {
		throw new InvalidCommandAttemptException();
	}

	public boolean addPassenger(Entity arg0) {
		throw new InvalidCommandAttemptException();
	}

	public boolean addScoreboardTag(String arg0) {
		throw new InvalidCommandAttemptException();
	}

	public boolean eject() {
		throw new InvalidCommandAttemptException();
	}

	public int getEntityId() {
		throw new InvalidCommandAttemptException();
	}

	public float getFallDistance() {
		throw new InvalidCommandAttemptException();
	}

	public int getFireTicks() {
		throw new InvalidCommandAttemptException();
	}

	public double getHeight() {
		throw new InvalidCommandAttemptException();
	}

	public EntityDamageEvent getLastDamageCause() {
		throw new InvalidCommandAttemptException();
	}

	public Location getLocation() {
		return new Location(Bukkit.getWorlds().get(0), 0, -1000, 0);
	}

	public Location getLocation(Location arg0) {
		return new Location(Bukkit.getWorlds().get(0), 0, -1000, 0);
	}

	public int getMaxFireTicks() {
		throw new InvalidCommandAttemptException();
	}

	public List<Entity> getNearbyEntities(double arg0, double arg1, double arg2) {
		throw new InvalidCommandAttemptException();
	}

	public Entity getPassenger() {
		throw new InvalidCommandAttemptException();
	}

	public List<Entity> getPassengers() {
		throw new InvalidCommandAttemptException();
	}

	public PistonMoveReaction getPistonMoveReaction() {
		throw new InvalidCommandAttemptException();
	}

	public int getPortalCooldown() {
		throw new InvalidCommandAttemptException();
	}

	public Set<String> getScoreboardTags() {
		throw new InvalidCommandAttemptException();
	}

	public Server getServer() {
		throw new InvalidCommandAttemptException();
	}

	public int getTicksLived() {
		throw new InvalidCommandAttemptException();
	}

	public EntityType getType() {
		return EntityType.PLAYER;
	}

	public UUID getUniqueId() {
		return uuid;
	}

	public Entity getVehicle() {
		throw new InvalidCommandAttemptException();
	}

	public Vector getVelocity() {
		throw new InvalidCommandAttemptException();
	}

	public double getWidth() {
		throw new InvalidCommandAttemptException();
	}

	public World getWorld() {
		throw new InvalidCommandAttemptException();
	}

	public boolean hasGravity() {
		throw new InvalidCommandAttemptException();
	}

	public boolean isCustomNameVisible() {
		throw new InvalidCommandAttemptException();
	}

	public boolean isDead() {
		throw new InvalidCommandAttemptException();
	}

	public boolean isEmpty() {
		throw new InvalidCommandAttemptException();
	}

	public boolean isGlowing() {
		throw new InvalidCommandAttemptException();
	}

	public boolean isInsideVehicle() {
		throw new InvalidCommandAttemptException();
	}

	public boolean isInvulnerable() {
		throw new InvalidCommandAttemptException();
	}

	public boolean isOnGround() {
		throw new InvalidCommandAttemptException();
	}

	public boolean isSilent() {
		throw new InvalidCommandAttemptException();
	}

	public boolean isValid() {
		throw new InvalidCommandAttemptException();
	}

	public boolean leaveVehicle() {
		throw new InvalidCommandAttemptException();
	}

	public void playEffect(EntityEffect arg0) {
		throw new InvalidCommandAttemptException();
	}

	public void remove() {
		throw new InvalidCommandAttemptException();
	}

	public boolean removePassenger(Entity arg0) {
		throw new InvalidCommandAttemptException();
	}

	public boolean removeScoreboardTag(String arg0) {
		throw new InvalidCommandAttemptException();
	}

	public void setCustomNameVisible(boolean arg0) {
		throw new InvalidCommandAttemptException();
	}
	
	public void setFallDistance(float arg0) {
		throw new InvalidCommandAttemptException();
	}
	
	public void setFireTicks(int arg0) {
		throw new InvalidCommandAttemptException();
	}
	
	public void setGlowing(boolean arg0) {
		throw new InvalidCommandAttemptException();
	}
	
	public void setGravity(boolean arg0) {
		throw new InvalidCommandAttemptException();
	}
	
	public void setInvulnerable(boolean arg0) {
		throw new InvalidCommandAttemptException();
	}
	
	public void setLastDamageCause(EntityDamageEvent arg0) {
		throw new InvalidCommandAttemptException();
	}
	
	public boolean setPassenger(Entity arg0) {
		throw new InvalidCommandAttemptException();
	}
	
	public void setPortalCooldown(int arg0) {
		throw new InvalidCommandAttemptException();
	}
	
	public void setSilent(boolean arg0) {
		throw new InvalidCommandAttemptException();
	}
	
	public void setTicksLived(int arg0) {
		throw new InvalidCommandAttemptException();
	}
	
	public void setVelocity(Vector arg0) {
		throw new InvalidCommandAttemptException();
	}
	
	public boolean teleport(Location arg0) {
		throw new InvalidCommandAttemptException();
	}
	
	public boolean teleport(Entity arg0) {
		throw new InvalidCommandAttemptException();
	}
	
	public boolean teleport(Location arg0, TeleportCause arg1) {
		throw new InvalidCommandAttemptException();
	}
	
	public boolean teleport(Entity arg0, TeleportCause arg1) {
		throw new InvalidCommandAttemptException();
	}
	
	public List<MetadataValue> getMetadata(String arg0) {
		throw new InvalidCommandAttemptException();
	}

	
	public boolean hasMetadata(String arg0) {
		throw new InvalidCommandAttemptException();
	}

	
	public void removeMetadata(String arg0, Plugin arg1) {
		throw new InvalidCommandAttemptException();
	}

	
	public void setMetadata(String arg0, MetadataValue arg1) {
		throw new InvalidCommandAttemptException();
	}

	
	public synchronized void sendMessage(String msg) {
		if (replies == null) {
			KiraBukkitGatewayPlugin.getInstance().getRabbit().replyToUser(uuid, msg, discordChannelId);
		} else {
			replies.add(msg);
		}
	}

	
	public void sendMessage(String[] arg0) {
		StringBuilder sb = new StringBuilder();
		Arrays.stream(arg0).forEach(s -> sb.append(s + '\n'));
		sendMessage(sb.toString());
	}

	
	public PermissionAttachment addAttachment(Plugin arg0) {
		throw new InvalidCommandAttemptException();
	}

	
	public PermissionAttachment addAttachment(Plugin arg0, int arg1) {
		throw new InvalidCommandAttemptException();
	}

	
	public PermissionAttachment addAttachment(Plugin arg0, String arg1, boolean arg2) {
		throw new InvalidCommandAttemptException();
	}

	
	public PermissionAttachment addAttachment(Plugin arg0, String arg1, boolean arg2, int arg3) {
		throw new InvalidCommandAttemptException();
	}

	
	public Set<PermissionAttachmentInfo> getEffectivePermissions() {
		throw new InvalidCommandAttemptException();
	}

	
	public boolean hasPermission(String arg0) {
		return KiraBukkitGatewayPlugin.getInstance().getPermsWrapper().hasPermission(uuid, arg0);
	}

	
	public boolean hasPermission(Permission arg0) {
		return KiraBukkitGatewayPlugin.getInstance().getPermsWrapper().hasPermission(uuid, arg0.getName());
	}

	
	public boolean isPermissionSet(String arg0) {
		throw new InvalidCommandAttemptException();
	}

	
	public boolean isPermissionSet(Permission arg0) {
		throw new InvalidCommandAttemptException();
	}

	
	public void recalculatePermissions() {
		throw new InvalidCommandAttemptException();
	}

	
	public void removeAttachment(PermissionAttachment arg0) {
		throw new InvalidCommandAttemptException();
	}

	
	public boolean isOp() {
		return false;
	}

	
	public void setOp(boolean arg0) {
		throw new InvalidCommandAttemptException();
	}

	
	public String getCustomName() {
		throw new InvalidCommandAttemptException();
	}

	
	public void setCustomName(String arg0) {
		throw new InvalidCommandAttemptException();
	}

	
	public void damage(double arg0) {
		throw new InvalidCommandAttemptException();
	}

	
	public void damage(double arg0, Entity arg1) {
		throw new InvalidCommandAttemptException();
	}

	
	public double getHealth() {
		throw new InvalidCommandAttemptException();
	}

	
	public double getMaxHealth() {
		throw new InvalidCommandAttemptException();
	}

	
	public void resetMaxHealth() {
		throw new InvalidCommandAttemptException();
	}

	
	public void setHealth(double arg0) {
		throw new InvalidCommandAttemptException();
	}

	
	public void setMaxHealth(double arg0) {
		throw new InvalidCommandAttemptException();
	}

	
	public <T extends Projectile> T launchProjectile(Class<? extends T> arg0) {
		throw new InvalidCommandAttemptException();
	}

	
	public <T extends Projectile> T launchProjectile(Class<? extends T> arg0, Vector arg1) {
		throw new InvalidCommandAttemptException();
	}

	
	public void abandonConversation(Conversation arg0) {
		throw new InvalidCommandAttemptException();
	}

	
	public void abandonConversation(Conversation arg0, ConversationAbandonedEvent arg1) {
		throw new InvalidCommandAttemptException();
	}

	
	public void acceptConversationInput(String arg0) {
		throw new InvalidCommandAttemptException();
	}

	
	public boolean beginConversation(Conversation arg0) {
		throw new InvalidCommandAttemptException();
	}

	
	public boolean isConversing() {
		throw new InvalidCommandAttemptException();
	}

	
	public long getFirstPlayed() {
		throw new InvalidCommandAttemptException();
	}

	
	public long getLastPlayed() {
		throw new InvalidCommandAttemptException();
	}

	
	public Player getPlayer() {
		throw new InvalidCommandAttemptException();
	}

	
	public boolean hasPlayedBefore() {
		throw new InvalidCommandAttemptException();
	}

	
	public boolean isBanned() {
		throw new InvalidCommandAttemptException();
	}

	
	public boolean isOnline() {
		throw new InvalidCommandAttemptException();
	}

	
	public boolean isWhitelisted() {
		throw new InvalidCommandAttemptException();
	}

	
	public void setWhitelisted(boolean arg0) {
		throw new InvalidCommandAttemptException();
	}

	
	public Map<String, Object> serialize() {
		throw new InvalidCommandAttemptException();
	}

	
	public Set<String> getListeningPluginChannels() {
		throw new InvalidCommandAttemptException();
	}

	
	public void sendPluginMessage(Plugin arg0, String arg1, byte[] arg2) {
		throw new InvalidCommandAttemptException();
	}

	
	public boolean canSee(Player arg0) {
		throw new InvalidCommandAttemptException();
	}

	
	public void chat(String arg0) {
		throw new InvalidCommandAttemptException();
	}

	
	public void decrementStatistic(Statistic arg0) throws IllegalArgumentException {
		throw new InvalidCommandAttemptException();
	}

	
	public void decrementStatistic(Statistic arg0, int arg1) throws IllegalArgumentException {
		throw new InvalidCommandAttemptException();
	}

	
	public void decrementStatistic(Statistic arg0, Material arg1) throws IllegalArgumentException {
		throw new InvalidCommandAttemptException();
	}

	
	public void decrementStatistic(Statistic arg0, EntityType arg1) throws IllegalArgumentException {
		throw new InvalidCommandAttemptException();
	}

	
	public void decrementStatistic(Statistic arg0, Material arg1, int arg2) throws IllegalArgumentException {
		throw new InvalidCommandAttemptException();
	}

	
	public void decrementStatistic(Statistic arg0, EntityType arg1, int arg2) {
		throw new InvalidCommandAttemptException();
	}

	
	public InetSocketAddress getAddress() {
		throw new InvalidCommandAttemptException();
	}

	
	public AdvancementProgress getAdvancementProgress(Advancement arg0) {
		throw new InvalidCommandAttemptException();
	}

	
	public boolean getAllowFlight() {
		throw new InvalidCommandAttemptException();
	}

	
	public Location getBedSpawnLocation() {
		return offlinePlayer.getBedSpawnLocation();
	}

	
	public Location getCompassTarget() {
		throw new InvalidCommandAttemptException();
	}

	
	public String getDisplayName() {
		return name;
	}

	
	public float getExhaustion() {
		throw new InvalidCommandAttemptException();
	}

	
	public float getExp() {
		throw new InvalidCommandAttemptException();
	}

	
	public float getFlySpeed() {
		throw new InvalidCommandAttemptException();
	}

	
	public int getFoodLevel() {
		throw new InvalidCommandAttemptException();
	}

	
	public double getHealthScale() {
		throw new InvalidCommandAttemptException();
	}

	
	public int getLevel() {
		throw new InvalidCommandAttemptException();
	}

	
	public String getLocale() {
		throw new InvalidCommandAttemptException();
	}

	
	public String getPlayerListName() {
		throw new InvalidCommandAttemptException();
	}

	
	public long getPlayerTime() {
		throw new InvalidCommandAttemptException();
	}

	
	public long getPlayerTimeOffset() {
		throw new InvalidCommandAttemptException();
	}

	
	public WeatherType getPlayerWeather() {
		throw new InvalidCommandAttemptException();
	}

	
	public float getSaturation() {
		throw new InvalidCommandAttemptException();
	}

	
	public CraftScoreboard getScoreboard() {
		throw new InvalidCommandAttemptException();
	}

	
	public Entity getSpectatorTarget() {
		throw new InvalidCommandAttemptException();
	}

	
	public int getStatistic(Statistic arg0) throws IllegalArgumentException {
		throw new InvalidCommandAttemptException();
	}

	
	public int getStatistic(Statistic arg0, Material arg1) throws IllegalArgumentException {
		throw new InvalidCommandAttemptException();
	}

	
	public int getStatistic(Statistic arg0, EntityType arg1) throws IllegalArgumentException {
		throw new InvalidCommandAttemptException();
	}

	
	public int getTotalExperience() {
		throw new InvalidCommandAttemptException();
	}

	
	public float getWalkSpeed() {
		throw new InvalidCommandAttemptException();
	}

	
	public void giveExp(int arg0) {
		throw new InvalidCommandAttemptException();
	}

	
	public void giveExpLevels(int arg0) {
		throw new InvalidCommandAttemptException();
	}

	
	public void hidePlayer(Player arg0) {
		throw new InvalidCommandAttemptException();
	}

	
	public void hidePlayer(Plugin arg0, Player arg1) {
		throw new InvalidCommandAttemptException();
	}

	
	public void incrementStatistic(Statistic arg0) throws IllegalArgumentException {
		throw new InvalidCommandAttemptException();
	}

	
	public void incrementStatistic(Statistic arg0, int arg1) throws IllegalArgumentException {
		throw new InvalidCommandAttemptException();
	}

	
	public void incrementStatistic(Statistic arg0, Material arg1) throws IllegalArgumentException {
		throw new InvalidCommandAttemptException();
	}

	
	public void incrementStatistic(Statistic arg0, EntityType arg1) throws IllegalArgumentException {
		throw new InvalidCommandAttemptException();
	}

	
	public void incrementStatistic(Statistic arg0, Material arg1, int arg2) throws IllegalArgumentException {
		throw new InvalidCommandAttemptException();
	}

	
	public void incrementStatistic(Statistic arg0, EntityType arg1, int arg2) throws IllegalArgumentException {
		throw new InvalidCommandAttemptException();
	}

	
	public boolean isFlying() {
		throw new InvalidCommandAttemptException();
	}

	
	public boolean isHealthScaled() {
		throw new InvalidCommandAttemptException();
	}

	
	public boolean isPlayerTimeRelative() {
		throw new InvalidCommandAttemptException();
	}

	
	public boolean isSleepingIgnored() {
		throw new InvalidCommandAttemptException();
	}

	
	public boolean isSneaking() {
		throw new InvalidCommandAttemptException();
	}

	
	public boolean isSprinting() {
		throw new InvalidCommandAttemptException();
	}

	
	public void kickPlayer(String arg0) {
		throw new InvalidCommandAttemptException();
	}

	
	public void loadData() {
		throw new InvalidCommandAttemptException();
	}

	
	public boolean performCommand(String arg0) {
		throw new InvalidCommandAttemptException();
	}

	
	public void playEffect(Location arg0, Effect arg1, int arg2) {
		throw new InvalidCommandAttemptException();
	}

	
	public <T> void playEffect(Location arg0, Effect arg1, T arg2) {
		throw new InvalidCommandAttemptException();
	}

	
	public void playNote(Location arg0, byte arg1, byte arg2) {
		throw new InvalidCommandAttemptException();
	}

	
	public void playNote(Location arg0, Instrument arg1, Note arg2) {
		throw new InvalidCommandAttemptException();
	}

	
	public void playSound(Location arg0, Sound arg1, float arg2, float arg3) {
		throw new InvalidCommandAttemptException();
	}

	
	public void playSound(Location arg0, String arg1, float arg2, float arg3) {
		throw new InvalidCommandAttemptException();
	}

	
	public void playSound(Location arg0, Sound arg1, SoundCategory arg2, float arg3, float arg4) {
		throw new InvalidCommandAttemptException();
	}

	
	public void playSound(Location arg0, String arg1, SoundCategory arg2, float arg3, float arg4) {
		throw new InvalidCommandAttemptException();
	}

	
	public void resetPlayerTime() {
		throw new InvalidCommandAttemptException();
	}

	
	public void resetPlayerWeather() {
		throw new InvalidCommandAttemptException();
	}

	
	public void resetTitle() {
		throw new InvalidCommandAttemptException();
	}

	
	public void saveData() {
		throw new InvalidCommandAttemptException();
	}

	
	public void sendBlockChange(Location arg0, Material arg1, byte arg2) {
		throw new InvalidCommandAttemptException();
	}

	
	public boolean sendChunkChange(Location arg0, int arg1, int arg2, int arg3, byte[] arg4) {
		throw new InvalidCommandAttemptException();
	}

	
	public void sendMap(MapView arg0) {
		throw new InvalidCommandAttemptException();
	}

	
	public void sendRawMessage(String arg0) {
		throw new InvalidCommandAttemptException();
	}

	
	public void sendSignChange(Location arg0, String[] arg1) throws IllegalArgumentException {
		throw new InvalidCommandAttemptException();
	}

	
	public void sendTitle(String arg0, String arg1) {
		throw new InvalidCommandAttemptException();
	}

	
	public void sendTitle(String arg0, String arg1, int arg2, int arg3, int arg4) {
		throw new InvalidCommandAttemptException();
	}

	
	public void setAllowFlight(boolean arg0) {
		throw new InvalidCommandAttemptException();
	}

	
	public void setBedSpawnLocation(Location arg0) {
		throw new InvalidCommandAttemptException();
	}

	
	public void setBedSpawnLocation(Location arg0, boolean arg1) {
		throw new InvalidCommandAttemptException();
	}

	
	public void setCompassTarget(Location arg0) {
		throw new InvalidCommandAttemptException();
	}

	
	public void setDisplayName(String arg0) {
		throw new InvalidCommandAttemptException();
	}

	
	public void setExhaustion(float arg0) {
		throw new InvalidCommandAttemptException();
	}

	
	public void setExp(float arg0) {
		throw new InvalidCommandAttemptException();
	}

	
	public void setFlySpeed(float arg0) throws IllegalArgumentException {
		throw new InvalidCommandAttemptException();
	}

	
	public void setFlying(boolean arg0) {
		throw new InvalidCommandAttemptException();
	}

	
	public void setFoodLevel(int arg0) {
		throw new InvalidCommandAttemptException();
	}

	
	public void setHealthScale(double arg0) throws IllegalArgumentException {
		throw new InvalidCommandAttemptException();
	}

	
	public void setHealthScaled(boolean arg0) {
		throw new InvalidCommandAttemptException();
	}

	
	public void setLevel(int arg0) {
		throw new InvalidCommandAttemptException();
	}

	
	public void setPlayerListName(String arg0) {
		throw new InvalidCommandAttemptException();
	}

	
	public void setPlayerTime(long arg0, boolean arg1) {
		throw new InvalidCommandAttemptException();
	}

	
	public void setPlayerWeather(WeatherType arg0) {
		throw new InvalidCommandAttemptException();
	}

	
	public void setResourcePack(String arg0) {
		throw new InvalidCommandAttemptException();
	}

	
	public void setResourcePack(String arg0, byte[] arg1) {
		throw new InvalidCommandAttemptException();
	}

	
	public void setSaturation(float arg0) {
		throw new InvalidCommandAttemptException();
	}

	
	public void setScoreboard(Scoreboard arg0) throws IllegalArgumentException, IllegalStateException {
		throw new InvalidCommandAttemptException();
	}

	
	public void setSleepingIgnored(boolean arg0) {
		throw new InvalidCommandAttemptException();
	}

	
	public void setSneaking(boolean arg0) {
		throw new InvalidCommandAttemptException();
	}

	
	public void setSpectatorTarget(Entity arg0) {
		throw new InvalidCommandAttemptException();
	}

	
	public void setSprinting(boolean arg0) {
		throw new InvalidCommandAttemptException();
	}

	
	public void setStatistic(Statistic arg0, int arg1) throws IllegalArgumentException {
		throw new InvalidCommandAttemptException();
	}

	
	public void setStatistic(Statistic arg0, Material arg1, int arg2) throws IllegalArgumentException {
		throw new InvalidCommandAttemptException();
	}

	
	public void setStatistic(Statistic arg0, EntityType arg1, int arg2) {
		throw new InvalidCommandAttemptException();
	}

	
	public void setTexturePack(String arg0) {
		throw new InvalidCommandAttemptException();
	}

	
	public void setTotalExperience(int arg0) {
		throw new InvalidCommandAttemptException();
	}

	
	public void setWalkSpeed(float arg0) throws IllegalArgumentException {
		throw new InvalidCommandAttemptException();
	}

	
	public void showPlayer(Player arg0) {
		throw new InvalidCommandAttemptException();
	}

	
	public void showPlayer(Plugin arg0, Player arg1) {
		throw new InvalidCommandAttemptException();
	}

	
	public void spawnParticle(Particle arg0, Location arg1, int arg2) {
		throw new InvalidCommandAttemptException();
	}

	
	public <T> void spawnParticle(Particle arg0, Location arg1, int arg2, T arg3) {
		throw new InvalidCommandAttemptException();
	}

	
	public void spawnParticle(Particle arg0, double arg1, double arg2, double arg3, int arg4) {
		throw new InvalidCommandAttemptException();
	}

	
	public <T> void spawnParticle(Particle arg0, double arg1, double arg2, double arg3, int arg4, T arg5) {
		throw new InvalidCommandAttemptException();
	}

	
	public void spawnParticle(Particle arg0, Location arg1, int arg2, double arg3, double arg4, double arg5) {
		throw new InvalidCommandAttemptException();
	}

	
	public <T> void spawnParticle(Particle arg0, Location arg1, int arg2, double arg3, double arg4, double arg5,
			T arg6) {
		throw new InvalidCommandAttemptException();
	}

	
	public void spawnParticle(Particle arg0, Location arg1, int arg2, double arg3, double arg4, double arg5,
			double arg6) {
		throw new InvalidCommandAttemptException();
	}

	
	public void spawnParticle(Particle arg0, double arg1, double arg2, double arg3, int arg4, double arg5, double arg6,
			double arg7) {
		throw new InvalidCommandAttemptException();
	}

	
	public <T> void spawnParticle(Particle arg0, Location arg1, int arg2, double arg3, double arg4, double arg5,
			double arg6, T arg7) {
		throw new InvalidCommandAttemptException();
	}

	
	public <T> void spawnParticle(Particle arg0, double arg1, double arg2, double arg3, int arg4, double arg5,
			double arg6, double arg7, T arg8) {
		throw new InvalidCommandAttemptException();
	}

	
	public void spawnParticle(Particle arg0, double arg1, double arg2, double arg3, int arg4, double arg5, double arg6,
			double arg7, double arg8) {
		throw new InvalidCommandAttemptException();
	}

	
	public <T> void spawnParticle(Particle arg0, double arg1, double arg2, double arg3, int arg4, double arg5,
			double arg6, double arg7, double arg8, T arg9) {
		throw new InvalidCommandAttemptException();
	}

	
	public Player.Spigot spigot() {
		return spigotPlayer;
	}

	
	public void stopSound(Sound arg0) {
		throw new InvalidCommandAttemptException();
	}

	
	public void stopSound(String arg0) {
		throw new InvalidCommandAttemptException();
	}

	
	public void stopSound(Sound arg0, SoundCategory arg1) {
		throw new InvalidCommandAttemptException();
	}

	
	public void stopSound(String arg0, SoundCategory arg1) {
		throw new InvalidCommandAttemptException();
	}

	
	public void updateInventory() {
		throw new InvalidCommandAttemptException();
	}

}
