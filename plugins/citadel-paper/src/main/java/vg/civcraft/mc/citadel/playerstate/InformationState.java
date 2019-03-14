package vg.civcraft.mc.citadel.playerstate;

import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.ReinforcementLogic;
import vg.civcraft.mc.citadel.Utility;
import vg.civcraft.mc.citadel.model.AcidManager;
import vg.civcraft.mc.citadel.model.Reinforcement;
import vg.civcraft.mc.civmodcore.util.TextUtil;

public class InformationState extends AbstractPlayerState {

	private static final DecimalFormat commaFormat = new DecimalFormat("#.##");
	private static final DecimalFormat roundingFormat = new DecimalFormat("0");

	private static String formatHealth(Reinforcement rein) {
		return String.format("%s%% (%s/%s)", commaFormat.format(rein.getHealth() / rein.getType().getHealth() * 100),
				roundingFormat.format(rein.getHealth()), rein.getType().getHealth());
	}

	private static String formatProgress(long start, long timeNeeded, String text) {
		long timeTaken = System.currentTimeMillis() - start;
		timeTaken = Math.max(timeTaken, timeNeeded);
		double progress = Math.min(1.0, ((double) timeTaken) / ((double) timeNeeded));
		return String.format("%s%% %s %s left", commaFormat.format(progress * 100), text,
				TextUtil.formatDuration(timeNeeded - timeTaken, TimeUnit.MILLISECONDS));
	}

	public InformationState(Player p, boolean bypass) {
		super(p, bypass);
	}

	public String getName() {
		return "Information mode";
	}

	@Override
	public void handleBlockPlace(BlockPlaceEvent e) {
	}

	@Override
	public void handleInteractBlock(PlayerInteractEvent e) {
		Reinforcement rein = ReinforcementLogic.getReinforcementProtecting(e.getClickedBlock());
		if (rein == null) {
			return;
		}
		Player player = e.getPlayer();
		if (player.getGameMode() == GameMode.CREATIVE) {
			e.setCancelled(true);
		}
		if (!rein.hasPermission(player, Citadel.infoPerm)) {
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
		if (acidMan.isPossibleAcidBlock(e.getClickedBlock())) {
			sb.append(ChatColor.GOLD);
			long remainingTime = acidMan.getRemainingAcidMaturationTime(rein);
			if (remainingTime == 0) {
				sb.append("Acid ready");
			} else {
				sb.append(formatProgress(rein.getCreationTime(), rein.getType().getAcidTime(), "acid timer"));
			}
		}
		Utility.sendAndLog(player, ChatColor.GREEN, sb.toString());
	}

}
