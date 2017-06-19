/**
 * @author Aleksey Terzi
 *
 */

package com.aleksey.castlegates.plugins.citadel;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.reinforcement.PlayerReinforcement;
import vg.civcraft.mc.citadel.reinforcement.Reinforcement;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.group.Group;

import com.aleksey.castlegates.CastleGates;
import com.aleksey.castlegates.DeprecatedMethods;
import com.aleksey.castlegates.database.ReinforcementInfo;
import com.aleksey.castlegates.utils.Helper;

public class CitadelManager extends Thread implements ICitadelManager, Runnable {
	private static class UpdatedReinforcement {
		public Reinforcement rein;
		public boolean deleted;

		public UpdatedReinforcement(Reinforcement rein, boolean deleted) {
			this.rein = rein;
			this.deleted = deleted;
		}
	}

	private long lastExecute = System.currentTimeMillis();
    private AtomicBoolean kill = new AtomicBoolean(false);
	private AtomicBoolean run = new AtomicBoolean(false);

    private List<UpdatedReinforcement> updatedReinforcements = new ArrayList<UpdatedReinforcement>();
	private Queue<UpdatedReinforcement> localUpdatedReinforcements = new ArrayDeque<UpdatedReinforcement>();

    public void init() {
    	startThread();
    }

    public void close() {
    	terminateThread();
    }

	public double getMaxRedstoneDistance() {
		return CastleGates.getConfigManager().getMaxRedstoneDistance();
	}

	public ICitadel getCitadel(List<Player> players, Location loc) {
		PlayerReinforcement playerRein = null;
    	boolean hasAccess = false;
    	boolean useJukeAlert = false;

		Reinforcement rein = Citadel.getReinforcementManager().getReinforcement(loc);

		hasAccess = rein == null || !(rein instanceof PlayerReinforcement);

		if(!hasAccess) {
			playerRein = (PlayerReinforcement)rein;

			if (players != null && players.size() > 0) {
				for (Player player : players) {
					if (playerRein.canAccessDoors(player) || player.hasPermission("citadel.admin")) {
						hasAccess = true;
						break;
					}
				}
			}

			if(!hasAccess) {
				if(CastleGates.getJukeAlertManager().hasJukeAlertAccess(loc, playerRein.getGroup().getName())) {
					hasAccess = true;
					useJukeAlert = true;
				}
			}
		}

		return new com.aleksey.castlegates.plugins.citadel.Citadel(players, playerRein, hasAccess, useJukeAlert);
	}

	public boolean canBypass(Player player, Location loc) {
		Reinforcement rein = Citadel.getReinforcementManager().getReinforcement(loc);

		if(rein == null || !(rein instanceof PlayerReinforcement)) return true;

		if(player == null) return false;

		PlayerReinforcement playerRein = (PlayerReinforcement)rein;

		return playerRein.canBypass(player)
			|| player.hasPermission("citadel.admin.bypassmode");
	}

	public boolean canViewInformation(Player player, Location loc) {
		Reinforcement rein = Citadel.getReinforcementManager().getReinforcement(loc);

		if(rein == null || !(rein instanceof PlayerReinforcement)) return true;

		if(player == null) return false;

		PlayerReinforcement playerRein = (PlayerReinforcement)rein;

		return playerRein.canViewInformation(player)
			|| player.hasPermission("citadel.admin.ctinfodetails");
	}

	public ReinforcementInfo removeReinforcement(Location loc) {
		Reinforcement rein = Citadel.getReinforcementManager().getReinforcement(loc);

		if(rein == null || !(rein instanceof PlayerReinforcement)) return null;

		PlayerReinforcement playerRein = (PlayerReinforcement)rein;

		ReinforcementInfo info = new ReinforcementInfo();
		info.material_id = DeprecatedMethods.getMaterialId(playerRein.getMaterial());
		info.durability = playerRein.getDurability();
		info.insecure = playerRein.isInsecure();
		info.group_id = playerRein.getGroupId();
		info.maturation_time = playerRein.getMaturationTime();
		info.lore = Helper.getLore(playerRein.getStackRepresentation());
		info.acid_time = playerRein.getAcidTime();

		synchronized(this.updatedReinforcements) {
			this.updatedReinforcements.add(new UpdatedReinforcement(rein, true));
		}

		return info;
	}

	public boolean createReinforcement(ReinforcementInfo info, Location loc) {
		if(info == null) return false;

		Group group = GroupManager.getGroup(info.group_id);
		ItemStack stack = getItemStack(info);

		PlayerReinforcement rein = new PlayerReinforcement(
				loc,
				info.durability,
				info.maturation_time,
				info.acid_time,
				group,
				stack
				);

		rein.setInsecure(info.insecure);

		synchronized(this.updatedReinforcements) {
			this.updatedReinforcements.add(new UpdatedReinforcement(rein, false));
		}

		return true;
	}

	private static ItemStack getItemStack(ReinforcementInfo info) {
		Material material = DeprecatedMethods.getMaterial(info.material_id);
		ItemStack stack = new ItemStack(material);

		Helper.setLore(stack, info.lore);

		return stack;
	}

    public void startThread() {
        setName("CastleGates CitadelManager Thread");
        setPriority(Thread.MIN_PRIORITY);
        start();

        CastleGates.getPluginLogger().log(Level.INFO, "CitadelManager thread started");
    }

	public void terminateThread() {
		this.kill.set(true);

		while (this.run.get()) {
			try {
				Thread.sleep(100);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		saveData();

		CastleGates.getPluginLogger().log(Level.INFO, "CitadelManager thread stopped");
	}

    @Override
    public void run() {
    	this.run.set(true);

    	try {
			while (!this.isInterrupted() && !this.kill.get()) {
				try {
					long timeWait = lastExecute + CastleGates.getConfigManager().getDataWorkerRate() - System.currentTimeMillis();
					lastExecute = System.currentTimeMillis();
					if (timeWait > 0) {
						Thread.sleep(timeWait);
					}

					saveData();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} finally {
    		this.run.set(false);
		}
	}

	private void saveData() {
		try {
			synchronized (this.updatedReinforcements) {
				if (this.updatedReinforcements.size() == 0) return;

				this.localUpdatedReinforcements.addAll(this.updatedReinforcements);
				this.updatedReinforcements.clear();
			}

			UpdatedReinforcement updated;

			while ((updated = this.localUpdatedReinforcements.poll()) != null) {
				if (updated.deleted) {
					Citadel.getReinforcementManager().deleteReinforcement(updated.rein);
				} else {
					Citadel.getReinforcementManager().saveInitialReinforcement(updated.rein);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
