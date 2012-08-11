package com.untamedears.citadel;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.untamedears.citadel.command.CommandHandler;
import com.untamedears.citadel.command.commands.AddModCommand;
import com.untamedears.citadel.command.commands.AllowCommand;
import com.untamedears.citadel.command.commands.BypassCommand;
import com.untamedears.citadel.command.commands.CreateCommand;
import com.untamedears.citadel.command.commands.DeleteCommand;
import com.untamedears.citadel.command.commands.DisallowCommand;
import com.untamedears.citadel.command.commands.FortifyCommand;
import com.untamedears.citadel.command.commands.GroupCommand;
import com.untamedears.citadel.command.commands.GroupInfoCommand;
import com.untamedears.citadel.command.commands.GroupsCommand;
import com.untamedears.citadel.command.commands.InfoCommand;
import com.untamedears.citadel.command.commands.JoinCommand;
import com.untamedears.citadel.command.commands.LeaveCommand;
import com.untamedears.citadel.command.commands.MaterialsCommand;
import com.untamedears.citadel.command.commands.MembersCommand;
import com.untamedears.citadel.command.commands.ModeratorsCommand;
import com.untamedears.citadel.command.commands.NonReinforceableCommand;
import com.untamedears.citadel.command.commands.OffCommand;
import com.untamedears.citadel.command.commands.PasswordCommand;
import com.untamedears.citadel.command.commands.PrivateCommand;
import com.untamedears.citadel.command.commands.PublicCommand;
import com.untamedears.citadel.command.commands.ReinforceCommand;
import com.untamedears.citadel.command.commands.RemoveModCommand;
import com.untamedears.citadel.command.commands.SecurableCommand;
import com.untamedears.citadel.command.commands.StatsCommand;
import com.untamedears.citadel.command.commands.TransferCommand;
import com.untamedears.citadel.command.commands.VersionCommand;
import com.untamedears.citadel.dao.CitadelDao;
import com.untamedears.citadel.entity.Faction;
import com.untamedears.citadel.entity.FactionMember;
import com.untamedears.citadel.entity.Member;
import com.untamedears.citadel.entity.Moderator;
import com.untamedears.citadel.entity.PersonalGroup;
import com.untamedears.citadel.entity.Reinforcement;
import com.untamedears.citadel.entity.ReinforcementKey;
import com.untamedears.citadel.listener.BlockListener;
import com.untamedears.citadel.listener.EntityListener;
import com.untamedears.citadel.listener.PlayerListener;

/**
 * User: JonnyD
 * Date: 07/18/12
 * Time: 11:57 PM
 */
public class Citadel extends JavaPlugin {    
    private static final Logger log = Logger.getLogger("Citadel");
    private static final CommandHandler commandHandler = new CommandHandler();
    private static final ReinforcementManager reinforcementManager = new ReinforcementManager();
    private static final GroupManager groupManager = new GroupManager();
    private static final PersonalGroupManager personalGroupManager = new PersonalGroupManager();
    private static final MemberManager memberManager = new MemberManager();
    private static final ConfigManager configManager = new ConfigManager();
    private static CitadelDao dao;
    private static Citadel plugin;
    
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
    	return commandHandler.dispatch(sender, label, args);
    }

    public void onEnable() {
        plugin = this;
        dao = new CitadelDao(this);
        dao.updateDatabase();
        setUpStorage();
        registerCommands();
        registerEvents();
        configManager.load();
        for(Player player : getServer().getOnlinePlayers()){
        	memberManager.addOnlinePlayer(player);
        }
        log.info("[Citadel] Citadel is now enabled.");
    }

    public void onDisable() {
        log.info("[Citadel] Citadel is now disabled.");
    }
    
    public void setUpStorage(){
    	GroupStorage groupStorage = new GroupStorage(dao);
    	groupManager.setStorage(groupStorage);
    	
    	PersonalGroupStorage personalGroupStorage = new PersonalGroupStorage(dao);
    	personalGroupManager.setStorage(personalGroupStorage);
    	
    	MemberStorage memberStorage = new MemberStorage(dao);
    	memberManager.setStorage(memberStorage);
    	
    	ReinforcementStorage reinforcementStorage = new ReinforcementStorage(dao);
    	reinforcementManager.setStorage(reinforcementStorage);
    }
    
    public void registerCommands(){
    	commandHandler.addCommand(new AddModCommand());
    	commandHandler.addCommand(new AllowCommand());
    	commandHandler.addCommand(new BypassCommand());
    	commandHandler.addCommand(new CreateCommand());
    	commandHandler.addCommand(new DeleteCommand());
    	commandHandler.addCommand(new DisallowCommand());
    	commandHandler.addCommand(new FortifyCommand());
    	commandHandler.addCommand(new GroupCommand());
    	commandHandler.addCommand(new GroupInfoCommand());
    	commandHandler.addCommand(new GroupsCommand());
    	//commandHandler.addCommand(new Help())
    	commandHandler.addCommand(new InfoCommand());
    	commandHandler.addCommand(new JoinCommand());
    	commandHandler.addCommand(new LeaveCommand());
    	commandHandler.addCommand(new MaterialsCommand());
    	commandHandler.addCommand(new MembersCommand());
    	commandHandler.addCommand(new ModeratorsCommand());
    	commandHandler.addCommand(new NonReinforceableCommand());
    	commandHandler.addCommand(new OffCommand());
    	commandHandler.addCommand(new PasswordCommand());
    	commandHandler.addCommand(new PrivateCommand());
    	commandHandler.addCommand(new PublicCommand());
    	commandHandler.addCommand(new ReinforceCommand());
    	commandHandler.addCommand(new RemoveModCommand());
    	commandHandler.addCommand(new SecurableCommand());
    	commandHandler.addCommand(new StatsCommand());
    	commandHandler.addCommand(new TransferCommand());
    	commandHandler.addCommand(new VersionCommand());
    }
    
    public void registerEvents(){
    	PluginManager pm = getServer().getPluginManager();
    	pm.registerEvents(new BlockListener(), this);
    	pm.registerEvents(new PlayerListener(), this);
    	pm.registerEvents(new EntityListener(), this);
    }

    @Override
    public List<Class<?>> getDatabaseClasses() {
        ArrayList<Class<?>> classes = new ArrayList<Class<?>>();
        classes.add(Faction.class);
        classes.add(Member.class);
        classes.add(Reinforcement.class);
        classes.add(ReinforcementKey.class);
        classes.add(FactionMember.class);
        classes.add(PersonalGroup.class);
        classes.add(Moderator.class);
        return classes;
    }
    
    public static void info(String message){
    	if(configManager.getVerboseLogging()){
    		log.info("[Citadel] " + message);
    	}
    }
    
    public static void severe(String message){
    	log.severe("[Citadel] " + message);
    }
    
    public static void warning(String message){
    	log.warning("[Citadel] " + message);
    }
    
    public static GroupManager getGroupManager(){
    	return groupManager;
    }
    
    public static PersonalGroupManager getPersonalGroupManager(){
    	return personalGroupManager;
    }
    
    public static MemberManager getMemberManager(){
    	return memberManager;
    }
    
    public static ReinforcementManager getReinforcementManager(){
    	return reinforcementManager;
    }
    
    public static ConfigManager getConfigManager(){
    	return configManager;
    }
    
    public static Citadel getPlugin(){
    	return plugin;
    }
}