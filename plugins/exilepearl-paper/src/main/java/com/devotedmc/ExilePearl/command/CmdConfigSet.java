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
		
		this.requiredArgs.add("rule");
		this.requiredArgs.add("value");
	}
	
	@Override
	public void perform() {
		
		ExileRule rule = ExileRule.fromString(argAsString(0));
		if (rule == null) {
			msg("<i>Unknown rule. Use <c>/ep config list <i>to list the rules.");
			return;
		}
		
		Boolean valBool = argAsBool(1);
		
		switch(rule) {
		
		case PEARL_RADIUS:
			config.setRulePearlRadius(argAsInt(1));
			break;
			
		default:
			config.setRule(rule, valBool);
			break;
		}
		
		msg("<g>Rule <a>%s <g>updated to <a>%s", rule.toString(), argAsString(1));
		msg("<i>Use <c>/ep config save <i>to save new values.");
	}
}
