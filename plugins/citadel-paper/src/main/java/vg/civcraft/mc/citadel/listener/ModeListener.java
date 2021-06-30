package vg.civcraft.mc.citadel.listener;

import java.text.DecimalFormat;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.CitadelPermissionHandler;
import vg.civcraft.mc.citadel.CitadelUtility;
import vg.civcraft.mc.citadel.ReinforcementLogic;
import vg.civcraft.mc.citadel.events.ReinforcementModeSwitchEvent;
import vg.civcraft.mc.citadel.model.AcidManager;
import vg.civcraft.mc.citadel.model.CitadelSettingManager;
import vg.civcraft.mc.citadel.model.HologramManager;
import vg.civcraft.mc.citadel.model.Reinforcement;
import vg.civcraft.mc.citadel.playerstate.AbstractPlayerState;
import vg.civcraft.mc.citadel.playerstate.PlayerStateManager;
import vg.civcraft.mc.civmodcore.players.scoreboard.bottom.BottomLine;
import vg.civcraft.mc.civmodcore.players.scoreboard.bottom.BottomLineAPI;
import vg.civcraft.mc.civmodcore.players.scoreboard.side.CivScoreBoard;
import vg.civcraft.mc.civmodcore.players.scoreboard.side.ScoreBoardAPI;
import vg.civcraft.mc.civmodcore.players.settings.PlayerSetting;
import vg.civcraft.mc.civmodcore.players.settings.SettingChangeListener;
import vg.civcraft.mc.civmodcore.players.settings.impl.DisplayLocationSetting;
import vg.civcraft.mc.civmodcore.utilities.DoubleInteractFixer;
import vg.civcraft.mc.civmodcore.utilities.TextUtil;

public class ModeListener implements Listener {

	private static final DecimalFormat commaFormat = new DecimalFormat("#.##");
	private static final DecimalFormat roundingFormat = new DecimalFormat("0");

	private DoubleInteractFixer interactFixer;
	private BottomLine ctiBottomLine;
	private CivScoreBoard ctiBoard;
	private BottomLine ctbBottomLine;
	private CivScoreBoard ctbBoard;
	private BottomLine reinBottomLine;
	private CivScoreBoard reinBoard;
	private CitadelSettingManager settingMan;
	private PlayerStateManager stateMan;

