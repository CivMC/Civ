package com.devotedmc.ExilePearl.command;

import org.bukkit.Location;

import com.devotedmc.ExilePearl.ExilePearl;
import com.devotedmc.ExilePearl.ExilePearlPlugin;
import com.devotedmc.ExilePearl.Lang;
import com.devotedmc.ExilePearl.PearlPlayer;

public class CmdPearlLocate extends PearlCommand {

	public CmdPearlLocate(ExilePearlPlugin plugin) {
		super(plugin);
		this.aliases.add("locate");
		
		this.optionalArgs.put("player", "you");

		this.senderMustBePlayer = true;
		this.setHelpShort("Locates your exile pearl");
	}

	@Override
	public void perform() {
		
		if(this.args.size() == 1) {			
			PearlPlayer player = plugin.getPearlPlayer(this.argAsString(0));
			if (player == null) {
				msg(Lang.pearlNoPlayer);
				return;
			}
			
			if(!player.isExiled() || !player.getBcastPlayers().contains(me())) {
				msg(Lang.pearlPlayerNotExiledOrBcasting);
				return;
			}
			
			msg(getBcastString(player.getExilePearl()));
		}
		
		if (!me().isExiled()) {

			msg(Lang.pearlNotExiled);
			return;
		}

		ExilePearl pearl = pearlApi.getPearl(me().getUniqueId());
		if (pearl.verifyLocation()) {

			Location l = pearl.getHolder().getLocation();
			String name = pearl.getHolder().getName();
			
			msg(Lang.pearlPearlIsHeld, name, l.getBlockX(), l.getBlockY(), l.getBlockZ(), l.getWorld().getName());
			
			String bcastMsg = getBcastString(pearl);
			
			for(PearlPlayer p : me().getBcastPlayers()) {
				p.msg(bcastMsg);
			}
			
		} else {
			plugin.log("%s is freed because the pearl could not be located.", pearl.getLocation());
			pearlApi.freePearl(pearl);
		}
	}
	
	/**
	 * Gets the pearl broadcast message
	 * @param pearl The pearl instance
	 * @return The broadcast message
	 */
	private String getBcastString(ExilePearl pearl) {

		Location l = pearl.getHolder().getLocation();
		String name = pearl.getHolder().getName();
		
		return plugin.formatText(Lang.pearlBroadcast, me().getName(), 
				name, l.getBlockX(), l.getBlockY(), l.getBlockZ(), l.getWorld().getName());
	}
}
