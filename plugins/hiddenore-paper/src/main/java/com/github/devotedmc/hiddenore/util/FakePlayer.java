package com.github.devotedmc.hiddenore.util;

import com.destroystokyo.paper.ClientOption;
import com.destroystokyo.paper.Title;
import com.destroystokyo.paper.block.TargetBlockInfo;
import com.destroystokyo.paper.block.TargetBlockInfo.FluidMode;
import com.destroystokyo.paper.entity.TargetEntityInfo;
import com.destroystokyo.paper.profile.PlayerProfile;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import io.papermc.paper.entity.LookAnchor;
import io.papermc.paper.entity.TeleportFlag;
import io.papermc.paper.math.Position;
import io.papermc.paper.threadedregions.scheduler.EntityScheduler;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.util.TriState;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.BanEntry;
import org.bukkit.Chunk;
import org.bukkit.DyeColor;
import org.bukkit.Effect;
import org.bukkit.EntityEffect;
import org.bukkit.FluidCollisionMode;
import org.bukkit.GameMode;
import org.bukkit.Instrument;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Note;
import org.bukkit.Particle;
import org.bukkit.Server;
import org.bukkit.ServerLinks;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.Statistic;
import org.bukkit.WeatherType;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.PistonMoveReaction;
import org.bukkit.block.Sign;
import org.bukkit.block.TileState;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.sign.Side;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.damage.DamageSource;
import org.bukkit.entity.*;
import org.bukkit.entity.memory.MemoryKey;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.inventory.InventoryCloseEvent.Reason;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent.Status;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
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
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import org.jetbrains.annotations.Unmodifiable;
import org.jetbrains.annotations.UnmodifiableView;

@SuppressWarnings("removal")
public class FakePlayer implements Player {

    private final ItemStack inHand;
    private final Location location;

    public FakePlayer(final Location location, final ItemStack inHand) {
        this.inHand = inHand;
        this.location = location;
    }

    @Override
    public String getName() {
        return "Spoof";
    }

    @Override
    public PlayerInventory getInventory() {
        return new PlayerInventory() {

            @Override
            public int getSize() {
                return 0;
            }

            @Override
            public int getMaxStackSize() {
                return 0;
            }

            @Override
            public void setMaxStackSize(int size) {
            }

            @Override
            public ItemStack getItem(int index) {
                return null;
            }

            @Override
            public HashMap<Integer, ItemStack> addItem(ItemStack... items) throws IllegalArgumentException {
                return new HashMap<>();
            }

            @Override
            public HashMap<Integer, ItemStack> removeItem(ItemStack... items) throws IllegalArgumentException {
                return new HashMap<>();
            }

            @Override
            public ItemStack[] getContents() {
                return null;
            }

            @Override
            public void setContents(ItemStack[] items) throws IllegalArgumentException {
            }

            @Override
            public ItemStack[] getStorageContents() {
                return null;
            }

            @Override
            public void setStorageContents(ItemStack[] items) throws IllegalArgumentException {
            }

            @Override
            public boolean contains(Material material) throws IllegalArgumentException {
                return false;
            }

            @Override
            public boolean contains(ItemStack item) {
                return false;
            }

            @Override
            public boolean contains(Material material, int amount) throws IllegalArgumentException {
                return false;
            }

            @Override
            public boolean contains(ItemStack item, int amount) {
                return false;
            }

            @Override
            public boolean containsAtLeast(ItemStack item, int amount) {
                return false;
            }

            @Override
            public HashMap<Integer, ? extends ItemStack> all(Material material) throws IllegalArgumentException {
                return new HashMap<>();
            }

            @Override
            public HashMap<Integer, ? extends ItemStack> all(ItemStack item) {
                return new HashMap<>();
            }

            @Override
            public int first(Material material) throws IllegalArgumentException {
                return 0;
            }

            @Override
            public int first(ItemStack item) {
                return 0;
            }

            @Override
            public int firstEmpty() {
                return 0;
            }

            @Override
            public void remove(Material material) throws IllegalArgumentException {
            }

            @Override
            public void remove(ItemStack item) {
            }

            @Override
            public void clear(int index) {
            }

            @Override
            public void clear() {
            }

            @Override
            public int close() {
                return 0;
            }

            @Override
            public List<HumanEntity> getViewers() {
                return new ArrayList<>();
            }

            @Override
            public InventoryType getType() {
                return InventoryType.PLAYER;
            }

            @Override
            public ListIterator<ItemStack> iterator() {
                return null;
            }

            @Override
            public ListIterator<ItemStack> iterator(int index) {
                return null;
            }

            @Override
            public Location getLocation() {
                return location;
            }

            @Override
            public ItemStack[] getArmorContents() {
                return null;
            }

            @Override
            public ItemStack[] getExtraContents() {
                return null;
            }

            @Override
            public ItemStack getHelmet() {
                return null;
            }

            @Override
            public ItemStack getChestplate() {
                return null;
            }

            @Override
            public ItemStack getLeggings() {
                return null;
            }

            @Override
            public ItemStack getBoots() {
                return null;
            }

            @Override
            public void setItem(int index, ItemStack item) {
            }

            @Override
            public void setArmorContents(ItemStack[] items) {
            }

            @Override
            public void setExtraContents(ItemStack[] items) {
            }

            @Override
            public void setHelmet(ItemStack helmet) {
            }

            @Override
            public void setChestplate(ItemStack chestplate) {
            }

            @Override
            public void setLeggings(ItemStack leggings) {
            }

            @Override
            public void setBoots(ItemStack boots) {
            }

            @Override
            public ItemStack getItemInMainHand() {
                return inHand;
            }

            @Override
            public void setItemInMainHand(ItemStack item) {
            }

            @Override
            public ItemStack getItemInOffHand() {
                return null;
            }

            @Override
            public void setItemInOffHand(ItemStack item) {
            }

            @Override
            public ItemStack getItemInHand() {
                return inHand;
            }

            @Override
            public void setItemInHand(ItemStack stack) {
            }

            @Override
            public int getHeldItemSlot() {
                return 0;
            }

            @Override
            public void setHeldItemSlot(int slot) {
            }

            @Override
            public HumanEntity getHolder() {
                return null;
            }

            @Override
            public void setItem(EquipmentSlot slot, ItemStack item) {
            }

            @Override
            public ItemStack getItem(EquipmentSlot slot) {
                return null;
            }

            @Override
            public InventoryHolder getHolder(boolean arg0) {
                return null;
            }

            @Override
            public boolean isEmpty() {
                return true;
            }

            @Override
            public HashMap<Integer, ItemStack> removeItemAnySlot(ItemStack... arg0) throws IllegalArgumentException {
                return null;
            }
        };
    }

    @Override
    public Inventory getEnderChest() {
        return null;
    }

    @Override
    public MainHand getMainHand() {
        return null;
    }

    @Override
    public boolean setWindowProperty(Property prop, int value) {
        return false;
    }

    @Override
    public int getEnchantmentSeed() {
        return 0;
    }

    @Override
    public void setEnchantmentSeed(int i) {

    }

    @Override
    public InventoryView getOpenInventory() {
        return null;
    }

    @Override
    public InventoryView openInventory(Inventory inventory) {
        return null;
    }

    @Override
    public InventoryView openWorkbench(Location location, boolean force) {
        return null;
    }

    @Override
    public InventoryView openEnchanting(Location location, boolean force) {
        return null;
    }

    @Override
    public void openInventory(InventoryView inventory) {
    }

    @Override
    public InventoryView openMerchant(Villager trader, boolean force) {
        return null;
    }

    @Override
    public void closeInventory() {
    }

    @Override
    public ItemStack getItemInHand() {
        return inHand;
    }

