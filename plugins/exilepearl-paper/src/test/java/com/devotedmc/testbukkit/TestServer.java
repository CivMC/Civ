package com.devotedmc.testbukkit;

import static org.mockito.Mockito.*;

import java.awt.image.BufferedImage;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.apache.commons.lang.Validate;
import org.bukkit.BanList;
import org.bukkit.BanList.Type;
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
import org.bukkit.Warning.WarningState;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.advancement.Advancement;
import org.bukkit.block.data.BlockData;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.boss.KeyedBossBar;
import org.bukkit.command.Command;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.generator.ChunkGenerator.ChunkData;
import org.bukkit.help.HelpMap;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.Recipe;
import org.bukkit.loot.LootTable;
import org.bukkit.map.MapView;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.plugin.SimpleServicesManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.Messenger;
import org.bukkit.plugin.messaging.StandardMessenger;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitWorker;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.util.CachedServerIcon;
import org.mockito.Mockito;

import net.md_5.bungee.api.chat.BaseComponent;

@SuppressWarnings("deprecation")
public class TestServer implements Server {

    private final String serverName = "TestBukkit";
    private final String serverVersion = "0.0.0";
    private final String bukkitVersion = "0.0.0";
    private Logger logger;
    private final ServicesManager servicesManager = new SimpleServicesManager();
    private final TestCommandMap commandMap = new TestCommandMap(this);
    private final StandardMessenger messenger = new StandardMessenger();
    private final TestPluginManager pluginManager = spy(new TestPluginManager(this, commandMap));
    private final TestScheduler scheduler = spy(new TestScheduler());
    private final TestItemFactory itemFactory = spy(new TestItemFactory());
    private final Map<String, TestWorld> worlds = new LinkedHashMap<String, TestWorld>();
    private List<TestPlayer> onlinePlayers = new LinkedList<TestPlayer>();
    private List<OfflinePlayer> offlinePlayers = new LinkedList<OfflinePlayer>();
    private YamlConfiguration configuration = new YamlConfiguration();
    private TestConsoleCommandSender consoleSender;
	private Set<Recipe> recipes = new HashSet<Recipe>();
	private PluginLoader pluginLoader = Mockito.mock(PluginLoader.class);
    private int maxPlayers = 50;
    private int viewDistance = 4;
    private boolean allowNether = true;
    private boolean allowEnd = true;
	private boolean allowFlight = false;
	private boolean isHardcore = false;
	private boolean useLogger = false;;
    
    /**
     * Use @RunWith(TestBukkitRunner.class) to use this class
     * @param useLogger
     */
	public TestServer(boolean useLogger) {
		configureLogger(useLogger);
        
        // Create default worlds
        createTestWorld(new WorldCreator("world"));
        createTestWorld(new WorldCreator("world_nether"));
        createTestWorld(new WorldCreator("world_the_end"));
        
        TestBukkit.setServer(this);
        
        consoleSender = new TestConsoleCommandSender();
    }
    
    public TestServer() {
    	this(false);
    }
    
    
    public void configureLogger(boolean useLogger) {
    	if (this.useLogger != useLogger && logger != null) {
    		return;
    	}
    	this.useLogger = useLogger;
    	
    	if (useLogger) {
    		logger = Logger.getLogger(serverName);
    	} else {
    		logger = Mockito.mock(Logger.class);
    		return;
    	}
    	
		// Format the logger output
		Formatter formatter = new Formatter() {
			private final DateFormat df = new SimpleDateFormat("hh:mm:ss");
			@Override
			public String format(LogRecord record) {
				String level = record.getLevel().getLocalizedName().toUpperCase();
				if (level.equals("WARNING")) {
					level = "WARN";
				}

				Throwable thrown = record.getThrown();
				if (thrown != null) {
					thrown.printStackTrace();
				}

				return String.format("[%s %s]: %s\n", df.format(new Date(record.getMillis())), level, formatMessage(record));
			}
		};

		logger.setUseParentHandlers(false);
		ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(formatter);
        logger.addHandler(handler);
    }
    


