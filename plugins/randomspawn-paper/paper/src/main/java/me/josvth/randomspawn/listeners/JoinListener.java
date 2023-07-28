package me.josvth.randomspawn.listeners;

import com.programmerdan.minecraft.banstick.data.BSPlayer;
import java.util.List;
import java.util.Set;
import me.josvth.randomspawn.RandomSpawn;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.metadata.FixedMetadataValue;

/**
 * Handle joins. 
 *
 */
public class JoinListener implements Listener{

	RandomSpawn plugin;

	public JoinListener(RandomSpawn instance) {
		plugin = instance;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	@EventHandler
	public void onPlayerTeleport(PlayerTeleportEvent event){
		plugin.sendGround(event.getPlayer(), event.getTo());
	}

	private static boolean isAlt(Player player) {
		BSPlayer bsPlayer = BSPlayer.byUUID(player.getUniqueId());
		if (bsPlayer == null) {
			return false;
		}
		Set<BSPlayer> directAssoc = bsPlayer.getTransitiveSharedPlayers(true);
		return directAssoc.size() > 1; // directAssoc always includes the player herself
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event){

		Player player = event.getPlayer();
		String playerName = player.getName();

		World world = player.getWorld();
		String worldName = world.getName();

		if(world.getEnvironment().equals(Environment.NETHER) || world.getEnvironment().equals(Environment.THE_END)) return;

		if(player.hasPlayedBefore()) return;

		List<String> randomSpawnFlags = plugin.yamlHandler.worlds.getStringList(worldName + ".randomspawnon");
		List<String> spawnPointFlags = plugin.yamlHandler.worlds.getStringList(worldName + ".spawnpointson");

		if (!randomSpawnFlags.contains("firstjoin") && !spawnPointFlags.contains("firstjoin")){
			player.teleport(getFirstSpawn(world));
			plugin.logDebug(playerName + " is teleported to the first spawn of " + worldName);
			return;
		}

		if (player.hasPermission("RandomSpawn.exclude")) { // checks if player should be excluded
			plugin.logDebug(playerName + " is excluded from Random Spawning.");
			return;
		}

		if (spawnPointFlags.contains("firstjoin") && !isAlt(player)) {
			plugin.logDebug(playerName + "First Join spawn point spawning");

			Location newSpawn = plugin.getSpawnSelector().getSpawnPointLocation(world);

			if (newSpawn != null) {
				plugin.sendGround(player, newSpawn);
				player.teleport(newSpawn.add(0, 3, 0));
				player.setMetadata("lasttimerandomspawned", new FixedMetadataValue(plugin, System.currentTimeMillis()));

				if (plugin.yamlHandler.worlds.getBoolean(worldName + ".keeprandomspawns",false)){
					player.setBedSpawnLocation(newSpawn);
				}

				if (plugin.yamlHandler.config.getString("messages.randomspawned") != null){
					player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.yamlHandler.config.getString("messages.randomspawned")));
				}
				return;
			}
		}

		if (randomSpawnFlags.contains("firstjoin")) {
			plugin.logDebug(playerName + " First Join random spawning");
			Location spawnLocation = plugin.getSpawnSelector().getRandomSpawnLocation(world);
			if (spawnLocation == null) {
				plugin.logDebug(playerName + " got unlucky and was not successfully randomspawned. Default behavior will apply");
				return;
			}

			plugin.sendGround(player, spawnLocation);

			player.teleport(spawnLocation.add(0, 3, 0));

			player.setMetadata("lasttimerandomspawned", new FixedMetadataValue(plugin, System.currentTimeMillis()));

			if (plugin.yamlHandler.worlds.getBoolean(worldName + ".keeprandomspawns",false)){
				player.setBedSpawnLocation(spawnLocation);
			}

			if (plugin.yamlHandler.config.getString("messages.randomspawned") != null){
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.yamlHandler.config.getString("messages.randomspawned")));
			}
			return;
		}

		// neither spawnpoints nor randomspawn were successful (or both were disabled)
		player.teleport(getFirstSpawn(world));
		plugin.logDebug(playerName + " is teleported to the first spawn of " + worldName);
	}

	@EventHandler
	public void onPlayerKick(PlayerKickEvent event){
		if(event.getPlayer().hasMetadata("lasttimerandomspawned")){
			if((event.getPlayer().getMetadata("lasttimerandomspawned").get(0).asLong() + (plugin.yamlHandler.config.getInt("nodamagetime",5)*1000)) > System.currentTimeMillis()){
				event.setReason("");
				event.setLeaveMessage("");
				event.setCancelled(true);
			}
		}
	}

	private Location getFirstSpawn(World world) {
		String worldName = world.getName();

		if (plugin.yamlHandler.worlds.contains(worldName +".firstspawn")){

			double x = plugin.yamlHandler.worlds.getDouble(worldName+".firstspawn.x");
			double y = plugin.yamlHandler.worlds.getDouble(worldName+".firstspawn.y");
			double z = plugin.yamlHandler.worlds.getDouble(worldName+".firstspawn.z");

			double dyaw = plugin.yamlHandler.worlds.getDouble(worldName+".firstspawn.yaw");
			double dpitch = plugin.yamlHandler.worlds.getDouble(worldName+".firstspawn.pitch");

			float yaw = (float)dyaw;
			float pitch = (float)dpitch;

			return new Location(world,x,y,z,yaw,pitch);

		}

		return world.getSpawnLocation();
	}

}
