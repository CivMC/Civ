package com.devotedmc.ExilePearl.command;

import com.devotedmc.ExilePearl.ExilePearlPlugin;
import com.devotedmc.ExilePearl.ExileRule;
import com.devotedmc.ExilePearl.PearlConfig;
import com.devotedmc.ExilePearl.util.TextUtil;

public class CmdConfigList extends PearlCommand
{
	
	private final PearlConfig config;
	private int lineLength;
	
	public CmdConfigList(ExilePearlPlugin plugin) {
		super(plugin);
		
		config = plugin.getPearlConfig();
		
		this.senderMustBePlayer = false;
		this.errorOnToManyArgs = false;
		
		this.aliases.add("list");
		this.helpShort = "Lists the exile rule options";
	}
	
	@Override
	public void perform() {
		
		StringBuilder sb = new StringBuilder();
		sb.append(TextUtil.instance().titleize("Exile Pearl Rules") + "\n");
		lineLength = 0;

		appendIntRule(sb, ExileRule.PEARL_RADIUS.toString(), config.getRulePearlRadius());
		appendBooleanRule(sb, ExileRule.DAMAGE_REINFORCEMENT.toString(), config.getRuleCanDamageReinforcement());
		appendBooleanRule(sb, ExileRule.CREATE_BASTION.toString(), config.getRuleCanCreateBastion());
		appendBooleanRule(sb, ExileRule.DAMAGE_BASTION.toString(), config.getRuleCanDamageBastion());
		appendBooleanRule(sb, ExileRule.ENTER_BASTION.toString(), config.getRuleCanEnterBastion());
		appendBooleanRule(sb, ExileRule.THROW_PEARL.toString(), config.getRuleCanThrowEnderPearl());
		appendBooleanRule(sb, ExileRule.CHAT.toString(), config.getRuleCanChatLocal());
		appendBooleanRule(sb, ExileRule.PVP.toString(), config.getRuleCanPvp());
		appendBooleanRule(sb, ExileRule.IGNITE.toString(), config.getRuleCanIgnite());
		appendBooleanRule(sb, ExileRule.USE_BUCKET.toString(), config.getRuleCanUseBucket());
		appendBooleanRule(sb, ExileRule.USE_POTIONS.toString(), config.getRuleCanUsePotions());
		appendBooleanRule(sb, ExileRule.USE_BED.toString(), config.getRuleCanUseBed());
		appendBooleanRule(sb, ExileRule.SUICIDE.toString(), config.getRuleCanSuicide());
		appendBooleanRule(sb, ExileRule.SNITCH.toString(), config.getRuleCanPlaceSnitch());
		appendBooleanRule(sb, ExileRule.MINE.toString(), config.getRuleCanMine());
		appendBooleanRule(sb, ExileRule.BREW.toString(), config.getRuleCanBrew());
		appendBooleanRule(sb, ExileRule.ENCHANT.toString(), config.getRuleCanEnchant());
		sb.setLength(sb.length() - 2);
		
		msg(sb.toString());
	}
	
	private void appendBooleanRule(StringBuilder sb, String name, boolean value) {
		String tag = "<g>";
		if (!value) {
			tag = "<b>";
		}
		
		String txt = String.format("%s%s<n>, ", tag, name);
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
