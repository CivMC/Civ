package org.bukkit.pseudo;

import com.destroystokyo.paper.entity.ai.MobGoals;
import com.destroystokyo.paper.profile.PlayerProfile;
import io.papermc.paper.datapack.DatapackManager;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.apache.commons.lang3.NotImplementedException;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Keyed;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.StructureType;
import org.bukkit.Tag;
import org.bukkit.UnsafeValues;
import org.bukkit.Warning;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.WorldCreator;
import org.bukkit.advancement.Advancement;
import org.bukkit.block.data.BlockData;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.boss.KeyedBossBar;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.craftbukkit.v1_18_R2.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_18_R2.inventory.CraftItemFactory;
import org.bukkit.craftbukkit.v1_18_R2.util.CraftMagicNumbers;
import org.bukkit.craftbukkit.v1_18_R2.util.Versioning;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.SpawnCategory;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.help.HelpMap;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.Recipe;
import org.bukkit.loot.LootTable;
import org.bukkit.map.MapView;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.plugin.messaging.Messenger;
import org.bukkit.potion.PotionBrewer;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.structure.StructureManager;
import org.bukkit.util.CachedServerIcon;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("deprecation")
public class PseudoServer implements Server {

	public static final PseudoServer INSTANCE = new PseudoServer();
	private static final Logger LOGGER = Logger.getLogger(PseudoServer.class.getSimpleName());

	public static void setup() {
		if (Bukkit.getServer() == null) { // Ignore highlighter
			final var previousLevel = LOGGER.getLevel();
			LOGGER.setLevel(Level.OFF); // This is to prevent unnecessary logging
			SharedConstants.tryDetectVersion(); // SharedConstants.tryDetectVersion()
			Bootstrap.bootStrap();
			Bootstrap.validate();
			Bukkit.setServer(INSTANCE);
			LOGGER.setLevel(previousLevel);
		}
	}

	@Nonnull
	@Override
	public Logger getLogger() {
		return LOGGER;
	}

	@Nonnull
	@Override
	public ItemFactory getItemFactory() {
		return CraftItemFactory.instance();
	}

	@Nonnull
	@Override
	public UnsafeValues getUnsafe() {
		return CraftMagicNumbers.INSTANCE;
	}

	@Nonnull
	@Override
	public BlockData createBlockData(@Nonnull final Material material) {
		return CraftBlockData.newData(material, null);
	}

	// ------------------------------------------------------------
	// Not implemented
	// ------------------------------------------------------------

	@Override
	public @NotNull File getPluginsFolder() {
		throw new NotImplementedException();
	}

	@Nonnull
	@Override
	public String getName() {
		return getClass().getSimpleName();
	}

	@Nonnull
	@Override
	public String getVersion() {
		return getClass().getPackage().getImplementationVersion();
	}

	@Nonnull
	@Override
	public String getBukkitVersion() {
		return Versioning.getBukkitVersion();
	}

	@Nonnull
	@Override
	public String getMinecraftVersion() {
		return SharedConstants.getCurrentVersion().toString();
	}

	@Nonnull
	@Override
	public Collection<? extends Player> getOnlinePlayers() {
		throw new NotImplementedException();
	}

	@Override
	public int getMaxPlayers() {
		throw new NotImplementedException();
	}

	@Override
	public void setMaxPlayers(final int i) {
		throw new NotImplementedException();
	}

	@Override
	public int getPort() {
		throw new NotImplementedException();
	}

	@Override
	public int getViewDistance() {
		throw new NotImplementedException();
	}

	@Override
	public int getSimulationDistance() {
		throw new NotImplementedException();
	}

	@Nonnull
	@Override
	public String getIp() {
		throw new NotImplementedException();
	}

	@Nonnull
	@Override
	public String getWorldType() {
		throw new NotImplementedException();
	}

	@Override
	public boolean getGenerateStructures() {
		throw new NotImplementedException();
	}

	@Override
	public int getMaxWorldSize() {
		throw new NotImplementedException();
	}

	@Override
	public boolean getAllowEnd() {
		throw new NotImplementedException();
	}