	public ModeListener(Citadel citadel) {
		interactFixer = new DoubleInteractFixer(citadel);
		this.stateMan = citadel.getStateManager();
		this.ctiBottomLine = BottomLineAPI.createBottomLine("ctiDisplay", 3);
		this.ctiBoard = ScoreBoardAPI.createBoard("ctiDisplay");
		this.ctbBottomLine = BottomLineAPI.createBottomLine("ctbDisplay", 3);
		this.ctbBoard = ScoreBoardAPI.createBoard("ctbDisplay");
		this.reinBottomLine = BottomLineAPI.createBottomLine("ctreinDisplay", 3);
		this.reinBoard = ScoreBoardAPI.createBoard("ctreinDisplay");
		this.settingMan = Citadel.getInstance().getSettingManager();
		settingMan.getInformationMode().registerListener(new SettingChangeListener<Boolean>() {
			@Override
			public void handle(UUID player, PlayerSetting<Boolean> setting, Boolean oldValue, Boolean newValue) {
				setCtiOverlay(Bukkit.getPlayer(player), newValue);
			}
		});
		settingMan.getInformationLocationSetting().registerListener(new SettingChangeListener<String>() {
			@Override
			public void handle(UUID player, PlayerSetting<String> setting, String oldValue, String newValue) {
				setCtiOverlay(Bukkit.getPlayer(player), settingMan.getInformationMode().getValue(player));
			}
		});
		settingMan.getBypass().registerListener(new SettingChangeListener<Boolean>() {
			@Override
			public void handle(UUID player, PlayerSetting<Boolean> setting, Boolean oldValue, Boolean newValue) {
				setCtbOverlay(Bukkit.getPlayer(player), newValue);
			}
		});
		settingMan.getBypassLocationSetting().registerListener(new SettingChangeListener<String>() {
			@Override
			public void handle(UUID player, PlayerSetting<String> setting, String oldValue, String newValue) {
				setCtbOverlay(Bukkit.getPlayer(player), settingMan.getBypass().getValue(player));
			}
		});
		settingMan.getModeLocationSetting().registerListener(new SettingChangeListener<String>() {

			@Override
			public void handle(UUID player, PlayerSetting<String> setting, String oldValue, String newValue) {
				setReinModeOverlay(Bukkit.getPlayer(player), stateMan.getState(Bukkit.getPlayer(player)));
			}
		});
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void modeChange(ReinforcementModeSwitchEvent event) {
		setReinModeOverlay(event.getPlayer(), event.getNewMode());
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void playerLogin(PlayerJoinEvent e) {
		setCtiOverlay(e.getPlayer(), settingMan.getInformationMode().getValue(e.getPlayer()));
		setCtbOverlay(e.getPlayer(), settingMan.getBypass().getValue(e.getPlayer()));
		setReinModeOverlay(e.getPlayer(), stateMan.getState(e.getPlayer()));
	}

	private void setCtiOverlay(Player player, boolean state) {
		updateDisplaySetting(player, settingMan.getInformationLocationSetting(), state, ChatColor.GOLD + "CTI",
				ctiBottomLine, ctiBoard);
	}

	private void setCtbOverlay(Player player, boolean state) {
		updateDisplaySetting(player, settingMan.getBypassLocationSetting(), state, ChatColor.AQUA + "CTB",
				ctbBottomLine, ctbBoard);
	}

	private void setReinModeOverlay(Player player, AbstractPlayerState state) {
		if (state == null) {
			return;
		}
		updateDisplaySetting(player, settingMan.getModeLocationSetting(), true, state.getOverlayText(), reinBottomLine,
				reinBoard);
	}

	private static void updateDisplaySetting(Player player, DisplayLocationSetting locSetting, boolean state, String text,
											 BottomLine bottomLine, CivScoreBoard scoreBoard) {
		if (player == null) {
			return;
		}
		if (text == null) {
			state = false;
		}
		if (!state) {
			// always clean up, value might have been changed
			bottomLine.removePlayer(player);
			scoreBoard.hide(player);
		} else {
			if (locSetting.showOnActionbar(player.getUniqueId())) {
				bottomLine.updatePlayer(player, text);
			}
			if (locSetting.showOnSidebar(player.getUniqueId())) {
				scoreBoard.set(player, text);
			}
		}
	}

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
		if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
			if (interactFixer.checkInteracted(e.getPlayer(), e.getClickedBlock())) {
				return;
			}
		} else if (e.getAction() != Action.LEFT_CLICK_BLOCK) {
			return;
		}
		if (!settingMan.getInformationMode().getValue(e.getPlayer())) {
			return;
		}
		Reinforcement rein = ReinforcementLogic.getReinforcementProtecting(e.getClickedBlock());
		Player player = e.getPlayer();
		boolean showChat = settingMan.shouldShowChatInCti(player.getUniqueId());
		if (rein == null) {
			if (showChat) {
				CitadelUtility.sendAndLog(e.getPlayer(), ChatColor.YELLOW, "Not reinforced");
			}
			return;
		}
		if (player.getGameMode() == GameMode.CREATIVE && e.getAction() == Action.LEFT_CLICK_BLOCK) {
			e.setCancelled(true);
		}
		boolean showHolo = settingMan.shouldShowHologramInCti(player.getUniqueId());
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
				sb.append("(Insecure) ");
			}
			if (ReinforcementLogic.getDecayDamage(rein) != 1) {
				String ctiDecayAmountFormat = commaFormat.format(ReinforcementLogic.getDecayDamage(rein));
				sb.append(String.format("%s(Decayed x%s) ", ChatColor.LIGHT_PURPLE, ctiDecayAmountFormat));
			}
			AcidManager acidMan = Citadel.getInstance().getAcidManager();
			if (acidMan.isPossibleAcidBlock(e.getClickedBlock())) {
				sb.append(ChatColor.GOLD);
				long remainingTime = acidMan.getRemainingAcidMaturationTime(rein);
				if (remainingTime == 0) {
					sb.append("Acid ready");
				} else {
					sb.append(String.format("%sAcid block mature in %s", ChatColor.YELLOW, TextUtil.formatDuration(remainingTime, TimeUnit.MILLISECONDS)));
				}
			}
			CitadelUtility.sendAndLog(player, ChatColor.GREEN, sb.toString().trim());
		}
		if (showHolo) {
			showHolo(rein, player);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
	public void onBlockBreak(BlockBreakEvent event) {
		// refresh hologram
		Reinforcement rein = ReinforcementLogic.getReinforcementProtecting(event.getBlock());
		if (rein == null) {
			return;
		}
		Player player = event.getPlayer();
		boolean showHolo = Citadel.getInstance().getSettingManager().shouldShowHologramInCti(player.getUniqueId());
		if (!showHolo) {
			return;
		}
		showHolo(rein, player);
	}

	private static void showHolo(Reinforcement rein, Player player) {
		HologramManager holoManager = Citadel.getInstance().getHologramManager();
		if (holoManager != null) {
			holoManager.showInfoHolo(rein, player);
		}
	}

}
