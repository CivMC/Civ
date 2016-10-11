package com.devotedmc.ExilePearl.command;

import java.util.ArrayList;

import com.devotedmc.ExilePearl.ExilePearlPlugin;

import vg.civcraft.mc.civmodcore.util.TextUtil;

public class CmdAutoHelp extends PearlCommand
{
	public CmdAutoHelp(ExilePearlPlugin plugin) {
		super(plugin);
		
		this.aliases.add("help");
		
		this.setHelpShort("");
		
		this.optionalArgs.put("page","1");
	}
	
	@Override
	public void perform() {
		if (this.commandChain.size() == 0) return;
		BaseCommand<?> cmd = this.commandChain.get(this.commandChain.size()-1);
		
		ArrayList<String> lines = new ArrayList<String>();
		
		lines.addAll(cmd.getLongHelp());
		
		for(BaseCommand<?> c : cmd.getSubCommands()) {
			if (!c.validSenderType(sender, false)) {
				continue;
			}
			
			// Only list help for commands that are visible or the sender has permission for
			if (c.getVisibility() == CommandVisibility.VISIBLE || (c.getVisibility() == CommandVisibility.SECRET && c.validSenderPermissions(sender, false))) {
				lines.add(c.getUseageTemplate(this.commandChain, true));
			}
		}
		
		int page = this.argAsInt(0, 1);
		
		msg(TextUtil.getPage(lines, page, "Help for command \""+cmd.getAliases().get(0)+"\""));
	}
}
