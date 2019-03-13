package vg.civcraft.mc.citadel.playerstate;

import static vg.civcraft.mc.citadel.Utility.timeUntilAcidMature;
import static vg.civcraft.mc.citadel.Utility.timeUntilMature;

import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import vg.civcraft.mc.citadel.AcidManager;
import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.Utility;
import vg.civcraft.mc.citadel.reinforcement.Reinforcement;
import vg.civcraft.mc.citadel.reinforcementtypes.ReinforcementType;
import vg.civcraft.mc.civmodcore.util.TextUtil;
import vg.civcraft.mc.namelayer.group.Group;

public class InformationState extends IPlayerState {

	private static final DecimalFormat commaFormat = new DecimalFormat("#.##");
	private static final DecimalFormat roundingFormat = new DecimalFormat("0");

	public InformationState(Player p) {
		super(p);
	}

	@Override
	public void handleBlockPlace(BlockPlaceEvent e) {
	}

	@Override
	public void handleInteractBlock(PlayerInteractEvent e) {
		Reinforcement rein = Citadel.getInstance().getReinforcementManager().getReinforcement(e.getClickedBlock());
		if (rein == null) {
			return;
		}
		Player player = e.getPlayer();
		if (player.getGameMode() == GameMode.CREATIVE) {
			e.setCancelled(true);
		}
		if (!rein.hasPermission(player, "REINFORCEMENT_INFO")) {
			String msg = String.format("Reinforced %s with %s", formatHealth(rein), rein.getType().getName());
			Utility.sendAndLog(player, ChatColor.RED, msg);
			return;
		}
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("Reinforced %s with %s on %s, ", formatHealth(rein), rein.getType().getName(),
				rein.getGroup().getName()));
		if (!rein.isMature()) {
			sb.append(ChatColor.GOLD);
			sb.append(formatProgress(rein.getCreationTime(), rein.getType().getMaturationTime(), "mature"));
			sb.append(" ");
		}
		if (rein.isInsecure()) {
			sb.append(ChatColor.AQUA);
			sb.append("(Insecure)");
		}
		AcidManager acidMan = Citadel.getInstance().getAcidManager();
		if(acidMan.isPossibleAcidBlock(e.getClickedBlock())) {
			sb.append(ChatColor.GOLD);
			long remainingTime = acidMan.getRemainingAcidMaturationTime(rein);
			if (remainingTime == 0) {
				sb.append("Acid ready");
			}
			else {
				sb.append(formatProgress(rein.getCreationTime(), rein.getType().getAcidTime(), "acid timer"));
			}		
		}
		Utility.sendAndLog(player, ChatColor.GREEN, sb.toString());
	}

	private static String formatProgress(long start, long timeNeeded, String text) {
		long timeTaken = System.currentTimeMillis() - start;
		timeTaken = Math.max(timeTaken, timeNeeded);
		double progress = Math.min(1.0, ((double) timeTaken) / ((double) timeNeeded));
		return String.format("%s%% %s %s left", commaFormat.format(progress * 100), text,
				TextUtil.formatDuration(timeNeeded - timeTaken, TimeUnit.MILLISECONDS));
	}

	private static String formatHealth(Reinforcement rein) {
		return String.format("%s%% (%s/%s)", commaFormat.format(rein.getHealth() / rein.getType().getHealth() * 100),
				roundingFormat.format(rein.getHealth()), rein.getType().getHealth());
	}

	@Override
	public void handleBreakBlock(BlockBreakEvent e) {
	}

}