	@Override
	public boolean getAllowNether() {
		throw new NotImplementedException();
	}

	@Override
	public @NotNull String getResourcePack() {
		throw new NotImplementedException();
	}

	@Override
	public @NotNull String getResourcePackHash() {
		throw new NotImplementedException();
	}

	@Override
	public @NotNull String getResourcePackPrompt() {
		throw new NotImplementedException();
	}

	@Override
	public boolean isResourcePackRequired() {
		throw new NotImplementedException();
	}

	@Override
	public boolean hasWhitelist() {
		throw new NotImplementedException();
	}

	@Override
	public void setWhitelist(final boolean b) {
		throw new NotImplementedException();
	}

	@Override
	public boolean isWhitelistEnforced() {
		throw new NotImplementedException();
	}

	@Override
	public void setWhitelistEnforced(boolean bl) {
		throw new NotImplementedException();
	}

	@Nonnull
	@Override
	public Set<OfflinePlayer> getWhitelistedPlayers() {
		throw new NotImplementedException();
	}

	@Override
	public void reloadWhitelist() {
		throw new NotImplementedException();
	}

	@Override
	public int broadcastMessage(@Nonnull final String s) {
		throw new NotImplementedException();
	}

	@Nonnull
	@Override
	public String getUpdateFolder() {
		throw new NotImplementedException();
	}

	@Nonnull
	@Override
	public File getUpdateFolderFile() {
		throw new NotImplementedException();
	}

	@Override
	public long getConnectionThrottle() {
		throw new NotImplementedException();
	}

	@Override
	public int getTicksPerAnimalSpawns() {
		throw new NotImplementedException();
	}

	@Override
	public int getTicksPerMonsterSpawns() {
		throw new NotImplementedException();
	}

	@Override
	public int getTicksPerWaterSpawns() {
		throw new NotImplementedException();
	}

	@Override
	public int getTicksPerWaterAmbientSpawns() {
		throw new NotImplementedException();
	}

	@Override
	public int getTicksPerWaterUndergroundCreatureSpawns() {
		throw new NotImplementedException();
	}

	@Override
	public int getTicksPerAmbientSpawns() {
		throw new NotImplementedException();
	}

	@Override
	public int getTicksPerSpawns(@NotNull SpawnCategory spawnCategory) {
		throw new NotImplementedException();
	}

	@Nullable
	@Override
	public Player getPlayer(@Nonnull final String s) {
		throw new NotImplementedException();
	}

	@Nullable
	@Override
	public Player getPlayerExact(@Nonnull final String s) {
		throw new NotImplementedException();
	}

	@Nonnull
	@Override
	public List<Player> matchPlayer(@Nonnull final String s) {
		throw new NotImplementedException();
	}

	@Nullable
	@Override
	public Player getPlayer(@Nonnull final UUID uuid) {
		throw new NotImplementedException();
	}

	@Nullable
	@Override
	public UUID getPlayerUniqueId(@Nonnull final String s) {
		throw new NotImplementedException();
	}

	@Nonnull
	@Override
	public PluginManager getPluginManager() {
		throw new NotImplementedException();
	}

	@Nonnull
	@Override
	public BukkitScheduler getScheduler() {
		throw new NotImplementedException();
	}

	@Nonnull
	@Override
	public ServicesManager getServicesManager() {
		throw new NotImplementedException();
	}

	@Nonnull
	@Override
	public List<World> getWorlds() {
		throw new NotImplementedException();
	}

	@Nullable
	@Override
	public World createWorld(@Nonnull final WorldCreator worldCreator) {
		throw new NotImplementedException();
	}

	@Override
	public boolean unloadWorld(@Nonnull final String s, final boolean b) {
		throw new NotImplementedException();
	}

	@Override
	public boolean unloadWorld(@Nonnull final World world, final boolean b) {
		throw new NotImplementedException();
	}

	@Nullable
	@Override
	public World getWorld(@Nonnull final String s) {
		throw new NotImplementedException();
	}

	@Nullable
	@Override
	public World getWorld(@Nonnull final UUID uuid) {
		throw new NotImplementedException();
	}