	@Override
	public void reload() {
        pluginManager.clearPlugins();
        commandMap.clearCommands();
        resetRecipes();

        List<BukkitWorker> overdueWorkers = getScheduler().getActiveWorkers();
        for (BukkitWorker worker : overdueWorkers) {
            Plugin plugin = worker.getOwner();
            String author = "<NoAuthorGiven>";
            if (plugin.getDescription().getAuthors().size() > 0) {
                author = plugin.getDescription().getAuthors().get(0);
            }
            getLogger().log(Level.SEVERE, String.format(
                "Nag author: '%s' of '%s' about the following: %s",
                author,
                plugin.getDescription().getName(),
                "This plugin is not properly shutting down its async tasks when it is being reloaded.  This may cause conflicts with the newly loaded version of the plugin"
            ));
        }
        loadPlugins();
        enablePlugins();

	}

    public void loadPlugins() {
        Plugin[] plugins = pluginManager.loadPlugins();
        for (Plugin plugin : plugins) {
            try {
                String message = String.format("Loading %s", plugin.getDescription().getFullName());
                plugin.getLogger().info(message);
                plugin.onLoad();
            } catch (Throwable ex) {
                logger.log(Level.SEVERE, ex.getMessage() + " initializing " + plugin.getDescription().getFullName() + " (Is it up to date?)", ex);
            }
        }
    }

    public void enablePlugins() {
        Plugin[] plugins = pluginManager.getPlugins();

        for (Plugin plugin : plugins) {
            if (!plugin.isEnabled()) {
                enablePlugin(plugin);
            }
        }
    }
    
    private void enablePlugin(Plugin plugin) {
        try {
            List<Permission> perms = plugin.getDescription().getPermissions();

            for (Permission perm : perms) {
                try {
                    pluginManager.addPermission(perm);
                } catch (IllegalArgumentException ex) {
                    getLogger().log(Level.WARNING, "Plugin " + plugin.getDescription().getFullName() + " tried to register permission '" + perm.getName() + "' but it's already registered", ex);
                }
            }

            pluginManager.enablePlugin(plugin);
        } catch (Throwable ex) {
            logger.log(Level.SEVERE, ex.getMessage() + " loading " + plugin.getDescription().getFullName() + " (Is it up to date?)", ex);
        }
    }

    public void disablePlugins() {
        pluginManager.disablePlugins();
    }

	@Override
	public void sendPluginMessage(Plugin source, String channel, byte[] message) {
        StandardMessenger.validatePluginMessage(getMessenger(), source, channel, message);

        for (Player player : getOnlinePlayers()) {
            player.sendPluginMessage(source, channel, message);
        }
	}

	@Override
	public Set<String> getListeningPluginChannels() {
        Set<String> result = new HashSet<String>();

        for (Player player : getOnlinePlayers()) {
            result.addAll(player.getListeningPluginChannels());
        }

        return result;
	}

	@Override
	public String getName() {
        return serverName;
	}

	@Override
	public String getVersion() {
        return serverVersion;
	}

	@Override
	public String getBukkitVersion() {
        return bukkitVersion;
	}

	@Override
	public Collection<? extends Player> getOnlinePlayers() {
		return onlinePlayers;
	}

	@Override
	public int getMaxPlayers() {
        return maxPlayers;
	}

	@Override
	public int getPort() {
		return 0;
	}

	@Override
	public int getViewDistance() {
		return viewDistance;
	}

	@Override
	public String getIp() {
		return "localhost";
	}

	@Override
	public String getWorldType() {
		return "DEFAULT";
	}

	@Override
	public boolean getGenerateStructures() {
		return false;
	}

	@Override
	public boolean getAllowEnd() {
		return allowEnd;
	}

	@Override
	public boolean getAllowNether() {
		return allowNether;
	}

	@Override
	public boolean hasWhitelist() {
		return false;
	}

	@Override
	public void setWhitelist(boolean value) {
	}

	@Override
	public Set<OfflinePlayer> getWhitelistedPlayers() {
		return new LinkedHashSet<OfflinePlayer>();
	}

	@Override
	public void reloadWhitelist() {
	}

	@Override
	public int broadcastMessage(String message) {
        return broadcast(message, BROADCAST_CHANNEL_USERS);
	}

	@Override
	public String getUpdateFolder() {
		return "update";
	}

	@Override
	public File getUpdateFolderFile() {
		throw new UnsupportedOperationException();
	}

	@Override
	public long getConnectionThrottle() {
		return -1;
	}

	@Override
	public int getTicksPerAnimalSpawns() {
		return -1;
	}

	@Override
	public int getTicksPerMonsterSpawns() {
		return -1;
	}

