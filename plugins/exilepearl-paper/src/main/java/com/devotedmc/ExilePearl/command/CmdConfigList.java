package com.devotedmc.ExilePearl.command;

import com.devotedmc.ExilePearl.ExilePearlApi;
import com.devotedmc.ExilePearl.ExileRule;
import com.devotedmc.ExilePearl.config.PearlConfig;

import vg.civcraft.mc.civmodcore.util.TextUtil;

public class CmdConfigList extends PearlCommand {
	private final PearlConfig config;
	private int lineLength;
	
	public CmdConfigList(ExilePearlApi pearlApi) {
		super(pearlApi);
		
		config = plugin.getPearlConfig();
		
		this.senderMustBePlayer = false;
		this.errorOnToManyArgs = false;
		
		this.aliases.add("list");
		this.helpShort = "Lists the exile rule options";
	}
	
	@Override
	public void perform() {
		
		StringBuilder sb = new StringBuilder();
		sb.append(TextUtil.titleize("Exile Pearl Rules") + "\n");
		lineLength = 0;

		appendIntRule(sb, ExileRule.PEARL_RADIUS.toString(), config.getRulePearlRadius());
		appendBooleanRule(sb, ExileRule.DAMAGE_REINFORCEMENT);
		appendBooleanRule(sb, ExileRule.CREATE_BASTION);
		appendBooleanRule(sb, ExileRule.DAMAGE_BASTION);
		appendBooleanRule(sb, ExileRule.ENTER_BASTION);
		appendBooleanRule(sb, ExileRule.THROW_PEARL);
		appendBooleanRule(sb, ExileRule.CHAT);
		appendBooleanRule(sb, ExileRule.PVP);
		appendBooleanRule(sb, ExileRule.KILL_PETS);
		appendBooleanRule(sb, ExileRule.KILL_MOBS);
		appendBooleanRule(sb, ExileRule.IGNITE);
		appendBooleanRule(sb, ExileRule.USE_BUCKET);
		appendBooleanRule(sb, ExileRule.USE_POTIONS);
		appendBooleanRule(sb, ExileRule.USE_BED);
		appendBooleanRule(sb, ExileRule.SUICIDE);
		appendBooleanRule(sb, ExileRule.SNITCH);
		appendBooleanRule(sb, ExileRule.MINE);
		appendBooleanRule(sb, ExileRule.BREW);
		appendBooleanRule(sb, ExileRule.ENCHANT);
		appendBooleanRule(sb, ExileRule.COLLECT_XP);
		appendBooleanRule(sb, ExileRule.USE_ANVIL);
		sb.setLength(sb.length() - 2);
		
		msg(sb.toString());
	}
	
	private void appendBooleanRule(StringBuilder sb, ExileRule rule) {
		String tag = "<g>";
		boolean value = config.canPerform(rule);
		if (!value) {
			tag = "<b>";
		}
		
		String txt = String.format("%s%s<n>, ", tag, rule.toString());
		int strLength = txt.length() - 6;
		if (lineLength + strLength > 50) {
			sb.append("\n");
			lineLength = 0;
		}
		
		sb.append(txt);
		lineLength += strLength;
	}
	
	private void appendIntRule(StringBuilder sb, String name, int value) {
		sb.append(String.format("<a>%s<n>(%d), ", name, value));
	}
}
