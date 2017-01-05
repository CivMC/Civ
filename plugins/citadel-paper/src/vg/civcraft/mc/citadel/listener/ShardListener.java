package vg.civcraft.mc.citadel.listener;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import vg.civcraft.mc.bettershards.events.PlayerChangeServerEvent;
import vg.civcraft.mc.bettershards.events.PlayerChangeServerReason;
import vg.civcraft.mc.citadel.PlayerState;
import vg.civcraft.mc.citadel.ReinforcementMode;
import vg.civcraft.mc.mercury.MercuryAPI;
import vg.civcraft.mc.mercury.events.AsyncPluginBroadcastMessageEvent;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.group.Group;

public class ShardListener implements Listener {

	@EventHandler(ignoreCancelled = true)
	public void serverChange(PlayerChangeServerEvent e) {
		StringBuilder sb;
		if (e.getReason() == PlayerChangeServerReason.PORTAL) {
			sb = new StringBuilder();
			sb.append("modetransfer");
			sb.append("|");
			sb.append(e.getPlayerUUID());
			sb.append("|");
			PlayerState ps = PlayerState.get(e.getPlayerUUID());
			sb.append(ps.getMode().toString());
			sb.append("|");
			sb.append(ps.getEasyMode());
			if (ps.getMode() == ReinforcementMode.REINFORCEMENT
					|| ps.getMode() == ReinforcementMode.REINFORCEMENT_FORTIFICATION) {
				sb.append("|");
				if (ps.getGroup() == null) {
					return;
				}
				sb.append(ps.getGroup().getName());
				if (ps.getMode() == ReinforcementMode.REINFORCEMENT_FORTIFICATION) {
					sb.append("|");
					ItemStack is = ps.getReinforcementType().getItemStack();
					sb.append(is.getType());
					sb.append("|");
					sb.append(is.getDurability());
					if (is.getItemMeta().getLore() != null) {
						for (String s : is.getItemMeta().getLore()) {
							sb.append("|");
							sb.append(s);
						}
					}
				}
			}
		}
		else {
			return;
		}
		MercuryAPI.sendMessage(e.getServerTravelingTo(), sb.toString(),
				"Citadel");
	}

	@EventHandler
	public void messageReceive(AsyncPluginBroadcastMessageEvent e) {
		if (!e.getChannel().equals("Citadel")) {
			return;
		}
		String[] data = e.getMessage().split("\\|");
		if (data[0].equals("modetransfer")) {
			UUID uuid = UUID.fromString(data[1]);
			PlayerState ps = PlayerState.get(uuid);
			ReinforcementMode mode = ReinforcementMode.valueOf(data[2]);
			ps.setMode(mode);
			boolean easyMode = Boolean.valueOf(data[3]);
			if (!easyMode) {
				//default for easy mode is true, so only change if its turned off
				ps.toggleEasyMode();
			}
			if (mode == ReinforcementMode.REINFORCEMENT
					|| mode == ReinforcementMode.REINFORCEMENT_FORTIFICATION) {
				Group g = GroupManager.getGroup(data[3]);
				ps.setGroup(g);
				if (mode == ReinforcementMode.REINFORCEMENT_FORTIFICATION) {
					Material m = Material.valueOf(data[4]);
					short dura = Short.parseShort(data[5]);
					List <String> lore = new LinkedList<String>();
					for(int i = 6; i < data.length; i++) {
						lore.add(data[i]);
					}
					ItemStack is = new ItemStack(m, 1, dura);
					ItemMeta im = is.getItemMeta();
					im.setLore(lore);
					is.setItemMeta(im);
					ps.setFortificationItemStack(is);
				}
			}
		}
	}
}
