package vg.civcraft.mc.citadel.listener;

import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.CitadelPermissionHandler;
import vg.civcraft.mc.citadel.CitadelUtility;
import vg.civcraft.mc.citadel.ReinforcementLogic;
import vg.civcraft.mc.citadel.model.AcidManager;
import vg.civcraft.mc.citadel.model.HologramManager;
import vg.civcraft.mc.citadel.model.Reinforcement;
import vg.civcraft.mc.civmodcore.util.TextUtil;

public class InformationModeListener implements Listener {

	private static final DecimalFormat commaFormat = new DecimalFormat("#.##");
	private static final DecimalFormat roundingFormat = new DecimalFormat("0");

	public static ChatColor getDamageColor(double relativeHealth) {
		if (relativeHealth >= 1.0) {
			return ChatColor.GREEN;
		} else if (relativeHealth >= 0.75) {
			return ChatColor.DARK_GREEN;
		} else if (relativeHealth >= 0.5) {
			return ChatColor.YELLOW;
		} else if (relativeHealth >= 0.25) {
			return ChatColor.RED;
		} else {
			return ChatColor.DARK_RED;
		}
	}

	public static String formatHealth(Reinforcement rein) {
		double broken = rein.getHealth() / rein.getType().getHealth();
		ChatColor color;
		if (broken >= 1.0) {
			color = ChatColor.GREEN;
		} else if (broken >= 0.75) {
			color = ChatColor.DARK_GREEN;
		} else if (broken >= 0.5) {
			color = ChatColor.YELLOW;
		} else if (broken >= 0.25) {
			color = ChatColor.RED;
		} else {
			color = ChatColor.DARK_RED;
		}
		return String.format("%s%s%% (%s/%s)", color.toString(),
				commaFormat.format(rein.getHealth() / rein.getType().getHealth() * 100),
				roundingFormat.format(rein.getHealth()), roundingFormat.format(rein.getType().getHealth()));
	}

	public static String formatProgress(long start, long timeNeeded, String text) {
		long timeTaken = System.currentTimeMillis() - start;
		timeTaken = Math.min(timeTaken, timeNeeded);
		double progress = Math.min(1.0, ((double) timeTaken) / ((double) timeNeeded));
		return String.format("%s%% %s %s", commaFormat.format(progress * 100), text,
				TextUtil.formatDuration(timeNeeded - timeTaken, TimeUnit.MILLISECONDS));
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void handleInteractBlock(PlayerInteractEvent e) {
		if (e.getAction() != Action.RIGHT_CLICK_BLOCK && e.getAction() != Action.LEFT_CLICK_BLOCK) {
			return;
		}
		if (!Citadel.getInstance().getSettingManager().getInformationMode().getValue(e.getPlayer())) {
			return;
		}
		Reinforcement rein = ReinforcementLogic.getReinforcementProtecting(e.getClickedBlock());
		Player player = e.getPlayer();
		boolean showChat = Citadel.getInstance().getSettingManager().shouldShowChatInCti(player.getUniqueId());
		if (rein == null) {
			if (showChat) {
				CitadelUtility.sendAndLog(e.getPlayer(), ChatColor.YELLOW, "Not reinforced");
			}
			return;
		}
		if (player.getGameMode() == GameMode.CREATIVE) {
			e.setCancelled(true);
		}
		boolean showHolo = Citadel.getInstance().getSettingManager().shouldShowHologramInCti(player.getUniqueId());
		if (!rein.hasPermission(player, CitadelPermissionHandler.getInfo())) {
			if (showChat) {
				Citadel.getInstance().getSettingManager().sendCtiEnemyMessage(player, rein);
			}
			if (showHolo) {
				showHolo(rein, player);
			}
			return;
		}
		if (showChat) {
			StringBuilder sb = new StringBuilder();
			sb.append(String.format("Reinforced at %s%s health with %s%s %son %s%s ", formatHealth(rein),
					ChatColor.GREEN, ChatColor.AQUA, rein.getType().getName(), ChatColor.GREEN, ChatColor.LIGHT_PURPLE,
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
			CitadelUtility.sendAndLog(player, ChatColor.GREEN, sb.toString().trim());
		}
		if (showHolo) {
			showHolo(rein, player);
		}

	}

	private static void showHolo(Reinforcement rein, Player player) {
		HologramManager holoManager = Citadel.getInstance().getHologramManager();
		if (holoManager != null) {
			holoManager.showInfoHolo(rein, player);
		}
	}

}
