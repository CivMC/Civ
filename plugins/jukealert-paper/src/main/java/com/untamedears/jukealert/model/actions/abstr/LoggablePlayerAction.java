package com.untamedears.jukealert.model.actions.abstr;

import com.untamedears.jukealert.JukeAlert;
import com.untamedears.jukealert.model.Snitch;
import com.untamedears.jukealert.model.actions.ActionCacheState;
import com.untamedears.jukealert.model.actions.LoggedActionPersistence;
import com.untamedears.jukealert.util.JAUtility;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.civmodcore.CivModCorePlugin;
import vg.civcraft.mc.civmodcore.inventory.gui.ClickableInventory;
import vg.civcraft.mc.civmodcore.inventory.gui.DecorationStack;
import vg.civcraft.mc.civmodcore.inventory.gui.IClickable;
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
	public TextComponent getChatRepresentation(Location reference, boolean live, boolean censor) {
		Location referenceLoc = getLocationForStringRepresentation();
		boolean sameWorld = JAUtility.isSameWorld(referenceLoc, reference);
		String referenceLocText = censor ? "[*** *** ***]" : JAUtility.formatLocation(referenceLoc, !sameWorld);

		TextComponent comp = new TextComponent(
				String.format("%s%s  %s%s  ", ChatColor.GOLD, getChatRepresentationIdentifier(), ChatColor.GREEN, NameAPI.getCurrentName(getPlayer())));
		if (live) {
			comp.addExtra(JAUtility.genTextComponent(snitch));
			comp.addExtra(String.format("  %s%s", ChatColor.YELLOW, referenceLocText));
		}
		else {
			//dont need to explicitly list location when retrieving logs and its the snitch location
			if (referenceLoc != snitch.getLocation()) {
				comp.addExtra(String.format("%s%s ", ChatColor.YELLOW, referenceLocText));
			}
			// Example: 2011-12-03T10:15:30
			comp.addExtra(new TextComponent(ChatColor.AQUA + getFormattedTime(DateTimeFormatter.ISO_LOCAL_DATE_TIME)));
		}
		return comp;
	}
	
	protected void enrichGUIItem(ItemStack item) {
		if (item.getType().isAir()) {
			JukeAlert.getInstance().getLogger().info("Tried to enrich air");
			item = new ItemStack(Material.STONE);
		}
		ItemUtils.addLore(item, String.format("%sPlayer: %s", ChatColor.GOLD, getPlayerName()),
			// Example: Tue, 3 Jun 2008 11:05:30 GMT
			String.format("%sTime: %s", ChatColor.LIGHT_PURPLE, getFormattedTime(DateTimeFormatter.RFC_1123_DATE_TIME)));
		ItemUtils.setDisplayName(item, ChatColor.GOLD + getGUIName());
	}
	
	protected String getGUIName() {
		return getChatRepresentationIdentifier();
	}
	
	protected Location getLocationForStringRepresentation() {
		return snitch.getLocation();
	}

	protected IClickable getEnrichedClickableSkullFor(UUID uuid) {
		CompletableFuture<ItemStack> itemReadyFuture = new CompletableFuture<>();
		ItemStack is = CivModCorePlugin.getInstance().getSkinCache().getHeadItem(getPlayer(),
				() -> new ItemStack(Material.PLAYER_HEAD),
				itemReadyFuture::complete);
		enrichGUIItem(is);
		return new DecorationStack(is) {
			@Override
			public void addedToInventory(ClickableInventory inv, int slot) {
				itemReadyFuture.thenAccept(newItem -> {
					LoggablePlayerAction.this.enrichGUIItem(newItem);
					this.item = newItem;
					inv.setItem(newItem, slot);
				});
				super.addedToInventory(inv, slot);
			}
		};
	}
}
