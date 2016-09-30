package com.devotedmc.ExilePearl.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.devotedmc.ExilePearl.Lang;
import com.devotedmc.ExilePearl.util.StringListIgnoresCase;
import com.devotedmc.ExilePearl.util.TextUtil;

public abstract class BaseCommand<T extends  JavaPlugin> {
	
	protected final T plugin;
	
	private final Map<String, String> permissionDescriptions = new HashMap<String, String>();
	private final List<BaseCommand<? extends  JavaPlugin>> subCommands = new ArrayList<BaseCommand<? extends  JavaPlugin>>();
	
	// The different names this commands will react to  
	protected final StringListIgnoresCase aliases = new StringListIgnoresCase();
	
	// Information on the args
	protected final List<String> requiredArgs = new ArrayList<String>();
	protected final LinkedHashMap<String, String> optionalArgs = new LinkedHashMap<String, String>();
	protected boolean errorOnToManyArgs = true;
	
	// FIELD: Help Short
	// This field may be left blank and will in such case be loaded from the permissions node instead.
	// Thus make sure the permissions node description is an action description like "eat hamburgers" or "do admin stuff".
	protected String helpShort = "";
	
	protected final List<String> helpLong = new ArrayList<String>();
	protected CommandVisibility visibility = CommandVisibility.VISIBLE;
	
	public List<String> getLongHelp() {
		return this.helpLong;
	}
	
	public CommandVisibility getVisibility() {
		return this.visibility;
	}
	
	// Some information on permissions
	protected boolean senderMustBePlayer;
	protected String permission;
	protected boolean senderMustConfirm;
	
	// Information available on execution of the command
	protected CommandSender sender; // Will always be set
	protected boolean senderIsConsole;
	protected List<String> args; // Will contain the arguments, or and empty list if there are none.
	protected List<BaseCommand<? extends  JavaPlugin>> commandChain = new ArrayList<BaseCommand<? extends  JavaPlugin>>(); // The command chain used to execute this command
	
	// Will only be set when the sender is a player
	private Player me;
	
	private TextUtil txt = TextUtil.instance();
	
	/**
	 * Creates a new SabreCommand instance
	 * @param sabreApi The Sabre API
	 */
	public BaseCommand(T plugin) {
		this.plugin = plugin;
	}
	
	
	/**
	 * Performs the command
	 */
	protected abstract void perform();
	
	
	/**
	 * Performs the command tab list completion
	 */
	protected List<String> performTabList() {
		return null;
	}
	
	
	/**
	 * Executes the command
	 * @param sender The command sender
	 * @param args The command arguments
	 * @param commandChain The command chain
	 */
	public final void execute(CommandSender sender, List<String> args, List<BaseCommand<? extends  JavaPlugin>> commandChain) {
		if (prepareCommand(sender, args, commandChain)) {
			perform();
		}
	}
	
	/**
	 * Executes the command
	 * @param sender The command sender
	 * @param args The command arguments
	 * @param commandChain The command chain
	 */
	public final void execute(CommandSender sender, List<String> args) {
		execute(sender, args, new ArrayList<BaseCommand<? extends  JavaPlugin>>());
	}
	
	
	/**
	 * Gets the tab list for the command
	 * @param sender The command sender
	 * @param args The command arguments
	 * @return The tab list
	 */
	public final List<String> getTabList(CommandSender sender, List<String> args, List<BaseCommand<? extends  JavaPlugin>> commandChain) {
		if (prepareCommand(sender, args, commandChain)) {
			return performTabList();
		}
		return null;
	}
	
	
	/**
	 * Prepares a command for execution
	 * @param sender The command sender
	 * @param args The command arguments
	 * @param commandChain The command chain
	 * @return true if the command should execute
	 */
	private boolean prepareCommand(CommandSender sender, List<String> args, List<BaseCommand<? extends  JavaPlugin>> commandChain) {
		// Set the execution-time specific variables
		this.sender = sender;
		if (sender instanceof Player) {
			Player p = (Player)sender;
			
			this.me = plugin.getServer().getPlayer(p.getUniqueId());
			this.senderIsConsole = false;
		} else {
			this.me = null;
			this.senderIsConsole = true;
		}
		this.args = args;
		this.commandChain = commandChain;
		
		// Sender must have permission for the root node of this command
		if ( !validSenderPermissions(sender, true)) {
			return false;
		}

		// Find a matching sub-command
		if (args.size() > 0 ) {
			for (BaseCommand<? extends  JavaPlugin> subCommand: this.subCommands) {
				if (subCommand.aliases.contains(args.get(0))) {
					args.remove(0);
					commandChain.add(this);
					subCommand.execute(sender, args, commandChain);
					return false;
				}
			}
		}
		
		if ( !validCall(this.sender, this.args)) {
			return false;
		}
		
		return true;
	}
	
	
	/**
	 * Gets the tab list for the command
	 * @param sender The command sender
	 * @param args The command arguments
	 * @return The tab list
	 */
	public final List<String> getTabList(CommandSender sender, List<String> args) {
		return getTabList(sender, args, new ArrayList<BaseCommand<? extends  JavaPlugin>>());
	}
	
	
	/**
	 * Gets the command aliases
	 * @return The command aliases
	 */
	public ArrayList<String> getAliases() {
		return this.aliases;
	}
	
