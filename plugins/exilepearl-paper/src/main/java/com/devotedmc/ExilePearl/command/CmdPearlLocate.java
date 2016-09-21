package com.devotedmc.ExilePearl.command;

import org.bukkit.Location;

import com.devotedmc.ExilePearl.ExilePearl;
import com.devotedmc.ExilePearl.ExilePearlPlugin;
import com.devotedmc.ExilePearl.Lang;
import com.devotedmc.ExilePearl.PearlPlayer;

public class CmdPearlLocate extends PearlCommand {

	public CmdPearlLocate(ExilePearlPlugin plugin) {
		super(plugin);
		this.aliases.add("l");
		this.aliases.add("locate");

		this.senderMustBePlayer = true;
		this.setHelpShort("Locates your exile pearl");
	}

	@Override
	public void perform() {
		ExilePearl pp = pearls.getById(me().getUniqueId());
		
		if (pp == null) {
			msg(Lang.pearlNotImprisoned);
			return;
		}
		
		if (pp.verifyLocation()) {

			Location l = pp.getHolder().getLocation();
			String name = pp.getHolder().getName();
			
			msg(Lang.pearlPearlIsHeld, name, l.getBlockX(), l.getBlockY(), l.getBlockZ(), l.getWorld().getName());
			
			String bcastMsg = plugin.formatText(Lang.pearlBroadcast, me().getName(), 
					name, l.getBlockX(), l.getBlockY(), l.getBlockZ(), l.getWorld().getName());
			
			for(PearlPlayer p : me().getBcastPlayers()) {
				p.msg(bcastMsg);
			}
			
		} else {
			plugin.log("%s is freed because the pearl could not be located.", pp.getLocation());
			pearls.freePearl(pp);
		}
	}
}
