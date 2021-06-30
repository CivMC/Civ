package vg.civcraft.mc.citadel.playerstate;

import java.util.HashMap;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.citadel.CitadelPermissionHandler;
import vg.civcraft.mc.citadel.CitadelUtility;
import vg.civcraft.mc.citadel.ReinforcementLogic;
import vg.civcraft.mc.citadel.events.ReinforcedBlockBreak;
import vg.civcraft.mc.citadel.events.ReinforcementBypassEvent;
import vg.civcraft.mc.citadel.events.ReinforcementDamageEvent;
import vg.civcraft.mc.citadel.model.Reinforcement;
import vg.civcraft.mc.citadel.reinforcementtypes.ReinforcementType;
import vg.civcraft.mc.civmodcore.players.settings.PlayerSettingAPI;
import vg.civcraft.mc.civmodcore.players.settings.impl.BooleanSetting;
import vg.civcraft.mc.civmodcore.utilities.DelayedItemDrop;

public abstract class AbstractPlayerState {

	protected UUID uuid;

	public AbstractPlayerState(Player p) {
		if (p == null) {
			throw new IllegalArgumentException("Player for player state can not be null");
		}
		this.uuid = p.getUniqueId();
	}

	public abstract String getName();

	public abstract void handleBlockPlace(BlockPlaceEvent e);

	public void handleBreakBlock(BlockBreakEvent e) {
		Reinforcement rein = ReinforcementLogic.getReinforcementProtecting(e.getBlock());
		if (rein == null) {
			// no reinforcement, normal break which we dont care about
			return;
		}
		if (CitadelUtility.isPlant(e.getBlock())) {
			if (rein.hasPermission(e.getPlayer(), CitadelPermissionHandler.getCrops())
					&& !e.getBlock().getLocation().equals(rein.getLocation())) {
				// allow, because player has crop permission and the only reinforcement
				// protecting is in the soil
				return;
			}
		}
		boolean hasAccess = rein.hasPermission(e.getPlayer(), CitadelPermissionHandler.getBypass());
		BooleanSetting setting = (BooleanSetting) PlayerSettingAPI.getSetting("citadelBypass");
		boolean hasByPass = setting.getValue(e.getPlayer());
		if (hasAccess && hasByPass) {
			ReinforcementBypassEvent bypassEvent = new ReinforcementBypassEvent(e.getPlayer(), rein);
			Bukkit.getPluginManager().callEvent(bypassEvent);
			if (bypassEvent.isCancelled()) {
				e.setCancelled(true);
				return;
			}
			if (rein.rollForItemReturn()) {
				giveReinforcement(e.getBlock().getLocation().clone().add(0.5, 0.5, 0.5), e.getPlayer(), rein.getType());
			}
			rein.setHealth(-1);
			return;
		}
		if (hasAccess) {
			CitadelUtility.sendAndLog(e.getPlayer(), ChatColor.GREEN,
					"You could bypass this reinforcement " + "if you turn bypass mode on with '/ctb'");
		}
		e.setCancelled(true);
		float damage = ReinforcementLogic.getDamageApplied(rein);
		ReinforcementDamageEvent dre = new ReinforcementDamageEvent(e.getPlayer(), rein, damage);
		Bukkit.getPluginManager().callEvent(dre);
		if (dre.isCancelled()) {
			return;
		}
		damage = dre.getDamageDone();
		ReinforcementLogic.damageReinforcement(rein, damage, e.getPlayer());
		if (rein.getHealth() <= 0) {
			// in the case of double chests or similar there might now be another rein
			// protecting this block
			Reinforcement backupRein = ReinforcementLogic.getReinforcementProtecting(e.getBlock());
			if (backupRein == null) {
				e.setCancelled(false);
				ReinforcedBlockBreak rbbe = new ReinforcedBlockBreak(e.getPlayer(), rein, e);
				Bukkit.getPluginManager().callEvent(rbbe);
			}
		}
	}

	public abstract void handleInteractBlock(PlayerInteractEvent e);

	protected static void giveReinforcement(Location location, Player p, ReinforcementType type) {
		HashMap<Integer, ItemStack> notAdded = p.getInventory().addItem(type.getItem().clone());
		if (!notAdded.isEmpty()) {
			DelayedItemDrop.dropAt(location, type.getItem().clone());
		}
	}

	public abstract String getOverlayText();

	@Override
	public abstract boolean equals(Object o);

}
