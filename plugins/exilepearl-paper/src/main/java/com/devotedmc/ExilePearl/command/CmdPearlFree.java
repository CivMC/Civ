package com.devotedmc.ExilePearl.command;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import com.devotedmc.ExilePearl.ExilePearl;
import com.devotedmc.ExilePearl.ExilePearlApi;
import com.devotedmc.ExilePearl.Lang;
import com.devotedmc.ExilePearl.PearlFreeReason;

public class CmdPearlFree extends PearlCommand {

	public CmdPearlFree(ExilePearlApi pearlApi) {
		super(pearlApi);
		this.aliases.add("free");

		this.senderMustBePlayer = true;
		this.setHelpShort("Free an exile pearl");
	}

	@Override
	public void perform() {
		ExilePearl pearl = plugin.getPearlFromItemStack(player().getInventory().getItemInMainHand());
		if (pearl == null) {
			msg(Lang.pearlMustBeHoldingPearl);
			return;
		}

		if (plugin.freePearl(pearl, PearlFreeReason.FREED_BY_PLAYER)) {
			msg(Lang.pearlYouFreed, pearl.getPlayerName());
			player().getInventory().setItemInMainHand(new ItemStack(Material.AIR));
		}
	}
}