	@Nullable
	@Override
	public World getWorld(@Nonnull final NamespacedKey namespacedKey) {
		throw new NotImplementedException();
	}

	@Override
	public @NotNull WorldBorder createWorldBorder() {
		throw new NotImplementedException();
	}

	@Nullable
	@Override
	public MapView getMap(final int i) {
		throw new NotImplementedException();
	}

	@Nonnull
	@Override
	public MapView createMap(@Nonnull final World world) {
		throw new NotImplementedException();
	}

	@Nonnull
	@Override
	public ItemStack createExplorerMap(@Nonnull final World world, @Nonnull final Location location, @Nonnull final StructureType structureType) {
		throw new NotImplementedException();
	}

	@Nonnull
	@Override
	public ItemStack createExplorerMap(@Nonnull final World world, @Nonnull final Location location, @Nonnull final StructureType structureType, final int i, final boolean b) {
		throw new NotImplementedException();
	}

	@Override
	public void reload() {
		throw new NotImplementedException();
	}

	@Override
	public void reloadData() {
		throw new NotImplementedException();
	}

	@Nullable
	@Override
	public PluginCommand getPluginCommand(@Nonnull final String s) {
		throw new NotImplementedException();
	}

	@Override
	public void savePlayers() {
		throw new NotImplementedException();
	}

	@Override
	public boolean dispatchCommand(@Nonnull final CommandSender commandSender, @Nonnull final String s) throws CommandException {
		throw new NotImplementedException();
	}

	@Override
	public boolean addRecipe(@Nullable final Recipe recipe) {
		throw new NotImplementedException();
	}

	@Nonnull
	@Override
	public List<Recipe> getRecipesFor(@Nonnull final ItemStack itemStack) {
		throw new NotImplementedException();
	}

	@Nullable
	@Override
	public Recipe getRecipe(@Nonnull final NamespacedKey namespacedKey) {
		throw new NotImplementedException();
	}

	@Nullable
	@Override
	public Recipe getCraftingRecipe(@Nonnull ItemStack[] itemStacks, @Nonnull World world) {
		throw new NotImplementedException();
	}

	@Nonnull
	@Override
	public ItemStack craftItem(@Nonnull ItemStack[] itemStacks, @Nonnull World world, @Nonnull Player player) {
		throw new NotImplementedException();
	}

	@Nonnull
	@Override
	public Iterator<Recipe> recipeIterator() {
		throw new NotImplementedException();
	}

	@Override
	public void clearRecipes() {
		throw new NotImplementedException();
	}

	@Override
	public void resetRecipes() {
		throw new NotImplementedException();
	}

	@Override
	public boolean removeRecipe(@Nonnull final NamespacedKey namespacedKey) {
		throw new NotImplementedException();
	}

	@Nonnull
	@Override
	public Map<String, String[]> getCommandAliases() {
		throw new NotImplementedException();
	}

	@Override
	public int getSpawnRadius() {
		throw new NotImplementedException();
	}

	@Override
	public void setSpawnRadius(final int i) {
		throw new NotImplementedException();
	}

	@Override
	public boolean getHideOnlinePlayers() {
		throw new NotImplementedException();
	}

	@Override
	public boolean getOnlineMode() {
		throw new NotImplementedException();
	}

	@Override
	public boolean getAllowFlight() {
		throw new NotImplementedException();
	}

	@Override
	public boolean isHardcore() {
		throw new NotImplementedException();
	}

	@Override
	public void shutdown() {
		throw new NotImplementedException();
	}

	@Override
	public int broadcast(@Nonnull final String s, @Nonnull final String s1) {
		throw new NotImplementedException();
	}

	@Override
	public int broadcast(@Nonnull Component component) {
		throw new NotImplementedException();
	}

	@Override
	public int broadcast(@Nonnull final Component component, @Nonnull final String s) {
		throw new NotImplementedException();
	}

	@Nonnull
	@Override
	public OfflinePlayer getOfflinePlayer(@Nonnull final String s) {
		throw new NotImplementedException();
	}

