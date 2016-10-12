package com.devotedmc.ExilePearl.command;

import com.devotedmc.ExilePearl.ExilePearlPlugin;
import com.devotedmc.ExilePearl.ExileRule;
import com.devotedmc.ExilePearl.config.PearlConfig;

public class CmdConfigSet extends PearlCommand
{
	private final PearlConfig config;
	
	public CmdConfigSet(ExilePearlPlugin plugin) {
		super(plugin);
		
		config = plugin.getPearlConfig();
		
		this.senderMustBePlayer = false;		
		
		this.aliases.add("set");
		this.helpShort = "Sets a exile rule value";
		
		this.commandArgs.add(required("rule", autoTab("exile_rule", "No matching rule found.")));
		this.commandArgs.add(required("value", autoTab("", "Enter the rule value.")));
	}
	
	@Override
	public void perform() {
		
		ExileRule rule = ExileRule.fromString(argAsString(0));
		if (rule == null) {
			msg("<i>Unknown rule. Use <c>/ep config list <i>to list the rules.");
			return;
		}
		
		Boolean valBool = argAsBool(1);
		String argStr = "";
		
		switch(rule) {
		
		case PEARL_RADIUS:
			config.setRulePearlRadius(argAsInt(1));
			argStr = argAsInt(1).toString();
			break;
			
		default:
			config.setRule(rule, valBool);
			argStr = valBool.toString();
			break;
		}
		
		msg("<g>Rule <a>%s <g>updated to <a>%s", rule.toString(), argStr);
		msg("<i>Use <c>/ep config save <i>to save new values.");
	}
}