    @Override
    public void setItemInHand(ItemStack item) {
    }

    @Override
    public ItemStack getItemOnCursor() {
        return null;
    }

    @Override
    public void setItemOnCursor(ItemStack item) {
    }

    @Override
    public boolean isSleeping() {
        return false;
    }

    @Override
    public boolean isClimbing() {
        return false;
    }

    @Override
    public int getSleepTicks() {
        return 0;
    }

    @Override
    public GameMode getGameMode() {
        return null;
    }

    @Override
    public void setGameMode(GameMode mode) {
    }

    @Override
    public boolean isBlocking() {
        return false;
    }

    @Override
    public int getExpToLevel() {
        return 0;
    }

    @Override
    public double getEyeHeight() {
        return 0;
    }

    @Override
    public double getEyeHeight(boolean ignoreSneaking) {
        return 0;
    }

    @Override
    public Location getEyeLocation() {
        return location;
    }

    @Override
    public List<Block> getLineOfSight(Set<Material> transparent, int maxDistance) {
        return null;
    }

    public Block getTargetBlock(HashSet<Byte> transparent, int maxDistance) {
        return null;
    }

    @Override
    public Block getTargetBlock(Set<Material> transparent, int maxDistance) {
        return null;
    }

    public List<Block> getLastTwoTargetBlocks(HashSet<Byte> transparent, int maxDistance) {
        return null;
    }

    @Override
    public List<Block> getLastTwoTargetBlocks(Set<Material> transparent, int maxDistance) {
        return null;
    }

    @Override
    public int getRemainingAir() {
        return 0;
    }

    @Override
    public void setRemainingAir(int ticks) {
    }

    @Override
    public int getMaximumAir() {
        return 0;
    }

    @Override
    public void setMaximumAir(int ticks) {
    }

    @Override
    public int getMaximumNoDamageTicks() {
        return 0;
    }

    @Override
    public void setMaximumNoDamageTicks(int ticks) {
    }

    @Override
    public double getLastDamage() {
        return 0;
    }

    @Override
    public void setLastDamage(double damage) {

    }

    @Override
    public int getNoDamageTicks() {

        return 0;
    }

    @Override
    public void setNoDamageTicks(int ticks) {

    }

    @Override
    public int getNoActionTicks() {
        return 0;
    }

    @Override
    public void setNoActionTicks(int i) {

    }

    @Override
    public Player getKiller() {

        return null;
    }

    @Override
    public boolean addPotionEffect(PotionEffect effect) {

        return false;
    }

    @Override
    public boolean addPotionEffect(PotionEffect effect, boolean force) {

        return false;
    }

    @Override
    public boolean addPotionEffects(Collection<PotionEffect> effects) {

        return false;
    }

    @Override
    public boolean hasPotionEffect(PotionEffectType type) {

        return false;
    }

    @Override
    public void removePotionEffect(PotionEffectType type) {

    }

    @Override
    public Collection<PotionEffect> getActivePotionEffects() {

        return null;
    }

    @Override
    public boolean clearActivePotionEffects() {
        return false;
    }

    @Override
    public boolean hasLineOfSight(Entity other) {

        return false;
    }

    @Override
    public boolean hasLineOfSight(@NotNull Location location) {
        return false;
    }

    @Override
    public boolean getRemoveWhenFarAway() {

        return false;
    }

    @Override
    public void setRemoveWhenFarAway(boolean remove) {

    }

    @Override
    public EntityEquipment getEquipment() {

        return null;
    }

    @Override
    public void setCanPickupItems(boolean pickup) {

    }

    @Override
    public boolean getCanPickupItems() {

        return false;
    }

    @Override
    public boolean isLeashed() {

        return false;
    }

    @Override
    public Entity getLeashHolder() throws IllegalStateException {

        return null;
    }

    @Override
    public boolean setLeashHolder(Entity holder) {

        return false;
    }

    @Override
    public boolean isGliding() {

        return false;
    }

    @Override
    public void setGliding(boolean gliding) {

    }

    @Override
    public void setAI(boolean ai) {

    }

    @Override
    public boolean hasAI() {

        return false;
    }

    @Override
    public void setCollidable(boolean collidable) {

    }

    @Override
    public boolean isCollidable() {

        return false;
    }

    @Override
    public AttributeInstance getAttribute(Attribute attribute) {

        return null;
    }

    @Override
    public void registerAttribute(@NotNull Attribute attribute) {

    }

    @Override
    public Location getLocation() {
        return location;
    }

    @Override
    public Location getLocation(Location loc) {
        return location;
    }

    @Override
    public void setVelocity(Vector velocity) {

    }

    @Override
    public Vector getVelocity() {

        return null;
    }

    @Override
    public World getWorld() {
        return location.getWorld();
    }

    @Override
    public boolean teleport(Location location) {

        return false;
    }

    @Override
    public boolean teleport(Location location, TeleportCause cause) {

        return false;
    }

    @Override
    public boolean teleport(Entity destination) {

        return false;
    }

    @Override
    public boolean teleport(Entity destination, TeleportCause cause) {

        return false;
    }

    @Override
    public @NotNull CompletableFuture<Boolean> teleportAsync(@NotNull Location location, @NotNull PlayerTeleportEvent.TeleportCause teleportCause, @NotNull TeleportFlag @NotNull ... teleportFlags) {
        return null;
    }

    @Override
    public List<Entity> getNearbyEntities(double x, double y, double z) {

        return null;
    }

    @Override
    public int getEntityId() {

        return 0;
    }

    @Override
    public int getFireTicks() {

        return 0;
    }

    @Override
    public int getMaxFireTicks() {

        return 0;
    }

    @Override
    public void setFireTicks(int ticks) {

    }

    @Override
    public void setVisualFire(boolean bl) {

    }

    @Override
    public boolean isVisualFire() {
        return false;
    }

    @Override
    public int getFreezeTicks() {
        return 0;
    }

    @Override
    public int getMaxFreezeTicks() {
        return 0;
    }

    @Override
    public void setFreezeTicks(int i) {

    }

    @Override
    public boolean isFrozen() {
        return false;
    }

    @Override
    public boolean isFreezeTickingLocked() {
        return false;
    }

    @Override
    public void lockFreezeTicks(boolean locked) {

    }

    @Override
    public void remove() {

    }

    @Override
    public boolean isDead() {

        return false;
    }

    @Override
    public boolean isValid() {

        return false;
    }

    @Override
    public Server getServer() {

        return null;
    }

    @Override
    public Entity getPassenger() {

        return null;
    }

    @Override
    public boolean setPassenger(Entity passenger) {

        return false;
    }

    @Override
    public boolean isEmpty() {

        return false;
    }

    @Override
    public boolean eject() {

        return false;
    }

    @Override
    public float getFallDistance() {

        return 0;
    }

    @Override
    public void setFallDistance(float distance) {

    }

    @Override
    public void setLastDamageCause(EntityDamageEvent event) {

    }

    @Override
    public EntityDamageEvent getLastDamageCause() {

        return null;
    }

    @Override
    public UUID getUniqueId() {

        return null;
    }

    @Override
    public int getTicksLived() {

        return 0;
    }

    @Override
    public void setTicksLived(int value) {

    }

    @Override
    public void playEffect(EntityEffect type) {

    }

    @Override
    public EntityType getType() {

        return null;
    }

    @Override
    public @NotNull Sound getSwimSound() {
        return null;
    }

    @Override
    public @NotNull Sound getSwimSplashSound() {
        return null;
    }

    @Override
    public @NotNull Sound getSwimHighSpeedSplashSound() {
        return null;
    }

