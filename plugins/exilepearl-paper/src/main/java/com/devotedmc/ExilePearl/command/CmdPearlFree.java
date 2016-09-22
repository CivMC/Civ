package com.devotedmc.ExilePearl.command;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import com.devotedmc.ExilePearl.ExilePearl;
import com.devotedmc.ExilePearl.ExilePearlPlugin;
import com.devotedmc.ExilePearl.Lang;

public class CmdPearlFree extends PearlCommand {

	public CmdPearlFree(ExilePearlPlugin plugin) {
		super(plugin);
		this.aliases.add("free");

		this.senderMustBePlayer = true;
		this.setHelpShort("Frees a prison pearl");
	}

	@SuppressWarnings("deprecation")
	@Override
	public void perform() {
		ExilePearl pearl = pearls.getPearlByItem(me().getBukkitPlayer().getItemInHand());
		if (pearl == null) {
			msg(Lang.pearlMustBeHoldingPearl);
			return;
		}
		
		if (pearls.freePearl(pearl)) {
			me().msg(Lang.pearlYouFreed, pearl.getName());
			me().getBukkitPlayer().setItemInHand(new ItemStack(Material.AIR));
		}
	}
}
