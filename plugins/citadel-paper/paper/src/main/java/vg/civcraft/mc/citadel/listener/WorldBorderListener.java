package vg.civcraft.mc.citadel.listener;

import java.util.Map;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.CitadelConfigManager;
import vg.civcraft.mc.citadel.events.ReinforcementCreationEvent;

public class WorldBorderListener implements Listener {

	private Map<World, CitadelConfigManager.WorldBorderBuffers> buffers;

	public WorldBorderListener() {
		this.buffers = Citadel.getInstance().getConfigManager().getWorldBorderBuffers();
	}

	@EventHandler
	public void onReinCreation (ReinforcementCreationEvent event) {
		World world = event.getReinforcement().getLocation().getWorld();
		if (!buffers.containsKey(world)) {
			return;
		}
		CitadelConfigManager.WorldBorderBuffers buffer = buffers.get(world);
		Location reinforcementLocation = event.getReinforcement().getLocation();
		double xCenter = (double) buffer.getBorderCenter().get("x");
		double zCenter = (double) buffer.getBorderCenter().get("z");
		double xBorder = (double) buffer.getBorderCenter().get("x") + buffer.getBufferSize();
		double zBorder = (double) buffer.getBorderCenter().get("z") + buffer.getBufferSize();
		switch (buffer.getBorderShape()) {
			case "square":
				if (Math.abs(reinforcementLocation.getBlockX()) > xBorder || Math.abs(reinforcementLocation.getBlockZ()) > zBorder) {
					event.getPlayer().sendMessage(Component.text("You cannot reinforce this close to the border!").color(
							NamedTextColor.RED));
					event.setCancelled(true);
					return;
				}
			case "circle":
				if (checkIfOutsideCircle(reinforcementLocation.getX(), xCenter, reinforcementLocation.getZ(), zCenter, buffer.getBufferSize())) {
					event.getPlayer().sendMessage(Component.text("You cannot reinforce this close to the border!").color(
							NamedTextColor.RED));
					event.setCancelled(true);
				}
		}
	}

	private boolean checkIfOutsideCircle(double x, double centerX, double z, double centerZ, double radius) {
		return (x - centerX) * (x - centerX) + (z - centerZ) * (z - centerZ) > radius * radius;
	}
}
