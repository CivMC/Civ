package com.devotedmc.ExilePearl.command;

import com.devotedmc.ExilePearl.ExilePearl;
import com.devotedmc.ExilePearl.ExilePearlApi;
import com.devotedmc.ExilePearl.Lang;
import com.devotedmc.ExilePearl.PearlType;

public class CmdSummon extends PearlCommand {

	public CmdSummon(ExilePearlApi pearlApi) {
		super(pearlApi);
		this.aliases.add("summon");
		this.aliases.add("ppsummon");
		
		this.senderMustBePlayer = true;
		this.setHelpShort("Summon a prisoner");
	}
	
	@Override
	public void perform() {
		ExilePearl pearl = plugin.getPearlFromItemStack(player().getInventory().getItemInMainHand());
		if(pearl == null) {
			msg(Lang.pearlMustBeHoldingPearl);
			return;
		}

		if(plugin.summonPearl(pearl, player())) {
			msg(Lang.pearlSummoned, pearl.getPlayerName());
		} else {
			msg(Lang.pearlCantSummon);
		}
	}
}
