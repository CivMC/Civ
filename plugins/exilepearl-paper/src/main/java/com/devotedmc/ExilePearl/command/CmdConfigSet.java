package com.devotedmc.ExilePearl.command;

import com.devotedmc.ExilePearl.ExilePearlPlugin;
import com.devotedmc.ExilePearl.ExileRule;
import com.devotedmc.ExilePearl.PearlConfig;

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
			
		case DAMAGE_BASTION:
			valBool = argAsBool(1);
			result = config.setRuleCanDamageBastion(valBool);
			break;
			
		case ENTER_BASTION:
			valBool = argAsBool(1);
			result = config.setRuleCanEnterBastion(valBool);
			break;
			
		case THROW_PEARL:
			valBool = argAsBool(1);
			result = config.setRuleCanThrowEnderPearl(valBool);
			break;
			
		case CHAT:
			valBool = argAsBool(1);
			result = config.setRuleCanChatLocal(valBool);
			break;
			
		case PVP:
			valBool = argAsBool(1);
			result = config.setRuleCanPvp(valBool);
			break;
			
		case IGNITE:
			valBool = argAsBool(1);
			result = config.setRuleCanIgnite(valBool);
			break;
			
		case USE_BUCKET:
			valBool = argAsBool(1);
			result = config.setRuleCanUseBucket(valBool);
			break;
			
		case USE_POTIONS:
			valBool = argAsBool(1);
			result = config.setRuleCanUsePotions(valBool);
			break;
			
		case USE_BED:
			valBool = argAsBool(1);
			result = config.setRuleCanUseBed(valBool);
			break;
			
		case SUICIDE:
			valBool = argAsBool(1);
			result = config.setRuleCanSuicide(valBool);
			break;
			
		case SNITCH:
			valBool = argAsBool(1);
			result = config.setRuleCanPlaceSnitch(valBool);
			break;
			
		case MINE:
			valBool = argAsBool(1);
			result = config.setRuleCanMine(valBool);
			break;
			
		case BREW:
			valBool = argAsBool(1);
			result = config.setRuleCanBrew(valBool);
			break;
			
		case ENCHANT:
			valBool = argAsBool(1);
			result = config.setRuleCanEnchant(valBool);
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
