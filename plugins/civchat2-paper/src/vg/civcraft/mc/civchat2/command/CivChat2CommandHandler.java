package vg.civcraft.mc.civchat2.command;

import vg.civcraft.mc.civmodcore.command.CommandHandler;
import vg.civcraft.mc.civchat2.command.commands.Exit;
import vg.civcraft.mc.civchat2.command.commands.GroupChat;
import vg.civcraft.mc.civchat2.command.commands.Ignore;
import vg.civcraft.mc.civchat2.command.commands.IgnoreGroup;
import vg.civcraft.mc.civchat2.command.commands.IgnoreList;
import vg.civcraft.mc.civchat2.command.commands.Reply;
import vg.civcraft.mc.civchat2.command.commands.Tell;
import vg.civcraft.mc.civchat2.command.commands.Afk;

public class CivChat2CommandHandler extends CommandHandler {

	public void registerCommands() {

		addCommands(new Tell("tell"));
		addCommands(new Afk("afk"));
		addCommands(new Reply("reply"));
		addCommands(new GroupChat("groupc"));
		addCommands(new Ignore("ignore"));
		addCommands(new IgnoreGroup("ignoregroup"));
		addCommands(new IgnoreList("ignorelist"));
		addCommands(new Exit("exit"));
	}
}