    @Override
    public boolean isInsideVehicle() {

        return false;
    }

    @Override
    public boolean leaveVehicle() {

        return false;
    }

    @Override
    public Entity getVehicle() {

        return null;
    }

    @Override
    public void setCustomName(String name) {

    }

    @Override
    public @Nullable
    Component customName() {
        return null;
    }

    @Override
    public void customName(@Nullable Component component) {

    }

    @Override
    public String getCustomName() {

        return "Spoof";
    }

    @Override
    public void setCustomNameVisible(boolean flag) {

    }

    @Override
    public boolean isCustomNameVisible() {

        return false;
    }

    @Override
    public void setVisibleByDefault(boolean b) {

    }

    @Override
    public boolean isVisibleByDefault() {
        return false;
    }

    @Override
    public @NotNull Set<Player> getTrackedBy() {
        return null;
    }

    @Override
    public void setGlowing(boolean flag) {

    }

    @Override
    public boolean isGlowing() {

        return false;
    }

    @Override
    public void setInvulnerable(boolean flag) {

    }

    @Override
    public boolean isInvulnerable() {

        return false;
    }

    @Override
    public void setMetadata(String metadataKey, MetadataValue newMetadataValue) {

    }

    @Override
    public List<MetadataValue> getMetadata(String metadataKey) {

        return null;
    }

    @Override
    public boolean hasMetadata(String metadataKey) {

        return false;
    }

    @Override
    public void removeMetadata(String metadataKey, Plugin owningPlugin) {

    }

    @Override
    public void sendMessage(String message) {

    }

    @Override
    public void sendMessage(String[] messages) {

    }

    @Override
    public boolean isPermissionSet(String name) {

        return false;
    }

    @Override
    public boolean isPermissionSet(Permission perm) {

        return false;
    }

    @Override
    public boolean hasPermission(String name) {

        return false;
    }

    @Override
    public boolean hasPermission(Permission perm) {

        return false;
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value) {

        return null;
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin) {

        return null;
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value, int ticks) {

        return null;
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, int ticks) {

        return null;
    }

    @Override
    public void removeAttachment(PermissionAttachment attachment) {

    }

    @Override
    public void recalculatePermissions() {

    }

    @Override
    public Set<PermissionAttachmentInfo> getEffectivePermissions() {

        return null;
    }

    @Override
    public boolean isOp() {

        return false;
    }

    @Override
    public void setOp(boolean value) {

    }

    @Override
    public void damage(double amount) {

    }

    @Override
    public void damage(double amount, Entity source) {

    }

    @Override
    public void damage(double v, @NotNull DamageSource damageSource) {

    }

    @Override
    public double getHealth() {

        return 0;
    }

    @Override
    public void setHealth(double health) {

    }

    @Override
	public void heal(double amount, @NotNull EntityRegainHealthEvent.RegainReason reason) {

	}

	@Override
    public double getMaxHealth() {

        return 0;
    }

    @Override
    public void setMaxHealth(double health) {

    }

    @Override
    public void resetMaxHealth() {

    }

    @Override
    public <T extends Projectile> T launchProjectile(Class<? extends T> projectile) {

        return null;
    }

    @Override
    public <T extends Projectile> T launchProjectile(Class<? extends T> projectile, Vector velocity) {

        return null;
    }

    @Override
    public <T extends Projectile> @NotNull T launchProjectile(@NotNull Class<? extends T> aClass, @org.jetbrains.annotations.Nullable Vector vector, @org.jetbrains.annotations.Nullable Consumer<? super T> consumer) {
        return null;
    }

    @Override
    public boolean isConversing() {

        return false;
    }

    @Override
    public void acceptConversationInput(String input) {

    }

    @Override
    public boolean beginConversation(Conversation conversation) {

        return false;
    }

    @Override
    public void abandonConversation(Conversation conversation) {

    }

    @Override
    public void abandonConversation(Conversation conversation, ConversationAbandonedEvent details) {

    }

    @Override
    public boolean isOnline() {

        return false;
    }

    @Override
    public boolean isConnected() {
        return false;
    }

    @Override
    public boolean isBanned() {

        return false;
    }

    @Override
    public <E extends BanEntry<? super PlayerProfile>> @org.jetbrains.annotations.Nullable E ban(@org.jetbrains.annotations.Nullable String s, @org.jetbrains.annotations.Nullable Date date, @org.jetbrains.annotations.Nullable String s1) {
        return null;
    }

    @Override
    public <E extends BanEntry<? super PlayerProfile>> @org.jetbrains.annotations.Nullable E ban(@org.jetbrains.annotations.Nullable String s, @org.jetbrains.annotations.Nullable Instant instant, @org.jetbrains.annotations.Nullable String s1) {
        return null;
    }

    @Override
    public <E extends BanEntry<? super PlayerProfile>> @org.jetbrains.annotations.Nullable E ban(@org.jetbrains.annotations.Nullable String s, @org.jetbrains.annotations.Nullable Duration duration, @org.jetbrains.annotations.Nullable String s1) {
        return null;
    }

    @Override
    public boolean isWhitelisted() {

        return false;
    }

    @Override
    public void setWhitelisted(boolean value) {

    }

    @Override
    public Player getPlayer() {

        return null;
    }

    @Override
    public long getFirstPlayed() {

        return 0;
    }

    @Override
    public long getLastPlayed() {

        return 0;
    }

    @Override
    public boolean hasPlayedBefore() {

        return false;
    }

    @Override
    public Map<String, Object> serialize() {

        return null;
    }

    @Override
    public void sendPluginMessage(Plugin source, String channel, byte[] message) {

    }

    @Override
    public Set<String> getListeningPluginChannels() {

        return null;
    }

    @Override
    public @NotNull Identity identity() {
        return Player.super.identity();
    }

    @Override
    public @UnmodifiableView @NotNull Iterable<? extends BossBar> activeBossBars() {
        return null;
    }

    @Override
    public @NotNull Component displayName() {
        return Component.text("Spoof");
    }

    @Override
    public void displayName(@Nullable Component component) {

    }

    @Override
    public String getDisplayName() {
        return "Spoof";
    }

    @Override
    public void setDisplayName(String name) {

    }

    @Override
    public void playerListName(@Nullable Component component) {

    }

    @Override
    public @Nullable Component playerListName() {
        return null;
    }

    @Override
    public @Nullable Component playerListHeader() {
        return null;
    }

    @Override
    public @Nullable Component playerListFooter() {
        return null;
    }

    @Override
    public String getPlayerListName() {
        return "Spoof";
    }

    @Override
    public void setPlayerListName(String name) {

    }

    @Override
    public void setCompassTarget(Location loc) {

    }

    @Override
    public Location getCompassTarget() {

        return null;
    }

    @Override
    public InetSocketAddress getAddress() {

        return null;
    }

    @Override
	public @org.jetbrains.annotations.Nullable InetSocketAddress getHAProxyAddress() {
		return null;
	}

	@Override
	public boolean isTransferred() {
		return false;
	}

	@Override
	public @NotNull CompletableFuture<byte[]> retrieveCookie(@NotNull NamespacedKey key) {
		return null;
	}

	@Override
	public void storeCookie(@NotNull NamespacedKey key, @NotNull byte[] value) {

	}

	@Override
	public void transfer(@NotNull String host, int port) {

	}

	@Override
    public void sendRawMessage(String message) {

    }

    @Override
    public void kickPlayer(String message) {

    }

    @Override
    public void kick() {

    }

    @Override
    public void kick(@Nullable Component component) {

    }

    @Override
    public void kick(@Nullable Component component,
                     PlayerKickEvent.Cause cause) {

    }