	@Override
	public Player getPlayer(String name) {
		for(Player p : onlinePlayers) {
			if (p.getName() == name) {
				return p;
			}
		}
		return null;
	}

	@Override
	public Player getPlayerExact(String name) {
		for(Player p : onlinePlayers) {
			if (p.getName() == name) {
				return p;
			}
		}
		return null;
	}

	@Override
    @Deprecated
	public List<Player> matchPlayer(String partialName) {
        Validate.notNull(partialName, "PartialName cannot be null");

        List<Player> matchedPlayers = new ArrayList<Player>();

        for (Player iterPlayer : this.getOnlinePlayers()) {
            String iterPlayerName = iterPlayer.getName();

            if (partialName.equalsIgnoreCase(iterPlayerName)) {
                // Exact match
                matchedPlayers.clear();
                matchedPlayers.add(iterPlayer);
                break;
            }
            if (iterPlayerName.toLowerCase(java.util.Locale.ENGLISH).contains(partialName.toLowerCase(java.util.Locale.ENGLISH))) {
                // Partial match
                matchedPlayers.add(iterPlayer);
            }
        }

        return matchedPlayers;
	}

	@Override
	public Player getPlayer(UUID id) {
		for(Player p : onlinePlayers) {
			if (p.getUniqueId() == id) {
				return p;
			}
		}
		return null;
	}

	@Override
	public TestPluginManager getPluginManager() {
		return pluginManager;
	}

	@Override
	public BukkitScheduler getScheduler() {
        return scheduler;
	}

	@Override
	public ServicesManager getServicesManager() {
        return servicesManager;
	}

	@Override
	public List<World> getWorlds() {
        return new ArrayList<World>(worlds.values());
	}

	public List<TestWorld> getTestWorlds() {
        return new ArrayList<TestWorld>(worlds.values());
	}

	@Override
	public World createWorld(WorldCreator creator) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean unloadWorld(String name, boolean save) {
		return true;
	}

	@Override
	public boolean unloadWorld(World world, boolean save) {
		return true;
	}

	@Override
	public World getWorld(String name) {
        Validate.notNull(name, "Name cannot be null");
        return worlds.get(name.toLowerCase(java.util.Locale.ENGLISH));
	}

	@Override
	public World getWorld(UUID uid) {
        for (World world : worlds.values()) {
            if (world.getUID().equals(uid)) {
                return world;
            }
        }
        return null;
	}

	@Override
	public MapView createMap(World world) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Logger getLogger() {
        return logger;
	}

	@Override
	public PluginCommand getPluginCommand(String name) {
        Command command = commandMap.getCommand(name);

        if (command instanceof PluginCommand) {
            return (PluginCommand) command;
        } else {
            return null;
        }
	}

	@Override
	public void savePlayers() {
	}

	@Override
	public boolean dispatchCommand(CommandSender sender, String commandLine) throws CommandException {
        Validate.notNull(sender, "Sender cannot be null");
        Validate.notNull(commandLine, "CommandLine cannot be null");

        if (commandMap.dispatch(sender, commandLine)) {
            return true;
        }

        sender.sendMessage("Unknown command. Type \"/help\" for help.");

        return false;
	}

	@Override
	public boolean addRecipe(Recipe recipe) {
		recipes.add(recipe);
		return true;
	}

	@Override
	public List<Recipe> getRecipesFor(ItemStack result) {
        Validate.notNull(result, "Result cannot be null");

        List<Recipe> results = new ArrayList<Recipe>();
        Iterator<Recipe> iter = recipeIterator();
        while (iter.hasNext()) {
            Recipe recipe = iter.next();
            ItemStack stack = recipe.getResult();
            if (stack.getType() != result.getType()) {
                continue;
            }
            if (result.getDurability() == -1 || result.getDurability() == stack.getDurability()) {
                results.add(recipe);
            }
        }
        return results;
	}

	@Override
	public Iterator<Recipe> recipeIterator() {
		return recipes.iterator();
	}

	@Override
	public void clearRecipes() {
	}

	@Override
	public void resetRecipes() {
	}

	@Override
	public Map<String, String[]> getCommandAliases() {
		return null;
	}

	@Override
	public int getSpawnRadius() {
		return 0;
	}

	@Override
	public void setSpawnRadius(int value) {
	}

	@Override
	public boolean getOnlineMode() {
		return false;
	}

	@Override
	public boolean getAllowFlight() {
		return allowFlight;
	}

