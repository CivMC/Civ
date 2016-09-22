package com.devotedmc.ExilePearl.command;

import com.devotedmc.ExilePearl.ExilePearlConfig;
import com.devotedmc.ExilePearl.ExilePearlPlugin;
import com.devotedmc.ExilePearl.ExileRule;

public class CmdConfigSet extends PearlCommand
{
	private final ExilePearlConfig config;
	
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
		
		boolean result = false;
		Boolean valBool = false;
		
		switch(rule) {
		
		case PEARL_RADIUS:
			result = config.setRulePearlRadius(argAsInt(1));
			break;
		case DAMAGE_REINFORCEMENT:
			valBool = argAsBool(1);
			result = config.setRuleCanDamageReinforcement(valBool);
			break;
		default:
			msg("<b>That rule isn't implemented yet.");
			return;
		}
		
		if (result) {
			msg("<g>Rule <a>%s <g>updated to <a>%s", rule.toString(), argAsString(1));
			msg("<i>Use <c>/ep config save <i>to save new values.");
		} else {
			msg("<b>Failed to set rule value.");
		}
	}
}
