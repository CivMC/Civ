package vg.civcraft.mc.namelayer.command;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import vg.civcraft.mc.namelayer.command.commands.*;


public class CommandHandler {
	public Map<String, Command> commands = new HashMap<String, Command>();
	
	public void registerCommands(){
		addCommands(new AcceptInvite("AcceptInvite"));
		// Going to remove super group for now. It will have problems.
		//addCommands(new AddSuperGroup("AddSuperGroup"));
		addCommands(new CreateGroup("CreateGroup"));
		addCommands(new DeleteGroup("DeleteGroup"));
		addCommands(new DisciplineGroup("DisiplineGroup"));
		addCommands(new GlobalStats("GlobalStats"));
		addCommands(new GroupStats("GroupStats"));
		addCommands(new InfoDump("InfoDump"));
		addCommands(new InvitePlayer("InvitePlayer"));
		addCommands(new JoinGroup("JoinGroup"));
		addCommands(new ListGroups("ListGroups"));
		addCommands(new ListMembers("ListMembers"));
		addCommands(new ListPermissions("ListPermissions"));
		addCommands(new MergeGroups("MergeGroups"));
		addCommands(new ModifyPermissions("ModifyPermissions"));
		addCommands(new RemoveMember("RemoveMember"));
		addCommands(new RemoveSuperGroup("RemoveSuperGroup"));
		addCommands(new SetPassword("SetPassword"));
		addCommands(new TransferGroup("TransferGroup"));
		addCommands(new LeaveGroup("LeaveGroup"));
		addCommands(new ListGroupTypes("ListGroupTypes"));
		addCommands(new ListPlayerTypes("ListPlayerTypes"));
		addCommands(new ListCurrentInvites("ListCurrentInvites"));
		addCommands(new ToggleAutoAcceptInvites("AutoAcceptInvites"));
		addCommands(new PromotePlayer("PromotePlayer"));
		addCommands(new RevokeInvite("RevokeInvite"));
		addCommands(new ChangePlayerName("ChangePlayerName"));
	}
	
	public void addCommands(Command command){
			commands.put(command.getIdentifier().toLowerCase(), command);
	}
	
	public boolean execute(CommandSender sender, org.bukkit.command.Command cmd, String[] args){
		if (commands.containsKey(cmd.getName().toLowerCase())){
			Command command = commands.get(cmd.getName().toLowerCase());
			if (args.length < command.getMinArguments() || args.length > command.getMaxArguments()){
				helpPlayer(command, sender);
				return true;
			}
			command.execute(sender, args);
		}
		return true;
	}

	public List<String> complete(CommandSender sender, org.bukkit.command.Command cmd, String[] args){
		if (commands.containsKey(cmd.getName().toLowerCase())){
			Command command = commands.get(cmd.getName().toLowerCase());
			return command.tabComplete(sender, args);
		}
		return null;
	}

	
	public void helpPlayer(Command command, CommandSender sender){
		sender.sendMessage(new StringBuilder().append(ChatColor.RED + "Command: " ).append(command.getName()).toString());
		sender.sendMessage(new StringBuilder().append(ChatColor.RED + "Description: " ).append(command.getDescription()).toString());
		sender.sendMessage(new StringBuilder().append(ChatColor.RED + "Usage: ").append(command.getUsage()).toString());
	}
}