	/**
	 * Gets the sender instance
	 * @return The sender instance
	 */
	protected Player me() {
		return me;
	}

	
	/**
	 * Adds a sub-command
	 * @param subCommand the sub-command to add
	 */
	public void addSubCommand(BaseCommand<? extends  JavaPlugin> subCommand) {
		subCommand.getCommandChain().addAll(this.commandChain);
		subCommand.getCommandChain().add(this);
		this.subCommands.add(subCommand);
	}
	
	
	/**
	 * Gets the command chain
	 * @return The command chain
	 */
	public List<BaseCommand<? extends  JavaPlugin>> getCommandChain() {
		return commandChain;
	}
	
	/**
	 * Gets the sub-commands
	 * @return The sub-commands
	 */
	public List<BaseCommand<? extends  JavaPlugin>> getSubCommands() {
		return this.subCommands;
	}
	
	/**
	 * Sets the short help string
	 * @param str The short help value
	 */
	protected void setHelpShort(String str) { 
		this.helpShort = str;
	}
	
	/**
	 * Gets the short help string
	 * @return The short help string
	 */
	protected String getHelpShort() {
		if (this.helpShort == null) {
			String pdesc = getPermissionDescription(this.permission);
			if (pdesc != null) {
				return pdesc;
			}
			return "*info unavailable*";
		}
		return this.helpShort;
	}
	
	
	/**
	 * This method validates that all prerequisites to perform this command has been met.
	 * @param sender The command sender
	 * @param args The command args
	 * @return true if the call if valid and can proceed
	 */
	protected boolean validCall(CommandSender sender, List<String> args) {
		if (!validSenderType(sender, true)) {
			return false;
		}
		
		if (!validSenderPermissions(sender, true)) {
			return false;
		}
		
		if (!validArgs(args, sender)) {
			return false;
		}
		return true;
	}
	
	/**
	 * Checks if the sender is the correct type (console/player)
	 * @param sender The sender to check
	 * @param informSenderIfNot Whether to inform the sender if not valid
	 * @return true if the sender is valid
	 */
	protected boolean validSenderType(CommandSender sender, boolean informSenderIfNot) {
		if (senderMustBePlayer && senderIsConsole) {
			if (informSenderIfNot) {
				msg(Lang.commandSenderMustBePlayer);
			}
			return false;
		}
		return true;
	}
	
	/**
	 * Checks if the sender has valid permissions
	 * @param sender The sender to check
	 * @param informSenderIfNot Whether to inform the sender if no permission
	 * @return true if the sender has permission
	 */
	public boolean validSenderPermissions(CommandSender sender, boolean informSenderIfNot) {
		if (this.permission == null) {
			return true;
		}
		return senderHasPerm(sender, this.permission, this.visibility, informSenderIfNot);
	}
	
