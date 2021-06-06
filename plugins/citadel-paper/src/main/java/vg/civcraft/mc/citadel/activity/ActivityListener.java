package vg.civcraft.mc.citadel.activity;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.CitadelPermissionHandler;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.group.Group;

public class ActivityListener implements Listener {

	private final ActivityMap map;
	private final int resolution;

	public ActivityListener(ActivityMap map) {
		this.map = map;
		this.resolution = Citadel.getInstance().getConfigManager().getActivityMapResolution();
	}

	@EventHandler
	public void on(PlayerMoveEvent event) {
		Location from = event.getFrom();
		Location to = event.getTo();
		// IDE says this is never true but it's wrong
		//noinspection ConstantConditions
		if (to == null || from == null || !map.isEnabled(to.getWorld())) {
			return;
		}

		int fsx = from.getBlockX() / resolution;
		int fsz = from.getBlockZ() / resolution;

		int tsx = to.getBlockX() / resolution;
		int tsz = to.getBlockZ() / resolution;

		if (fsx != tsx || fsz != tsz) {
			doUpdate(to, event.getPlayer());
		}
	}

	@EventHandler
	public void on(PlayerJoinEvent event) {
		Location location = event.getPlayer().getLocation();
		if (map.isEnabled(location.getWorld())) {
			doUpdate(location, event.getPlayer());
		}
	}

	private void doUpdate(Location location, Player player) {
		int sx = location.getBlockX() / resolution;
		int sz = location.getBlockZ() / resolution;

		Bukkit.getScheduler().runTaskAsynchronously(Citadel.getInstance(), () -> {
			// Why the fuck is this a database call with no cache
			List<String> groupNames = NameAPI.getGroupManager()
					.getAllGroupNames(player.getUniqueId());
			List<Integer> groupIds = new ArrayList<>();
			for (String groupName : groupNames) {
				Group group = GroupManager.getGroup(groupName);
				if (NameAPI.getGroupManager().hasAccess(group, player.getUniqueId(), CitadelPermissionHandler.getBypass())) {
					groupIds.add(group.getGroupId());
				}
			}

			map.update(location.getWorld(), groupIds, sx, sz);
		});
	}
}