	@Override
	public boolean isHardcore() {
		return isHardcore;
	}


	@Override
	public void shutdown() {
	}

	@Override
	public int broadcast(String message, String permission) {
        int count = 0;
        Set<Permissible> permissibles = getPluginManager().getPermissionSubscriptions(permission);

        for (Permissible permissible : permissibles) {
            if (permissible instanceof CommandSender && permissible.hasPermission(permission)) {
                CommandSender user = (CommandSender) permissible;
                user.sendMessage(message);
                count++;
            }
        }

        return count;
	}

	@Override
	public OfflinePlayer getOfflinePlayer(String name) {
		for(OfflinePlayer p : offlinePlayers) {
			if (p.getName() == name) {
				return p;
			}
		}
		return null;
	}

	@Override
	public OfflinePlayer getOfflinePlayer(UUID id) {
		for(OfflinePlayer p : offlinePlayers) {
			if (p.getUniqueId() == id) {
				return p;
			}
		}
		return null;
	}

	@Override
	public Set<String> getIPBans() {
		return new HashSet<String>();
	}

	@Override
	public void banIP(String address) {
	}

	@Override
	public void unbanIP(String address) {
	}

	@Override
	public Set<OfflinePlayer> getBannedPlayers() {
		return new HashSet<OfflinePlayer>();
	}

	@Override
	public BanList getBanList(Type type) {
		return null;
	}

	@Override
	public Set<OfflinePlayer> getOperators() {
		return new HashSet<OfflinePlayer>();
	}

	@Override
	public GameMode getDefaultGameMode() {
		return GameMode.SURVIVAL;
	}

	@Override
	public void setDefaultGameMode(GameMode mode) {
	}

	@Override
	public ConsoleCommandSender getConsoleSender() {
		return consoleSender;
	}

	@Override
	public File getWorldContainer() {
		return null;
	}

	@Override
	public OfflinePlayer[] getOfflinePlayers() {
		return new OfflinePlayer[0];
	}

	@Override
	public Messenger getMessenger() {
        return messenger;
	}

	@Override
	public HelpMap getHelpMap() {
		return null;
	}