	/**
	 * Checks if the valid arguments were sent for the command
	 * @param args The args to check
	 * @param sender The command sender
	 * @return true if the commands are valid
	 */
	protected boolean validArgs(List<String> args, CommandSender sender)
	{
		if (args.size() < this.requiredArgs.size())
		{
			if (sender != null)
			{
				msg(Lang.commandToFewArgs);
				sender.sendMessage(this.getUsageTemplate());
			}
			return false;
		}
		
		if (args.size() > this.requiredArgs.size() + this.optionalArgs.size() && this.errorOnToManyArgs)
		{
			if (sender != null)
			{
				// Get the to many string slice
				List<String> theToMany = args.subList(this.requiredArgs.size() + this.optionalArgs.size(), args.size());
				msg(Lang.commandToManyArgs, txt.implode(theToMany, " "));
				sender.sendMessage(this.getUsageTemplate());
			}
			return false;
		}
		return true;
	}
	
	/**
	 * Checks if the valid arguments were sent for the command
	 * @param args The args to check
	 * @return true if the args are valid
	 */
	protected boolean validArgs(List<String> args) {
		return this.validArgs(args, null);
	}
	
	/**
	 * Gets the complete usage template for a command chain
	 * @param commandChain The command chain
	 * @param addShortHelp Whether to add a short help description
	 * @return The full command help
	 */
	public String getUseageTemplate(List<BaseCommand<? extends  JavaPlugin>> commandChain, boolean addShortHelp) {
		StringBuilder ret = new StringBuilder();
		ret.append(txt.parseTags("<c>"));
		ret.append('/');
		
		for (BaseCommand<? extends  JavaPlugin> mc : commandChain) {
			ret.append(txt.implode(mc.aliases, ","));
			ret.append(' ');
		}
		
		ret.append(txt.implode(this.aliases, ","));
		
		List<String> args = new ArrayList<String>();
		
		for (String requiredArg : this.requiredArgs) {
			args.add("<"+requiredArg+">");
		}
		
		for (Entry<String, String> optionalArg : this.optionalArgs.entrySet()) {
			String val = optionalArg.getValue();
			if (val == null) {
				val = "";
			} else {
				val = "="+val;
			}
			args.add("["+optionalArg.getKey()+val+"]");
		}
		
		if (args.size() > 0) {
			ret.append(txt.parseTags("<p> "));
			ret.append(txt.implode(args, " "));
		}
		
		if (addShortHelp) {
			ret.append(txt.parseTags(" <i>"));
			ret.append(this.getHelpShort());
		}
		
		return ret.toString();
	}
	
	/**
	 * Gets the usage template for the command
	 * @param addShortHelp whether to add a short help description
	 * @return The usage template
	 */
	public String getUsageTemplate(boolean addShortHelp) {
		return getUseageTemplate(this.commandChain, addShortHelp);
	}
	
	/**
	 * Gets the usage template for the command
	 * @return The usage template
	 */
	public String getUsageTemplate() {
		return getUsageTemplate(false);
	}

	/**
	 * Formats and sends a message to the player
	 * @param str The message to send 
	 * @param args The message arguments
	 */
	protected void msg(String str, Object... args) {
		str = parse(str, args);
		if (senderIsConsole) {
			sender.sendMessage(str);
		} else {
			me.sendMessage(str);
		}
	}
	
	/**
	 * Sends a list of messages to the player
	 * @param msgs The messages to send
	 */
	protected void msg(List<String> msgs) {
		for(String msg : msgs) {
			this.msg(msg);
		}
	}
	
	/**
	 * Gets whether an argument is set
	 * @param index The index to check
	 * @return true if the argument has a value
	 */
	protected boolean argIsSet(int idx) {
		if (this.args.size() < idx+1) {
			return false;
		}
		return true;
	}
	
	/**
	 * Gets an argument as a string
	 * @param index The index
	 * @param def The default value
	 * @return The string argument
	 */
	protected String argAsString(int index, String def) {
		if (this.args.size() < index + 1) {
			return def;
		}
		return this.args.get(index);
	}
	
	/**
	 * Gets an argument as a string
	 * @param index The index
	 * @return The string argument
	 */
	protected String argAsString(int idx) {
		return this.argAsString(idx, null);
	}
	
	/**
	 * Gets an integer value from a string
	 * @param str The string value
	 * @param def The default value
	 * @return The integer argument
	 */
	protected Integer strAsInt(String str, Integer def) {
		if (str == null) return def;
		try {
			Integer ret = Integer.parseInt(str);
			return ret;
		} catch (Exception e) {
			return def;
		}
	}
	