	@Nullable
	@Override
	public OfflinePlayer getOfflinePlayerIfCached(@Nonnull final String s) {
		throw new NotImplementedException();
	}

	@Nonnull
	@Override
	public OfflinePlayer getOfflinePlayer(@Nonnull final UUID uuid) {
		throw new NotImplementedException();
	}

	@Override
	public org.bukkit.profile.@NotNull PlayerProfile createPlayerProfile(
			@org.jetbrains.annotations.Nullable UUID uniqueId, @org.jetbrains.annotations.Nullable String name) {
		throw new NotImplementedException();
	}

	@Override
	public org.bukkit.profile.@NotNull PlayerProfile createPlayerProfile(
			@NotNull UUID uniqueId) {
		throw new NotImplementedException();
	}

	@Override
	public org.bukkit.profile.@NotNull PlayerProfile createPlayerProfile(
			@NotNull String name) {
		throw new NotImplementedException();
	}

	@Nonnull
	@Override
	public Set<String> getIPBans() {
		throw new NotImplementedException();
	}

	@Override
	public void banIP(@Nonnull final String s) {
		throw new NotImplementedException();
	}

	@Override
	public void unbanIP(@Nonnull final String s) {
		throw new NotImplementedException();
	}

	@Nonnull
	@Override
	public Set<OfflinePlayer> getBannedPlayers() {
		throw new NotImplementedException();
	}

	@Nonnull
	@Override
	public BanList getBanList(@Nonnull final BanList.Type type) {
		throw new NotImplementedException();
	}

	@Nonnull
	@Override
	public Set<OfflinePlayer> getOperators() {
		throw new NotImplementedException();
	}

	@Nonnull
	@Override
	public GameMode getDefaultGameMode() {
		throw new NotImplementedException();
	}

	@Override
	public void setDefaultGameMode(@Nonnull final GameMode gameMode) {
		throw new NotImplementedException();
	}

	@Nonnull
	@Override
	public ConsoleCommandSender getConsoleSender() {
		throw new NotImplementedException();
	}

	@Override
	public @NotNull CommandSender createCommandSender(
			@NotNull Consumer<? super Component> feedback) {
		throw new NotImplementedException();
	}

	@Nonnull
	@Override
	public File getWorldContainer() {
		throw new NotImplementedException();
	}

	@Nonnull
	@Override
	public OfflinePlayer[] getOfflinePlayers() {
		throw new NotImplementedException();
	}

	@Nonnull
	@Override
	public Messenger getMessenger() {
		throw new NotImplementedException();
	}

	@Nonnull
	@Override
	public HelpMap getHelpMap() {
		throw new NotImplementedException();
	}

	@Nonnull
	@Override
	public Inventory createInventory(@Nullable final InventoryHolder inventoryHolder, @Nonnull final InventoryType inventoryType) {
		throw new NotImplementedException();
	}

	@Nonnull
	@Override
	public Inventory createInventory(@Nullable final InventoryHolder inventoryHolder, @Nonnull final InventoryType inventoryType, @Nonnull final Component component) {
		throw new NotImplementedException();
	}

	@Nonnull
	@Override
	public Inventory createInventory(@Nullable final InventoryHolder inventoryHolder, @Nonnull final InventoryType inventoryType, @Nonnull final String s) {
		throw new NotImplementedException();
	}

	@Nonnull
	@Override
	public Inventory createInventory(@Nullable final InventoryHolder inventoryHolder, final int i) throws IllegalArgumentException {
		throw new NotImplementedException();
	}

	@Nonnull
	@Override
	public Inventory createInventory(@Nullable final InventoryHolder inventoryHolder, final int i, @Nonnull final Component component) throws IllegalArgumentException {
		throw new NotImplementedException();
	}

	@Nonnull
	@Override
	public Inventory createInventory(@Nullable final InventoryHolder inventoryHolder, final int i, @Nonnull final String s) throws IllegalArgumentException {
		throw new NotImplementedException();
	}

	@Nonnull
	@Override
	public Merchant createMerchant(@Nullable final Component component) {
		throw new NotImplementedException();
	}