    @Override
    public <E extends BanEntry<? super PlayerProfile>> @org.jetbrains.annotations.Nullable E ban(@org.jetbrains.annotations.Nullable String s, @org.jetbrains.annotations.Nullable Date date, @org.jetbrains.annotations.Nullable String s1, boolean b) {
        return null;
    }

    @Override
    public <E extends BanEntry<? super PlayerProfile>> @org.jetbrains.annotations.Nullable E ban(@org.jetbrains.annotations.Nullable String s, @org.jetbrains.annotations.Nullable Instant instant, @org.jetbrains.annotations.Nullable String s1, boolean b) {
        return null;
    }

    @Override
    public <E extends BanEntry<? super PlayerProfile>> @org.jetbrains.annotations.Nullable E ban(@org.jetbrains.annotations.Nullable String s, @org.jetbrains.annotations.Nullable Duration duration, @org.jetbrains.annotations.Nullable String s1, boolean b) {
        return null;
    }

    @Override
    public @org.jetbrains.annotations.Nullable BanEntry<InetAddress> banIp(@org.jetbrains.annotations.Nullable String s, @org.jetbrains.annotations.Nullable Date date, @org.jetbrains.annotations.Nullable String s1, boolean b) {
        return null;
    }

    @Override
    public @org.jetbrains.annotations.Nullable BanEntry<InetAddress> banIp(@org.jetbrains.annotations.Nullable String s, @org.jetbrains.annotations.Nullable Instant instant, @org.jetbrains.annotations.Nullable String s1, boolean b) {
        return null;
    }

    @Override
    public @org.jetbrains.annotations.Nullable BanEntry<InetAddress> banIp(@org.jetbrains.annotations.Nullable String s, @org.jetbrains.annotations.Nullable Duration duration, @org.jetbrains.annotations.Nullable String s1, boolean b) {
        return null;
    }

    @Override
    public void chat(String msg) {

    }

    @Override
    public boolean performCommand(String command) {

        return false;
    }

    @Override
    public boolean isSneaking() {

        return false;
    }

    @Override
    public void setSneaking(boolean sneak) {

    }

    @Override
    public void setPose(@NotNull Pose pose, boolean b) {

    }

    @Override
    public boolean hasFixedPose() {
        return false;
    }

    @Override
    public boolean isSprinting() {

        return false;
    }

    @Override
    public void setSprinting(boolean sprinting) {

    }

    @Override
    public void saveData() {

    }

    @Override
    public void loadData() {

    }

    @Override
    public void setSleepingIgnored(boolean isSleeping) {

    }

    @Override
    public boolean isSleepingIgnored() {

        return false;
    }

    @Override
    public void playNote(Location loc, byte instrument, byte note) {

    }

    @Override
    public void playNote(Location loc, Instrument instrument, Note note) {

    }

    @Override
    public void playSound(Location location, Sound sound, float volume, float pitch) {

    }

    @Override
    public void playSound(Location location, String sound, float volume, float pitch) {

    }

    @Override
    public void playEffect(Location loc, Effect effect, int data) {

    }

    @Override
    public <T> void playEffect(Location loc, Effect effect, T data) {

    }

    @Override
    public boolean breakBlock(@NotNull Block block) {
        return false;
    }

    @Override
    public void sendBlockChange(Location loc, Material material, byte data) {

    }

    @Override
    public void sendSignChange(Location loc, String[] lines) throws IllegalArgumentException {

    }

    @Override
    public void sendMap(MapView map) {

    }

    @Override
    public void showWinScreen() {

    }

    @Override
    public boolean hasSeenWinScreen() {
        return false;
    }

    @Override
    public void setHasSeenWinScreen(boolean b) {

    }

    @Override
    public void updateInventory() {

    }

    @Override
    public @Nullable GameMode getPreviousGameMode() {
        return null;
    }

    @Override
    public void incrementStatistic(Statistic statistic) throws IllegalArgumentException {

    }

    @Override
    public void decrementStatistic(Statistic statistic) throws IllegalArgumentException {

    }

    @Override
    public void incrementStatistic(Statistic statistic, int amount) throws IllegalArgumentException {

    }

    @Override
    public void decrementStatistic(Statistic statistic, int amount) throws IllegalArgumentException {

    }

    @Override
    public void setStatistic(Statistic statistic, int newValue) throws IllegalArgumentException {

    }

    @Override
    public int getStatistic(Statistic statistic) throws IllegalArgumentException {

        return 0;
    }

    @Override
    public void incrementStatistic(Statistic statistic, Material material) throws IllegalArgumentException {

    }

    @Override
    public void decrementStatistic(Statistic statistic, Material material) throws IllegalArgumentException {

    }

    @Override
    public int getStatistic(Statistic statistic, Material material) throws IllegalArgumentException {

        return 0;
    }

    @Override
    public void incrementStatistic(Statistic statistic, Material material, int amount) throws IllegalArgumentException {

    }

    @Override
    public void decrementStatistic(Statistic statistic, Material material, int amount) throws IllegalArgumentException {

    }

    @Override
    public void setStatistic(Statistic statistic, Material material, int newValue) throws IllegalArgumentException {

    }

    @Override
    public void incrementStatistic(Statistic statistic, EntityType entityType) throws IllegalArgumentException {

    }

    @Override
    public void decrementStatistic(Statistic statistic, EntityType entityType) throws IllegalArgumentException {

    }

    @Override
    public int getStatistic(Statistic statistic, EntityType entityType) throws IllegalArgumentException {

        return 0;
    }

    @Override
    public void incrementStatistic(Statistic statistic, EntityType entityType, int amount)
        throws IllegalArgumentException {

    }

    @Override
    public void decrementStatistic(Statistic statistic, EntityType entityType, int amount) {

    }

    @Override
    public void setStatistic(Statistic statistic, EntityType entityType, int newValue) {

    }

    @Override
    public void setPlayerTime(long time, boolean relative) {

    }

    @Override
    public long getPlayerTime() {

        return 0;
    }

    @Override
    public long getPlayerTimeOffset() {

        return 0;
    }

    @Override
    public boolean isPlayerTimeRelative() {

        return false;
    }

    @Override
    public void resetPlayerTime() {

    }

    @Override
    public void setPlayerWeather(WeatherType type) {

    }

    @Override
    public WeatherType getPlayerWeather() {

        return null;
    }

    @Override
    public void resetPlayerWeather() {

    }

    @Override
    public void giveExp(int amount) {

    }

    @Override
    public int getExpCooldown() {
        return 0;
    }

    @Override
    public void setExpCooldown(int i) {

    }

    @Override
    public void giveExpLevels(int amount) {

    }

    @Override
    public float getExp() {

        return 0;
    }

    @Override
    public void setExp(float exp) {

    }

    @Override
    public int getLevel() {

        return 0;
    }

    @Override
    public void setLevel(int level) {

    }

    @Override
    public int getTotalExperience() {

        return 0;
    }

    @Override
    public void setTotalExperience(int exp) {

    }

    @Override
    public @Range(from = 0L, to = 2147483647L) int calculateTotalExperiencePoints() {
        return 0;
    }

    @Override
    public void setExperienceLevelAndProgress(@Range(from = 0L, to = 2147483647L) int i) {

    }

    @Override
    public int getExperiencePointsNeededForNextLevel() {
        return 0;
    }

    @Override
    public float getExhaustion() {

        return 0;
    }

    @Override
    public void setExhaustion(float value) {

    }

    @Override
    public float getSaturation() {

        return 0;
    }

    @Override
    public void setSaturation(float value) {

    }

    @Override
    public int getFoodLevel() {

        return 0;
    }

    @Override
    public void setFoodLevel(int value) {

    }

