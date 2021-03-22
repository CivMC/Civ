package com.untamedears.jukealert.model.actions.abstr;

import com.untamedears.jukealert.JukeAlert;
import com.untamedears.jukealert.model.Snitch;
import com.untamedears.jukealert.model.actions.ActionCacheState;
import com.untamedears.jukealert.model.actions.LoggedActionPersistence;
import com.untamedears.jukealert.util.JAUtility;
import java.util.UUID;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import vg.civcraft.mc.civmodcore.inventory.items.ItemUtils;
import vg.civcraft.mc.namelayer.NameAPI;

public abstract class LoggablePlayerAction extends PlayerAction implements LoggableAction {
	
	private ActionCacheState state;
	private int id;

	public LoggablePlayerAction(long time, Snitch snitch, UUID player) {
		super(time, snitch, player);
		state = ActionCacheState.NEW;
	}
	
	
	@Override
	public LoggedActionPersistence getPersistence() {
		return new LoggedActionPersistence(player, null, time, null);
	}
	
	@Override
	public void setID(int id) {
		this.id = id;
		state = ActionCacheState.NORMAL;
	}
	
	@Override
	public int getID() {
		return id;
	}
	
	@Override
	public void setCacheState(ActionCacheState state) {
		this.state = state;
	}
	
	@Override
	public ActionCacheState getCacheState() {
		return state;
	}

	@Override
	public boolean isLifeCycleEvent() {
		return false;
	}
	
	@Override
	public TextComponent getChatRepresentation(Location reference, boolean live) {
		Location referenceLoc = getLocationForStringRepresentation();
		boolean sameWorld = JAUtility.isSameWorld(referenceLoc, reference);
		TextComponent comp = new TextComponent(
				String.format("%s%s  %s%s  ", ChatColor.GOLD, getChatRepresentationIdentifier(), ChatColor.GREEN, NameAPI.getCurrentName(getPlayer())));
		if (live) {
			comp.addExtra(JAUtility.genTextComponent(snitch));
			comp.addExtra(String.format("  %s%s", ChatColor.YELLOW,
					JAUtility.formatLocation(referenceLoc, !sameWorld)));
		}
		else {
			//dont need to explicitly list location when retrieving logs and its the snitch location
			if (referenceLoc != snitch.getLocation()) {
				comp.addExtra(String.format("%s%s", ChatColor.YELLOW,
						JAUtility.formatLocation(referenceLoc, !sameWorld)));
			}
			comp.addExtra(new TextComponent(ChatColor.AQUA + getFormattedTime()));
		}
		return comp;
	}
	
	protected void enrichGUIItem(ItemStack item) {
		if (item.getType().isAir()) {
			JukeAlert.getInstance().getLogger().info("Tried to enrich air");
			item = new ItemStack(Material.STONE);
		}
		ItemUtils.addLore(item, String.format("%sPlayer: %s", ChatColor.GOLD, getPlayerName()),
				String.format("%sTime: %s", ChatColor.LIGHT_PURPLE,getFormattedTime()));
		ItemUtils.setDisplayName(item, ChatColor.GOLD + getGUIName());
	}
	
	protected String getGUIName() {
		return getChatRepresentationIdentifier();
	}
	
	protected Location getLocationForStringRepresentation() {
		return snitch.getLocation();
	}
	
	protected abstract String getChatRepresentationIdentifier();
	
	protected ItemStack getSkullFor(UUID uuid) {
		ItemStack is = new ItemStack(Material.PLAYER_HEAD);
		SkullMeta skullMeta = (SkullMeta) is.getItemMeta();
		skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(uuid));
		is.setItemMeta(skullMeta);
		return is;
	}

}