	@Nonnull
	@Override
	public Merchant createMerchant(@Nullable final String s) {
		throw new NotImplementedException();
	}

	@Override
	public int getMonsterSpawnLimit() {
		throw new NotImplementedException();
	}

	@Override
	public int getAnimalSpawnLimit() {
		throw new NotImplementedException();
	}

	@Override
	public int getWaterAnimalSpawnLimit() {
		throw new NotImplementedException();
	}

	@Override
	public int getWaterAmbientSpawnLimit() {
		throw new NotImplementedException();
	}

	@Override
	public int getWaterUndergroundCreatureSpawnLimit() {
		throw new NotImplementedException();
	}

	@Override
	public int getAmbientSpawnLimit() {
		throw new NotImplementedException();
	}

	@Override
	public int getSpawnLimit(@NotNull SpawnCategory spawnCategory) {
		throw new NotImplementedException();
	}

	@Override
	public boolean isPrimaryThread() {
		throw new NotImplementedException();
	}

	@Nonnull
	@Override
	public Component motd() {
		throw new NotImplementedException();
	}

	@Nonnull
	@Override
	public String getMotd() {
		throw new NotImplementedException();
	}

	@Nullable
	@Override
	public Component shutdownMessage() {
		throw new NotImplementedException();
	}

	@Nullable
	@Override
	public String getShutdownMessage() {
		throw new NotImplementedException();
	}

	@Nonnull
	@Override
	public Warning.WarningState getWarningState() {
		throw new NotImplementedException();
	}

	@Nonnull
	@Override
	public ScoreboardManager getScoreboardManager() {
		throw new NotImplementedException();
	}

	@Nullable
	@Override
	public CachedServerIcon getServerIcon() {
		throw new NotImplementedException();
	}

	@Nonnull
	@Override
	public CachedServerIcon loadServerIcon(@Nonnull final File file) throws IllegalArgumentException, Exception {
		throw new NotImplementedException();
	}

	@Nonnull
	@Override
	public CachedServerIcon loadServerIcon(@Nonnull final BufferedImage bufferedImage) throws IllegalArgumentException, Exception {
		throw new NotImplementedException();
	}

	@Override
	public void setIdleTimeout(final int i) {
		throw new NotImplementedException();
	}

	@Override
	public int getIdleTimeout() {
		throw new NotImplementedException();
	}

	@Nonnull
	@Override
	public ChunkGenerator.ChunkData createChunkData(@Nonnull final World world) {
		throw new NotImplementedException();
	}

	@Override
	public ChunkGenerator.@NotNull ChunkData createVanillaChunkData(
			@NotNull World world, int x, int z) {
		throw new NotImplementedException();
	}

	@Nonnull
	@Override
	public BossBar createBossBar(@Nullable final String s, @Nonnull final BarColor barColor, @Nonnull final BarStyle barStyle, @Nonnull final BarFlag... barFlags) {
		throw new NotImplementedException();
	}

	@Nonnull
	@Override
	public KeyedBossBar createBossBar(@Nonnull final NamespacedKey namespacedKey, @Nullable final String s, @Nonnull final BarColor barColor, @Nonnull final BarStyle barStyle, @Nonnull final BarFlag... barFlags) {
		throw new NotImplementedException();
	}

	@Nonnull
	@Override
	public Iterator<KeyedBossBar> getBossBars() {
		throw new NotImplementedException();
	}

	@Nullable
	@Override
	public KeyedBossBar getBossBar(@Nonnull final NamespacedKey namespacedKey) {
		throw new NotImplementedException();
	}

	@Override
	public boolean removeBossBar(@Nonnull final NamespacedKey namespacedKey) {
		throw new NotImplementedException();
	}

	@Nullable
	@Override
	public Entity getEntity(@Nonnull final UUID uuid) {
		throw new NotImplementedException();
	}

	@Nonnull
	@Override
	public double[] getTPS() {
		throw new NotImplementedException();
	}

	@Nonnull
	@Override
	public long[] getTickTimes() {
		throw new NotImplementedException();
	}