    @Override
    public int getSaturatedRegenRate() {
        return 0;
    }

    @Override
    public void setSaturatedRegenRate(int i) {

    }

    @Override
    public int getUnsaturatedRegenRate() {
        return 0;
    }

    @Override
    public void setUnsaturatedRegenRate(int i) {

    }

    @Override
    public int getStarvationRate() {
        return 0;
    }

    @Override
    public void setStarvationRate(int i) {

    }

    @Override
    public @org.jetbrains.annotations.Nullable Location getLastDeathLocation() {
        return null;
    }

    @Override
    public void setLastDeathLocation(@org.jetbrains.annotations.Nullable Location location) {

    }

    @Override
    public @org.jetbrains.annotations.Nullable Firework fireworkBoost(@NotNull ItemStack itemStack) {
        return null;
    }

    @Override
    public Location getBedSpawnLocation() {

        return null;
    }

    @Override
    public @org.jetbrains.annotations.Nullable Location getRespawnLocation() {
        return null;
    }

    @Override
    public void setBedSpawnLocation(Location location) {

    }

    @Override
    public void setRespawnLocation(@org.jetbrains.annotations.Nullable Location location) {

    }

    @Override
    public void setBedSpawnLocation(Location location, boolean force) {

    }

    @Override
    public void setRespawnLocation(@org.jetbrains.annotations.Nullable Location location, boolean b) {

    }

    @Override
    public boolean getAllowFlight() {

        return false;
    }

    @Override
    public void setAllowFlight(boolean flight) {

    }

    @Override
    public void setFlyingFallDamage(@NotNull TriState triState) {

    }

    @Override
    public @NotNull TriState hasFlyingFallDamage() {
        return null;
    }

    @Override
    public void hidePlayer(Plugin plugin, Player player) {

    }

    @Override
    public void hidePlayer(Player player) {

    }

    @Override
    public void showPlayer(Plugin plugin, Player player) {

    }

    @Override
    public void showPlayer(Player player) {

    }

    @Override
    public boolean canSee(Player player) {

        return false;
    }

    @Override
    public void hideEntity(Plugin plugin, Entity entity) {

    }

    @Override
    public void showEntity(Plugin plugin, Entity entity) {

    }

    @Override
    public boolean canSee(Entity entity) {
        return false;
    }

    @Override
    public boolean isListed(@NotNull Player player) {
        return false;
    }

    @Override
    public boolean unlistPlayer(@NotNull Player player) {
        return false;
    }

    @Override
    public boolean listPlayer(@NotNull Player player) {
        return false;
    }

    @Override
    public boolean isOnGround() {

        return false;
    }

    @Override
    public boolean isFlying() {

        return false;
    }

    @Override
    public void setFlying(boolean value) {

    }

    @Override
    public void setFlySpeed(float value) throws IllegalArgumentException {

    }

    @Override
    public void setWalkSpeed(float value) throws IllegalArgumentException {

    }

    @Override
    public float getFlySpeed() {

        return 0;
    }

    @Override
    public float getWalkSpeed() {

        return 0;
    }

    @Override
    public void setTexturePack(String url) {

    }

    @Override
    public void setResourcePack(String url) {

    }

    @Override
    public Scoreboard getScoreboard() {

        return null;
    }

    @Override
    public void setScoreboard(Scoreboard scoreboard) throws IllegalArgumentException, IllegalStateException {

    }

    @Override
    public @Nullable WorldBorder getWorldBorder() {
        return null;
    }

    @Override
    public void setWorldBorder(@Nullable WorldBorder border) {

    }

    @Override
    public boolean isHealthScaled() {

        return false;
    }

    @Override
    public void setHealthScaled(boolean scale) {

    }

    @Override
    public void setHealthScale(double scale) throws IllegalArgumentException {

    }

    @Override
    public double getHealthScale() {

        return 0;
    }

    @Override
    public void sendHealthUpdate(double v, int i, float v1) {

    }

    @Override
    public void sendHealthUpdate() {

    }

    @Override
    public Entity getSpectatorTarget() {

        return null;
    }

    @Override
    public void setSpectatorTarget(Entity entity) {

    }

    @Override
    public void sendTitle(String title, String subtitle) {

    }

    @Override
    public void resetTitle() {

    }

    @Override
    public void spawnParticle(Particle particle, Location location, int count) {

    }

    @Override
    public void spawnParticle(Particle particle, double x, double y, double z, int count) {

    }

    @Override
    public <T> void spawnParticle(Particle particle, Location location, int count, T data) {

    }

    @Override
    public <T> void spawnParticle(Particle particle, double x, double y, double z, int count, T data) {

    }

    @Override
    public void spawnParticle(Particle particle, Location location, int count, double offsetX, double offsetY,
                              double offsetZ) {

    }

    @Override
    public void spawnParticle(Particle particle, double x, double y, double z, int count, double offsetX,
                              double offsetY, double offsetZ) {

    }

    @Override
    public <T> void spawnParticle(Particle particle, Location location, int count, double offsetX, double offsetY,
                                  double offsetZ, T data) {

    }

    @Override
    public <T> void spawnParticle(Particle particle, double x, double y, double z, int count, double offsetX,
                                  double offsetY, double offsetZ, T data) {

    }

    @Override
    public void spawnParticle(Particle particle, Location location, int count, double offsetX, double offsetY,
                              double offsetZ, double extra) {

    }

	@Override
	public void spawnParticle(Particle particle, double x, double y, double z, int count, double offsetX,
				double offsetY, double offsetZ, double extra) {

    }

    @Override
    public <T> void spawnParticle(Particle particle, Location location, int count, double offsetX, double offsetY,
                                  double offsetZ, double extra, T data) {

    }

	@Override
	public <T> void spawnParticle(Particle particle, double x, double y, double z, int count, double offsetX,
								  double offsetY, double offsetZ, double extra, T data) {

    }

    @Override
	public <T> void spawnParticle(@NotNull Particle particle, @NotNull Location location, int i, double v, double v1, double v2, double v3, @org.jetbrains.annotations.Nullable T t, boolean b) {

	}

	@Override
	public <T> void spawnParticle(@NotNull Particle particle, double v, double v1, double v2, int i, double v3, double v4, double v5, double v6, @org.jetbrains.annotations.Nullable T t, boolean b) {

	}

	@Override
    public Spigot spigot() {
        return null;
    }

    @Override
    public void sendEntityEffect(@NotNull EntityEffect effect, @NotNull Entity target) {

    }

    @Override
    public Component name() {
        return null;
    }

    @Override
    public Component teamDisplayName() {
        return null;
    }

    @Override
    public boolean isSilent() {
        return false;
    }

    @Override
    public void setSilent(boolean flag) {
    }

    @Override
    public boolean hasGravity() {
        return false;
    }

    @Override
    public void setGravity(boolean gravity) {
    }

    @Override
    public void stopSound(Sound sound) {
    }

    @Override
    public void stopSound(String sound) {
    }

    @Override
    public int getCooldown(Material arg0) {
        return 0;
    }

    @Override
    public Entity getShoulderEntityLeft() {
        return null;
    }

    @Override
    public Entity getShoulderEntityRight() {
        return null;
    }

    @Override
    public boolean hasCooldown(Material arg0) {
        return false;
    }

    @Override
    public boolean isHandRaised() {
        return false;
    }

    @Override
    public @NotNull EquipmentSlot getHandRaised() {
        return null;
    }

    @Override
    public @Nullable ItemStack getItemInUse() {
        return null;
    }

    @Override
    public int getItemInUseTicks() {
        return 0;
    }

    @Override
    public void setItemInUseTicks(int i) {

    }

