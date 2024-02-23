package vg.civcraft.mc.civduties.external;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import net.milkbowl.vault.permission.Permission;
import vg.civcraft.mc.civduties.CivDuties;

public class VaultManager {

	private static Permission permissionProvider = null;

	public VaultManager() {
		if (CivDuties.getInstance().isVaultEnabled()) {
			setupPermissions();
		}
	}

	private boolean setupPermissions() {
		RegisteredServiceProvider<Permission> permissionProvider = CivDuties.getInstance().getServer()
				.getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
		if (permissionProvider != null) {
			VaultManager.permissionProvider = permissionProvider.getProvider();
			return true;
		}
		CivDuties.getInstance().getLogger().log(Level.WARNING, "Duties was unable to find a permissions plugin");
		return false;
	}

	public boolean addPermissionsToPlayer(Player player, Map<String, Boolean> permissions) {
		if (permissionProvider == null) {
			return false;
		}
		
		for (Map.Entry<String,Boolean> entry: permissions.entrySet()) {
			if(entry.getValue()){
				permissionProvider.playerAdd(player, entry.getKey());
				return true;
			}
			permissionProvider.playerRemove(player, entry.getKey());
		}
		return true;
	}
	
	public boolean removePermissionsFromPlayer(Player player, Map<String, Boolean> permissions) {
		if (permissionProvider == null) {
			return false;
		}
		
		for (Map.Entry<String,Boolean> entry: permissions.entrySet()) {
			if(!entry.getValue()){
				permissionProvider.playerAdd(player, entry.getKey());
				return true;
			}
			permissionProvider.playerRemove(player, entry.getKey());
		}
		return true;
	}
	
	public boolean addPlayerToGroups(Player player, List<String> groups){
		if (permissionProvider == null) {
			return false;
		}
		
		for (String group : groups) {
			permissionProvider.playerAddGroup(player, group);
		}
		return true;
	}
	
	public boolean removePlayerFromGroups(Player player, List<String> groups){
		if (permissionProvider == null) {
			return false;
		}
		
		for (String group : groups) {
			permissionProvider.playerRemoveGroup(player, group);
		}
		return true;
	}
}