	@Override
	public double getAverageTickTime() {
		throw new NotImplementedException();
	}

	@Nonnull
	@Override
	public CommandMap getCommandMap() {
		throw new NotImplementedException();
	}

	@Nullable
	@Override
	public Advancement getAdvancement(@Nonnull final NamespacedKey namespacedKey) {
		throw new NotImplementedException();
	}

	@Nonnull
	@Override
	public Iterator<Advancement> advancementIterator() {
		throw new NotImplementedException();
	}

	@Nonnull
	@Override
	public BlockData createBlockData(@Nonnull final Material material, @Nullable final Consumer<BlockData> consumer) {
		throw new NotImplementedException();
	}

	@Nonnull
	@Override
	public BlockData createBlockData(@Nonnull final String s) throws IllegalArgumentException {
		throw new NotImplementedException();
	}

	@Nonnull
	@Override
	public BlockData createBlockData(@Nullable final Material material, @Nullable final String s) throws IllegalArgumentException {
		throw new NotImplementedException();
	}

	@Override
	public <T extends Keyed> Tag<T> getTag(@Nonnull final String s, @Nonnull final NamespacedKey namespacedKey, @Nonnull final Class<T> aClass) {
		throw new NotImplementedException();
	}

	@Nonnull
	@Override
	public <T extends Keyed> Iterable<Tag<T>> getTags(@Nonnull final String s, @Nonnull final Class<T> aClass) {
		throw new NotImplementedException();
	}

	@Nullable
	@Override
	public LootTable getLootTable(@Nonnull final NamespacedKey namespacedKey) {
		throw new NotImplementedException();
	}

	@Nonnull
	@Override
	public List<Entity> selectEntities(@Nonnull final CommandSender commandSender, @Nonnull final String s) throws IllegalArgumentException {
		throw new NotImplementedException();
	}

	@Override
	public @NotNull StructureManager getStructureManager() {
		throw new NotImplementedException();
	}

	@Nonnull
	@Override
	public Spigot spigot() {
		throw new NotImplementedException();
	}

	@Override
	public void reloadPermissions() {
		throw new NotImplementedException();
	}

	@Override
	public boolean reloadCommandAliases() {
		throw new NotImplementedException();
	}

	@Override
	public boolean suggestPlayerNamesWhenNullTabCompletions() {
		throw new NotImplementedException();
	}

	@Nonnull
	@Override
	public String getPermissionMessage() {
		throw new NotImplementedException();
	}

	@Nonnull
	@Override
	public PlayerProfile createProfile(@Nonnull final UUID uuid) {
		throw new NotImplementedException();
	}

	@Nonnull
	@Override
	public PlayerProfile createProfile(@Nonnull final String s) {
		throw new NotImplementedException();
	}

	@Nonnull
	@Override
	public PlayerProfile createProfile(@Nullable final UUID uuid, @Nullable final String s) {
		throw new NotImplementedException();
	}

	@Override
	public @NotNull PlayerProfile createProfileExact(@Nullable UUID uuid,
													 @Nullable String name) {
		throw new NotImplementedException();
	}

	@Override
	public int getCurrentTick() {
		throw new NotImplementedException();
	}

	@Override
	public boolean isStopping() {
		throw new NotImplementedException();
	}

	@Nonnull
	@Override
	public MobGoals getMobGoals() {
		throw new NotImplementedException();
	}

	@Nonnull
	@Override
	public DatapackManager getDatapackManager() {
		throw new NotImplementedException();
	}

	@Override
	public @NotNull PotionBrewer getPotionBrewer() {
		throw new NotImplementedException();
	}

	@Override
	public void sendPluginMessage(@Nonnull final Plugin plugin, @Nonnull final String s, @Nonnull final byte[] bytes) {
		throw new NotImplementedException();
	}

	@Nonnull
	@Override
	public Set<String> getListeningPluginChannels() {
		throw new NotImplementedException();
	}

	@Nonnull
	@Override
	public Iterable<? extends Audience> audiences() {
		throw new NotImplementedException();
	}

}
