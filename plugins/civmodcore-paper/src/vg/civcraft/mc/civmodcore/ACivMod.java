package vg.civcraft.mc.civmodcore;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import org.mcstats.Metrics;
import vg.civcraft.mc.civmodcore.annotations.*;
import vg.civcraft.mc.civmodcore.chatDialog.ChatListener;
import vg.civcraft.mc.civmodcore.chatDialog.DialogManager;
import vg.civcraft.mc.civmodcore.command.CommandHandler;
import vg.civcraft.mc.civmodcore.interfaces.ApiManager;
import vg.civcraft.mc.civmodcore.inventorygui.ClickableInventoryListener;
import vg.civcraft.mc.civmodcore.itemHandling.NiceNames;

public abstract class ACivMod extends JavaPlugin {

	/**
	 * The {@link Logger} assigned to this plugin by Spigot. For the sake of consistency,
	 * it should never be accessed directly and always through {@link #log()}.
	 */
	private static Logger logger;

	/**
	 * Returns the logger for this plugin. Assumes the plugin has already been loaded.
	 *
	 * @return The logger for this plugin.
	 */
	public static Logger log() {
		return logger;
	}

	protected ACivMod() {
		logger = getLogger();
	}

	protected CommandHandler handle;

	private static boolean initializedAPIs = false;

	public Config GetConfig() {
		if (config_ == null) {
			log().info("Config not initialized. Most likely due to overriding onLoad and not calling super.onLoad()");
		}
		return config_;
	}

	private Config config_ = null;

	public ClassLoader classLoader = null;

	public ApiManager apis;

	@Override
	public boolean onCommand(
		CommandSender sender,
		Command command,
		String label,
		String[] args) {
		if (//!(sender instanceof ConsoleCommandSender) ||
			!command.getName().equals(getPluginName().toLowerCase()) ||
				args.length < 1) {
			return handle == null ? false : handle.execute(sender, command, args);
		}
		String option = args[0];
		String value = null;
		String subvalue = null;
		boolean set = false;
		boolean subvalue_set = false;
		String msg = "";
		if (args.length > 1) {
			value = args[1];
			set = true;
		}
		if (args.length > 2) {
			subvalue = args[2];
			subvalue_set = true;
		}
		ConfigOption opt = config_.get(option);
		if (opt != null) {
			if (set) {
				opt.set(value);
			}
			msg = String.format("%s = %s", option, opt.getString());
		} else if (option.equals("debug")) {
			if (set) {
				config_.DebugLog = toBool(value);
			}
			msg = String.format("debug = %s", config_.DebugLog);
		} else if (option.equals("save")) {
			config_.save();
			msg = "Configuration saved";
		} else if (option.equals("reload")) {
			config_.reload();
			msg = "Configuration loaded";
		} else {
			msg = String.format("Unknown option %s", option);
		}
		sender.sendMessage(msg);
		return true;
	}
	// ================================================
	// General

	@Override
	public void onLoad() {
		classLoader = getClassLoader();
		loadConfiguration();
		log().info("Configuration Loaded");
	}

	private void loadConfiguration() {
		config_ = new Config(this);
		log().info("loaded config for: " + getPluginName() + "Config: " + (config_ != null));
	}

	@Override
	public void onEnable() {
		registerCommands();
		initApis(this);
		//global_instance_ = this;
		log().info("Main Plugin Events and Config Command registered");

		try {
			Metrics metrics = new Metrics(this);
			metrics.start();
		} catch (IOException e) {
			// Failed to submit the stats :-(
		}
	}

	private static synchronized void initApis(ACivMod instance) {
		if (!initializedAPIs) {
			initializedAPIs = true;
			instance.registerEvents();
			new NiceNames().loadNames();
			new DialogManager();
		}
	}

	private void registerEvents() {
		getServer().getPluginManager().registerEvents(new ClickableInventoryListener(), this);
		getServer().getPluginManager().registerEvents(new ChatListener(), this);
	}

	public void registerCommands() {
		ConsoleCommandSender console = getServer().getConsoleSender();
		console.addAttachment(this, getPluginName().toLowerCase() + ".console", true);
	}
//    public boolean isInitiaized() {
//      return global_instance_ != null;
//    }

	public boolean toBool(String value) {
		if (value.equals("1") || value.equalsIgnoreCase("true")) {
			return true;
		}
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
		return handle == null ? null : handle.complete(sender, cmd, args);
	}

	public CommandHandler getCommandHandler() {
		return handle;
	}

	protected void setCommandHandler(CommandHandler handle) {
		this.handle = handle;
	}

	protected abstract String getPluginName();

	@Deprecated
	public void severe(String message) {
		log().severe("[" + getPluginName() + "] " + message);
	}

	@Deprecated
	public void warning(String message) {
		log().warning("[" + getPluginName() + "] " + message);
	}

	@Deprecated
	public void info(String message) {
		log().info("[" + getPluginName() + "] " + message);
	}

	@Deprecated
	public void debug(String message) {
		if (config_.DebugLog) {
			log().info("[" + getPluginName() + "] " + message);
		}
	}

}
