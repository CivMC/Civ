package com.devotedmc.ExilePearl.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.devotedmc.ExilePearl.Lang;
import com.devotedmc.ExilePearl.util.StringListIgnoresCase;

import vg.civcraft.mc.civmodcore.util.TextUtil;

public abstract class BaseCommand<T extends Plugin> {
	
	protected final T plugin;
	
	private final Map<String, String> permissionDescriptions = new HashMap<String, String>();
	private final List<BaseCommand<? extends Plugin>> subCommands = new ArrayList<BaseCommand<? extends Plugin>>();
	
	// The different names this commands will react to  
	protected final StringListIgnoresCase aliases = new StringListIgnoresCase();
	
	// Information on the args
	protected final List<CommandArg> commandArgs = new ArrayList<CommandArg>();
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
	protected List<BaseCommand<? extends Plugin>> commandChain = new ArrayList<BaseCommand<? extends Plugin>>(); // The command chain used to execute this command
	
	// Will only be set when the sender is a player
	private Player me;
	
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
	public final void execute(CommandSender sender, List<String> args, List<BaseCommand<? extends Plugin>> commandChain) {
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
		execute(sender, args, new ArrayList<BaseCommand<? extends Plugin>>());
	}
	
	
	/**
	 * Gets the tab list for the command
	 * @param sender The command sender
	 * @param args The command arguments
	 * @return The tab list
	 */
	public final List<String> getTabList(CommandSender sender, List<String> args, List<BaseCommand<? extends Plugin>> commandChain) {
		return prepareTab(sender, args, commandChain);
	}
	
	
	/**
	 * Prepares a command for execution
	 * @param sender The command sender
	 * @param args The command arguments
	 * @param commandChain The command chain
	 * @return true if the command should execute
	 */
	private boolean prepareCommand(CommandSender sender, List<String> args, List<BaseCommand<? extends Plugin>> commandChain) {
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
			for (BaseCommand<? extends Plugin> subCommand: this.subCommands) {
				if (subCommand.aliases.contains(args.get(0))) {
					// Pretend like this command doesn't exist
					if (!subCommand.validSenderPermissions(sender, false) && subCommand.visibility != CommandVisibility.VISIBLE) {
						sendTooManyArgs();
						return false;
					}				
					
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
	 * Prepares a tab command for execution
	 * @param sender The command sender
	 * @param args The command arguments
	 * @param commandChain The command chain
	 * @return the tab results
	 */
	private List<String> prepareTab(CommandSender sender, List<String> args, List<BaseCommand<? extends Plugin>> commandChain) {
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
			return null;
		}

		// Find a matching sub-command
		if (args.size() > 0 ) {
			for (BaseCommand<? extends Plugin> subCommand: this.subCommands) {
				if (subCommand.aliases.contains(args.get(0))) {
					args.remove(0);
					commandChain.add(this);
					return subCommand.getTabList(sender, args, commandChain);
				}
			}
		}
		
		List<String> subCommands = new ArrayList<String>();
		
		// Get the tab list of any matching sub-commands
		if (args.size() > 0 ) {
			for (BaseCommand<? extends Plugin> subCommand: this.subCommands) {
				if (!(subCommand.validSenderPermissions(sender, false))) {
					continue;
				}
				
				if (subCommand.senderMustBePlayer && senderIsConsole) {
					continue;
				}
				
				for(String alias : subCommand.aliases) {
					if (alias.startsWith(args.get(0))) {
						subCommands.add(alias);
						break;
					}
				}
			}
		}
		
		// Return the matching tab commands if there are any
		if (this.subCommands.size() > 0) {
			if (subCommands.size() > 0) {
				return subCommands;
			} else {
				return null;
			}
		}
		
		// This is the case when someone tabs at the end of a command before pressing space
		if (args.size() == 0 || (args.size() == 1 && args.get(0) == "")) {
			return null;
		}
		
		// Check if the arg is an auto-tab type
	   if(args.size() > commandArgs.size()) {
		   msg("<i>No more arguments are needed.");
		   return null;
	   }
		
		CommandArg tabArg = commandArgs.get(args.size() - 1);
		if (tabArg == null) {
			return null;
		}
		
		List<String> tabList = new ArrayList<String>();
		
		if (tabArg.isAutoTab()) {
			tabList = getAutoTab(tabArg.getAutoTab().getName(), args.get(args.size() - 1));
			if (tabList != null) {
				return tabList;
			}
		}
		
		// Otherwise perform the tab routine for the command itself
		tabList = performTabList();
		if (tabList != null) {
			return tabList;
		}
		
		// If still nothing found, send the help message
		if(tabArg.isAutoTab() && tabArg.getAutoTab().getHelp() != null) {
			msg("<i>" + tabArg.getAutoTab().getHelp());
		}
		return null;
	}
	
	protected List<String> getAutoTab(String name, String pattern) {		
		List<String> tabList = new ArrayList<String>();
		
		switch(name) {
		case "player":
			for (Player p: Bukkit.getOnlinePlayers()) {
				if (p.getName().toLowerCase().startsWith(pattern.toLowerCase()))
					tabList.add(p.getName());
			}
			break;
		default:
		{
			List<String> customTab = getCustomAutoTab(name, pattern);
			if (customTab != null) {
				tabList.addAll(customTab);
			}
		}
			break;
		}
		
		if (tabList.size() == 0) {
			return null;
		}
		
		return tabList;
	}
	
	/**
	 * Method that can be overridden for getting custom auto-tabs
	 * @param tabArg The tab argument
	 * @param pattern The search pattern
	 * @return The tab list if any results
	 */
	protected List<String> getCustomAutoTab(String tabName, String pattern) {
		return null;
	}
	
	
	/**
	 * Gets the tab list for the command
	 * @param sender The command sender
	 * @param args The command arguments
	 * @return The tab list
	 */
	public final List<String> getTabList(CommandSender sender, List<String> args) {
		return getTabList(sender, args, new ArrayList<BaseCommand<? extends Plugin>>());
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
	protected Player player() {
		return me;
	}

	
	/**
	 * Adds a sub-command
	 * @param subCommand the sub-command to add
	 */
	public void addSubCommand(BaseCommand<? extends Plugin> subCommand) {
		subCommand.getCommandChain().addAll(this.commandChain);
		subCommand.getCommandChain().add(this);
		this.subCommands.add(subCommand);
	}
	
	
	/**
	 * Gets the command chain
	 * @return The command chain
	 */
	public List<BaseCommand<? extends Plugin>> getCommandChain() {
		return commandChain;
	}
	
	/**
	 * Gets the sub-commands
	 * @return The sub-commands
	 */
	public List<BaseCommand<? extends Plugin>> getSubCommands() {
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
		if (senderMustBePlayer && (!(sender instanceof Player))) {
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
	protected boolean validArgs(List<String> args, CommandSender sender) {
		if (args.size() < numRequiredArgs()) {
			msg(Lang.commandToFewArgs);
			msg(getUsageTemplate());
			return false;
		}
		
		if (args.size() > this.commandArgs.size() && this.errorOnToManyArgs) {
			sendTooManyArgs();
			return false;
		}
		return true;
	}
	
	private void sendTooManyArgs() {
		if (args.size() > this.commandArgs.size() && this.errorOnToManyArgs) {
			// Get the to many string slice
			List<String> theToMany = args.subList(this.commandArgs.size(), args.size());
			msg(Lang.commandToManyArgs, TextUtil.implode(theToMany, " "));
			msg(this.getUsageTemplate());
		}
	}
	
	/**
	 * Gets the number of required arguments
	 * @return The number of required arguments
	 */
	protected int numRequiredArgs() {
		int i = 0;
		for (CommandArg a : commandArgs) {
			if (a.isRequired()) {
				i++;
			}
		}
		return i;
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
	public String getUseageTemplate(List<BaseCommand<? extends Plugin>> commandChain, boolean addShortHelp) {
		StringBuilder ret = new StringBuilder();
		ret.append(TextUtil.parseTags("<c>"));
		ret.append('/');
		
		for (BaseCommand<? extends Plugin> mc : commandChain) {
			ret.append(TextUtil.implode(mc.aliases, ","));
			ret.append(' ');
		}
		
		ret.append(TextUtil.implode(this.aliases, ","));
		
		if (commandArgs.size() > 0) {
			for (CommandArg a : this.commandArgs) {
				ret.append("<p> ");
				ret.append(a.toString());
			}
		}
		
		if (addShortHelp) {
			ret.append(TextUtil.parseTags(" <i>"));
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
	
	protected void msg(Player player, String str) {
		str = parse(str);
		player.sendMessage(str);
	}

	/**
	 * Formats and sends a message to the player
	 * @param str The message to send 
	 * @param args The message arguments
	 */
	protected void msg(Player player, String str, Object... args) {
		str = parse(str, args);
		player.sendMessage(str);
	}
	
	protected void msg(String str) {
		str = parse(str);
		if (senderIsConsole) {
			sender.sendMessage(str);
		} else {
			me.sendMessage(str);
		}
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
			if (informSenderIfNot) {
				me.sendMessage(Lang.unknownCommand);
			}
		}
		else if (informSenderIfNot) {
			msg(getForbiddenMessage(perm));
		}
		return false;
	}
	
	
	/**
	 * Formats a string through the text tag parser
	 * @param str The string to format
	 * @param args The string arguments
	 * @return The formatted string
	 */
	protected static String parse(String str, Object... args) {
		return TextUtil.parse(str, args);
	}
	
	protected static String parse(String str) {
		return TextUtil.parse(str);
	}
	
	
	/*
	 * The following are utility methods for easily generating 
	 * command and auto-tab instances
	 */
	
	
	protected final static AutoTab autoTab(String name, String help) {
		return new AutoTab(name, help);
	}
	
	protected final static AutoTab autoTab(String name) {
		return new AutoTab(name, null);
	}
	
	protected final static CommandArg required(String name, AutoTab autoTab) {
		return new RequiredCommandArg(name, autoTab);
	}
	
	protected final static CommandArg required(String name) {
		return required(name, null);
	}
	
	protected final static CommandArg requiredPlayer(String name) {
		return required(name, autoTab("player", "No matching player found."));
	}
	
	protected final static CommandArg optional(String name, String defValue, AutoTab autoTab) {
		return new OptionalCommandArg(name, defValue, autoTab);
	}
	
	protected final static CommandArg optional(String name, String defValue) {
		return optional(name, defValue, null);
	}
	
	protected final static CommandArg optional(String name, AutoTab autoTab) {
		return optional(name, null, autoTab);
	}
	
	protected final static CommandArg optional(String name) {
		return optional(name, null, null);
	}
	
	protected final static CommandArg optionalPlayer(String name) {
		return optional(name, autoTab("player", "No matching player found."));
	}
}
