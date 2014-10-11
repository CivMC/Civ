package vg.civcraft.mc.command;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.command.CommandSender;

import vg.civcraft.mc.command.commands.AcceptInvite;
import vg.civcraft.mc.command.commands.AddSuperGroup;
import vg.civcraft.mc.command.commands.CreateGroup;
import vg.civcraft.mc.command.commands.DeleteGroup;
import vg.civcraft.mc.command.commands.DisciplineGroup;
import vg.civcraft.mc.command.commands.GlobalStats;
import vg.civcraft.mc.command.commands.GroupStats;
import vg.civcraft.mc.command.commands.InvitePlayer;
import vg.civcraft.mc.command.commands.JoinGroup;
import vg.civcraft.mc.command.commands.ListGroups;
import vg.civcraft.mc.command.commands.ListMembers;
import vg.civcraft.mc.command.commands.MergeGroups;
import vg.civcraft.mc.command.commands.ModifyPermissions;
import vg.civcraft.mc.command.commands.RemoveMember;
import vg.civcraft.mc.command.commands.RemoveSuperGroup;
import vg.civcraft.mc.command.commands.SetPassword;
import vg.civcraft.mc.command.commands.TransferGroup;

public class CommandHandler {
	public Map<String, Command> commands = new HashMap<String, Command>();
	
	public void registerCommands(){
		addCommands(new AcceptInvite("AcceptInvite"));
		addCommands(new AddSuperGroup("AddSuperGroup"));
		addCommands(new CreateGroup("CreateGroup"));
		addCommands(new DeleteGroup("DeleteGroup"));
		addCommands(new DisciplineGroup("DisiplineGroup"));
		addCommands(new GlobalStats("GlobalStats"));
		addCommands(new GroupStats("GroupStats"));
		addCommands(new InvitePlayer("InvitePlayer"));
		addCommands(new JoinGroup("JoinGroup"));
		addCommands(new ListGroups("ListGroups"));
		addCommands(new ListMembers("ListMembers"));
		addCommands(new MergeGroups("MergeGroups"));
		addCommands(new ModifyPermissions("ModifyPermissions"));
		addCommands(new RemoveMember("RemoveMember"));
		addCommands(new RemoveSuperGroup("RemoveSuperGroup"));
		addCommands(new SetPassword("SetPassword"));
		addCommands(new TransferGroup("TransferGroup"));
	}
	
	public void addCommands(Command command){
			commands.put(command.getIdentifier().toLowerCase(), command);
	}
	
	public boolean execute(CommandSender sender, String label, String[] args){
		if (commands.containsKey(label)){
			Command command = commands.get(label);
			if (args.length < command.getMinArguments() || args.length > command.getMaxArguments()){
				helpPlayer(command, sender);
				return true;
			}
			command.execute(sender, args);
		}
		return true;
	}
	
	public void helpPlayer(Command command, CommandSender sender){
		sender.sendMessage(new StringBuilder().append("§cCommand:§e " ).append(command.getName()).toString());
		sender.sendMessage(new StringBuilder().append("§cDescription:§e " ).append(command.getDescription()).toString());
		sender.sendMessage(new StringBuilder().append("§cUsage:§e ").append(command.getUsage()).toString());
	}
}
