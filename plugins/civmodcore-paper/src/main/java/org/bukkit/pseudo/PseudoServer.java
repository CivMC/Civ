package org.bukkit.pseudo;

import com.destroystokyo.paper.entity.ai.MobGoals;
import com.destroystokyo.paper.profile.PlayerProfile;
import io.papermc.paper.ban.BanListType;
import io.papermc.paper.datapack.DatapackManager;
import io.papermc.paper.math.Position;
import io.papermc.paper.threadedregions.scheduler.AsyncScheduler;
import io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler;
import io.papermc.paper.threadedregions.scheduler.RegionScheduler;
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
import org.bukkit.Registry;
import org.bukkit.Server;
import org.bukkit.ServerTickManager;
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
import org.bukkit.craftbukkit.v1_20_R3.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_20_R3.inventory.CraftItemFactory;
import org.bukkit.craftbukkit.v1_20_R3.util.CraftMagicNumbers;
import org.bukkit.craftbukkit.v1_20_R3.util.Versioning;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.SpawnCategory;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.help.HelpMap;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemCraftResult;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.Recipe;
import org.bukkit.loot.LootTable;
import org.bukkit.map.MapCursor;
import org.bukkit.map.MapView;
import org.bukkit.packs.DataPackManager;
import org.bukkit.packs.ResourcePack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.plugin.messaging.Messenger;
import org.bukkit.potion.PotionBrewer;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.structure.StructureManager;
import org.bukkit.util.CachedServerIcon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.image.BufferedImage;
import java.io.File;
import java.net.InetAddress;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

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

    @NotNull
    @Override
    public Logger getLogger() {
        return LOGGER;
    }

    @NotNull
    @Override
    public ItemFactory getItemFactory() {
        return CraftItemFactory.instance();
    }

    @NotNull
    @Override
    public UnsafeValues getUnsafe() {
        return CraftMagicNumbers.INSTANCE;
    }

    @NotNull
    @Override
    public BlockData createBlockData(@NotNull final Material material) {
        return CraftBlockData.newData(material, null);
    }

    @Override
    public @NotNull BlockData createBlockData(@NotNull Material material, @org.jetbrains.annotations.Nullable Consumer<? super BlockData> consumer) {
        return null;
    }

    // ------------------------------------------------------------
    // Not implemented
    // ------------------------------------------------------------

    @Override
    public @NotNull File getPluginsFolder() {
        throw new NotImplementedException();
    }

    @NotNull
    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    @NotNull
    @Override
    public String getVersion() {
        return getClass().getPackage().getImplementationVersion();
    }

    @NotNull
    @Override
    public String getBukkitVersion() {
        return Versioning.getBukkitVersion();
    }

    @NotNull
    @Override
    public String getMinecraftVersion() {
        return SharedConstants.getCurrentVersion().toString();
    }

    @NotNull
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

    @NotNull
    @Override
    public String getIp() {
        throw new NotImplementedException();
    }

    @NotNull
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
    public boolean isLoggingIPs() {
        throw new NotImplementedException();
    }

    @Override
    public @NotNull List<String> getInitialEnabledPacks() {
        throw new NotImplementedException();
    }

    @Override
    public @NotNull List<String> getInitialDisabledPacks() {
        throw new NotImplementedException();
    }

    @Override
    public @NotNull DataPackManager getDataPackManager() {
        throw new NotImplementedException();
    }

    @Override
    public @NotNull ServerTickManager getServerTickManager() {
        throw new NotImplementedException();
    }

    @Override
    public @org.jetbrains.annotations.Nullable ResourcePack getServerResourcePack() {
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

    @NotNull
    @Override
    public Set<OfflinePlayer> getWhitelistedPlayers() {
        throw new NotImplementedException();
    }

    @Override
    public void reloadWhitelist() {
        throw new NotImplementedException();
    }

    @Override
    public int broadcastMessage(@NotNull final String s) {
        throw new NotImplementedException();
    }

    @NotNull
    @Override
    public String getUpdateFolder() {
        throw new NotImplementedException();
    }

    @NotNull
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
    public Player getPlayer(@NotNull final String s) {
        throw new NotImplementedException();
    }

    @Nullable
    @Override
    public Player getPlayerExact(@NotNull final String s) {
        throw new NotImplementedException();
    }

    @NotNull
    @Override
    public List<Player> matchPlayer(@NotNull final String s) {
        throw new NotImplementedException();
    }

    @Nullable
    @Override
    public Player getPlayer(@NotNull final UUID uuid) {
        throw new NotImplementedException();
    }

    @Nullable
    @Override
    public UUID getPlayerUniqueId(@NotNull final String s) {
        throw new NotImplementedException();
    }

    @NotNull
    @Override
    public PluginManager getPluginManager() {
        throw new NotImplementedException();
    }

    @NotNull
    @Override
    public BukkitScheduler getScheduler() {
        throw new NotImplementedException();
    }

    @NotNull
    @Override
    public ServicesManager getServicesManager() {
        throw new NotImplementedException();
    }

    @NotNull
    @Override
    public List<World> getWorlds() {
        throw new NotImplementedException();
    }

    @Override
    public boolean isTickingWorlds() {
        throw new NotImplementedException();
    }

    @Nullable
    @Override
    public World createWorld(@NotNull final WorldCreator worldCreator) {
        throw new NotImplementedException();
    }

    @Override
    public boolean unloadWorld(@NotNull final String s, final boolean b) {
        throw new NotImplementedException();
    }

    @Override
    public boolean unloadWorld(@NotNull final World world, final boolean b) {
        throw new NotImplementedException();
    }

    @Nullable
    @Override
    public World getWorld(@NotNull final String s) {
        throw new NotImplementedException();
    }

    @Nullable
    @Override
    public World getWorld(@NotNull final UUID uuid) {
        throw new NotImplementedException();
    }

    @Nullable
    @Override
    public World getWorld(@NotNull final NamespacedKey namespacedKey) {
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

    @NotNull
    @Override
    public MapView createMap(@NotNull final World world) {
        throw new NotImplementedException();
    }

    @NotNull
    @Override
    public ItemStack createExplorerMap(@NotNull final World world, @NotNull final Location location, @NotNull final StructureType structureType) {
        throw new NotImplementedException();
    }

    @NotNull
    @Override
    public ItemStack createExplorerMap(@NotNull final World world, @NotNull final Location location, @NotNull final StructureType structureType, final int i, final boolean b) {
        throw new NotImplementedException();
    }

    @Override
    public @org.jetbrains.annotations.Nullable ItemStack createExplorerMap(@NotNull World world, @NotNull Location location, org.bukkit.generator.structure.@NotNull StructureType structureType, @NotNull MapCursor.Type type, int i, boolean b) {
        return null;
    }

    @Override
    public void reload() {
        throw new NotImplementedException();
    }

    @Override
    public void reloadData() {
        throw new NotImplementedException();
    }

    @Override
    public void updateResources() {

    }

    @Override
    public void updateRecipes() {

    }

    @Nullable
    @Override
    public PluginCommand getPluginCommand(@NotNull final String s) {
        throw new NotImplementedException();
    }

    @Override
    public void savePlayers() {
        throw new NotImplementedException();
    }

    @Override
    public boolean dispatchCommand(@NotNull final CommandSender commandSender, @NotNull final String s) throws CommandException {
        throw new NotImplementedException();
    }

    @Override
    public boolean addRecipe(@Nullable final Recipe recipe) {
        throw new NotImplementedException();
    }

    @Override
    public boolean addRecipe(@org.jetbrains.annotations.Nullable Recipe recipe, boolean b) {
        return false;
    }

    @NotNull
    @Override
    public List<Recipe> getRecipesFor(@NotNull final ItemStack itemStack) {
        throw new NotImplementedException();
    }

    @Nullable
    @Override
    public Recipe getRecipe(@NotNull final NamespacedKey namespacedKey) {
        throw new NotImplementedException();
    }

    @Nullable
    @Override
    public Recipe getCraftingRecipe(@NotNull ItemStack[] itemStacks, @NotNull World world) {
        throw new NotImplementedException();
    }

    @NotNull
    @Override
    public ItemStack craftItem(@NotNull ItemStack[] itemStacks, @NotNull World world, @NotNull Player player) {
        throw new NotImplementedException();
    }

    @Override
    public @NotNull ItemStack craftItem(@NotNull ItemStack[] itemStacks, @NotNull World world) {
        return null;
    }

    @Override
    public @NotNull ItemCraftResult craftItemResult(@NotNull ItemStack[] itemStacks, @NotNull World world, @NotNull Player player) {
        return null;
    }

    @Override
    public @NotNull ItemCraftResult craftItemResult(@NotNull ItemStack[] itemStacks, @NotNull World world) {
        return null;
    }

    @NotNull
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
    public boolean removeRecipe(@NotNull final NamespacedKey namespacedKey) {
        throw new NotImplementedException();
    }

    @Override
    public boolean removeRecipe(@NotNull NamespacedKey namespacedKey, boolean b) {
        return false;
    }

    @NotNull
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
    public boolean shouldSendChatPreviews() {
        return false;
    }

    @Override
    public boolean isEnforcingSecureProfiles() {
        return false;
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
    public int broadcast(@NotNull final String s, @NotNull final String s1) {
        throw new NotImplementedException();
    }

    @Override
    public int broadcast(@NotNull Component component) {
        throw new NotImplementedException();
    }

    @Override
    public int broadcast(@NotNull final Component component, @NotNull final String s) {
        throw new NotImplementedException();
    }

    @NotNull
    @Override
    public OfflinePlayer getOfflinePlayer(@NotNull final String s) {
        throw new NotImplementedException();
    }

    @Nullable
    @Override
    public OfflinePlayer getOfflinePlayerIfCached(@NotNull final String s) {
        throw new NotImplementedException();
    }

    @NotNull
    @Override
    public OfflinePlayer getOfflinePlayer(@NotNull final UUID uuid) {
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

    @NotNull
    @Override
    public Set<String> getIPBans() {
        throw new NotImplementedException();
    }

    @Override
    public void banIP(@NotNull final String s) {
        throw new NotImplementedException();
    }

    @Override
    public void unbanIP(@NotNull final String s) {
        throw new NotImplementedException();
    }

    @Override
    public void banIP(@NotNull InetAddress inetAddress) {

    }

    @Override
    public void unbanIP(@NotNull InetAddress inetAddress) {

    }

    @NotNull
    @Override
    public Set<OfflinePlayer> getBannedPlayers() {
        throw new NotImplementedException();
    }

    @Override
    public <B extends BanList<E>, E> @NotNull B getBanList(@NotNull BanListType<B> banListType) {
        return null;
    }

    @NotNull
    @Override
    public <T extends BanList<?>> T getBanList(@NotNull final BanList.Type type) {
        throw new NotImplementedException();
    }

    @NotNull
    @Override
    public Set<OfflinePlayer> getOperators() {
        throw new NotImplementedException();
    }

    @NotNull
    @Override
    public GameMode getDefaultGameMode() {
        throw new NotImplementedException();
    }

    @Override
    public void setDefaultGameMode(@NotNull final GameMode gameMode) {
        throw new NotImplementedException();
    }

    @NotNull
    @Override
    public ConsoleCommandSender getConsoleSender() {
        throw new NotImplementedException();
    }

    @Override
    public @NotNull CommandSender createCommandSender(
        @NotNull Consumer<? super Component> feedback) {
        throw new NotImplementedException();
    }

    @NotNull
    @Override
    public File getWorldContainer() {
        throw new NotImplementedException();
    }

    @NotNull
    @Override
    public OfflinePlayer[] getOfflinePlayers() {
        throw new NotImplementedException();
    }

    @NotNull
    @Override
    public Messenger getMessenger() {
        throw new NotImplementedException();
    }

    @NotNull
    @Override
    public HelpMap getHelpMap() {
        throw new NotImplementedException();
    }

    @NotNull
    @Override
    public Inventory createInventory(@Nullable final InventoryHolder inventoryHolder, @NotNull final InventoryType inventoryType) {
        throw new NotImplementedException();
    }

    @NotNull
    @Override
    public Inventory createInventory(@Nullable final InventoryHolder inventoryHolder, @NotNull final InventoryType inventoryType, @NotNull final Component component) {
        throw new NotImplementedException();
    }

    @NotNull
    @Override
    public Inventory createInventory(@Nullable final InventoryHolder inventoryHolder, @NotNull final InventoryType inventoryType, @NotNull final String s) {
        throw new NotImplementedException();
    }

    @NotNull
    @Override
    public Inventory createInventory(@Nullable final InventoryHolder inventoryHolder, final int i) throws IllegalArgumentException {
        throw new NotImplementedException();
    }

    @NotNull
    @Override
    public Inventory createInventory(@Nullable final InventoryHolder inventoryHolder, final int i, @NotNull final Component component) throws IllegalArgumentException {
        throw new NotImplementedException();
    }

    @NotNull
    @Override
    public Inventory createInventory(@Nullable final InventoryHolder inventoryHolder, final int i, @NotNull final String s) throws IllegalArgumentException {
        throw new NotImplementedException();
    }

    @NotNull
    @Override
    public Merchant createMerchant(@Nullable final Component component) {
        throw new NotImplementedException();
    }

    @NotNull
    @Override
    public Merchant createMerchant(@Nullable final String s) {
        throw new NotImplementedException();
    }

    @Override
    public int getMaxChainedNeighborUpdates() {
        return 0;
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

    @NotNull
    @Override
    public Component motd() {
        throw new NotImplementedException();
    }

    @Override
    public void motd(@NotNull Component component) {

    }

    @NotNull
    @Override
    public String getMotd() {
        throw new NotImplementedException();
    }

    @Override
    public void setMotd(@NotNull String s) {

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

    @NotNull
    @Override
    public Warning.WarningState getWarningState() {
        throw new NotImplementedException();
    }

    @NotNull
    @Override
    public ScoreboardManager getScoreboardManager() {
        throw new NotImplementedException();
    }

    @Override
    public @NotNull Criteria getScoreboardCriteria(@NotNull String s) {
        return null;
    }

    @Nullable
    @Override
    public CachedServerIcon getServerIcon() {
        throw new NotImplementedException();
    }

    @NotNull
    @Override
    public CachedServerIcon loadServerIcon(@NotNull final File file) throws IllegalArgumentException, Exception {
        throw new NotImplementedException();
    }

    @NotNull
    @Override
    public CachedServerIcon loadServerIcon(@NotNull final BufferedImage bufferedImage) throws IllegalArgumentException, Exception {
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

    @NotNull
    @Override
    public ChunkGenerator.ChunkData createChunkData(@NotNull final World world) {
        throw new NotImplementedException();
    }

    @Override
    public ChunkGenerator.@NotNull ChunkData createVanillaChunkData(
        @NotNull World world, int x, int z) {
        throw new NotImplementedException();
    }

    @NotNull
    @Override
    public BossBar createBossBar(@Nullable final String s, @NotNull final BarColor barColor, @NotNull final BarStyle barStyle, @NotNull final BarFlag... barFlags) {
        throw new NotImplementedException();
    }

    @NotNull
    @Override
    public KeyedBossBar createBossBar(@NotNull final NamespacedKey namespacedKey, @Nullable final String s, @NotNull final BarColor barColor, @NotNull final BarStyle barStyle, @NotNull final BarFlag... barFlags) {
        throw new NotImplementedException();
    }

    @NotNull
    @Override
    public Iterator<KeyedBossBar> getBossBars() {
        throw new NotImplementedException();
    }

    @Nullable
    @Override
    public KeyedBossBar getBossBar(@NotNull final NamespacedKey namespacedKey) {
        throw new NotImplementedException();
    }

    @Override
    public boolean removeBossBar(@NotNull final NamespacedKey namespacedKey) {
        throw new NotImplementedException();
    }

    @Nullable
    @Override
    public Entity getEntity(@NotNull final UUID uuid) {
        throw new NotImplementedException();
    }

    @NotNull
    @Override
    public double[] getTPS() {
        throw new NotImplementedException();
    }

    @NotNull
    @Override
    public long[] getTickTimes() {
        throw new NotImplementedException();
    }

    @Override
    public double getAverageTickTime() {
        throw new NotImplementedException();
    }

    @NotNull
    @Override
    public CommandMap getCommandMap() {
        throw new NotImplementedException();
    }

    @Nullable
    @Override
    public Advancement getAdvancement(@NotNull final NamespacedKey namespacedKey) {
        throw new NotImplementedException();
    }

    @NotNull
    @Override
    public Iterator<Advancement> advancementIterator() {
        throw new NotImplementedException();
    }

    @NotNull
    @Override
    public BlockData createBlockData(@NotNull final String s) throws IllegalArgumentException {
        throw new NotImplementedException();
    }

    @NotNull
    @Override
    public BlockData createBlockData(@Nullable final Material material, @Nullable final String s) throws IllegalArgumentException {
        throw new NotImplementedException();
    }

    @Override
    public <T extends Keyed> Tag<T> getTag(@NotNull final String s, @NotNull final NamespacedKey namespacedKey, @NotNull final Class<T> aClass) {
        throw new NotImplementedException();
    }

    @NotNull
    @Override
    public <T extends Keyed> Iterable<Tag<T>> getTags(@NotNull final String s, @NotNull final Class<T> aClass) {
        throw new NotImplementedException();
    }

    @Nullable
    @Override
    public LootTable getLootTable(@NotNull final NamespacedKey namespacedKey) {
        throw new NotImplementedException();
    }

    @NotNull
    @Override
    public List<Entity> selectEntities(@NotNull final CommandSender commandSender, @NotNull final String s) throws IllegalArgumentException {
        throw new NotImplementedException();
    }

    @Override
    public @NotNull StructureManager getStructureManager() {
        throw new NotImplementedException();
    }

    @Override
    public @org.jetbrains.annotations.Nullable <T extends Keyed> Registry<T> getRegistry(@NotNull Class<T> aClass) {
        throw new NotImplementedException();
    }

    @NotNull
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

    @NotNull
    @Override
    public String getPermissionMessage() {
        throw new NotImplementedException();
    }

    @Override
    public @NotNull Component permissionMessage() {
        return null;
    }

    @NotNull
    @Override
    public PlayerProfile createProfile(@NotNull final UUID uuid) {
        throw new NotImplementedException();
    }

    @NotNull
    @Override
    public PlayerProfile createProfile(@NotNull final String s) {
        throw new NotImplementedException();
    }

    @NotNull
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

    @NotNull
    @Override
    public MobGoals getMobGoals() {
        throw new NotImplementedException();
    }

    @NotNull
    @Override
    public DatapackManager getDatapackManager() {
        throw new NotImplementedException();
    }

    @Override
    public @NotNull PotionBrewer getPotionBrewer() {
        throw new NotImplementedException();
    }

    @Override
    public @NotNull RegionScheduler getRegionScheduler() {
        throw new NotImplementedException();
    }

    @Override
    public @NotNull AsyncScheduler getAsyncScheduler() {
        throw new NotImplementedException();
    }

    @Override
    public @NotNull GlobalRegionScheduler getGlobalRegionScheduler() {
        throw new NotImplementedException();
    }

    @Override
    public boolean isOwnedByCurrentRegion(@NotNull World world, @NotNull Position position) {
        throw new NotImplementedException();
    }

    @Override
    public boolean isOwnedByCurrentRegion(@NotNull World world, @NotNull Position position, int i) {
        throw new NotImplementedException();
    }

    @Override
    public boolean isOwnedByCurrentRegion(@NotNull Location location) {
        throw new NotImplementedException();
    }

    @Override
    public boolean isOwnedByCurrentRegion(@NotNull Location location, int i) {
        throw new NotImplementedException();
    }

    @Override
    public boolean isOwnedByCurrentRegion(@NotNull World world, int i, int i1) {
        throw new NotImplementedException();
    }

    @Override
    public boolean isOwnedByCurrentRegion(@NotNull World world, int i, int i1, int i2) {
        throw new NotImplementedException();
    }

    @Override
    public boolean isOwnedByCurrentRegion(@NotNull Entity entity) {
        throw new NotImplementedException();
    }

    @Override
    public void sendPluginMessage(@NotNull final Plugin plugin, @NotNull final String s, @NotNull final byte[] bytes) {
        throw new NotImplementedException();
    }

    @NotNull
    @Override
    public Set<String> getListeningPluginChannels() {
        throw new NotImplementedException();
    }

    @NotNull
    @Override
    public Iterable<? extends Audience> audiences() {
        throw new NotImplementedException();
    }

}