	@Override
	public Inventory createInventory(InventoryHolder owner, InventoryType type) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Inventory createInventory(InventoryHolder owner, InventoryType type, String title) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Inventory createInventory(InventoryHolder owner, int size) throws IllegalArgumentException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Inventory createInventory(InventoryHolder owner, int size, String title) throws IllegalArgumentException {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getMonsterSpawnLimit() {
		return 0;
	}

	@Override
	public int getAnimalSpawnLimit() {
		return 0;
	}

	@Override
	public int getWaterAnimalSpawnLimit() {
		return 0;
	}

	@Override
	public int getAmbientSpawnLimit() {
		return 0;
	}

	@Override
	public boolean isPrimaryThread() {
		return true;
	}

	@Override
	public String getMotd() {
		return "";
	}

	@Override
	public String getShutdownMessage() {
		return "";
	}

	@Override
	public WarningState getWarningState() {
		return WarningState.DEFAULT;
	}

	@Override
	public ItemFactory getItemFactory() {
		return itemFactory;
	}

	@Override
	public ScoreboardManager getScoreboardManager() {
		return null;
	}

	@Override
	public CachedServerIcon getServerIcon() {
		return null;
	}

	@Override
	public CachedServerIcon loadServerIcon(File file) throws IllegalArgumentException, Exception {
		return null;
	}

	@Override
	public CachedServerIcon loadServerIcon(BufferedImage image) throws IllegalArgumentException, Exception {
		return null;
	}

	@Override
	public void setIdleTimeout(int threshold) {
	}

	@Override
	public int getIdleTimeout() {
		return 0;
	}

	@Override
	public ChunkData createChunkData(World world) {
		throw new UnsupportedOperationException();
	}

	@Override
	public BossBar createBossBar(String title, BarColor color, BarStyle style, BarFlag... flags) {
		throw new UnsupportedOperationException();
	}

	@Override
	public UnsafeValues getUnsafe() {
		return null;
	}

    private final Spigot spigot = new Spigot()
    {

        @Override
        public YamlConfiguration getConfig()
        {
            return configuration;
        }

        @Override
        public void restart() {
            restart();
        }

        @Override
        public void broadcast(BaseComponent component) {
            for (Player player : getOnlinePlayers()) {
                player.spigot().sendMessage(component);
            }
        }

        @Override
        public void broadcast(BaseComponent... components) {
            for (Player player : getOnlinePlayers()) {
                player.spigot().sendMessage(components);
            }
        }
    };

	@Override
	public Spigot spigot() {
		return spigot;
	}

    public World createTestWorld(WorldCreator creator) {    	
    	TestWorld testWorld = TestWorld.create(creator.name(), creator.environment(), creator.type());
    	worlds.put(testWorld.getName(), testWorld);
    	return testWorld;
    }
    
    public <T extends JavaPlugin> TestPlugin<T> addPlugin(Class<T> clazz) throws Exception {
    	return pluginManager.addPlugin(clazz);
    }
    
    public void addPlayer(TestPlayer p) {
    	this.onlinePlayers.add(p);
    }
    
    public PluginLoader getPluginLoader() {
    	return pluginLoader;
    }
    
    public PluginDescriptionFile getDescription(JavaPlugin plugin) throws InvalidDescriptionException {
    	return pluginManager.createDescription(plugin);
    }
    
    public FileConfiguration getPluginConfig(JavaPlugin plugin) {
    	return pluginManager.getPluginConfig(plugin);
    }
    
    public File getTestPluginDataFolder(JavaPlugin plugin) {
    	String name = plugin.getName();
    	File file = mock(File.class);
    	when(file.getPath()).thenReturn("\\plugins\\" + name);
    	when(file.getAbsolutePath()).thenReturn("\\plugins\\" + name);
    	when(file.getName()).thenReturn("\\plugins\\" + name);
    	when(file.getParent()).thenReturn("\\plugins\\");
    	when(file.exists()).thenReturn(true);
    	when(file.isFile()).thenReturn(false);
    	when(file.isDirectory()).thenReturn(true);
    	return file;
    }
    
    public File getTestPluginFile(JavaPlugin plugin) {
    	String name = plugin.getName() + ".jar";
    	File file = mock(File.class);
    	when(file.getPath()).thenReturn("\\plugins\\" + name);
    	when(file.getAbsolutePath()).thenReturn("\\plugins\\" + name);
    	when(file.getName()).thenReturn("\\plugins\\" + name);
    	when(file.getParent()).thenReturn("\\plugins\\");
    	when(file.exists()).thenReturn(true);
    	when(file.isFile()).thenReturn(true);
    	when(file.isDirectory()).thenReturn(false);
    	return file;
    }
    
    public File getTestPluginConfigFile(JavaPlugin plugin) {
    	String name = plugin.getName();
    	File file = mock(File.class);
    	when(file.getPath()).thenReturn("\\plugins\\" + name + "\\config.yml");
    	when(file.getAbsolutePath()).thenReturn("\\plugins\\" + name + "\\config.yml");
    	when(file.getName()).thenReturn("\\plugins\\" + name + "\\config.yml");
    	when(file.getParent()).thenReturn("\\plugins\\" + name);
    	when(file.exists()).thenReturn(true);
    	when(file.isFile()).thenReturn(true);
    	when(file.isDirectory()).thenReturn(false);
    	return file;
    }

	@Override
	public void reloadData() {
		// TODO Auto-generated method stub

	}

	@Override
	public Merchant createMerchant(String title) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Entity getEntity(UUID uuid) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Advancement getAdvancement(NamespacedKey key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterator<Advancement> advancementIterator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MapView getMap(int id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ItemStack createExplorerMap(World world, Location location, StructureType structureType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ItemStack createExplorerMap(World world, Location location, StructureType structureType, int radius,
			boolean findUnexplored) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public KeyedBossBar createBossBar(NamespacedKey key, String title, BarColor color, BarStyle style,
			BarFlag... flags) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterator<KeyedBossBar> getBossBars() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public KeyedBossBar getBossBar(NamespacedKey key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean removeBossBar(NamespacedKey key) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public BlockData createBlockData(Material material) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BlockData createBlockData(Material material, Consumer<BlockData> consumer) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BlockData createBlockData(String data) throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BlockData createBlockData(Material material, String data) throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T extends Keyed> Tag<T> getTag(String registry, NamespacedKey tag, Class<T> clazz) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T extends Keyed> Iterable<Tag<T>> getTags(String registry, Class<T> clazz) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LootTable getLootTable(NamespacedKey key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Entity> selectEntities(CommandSender sender, String selector) throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return null;
	}
}