    @Override
    public InventoryView openMerchant(Merchant arg0, boolean arg1) {
        return null;
    }

    @Override
    public void setCooldown(Material arg0, int arg1) {

    }

    @Override
    public boolean isDeeplySleeping() {
        return false;
    }

    @Override
    public void setShoulderEntityLeft(Entity arg0) {

    }

    @Override
    public void setShoulderEntityRight(Entity arg0) {

    }

    @Override
    public PotionEffect getPotionEffect(PotionEffectType arg0) {
        return null;
    }

    @Override
    public boolean addPassenger(Entity arg0) {
        return false;
    }

    @Override
    public boolean addScoreboardTag(String arg0) {
        return false;
    }

    @Override
    public double getHeight() {
        return 1.0;
    }

    @Override
    public List<Entity> getPassengers() {
        return null;
    }

    @Override
    public int getPortalCooldown() {
        return 0;
    }

    @Override
    public Set<String> getScoreboardTags() {
        return null;
    }

    @Override
    public double getWidth() {
        return 0.33;
    }

    @Override
    public boolean removePassenger(Entity arg0) {
        return false;
    }

    @Override
    public boolean removeScoreboardTag(String arg0) {
        return false;
    }

    @Override
    public void setPortalCooldown(int arg0) {

    }

    @Override
    public AdvancementProgress getAdvancementProgress(Advancement arg0) {
        return null;
    }

    @Override
    public String getLocale() {
        return null;
    }

    @Override
    public void playSound(Location arg0, Sound arg1, SoundCategory arg2, float arg3, float arg4) {

    }

    @Override
    public void playSound(Location arg0, String arg1, SoundCategory arg2, float arg3, float arg4) {

    }

    @Override
    public void playSound(@NotNull Location location, @NotNull Sound sound, @NotNull SoundCategory soundCategory, float v, float v1, long l) {

    }

    @Override
    public void playSound(@NotNull Location location, @NotNull String s, @NotNull SoundCategory soundCategory, float v, float v1, long l) {

    }

    @Override
    public void playSound(@NotNull Entity entity, @NotNull Sound sound, float volume, float pitch) {

    }

    @Override
    public void playSound(@NotNull Entity entity, @NotNull String s, float v, float v1) {

    }

    @Override
    public void playSound(@NotNull Entity entity, @NotNull Sound sound,
                          @NotNull SoundCategory category, float volume, float pitch) {

    }

    @Override
    public void playSound(@NotNull Entity entity, @NotNull String s, @NotNull SoundCategory soundCategory, float v, float v1) {

    }

    @Override
    public void playSound(@NotNull Entity entity, @NotNull Sound sound, @NotNull SoundCategory soundCategory, float v, float v1, long l) {

    }

    @Override
    public void playSound(@NotNull Entity entity, @NotNull String s, @NotNull SoundCategory soundCategory, float v, float v1, long l) {

    }

    @Override
    public void sendTitle(String arg0, String arg1, int arg2, int arg3, int arg4) {

    }

    @Override
    public void setResourcePack(String arg0, byte[] arg1) {

    }

    @Override
    public void setResourcePack(@NotNull String url,
                                @Nullable byte[] hash,
                                @Nullable String prompt) {
    }

    @Override
    public void setResourcePack(@NotNull String url, @Nullable byte[] hash, boolean force) {

    }

    @Override
    public void setResourcePack(@NotNull String url, @Nullable byte[] hash,
                                @Nullable String prompt, boolean force) {

    }

    @Override
    public void setResourcePack(@NotNull String url, @Nullable byte[] hash,
                                @Nullable Component prompt, boolean force) {

    }

    @Override
    public void setResourcePack(@NotNull UUID uuid, @NotNull String s, @org.jetbrains.annotations.Nullable byte[] bytes, @org.jetbrains.annotations.Nullable String s1, boolean b) {

    }

    @Override
    public void setResourcePack(@NotNull UUID uuid, @NotNull String s, byte @org.jetbrains.annotations.Nullable [] bytes, @org.jetbrains.annotations.Nullable Component component, boolean b) {

    }

    @Override
    public void stopSound(Sound arg0, SoundCategory arg1) {

    }

    @Override
    public void stopSound(String arg0, SoundCategory arg1) {

    }

    @Override
    public void stopSound(@NotNull SoundCategory soundCategory) {

    }

    @Override
    public void stopAllSounds() {

    }

    @Override
    public PistonMoveReaction getPistonMoveReaction() {
        return null;
    }

    @Override
    public boolean isSwimming() {
        return false;
    }

    @Override
    public void setSwimming(boolean arg0) {

    }

    @Override
    public void sendBlockChange(Location arg0, BlockData arg1) {

    }

    @Override
    public void sendBlockChanges(@NotNull Collection<BlockState> collection) {

    }

    @Override
    public void sendBlockChanges(@NotNull Collection<BlockState> collection, boolean b) {

    }

    @Override
    public void sendBlockDamage(@NotNull Location location, float f) {

    }

    @Override
    public void sendMultiBlockChange(@NotNull Map<? extends Position, BlockData> map) {

    }

    @Override
    public void sendBlockDamage(@NotNull Location location, float v, @NotNull Entity entity) {

    }

    @Override
    public void sendBlockDamage(@NotNull Location location, float v, int i) {

    }

    @Override
    public void sendEquipmentChange(LivingEntity livingEntity, EquipmentSlot equipmentSlot, ItemStack itemStack) {

    }

    @Override
    public void sendEquipmentChange(@NotNull LivingEntity livingEntity, @NotNull Map<EquipmentSlot, ItemStack> map) {

    }

    @Override
    public void sendSignChange(@NotNull Location location, @org.jetbrains.annotations.Nullable List<? extends Component> list, @NotNull DyeColor dyeColor, boolean b) throws IllegalArgumentException {

    }

    @Override
    public boolean isRiptiding() {
        return false;
    }

    @Override
    public void setRiptiding(boolean riptiding) {

    }

    @Override
    public boolean isPersistent() {
        return false;
    }

    @Override
    public void setPersistent(boolean arg0) {
    }

    @Override
    public String getPlayerListFooter() {
        return null;
    }

    @Override
    public String getPlayerListHeader() {
        return null;
    }

    @Override
    public void setPlayerListFooter(String arg0) {
    }

    @Override
    public void setPlayerListHeader(String arg0) {
    }

    @Override
    public void setPlayerListHeaderFooter(String arg0, String arg1) {
    }

    @Override
    public void updateCommands() {
    }

    @Override
    public boolean sleep(Location location, boolean force) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void wakeup(boolean setSpawnLocation) {
        // TODO Auto-generated method stub

    }

    @Override
    public void startRiptideAttack(int duration, float attackStrength, @Nullable ItemStack attackItem) {

    }

    @Override
    public Location getBedLocation() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean discoverRecipe(NamespacedKey recipe) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public int discoverRecipes(Collection<NamespacedKey> recipes) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean undiscoverRecipe(NamespacedKey recipe) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public int undiscoverRecipes(Collection<NamespacedKey> recipes) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Block getTargetBlockExact(int maxDistance) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Block getTargetBlockExact(int maxDistance, FluidCollisionMode fluidCollisionMode) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public RayTraceResult rayTraceBlocks(double maxDistance) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public RayTraceResult rayTraceBlocks(double maxDistance, FluidCollisionMode fluidCollisionMode) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T> T getMemory(MemoryKey<T> memoryKey) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T> void setMemory(MemoryKey<T> memoryKey, T memoryValue) {
        // TODO Auto-generated method stub

    }

    @Override
    public @org.jetbrains.annotations.Nullable Sound getHurtSound() {
        return null;
    }

