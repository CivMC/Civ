package com.devotedmc.ExilePearl.command;

import com.devotedmc.ExilePearl.ExilePearl;
import com.devotedmc.ExilePearl.ExilePearlApi;
import com.devotedmc.ExilePearl.Lang;
import com.devotedmc.ExilePearl.PearlFreeReason;

public class CmdPearlLocate extends PearlCommand {

	public CmdPearlLocate(ExilePearlApi pearlApi) {
		super(pearlApi);
		this.aliases.add("locate");

		this.senderMustBePlayer = true;
		this.setHelpShort("Locates your exile pearl");
	}

	@Override
	public void perform() {
		
		ExilePearl pearl = plugin.getPearl(player().getUniqueId());
		
		if (pearl == null) {
			msg(Lang.pearlNotExiled);
			return;
		}

		if (pearl.verifyLocation()) {
			pearl.performBroadcast();
			
		} else {
			plugin.freePearl(pearl, PearlFreeReason.VALIDATION_FAILED);
		}
	}
}