	/**
	 * Gets an argument as an integer
	 * @param index The index
	 * @param def The default value
	 * @return The integer argument
	 */
	protected Integer argAsInt(int index, Integer def) {
		return strAsInt(this.argAsString(index), def);
	}
	
	/**
	 * Gets an argument as a integer
	 * @param index The index
	 * @return The integer argument
	 */
	protected Integer argAsInt(int index) {
		return this.argAsInt(index, null);
	}
	
	/**
	 * Gets an default value from a string
	 * @param str The string value
	 * @param def The default value
	 * @return The double argument
	 */
	protected Double strAsDouble(String str, Double def) {
		if (str == null) return def;
		try {
			Double ret = Double.parseDouble(str);
			return ret;
		} catch (Exception e) {
			return def;
		}
	}
	
	/**
	 * Gets an argument as a double
	 * @param index The index
	 * @param def The default value
	 * @return The double argument
	 */
	protected Double argAsDouble(int idx, Double def) {
		return strAsDouble(this.argAsString(idx), def);
	}
	
	/**
	 * Gets an argument as a double
	 * @param index The index
	 * @return The double argument
	 */
	protected Double argAsDouble(int idx) {
		return this.argAsDouble(idx, null);
	}
	
	/**
	 * Gets an boolean value from a string
	 * Values that qualify as a 'true' value are [y, t, on, +, 1]
	 * @param str The string value
	 * @return The boolean argument
	 */
	protected Boolean strAsBool(String str) {
		str = str.toLowerCase();
		if (str.startsWith("y") || str.startsWith("t") || str.startsWith("on") || str.startsWith("1")) {
			return true;
		}
		return false;
	}
	
	/**
	 * Gets an argument as a boolean
	 * @param index The index
	 * @param def The default value
	 * @return The boolean argument
	 */
	protected Boolean argAsBool(int idx, Boolean def) {
		String str = this.argAsString(idx);
		if (str == null) {
			return def;
		}
		
		return strAsBool(str);
	}
	
	/**
	 * Gets an argument as a boolean
	 * @param index The index
	 * @return The boolean argument
	 */
	protected Boolean argAsBool(int index) {
		return this.argAsBool(index, false);
	}
	
	
	/**
	 * Gets the forbidden message for a command.
	 * @param perm The permission to check
	 * @return The permission forbidden message
	 */
	protected String getForbiddenMessage(String perm) {
		return parse(Lang.permForbidden, getPermissionDescription(perm));
	}

	
	/**
	 * Gets the permission description for the command
	 * @param perm The permission to check
	 * @return The permission description
	 */
	private String getPermissionDescription(String perm) {
		String desc = permissionDescriptions.get(perm);
		if (desc == null) {
			return Lang.permDoThat;
		}
		return desc;
	}
	
	
	/**
	 * Checks if a sender has a certain permission
	 * @param me The command sender
	 * @param perm The permission to check
	 * @return true if the sender has the permission
	 */
	private boolean senderHasPerm(CommandSender me, String perm) {
		if (me == null) {
			return false;
		}
		
		if (!senderIsConsole) {
			return me.hasPermission(perm);
		}

		return me.hasPermission(perm);
	}
	
	
	/**
	 * Checks if a sender has a certain permission
	 * @param me The command sender
	 * @param perm The permission to check
	 * @param visiblity The command visibility
	 * @param informSenderIfNot Whether to inform the sender of invalid perms
	 * @return true if the sender has the permission
	 */
	private boolean senderHasPerm(CommandSender me, String perm, CommandVisibility visiblity, boolean informSenderIfNot) {
		if (senderHasPerm(me, perm)) {
			return true;
		}
		else if (visiblity != CommandVisibility.VISIBLE) {
			me.sendMessage(Lang.unknownCommand);
		}
		else if (informSenderIfNot && me != null) {
			me.sendMessage(this.getForbiddenMessage(perm));
		}
		return false;
	}
	
	
	/**
	 * Formats a string through the text tag parser
	 * @param str The string to format
	 * @param args The string arguments
	 * @return The formatted string
	 */
	protected String parse(String str, Object... args) {
		return txt.parse(str, args);
	}
}