    @Override
    public @org.jetbrains.annotations.Nullable Sound getDeathSound() {
        return null;
    }

    @Override
    public @NotNull Sound getFallDamageSound(int i) {
        return null;
    }

    @Override
    public @NotNull Sound getFallDamageSoundSmall() {
        return null;
    }

    @Override
    public @NotNull Sound getFallDamageSoundBig() {
        return null;
    }

    @Override
    public @NotNull Sound getDrinkingSound(@NotNull ItemStack itemStack) {
        return null;
    }

    @Override
    public @NotNull Sound getEatingSound(@NotNull ItemStack itemStack) {
        return null;
    }

    @Override
    public boolean canBreatheUnderwater() {
        return false;
    }

    @Override
    public double getAbsorptionAmount() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void setAbsorptionAmount(double arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public BoundingBox getBoundingBox() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setRotation(float yaw, float pitch) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean teleport(@NotNull Location location, @NotNull PlayerTeleportEvent.TeleportCause teleportCause, @NotNull TeleportFlag @NotNull ... teleportFlags) {
        return false;
    }

    @Override
    public void lookAt(double v, double v1, double v2, @NotNull LookAnchor lookAnchor) {

    }

    @Override
    public void lookAt(@NotNull Entity entity, @NotNull LookAnchor lookAnchor, @NotNull LookAnchor lookAnchor1) {

    }

    @Override
    public void showElderGuardian(boolean b) {

    }

    @Override
    public int getWardenWarningCooldown() {
        return 0;
    }

    @Override
    public void setWardenWarningCooldown(int i) {

    }

    @Override
    public int getWardenTimeSinceLastWarning() {
        return 0;
    }

    @Override
    public void setWardenTimeSinceLastWarning(int i) {

    }

    @Override
    public int getWardenWarningLevel() {
        return 0;
    }

    @Override
    public void setWardenWarningLevel(int i) {

    }

    @Override
    public void increaseWardenWarningLevel() {

    }

    @Override
    public @NotNull Duration getIdleDuration() {
        return null;
    }

    @Override
    public void resetIdleDuration() {

    }

    @Override
    public @NotNull @Unmodifiable Set<Long> getSentChunkKeys() {
        return Collections.emptySet();
    }

    @Override
    public @NotNull @Unmodifiable Set<Chunk> getSentChunks() {
        return Collections.emptySet();
    }

    @Override
    public boolean isChunkSent(long l) {
        return false;
    }

    @Override
    public BlockFace getFacing() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Pose getPose() {
        // TODO Auto-generated metod stub
        return null;
    }

    @Override
    public @NotNull SpawnCategory getSpawnCategory() {
        return null;
    }

    @Override
    public boolean isInWorld() {
        return false;
    }

    @Override
	public @org.jetbrains.annotations.Nullable String getAsString() {
		return "";
	}

	@Override
    public @org.jetbrains.annotations.Nullable EntitySnapshot createSnapshot() {
        return null;
    }

    @Override
    public @NotNull Entity copy() {
        return null;
    }

    @Override
    public @NotNull Entity copy(@NotNull Location location) {
        return null;
    }

    @Override
    public PersistentDataContainer getPersistentDataContainer() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void sendSignChange(Location loc, String[] lines, DyeColor dyeColor) throws IllegalArgumentException {
        // TODO Auto-generated method stub

    }

    @Override
    public void sendSignChange(@NotNull Location location,
                               @Nullable String[] strings,
                               @NotNull DyeColor dyeColor, boolean bl)
        throws IllegalArgumentException {

    }

    @Override
    public void sendBlockUpdate(@NotNull Location location, @NotNull TileState tileState) throws IllegalArgumentException {

    }

    @Override
    public void sendPotionEffectChange(@NotNull LivingEntity livingEntity, @NotNull PotionEffect potionEffect) {

    }

    @Override
    public void sendPotionEffectChangeRemove(@NotNull LivingEntity livingEntity, @NotNull PotionEffectType potionEffectType) {

    }

    @Override
    public int getClientViewDistance() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public @NotNull Locale locale() {
        return Locale.ENGLISH;
    }

    @Override
    public int getPing() {
        return 0;
    }

    @Override
    public void openBook(ItemStack book) {
        // TODO Auto-generated method stub

    }

    @Override
    public float getAttackCooldown() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean hasDiscoveredRecipe(NamespacedKey recipe) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Set<NamespacedKey> getDiscoveredRecipes() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void attack(Entity target) {
        // TODO Auto-generated method stub

    }

    @Override
    public void swingMainHand() {
        // TODO Auto-generated method stub

    }

    @Override
    public void swingOffHand() {
        // TODO Auto-generated method stub

    }

    @Override
    public void playHurtAnimation(float v) {

    }

    @Override
    public Set<UUID> getCollidableExemptions() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void sendExperienceChange(float progress) {
        // TODO Auto-generated method stub

    }

    @Override
    public void sendExperienceChange(float progress, int level) {
        // TODO Auto-generated method stub

    }

    @Override
    public void closeInventory(Reason arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean dropItem(boolean arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Location getPotentialBedLocation() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public @org.jetbrains.annotations.Nullable FishHook getFishHook() {
        return null;
    }

    @Override
    public InventoryView openAnvil(Location arg0, boolean arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public InventoryView openCartographyTable(Location arg0, boolean arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public InventoryView openGrindstone(Location arg0, boolean arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public InventoryView openLoom(Location arg0, boolean arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void openSign(Sign arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void openSign(@NotNull Sign sign, @NotNull Side side) {

    }

    @Override
    public void showDemoScreen() {

    }

    @Override
    public boolean isAllowingServerListings() {
        return false;
    }

    @Override
    public InventoryView openSmithingTable(Location arg0, boolean arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public InventoryView openStonecutter(Location arg0, boolean arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Entity releaseLeftShoulderEntity() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Entity releaseRightShoulderEntity() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void clearActiveItem() {
        // TODO Auto-generated method stub

    }

    @Override
    public int getActiveItemRemainingTime() {
        return 0;
    }

    @Override
    public void setActiveItemRemainingTime(@Range(from = 0L, to = 2147483647L) int i) {

    }

    @Override
    public boolean hasActiveItem() {
        return false;
    }

    @Override
    public int getActiveItemUsedTime() {
        return 0;
    }

    @Override
    public @NotNull EquipmentSlot getActiveItemHand() {
        return null;
    }

    @Override
    public float getSidewaysMovement() {
        return 0;
    }

    @Override
    public float getUpwardsMovement() {
        return 0;
    }

    @Override
    public float getForwardsMovement() {
        return 0;
    }

    @Override
    public void startUsingItem(@NotNull EquipmentSlot equipmentSlot) {

    }

    @Override
    public void completeUsingActiveItem() {

    }

    @Override
    public ItemStack getActiveItem() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getArrowCooldown() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getArrowsInBody() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getArrowsStuck() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public EntityCategory getCategory() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getHandRaisedTime() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public float getHurtDirection() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getItemUseRemainingTime() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getShieldBlockingDelay() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Block getTargetBlock(int arg0, FluidMode arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public BlockFace getTargetBlockFace(int arg0, FluidMode arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public @org.jetbrains.annotations.Nullable BlockFace getTargetBlockFace(int i, @NotNull FluidCollisionMode fluidCollisionMode) {
        return null;
    }

    @Override
    public TargetBlockInfo getTargetBlockInfo(int arg0, FluidMode arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Entity getTargetEntity(int arg0, boolean arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TargetEntityInfo getTargetEntityInfo(int arg0, boolean arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public @org.jetbrains.annotations.Nullable RayTraceResult rayTraceEntities(int i, boolean b) {
        return null;
    }

    @Override
    public boolean isInvisible() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void setNoPhysics(boolean b) {

    }

    @Override
    public boolean hasNoPhysics() {
        return false;
    }

    @Override
    public boolean isJumping() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void playPickupItemAnimation(Item arg0, int arg1) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setArrowCooldown(int arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setArrowsInBody(int arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setArrowsInBody(int i, boolean b) {

    }

    @Override
    public void setNextArrowRemoval(@Range(from = 0L, to = 2147483647L) int i) {

    }

    @Override
    public int getNextArrowRemoval() {
        return 0;
    }

    @Override
    public int getBeeStingerCooldown() {
        return 0;
    }

    @Override
    public void setBeeStingerCooldown(int i) {

    }

    @Override
    public int getBeeStingersInBody() {
        return 0;
    }

    @Override
    public void setBeeStingersInBody(int i) {

    }

    @Override
    public void setNextBeeStingerRemoval(@Range(from = 0L, to = 2147483647L) int i) {

    }

    @Override
    public int getNextBeeStingerRemoval() {
        return 0;
    }

    @Override
    public void setArrowsStuck(int arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setHurtDirection(float arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void knockback(double v, double v1, double v2) {

    }

    @Override
    public void broadcastSlotBreak(@NotNull EquipmentSlot equipmentSlot) {

    }

    @Override
    public void broadcastSlotBreak(@NotNull EquipmentSlot equipmentSlot, @NotNull Collection<Player> collection) {

    }

    @Override
    public @NotNull ItemStack damageItemStack(@NotNull ItemStack itemStack, int i) {
        return null;
    }

    @Override
    public void damageItemStack(@NotNull EquipmentSlot equipmentSlot, int i) {

    }

    @Override
    public float getBodyYaw() {
        return 0;
    }

    @Override
    public void setBodyYaw(float v) {

    }

    @Override
    public boolean canUseEquipmentSlot(@NotNull EquipmentSlot equipmentSlot) {
        return false;
    }

    @Override
    public void setInvisible(boolean arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setJumping(boolean arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setKiller(Player arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setShieldBlockingDelay(int arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean fromMobSpawner() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Chunk getChunk() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SpawnReason getEntitySpawnReason() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isUnderWater() {
        return false;
    }

    @Override
    public Location getOrigin() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isInBubbleColumn() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isInLava() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isInRain() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isInWater() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isInWaterOrBubbleColumn() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isInWaterOrRain() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isInWaterOrRainOrBubbleColumn() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isTicking() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void sendMessage(UUID arg0, String arg1) {
        // TODO Auto-generated method stub

    }

    @Override
    public void sendMessage(UUID arg0, String[] arg1) {
        // TODO Auto-generated method stub

    }

    @Override
    public void sendRawMessage(UUID arg0, String arg1) {
        // TODO Auto-generated method stub

    }

    @Override
    public long getLastLogin() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public long getLastSeen() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getProtocolVersion() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public InetSocketAddress getVirtualHost() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int applyMending(int arg0) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Firework boostElytra(ItemStack arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void sendOpLevel(byte b) {

    }

    @Override
    public void addAdditionalChatCompletions(@NotNull Collection<String> collection) {

    }

    @Override
    public void removeAdditionalChatCompletions(@NotNull Collection<String> collection) {

    }

    @Override
    public @NotNull Set<Player> getTrackedPlayers() {
        return null;
    }

    @Override
    public boolean spawnAt(Location location, SpawnReason spawnReason) {
        return false;
    }

    @Override
    public boolean isInPowderedSnow() {
        return false;
    }

    @Override
    public double getX() {
        return 0;
    }

    @Override
    public double getY() {
        return 0;
    }

    @Override
    public double getZ() {
        return 0;
    }

    @Override
    public float getPitch() {
        return 0;
    }

    @Override
    public float getYaw() {
        return 0;
    }

    @Override
    public boolean collidesAt(@NotNull Location location) {
        return false;
    }

    @Override
    public boolean wouldCollideUsing(@NotNull BoundingBox boundingBox) {
        return false;
    }

    @Override
    public @NotNull EntityScheduler getScheduler() {
        return null;
    }

    @Override
    public @NotNull String getScoreboardEntryName() {
        return null;
    }

    @Override
    public void broadcastHurtAnimation(@NotNull Collection<Player> players) {

    }

    @Override
    public boolean getAffectsSpawning() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public String getClientBrandName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T> T getClientOption(ClientOption<T> arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public float getCooldownPeriod() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public float getCooledAttackStrength(float arg0) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public PlayerProfile getPlayerProfile() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getResourcePackHash() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Status getResourcePackStatus() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getViewDistance() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void giveExp(int arg0, boolean arg1) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean hasResourcePack() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void addResourcePack(@NotNull UUID uuid, @NotNull String s, @org.jetbrains.annotations.Nullable byte[] bytes, @org.jetbrains.annotations.Nullable String s1, boolean b) {

    }

    @Override
    public void removeResourcePack(@NotNull UUID uuid) {

    }

    @Override
    public void removeResourcePacks() {

    }

    @Override
    public void hideTitle() {
        // TODO Auto-generated method stub

    }

    @Override
    public void sendHurtAnimation(float v) {

    }

    @Override
    public void sendLinks(@NotNull ServerLinks serverLinks) {

    }

    @Override
    public void addCustomChatCompletions(@NotNull Collection<String> collection) {

    }

    @Override
    public void removeCustomChatCompletions(@NotNull Collection<String> collection) {

    }

    @Override
    public void setCustomChatCompletions(@NotNull Collection<String> collection) {

    }

    @Override
    public void resetCooldown() {
        // TODO Auto-generated method stub

    }

    @Override
    public void sendActionBar(String arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void sendActionBar(BaseComponent... arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void sendActionBar(char arg0, String arg1) {
        // TODO Auto-generated method stub

    }

    @Override
    public void sendTitle(Title arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setAffectsSpawning(boolean arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setPlayerListHeaderFooter(BaseComponent[] arg0, BaseComponent[] arg1) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setPlayerListHeaderFooter(BaseComponent arg0, BaseComponent arg1) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setPlayerProfile(PlayerProfile arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setResourcePack(String arg0, String arg1) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setResourcePack(@NotNull String string,
                                @NotNull String string2, boolean bl) {

    }

    @Override
    public void setResourcePack(@NotNull String string,
                                @NotNull String string2, boolean bl,
                                @Nullable Component component) {

    }

    @Override
    public void setSubtitle(BaseComponent[] arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setSubtitle(BaseComponent arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setTitleTimes(int arg0, int arg1, int arg2) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setViewDistance(int arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public int getSimulationDistance() {
        return 0;
    }

    @Override
    public void setSimulationDistance(int simulationDistance) {

    }

    @Override
    public int getNoTickViewDistance() {
        return 0;
    }

    @Override
    public void setNoTickViewDistance(int i) {

    }

    @Override
    public int getSendViewDistance() {
        return 0;
    }

    @Override
    public void setSendViewDistance(int i) {

    }

    @Override
    public void showTitle(BaseComponent[] arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void showTitle(BaseComponent arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void showTitle(BaseComponent[] arg0, BaseComponent[] arg1, int arg2, int arg3, int arg4) {
        // TODO Auto-generated method stub

    }

    @Override
    public void showTitle(BaseComponent arg0, BaseComponent arg1, int arg2, int arg3, int arg4) {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateTitle(Title arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public @NotNull TriState getFrictionState() {
        return null;
    }

    @Override
    public void setFrictionState(@NotNull TriState triState) {

    }
}
