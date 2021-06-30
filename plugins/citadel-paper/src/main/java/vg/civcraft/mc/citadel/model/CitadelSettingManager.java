package vg.civcraft.mc.citadel.model;

import java.text.DecimalFormat;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.CitadelUtility;
import vg.civcraft.mc.citadel.ReinforcementLogic;
import vg.civcraft.mc.citadel.listener.ModeListener;
import vg.civcraft.mc.citadel.reinforcementtypes.ReinforcementType;
import vg.civcraft.mc.civmodcore.players.settings.PlayerSettingAPI;
import vg.civcraft.mc.civmodcore.players.settings.gui.MenuSection;
import vg.civcraft.mc.civmodcore.players.settings.impl.BooleanSetting;
import vg.civcraft.mc.civmodcore.players.settings.impl.BoundedIntegerSetting;
import vg.civcraft.mc.civmodcore.players.settings.impl.CommandReplySetting;
import vg.civcraft.mc.civmodcore.players.settings.impl.DecimalFormatSetting;
import vg.civcraft.mc.civmodcore.players.settings.impl.DisplayLocationSetting;
import vg.civcraft.mc.civmodcore.players.settings.impl.StringSetting;

public class CitadelSettingManager {

	private BooleanSetting byPass;
	private BooleanSetting informationMode;
	private BooleanSetting showChatMsgInCti;
	private BooleanSetting showHologramInCti;
	private BooleanSetting easyMode;
	private BooleanSetting ctoDisableCti;
	private BooleanSetting ctoDisableCtb;

	private BoundedIntegerSetting hologramDuration;

	private CommandReplySetting ctiNotReinforced;
	// private CommandReplySetting ctiAllied;
	private CommandReplySetting ctiEnemy;
	// private CommandReplySetting modeSwitch;
	private DecimalFormatSetting ctiPercentageHealth;
	private DecimalFormatSetting ctiReinforcementHealth;
	private DecimalFormatSetting ctiDecayMultiplier;
	private StringSetting ctiDecay;
	private DisplayLocationSetting ctbLocationSetting;
	private DisplayLocationSetting modeLocationSetting;
	private DisplayLocationSetting ctiLocationSetting;

	public CitadelSettingManager() {
		initSettings();
	}

	public DisplayLocationSetting getBypassLocationSetting() {
		return ctbLocationSetting;
	}
	
	public DisplayLocationSetting getModeLocationSetting() {
		return modeLocationSetting;
	}
	
	public DisplayLocationSetting getInformationLocationSetting() {
		return ctiLocationSetting;
	}
	
	public BooleanSetting getBypass() {
		return byPass;
	}

	public BooleanSetting getInformationMode() {
		return informationMode;
	}

	public BooleanSetting getEasyMode() {
		return easyMode;
	}

	public boolean shouldShowChatInCti(UUID uuid) {
		return showChatMsgInCti.getValue(uuid);
	}
	
	public boolean shouldCtoDisableCti(UUID uuid) {
		return ctoDisableCti.getValue(uuid);
	}
	
	public boolean shouldCtoDisableCtb(UUID uuid) {
		return ctoDisableCtb.getValue(uuid);
	}

	public boolean shouldShowHologramInCti(UUID uuid) {
		return showHologramInCti.getValue(uuid);
	}

	public boolean isInEasyMode(UUID uuid) {
		return easyMode.getValue(uuid);
	}

	public int getHologramDuration(UUID uuid) {
		return hologramDuration.getValue(uuid);
	}

	void initSettings() {
		MenuSection menu = PlayerSettingAPI.getMainMenu().createMenuSection("Citadel",
				"Citadel and reinforcement related settings", new ItemStack(Material.IRON_INGOT));
		byPass = new BooleanSetting(Citadel.getInstance(), true, "Bypass", "citadelBypass",
				"Allows you to bypass reinforcements you have permission for and break them in a single break");
		PlayerSettingAPI.registerSetting(byPass, menu);

		informationMode = new BooleanSetting(Citadel.getInstance(), false, "Information mode", "citadelInformationMode",
				"Displays information about reinforced blocks when interacting with them");
		PlayerSettingAPI.registerSetting(informationMode, menu);
		
		easyMode = new BooleanSetting(Citadel.getInstance(), false, "Easy reinforcing mode", "citadelEasyMode",
				"Allows automatically reinforcing to your default group with reinforcement materials from your off hand");
		PlayerSettingAPI.registerSetting(easyMode, menu);

		showChatMsgInCti = new BooleanSetting(Citadel.getInstance(), true, "Show chat message in information mode",
				"citadelCtiShowChatMsg", "Should chat messages be shown in reinforcement information mode");
		PlayerSettingAPI.registerSetting(showChatMsgInCti, menu);

		showHologramInCti = new BooleanSetting(Citadel.getInstance(), true, "Show holograms in information mode",
				"citadelCtiShowHologram", "Should holograms be shown in reinforcement information mode");
		PlayerSettingAPI.registerSetting(showHologramInCti, menu);

		hologramDuration = new BoundedIntegerSetting(Citadel.getInstance(), 5000, "Hologram visibility duration",
				"citadelHologramCullTimer", new ItemStack(Material.CLOCK),
				"How long should holograms in information mode remain visible, measured in milli seconds", false, 1000,
				30000);
		PlayerSettingAPI.registerSetting(hologramDuration, menu);
		
		ctbLocationSetting = new DisplayLocationSetting(Citadel.getInstance(), DisplayLocationSetting.DisplayLocation.NONE, "Bypass display location"
				, "citadelBypassDisplayLocation", new ItemStack(Material.GOLDEN_PICKAXE), "bypass");
		PlayerSettingAPI.registerSetting(ctbLocationSetting, menu);
		
		ctiLocationSetting = new DisplayLocationSetting(Citadel.getInstance(), DisplayLocationSetting.DisplayLocation.SIDEBAR, "Information mode display location"
				, "citadelInfoModeDisplayLocation", new ItemStack(Material.BOOKSHELF), "reinforcement info mode");
		PlayerSettingAPI.registerSetting(ctiLocationSetting, menu);
		
		modeLocationSetting = new DisplayLocationSetting(Citadel.getInstance(), DisplayLocationSetting.DisplayLocation.SIDEBAR, "Citadel mode display location"
				, "citadelReinModeDisplayLocation", new ItemStack(Material.NETHER_STAR), "Citadel mode");
		PlayerSettingAPI.registerSetting(modeLocationSetting, menu);
		
		ctoDisableCtb =  new BooleanSetting(Citadel.getInstance(), false, "/cto disable /ctb",
				"citadelCtoDisableCtb", "Should /cto disable Bypass mode (/ctb)");
		PlayerSettingAPI.registerSetting(ctoDisableCtb, menu);
		
		ctoDisableCti =  new BooleanSetting(Citadel.getInstance(), true, "/cto disable /cti",
				"citadelCtoDisableCti", "Should /cto disable Information mode (/cti)");
		PlayerSettingAPI.registerSetting(ctoDisableCti, menu);		
		
		MenuSection commandSection = menu.createMenuSection("Command replies",
				"Allows configuring the replies received when interacting with reinforcements or Citadel commands. For advanced users only");

		ctiNotReinforced = new CommandReplySetting(Citadel.getInstance(), ChatColor.YELLOW + "Not reinforced",
				"CTI Message Unreinforced", "citadel_cti_unreinforced", new ItemStack(Material.YELLOW_TERRACOTTA),
				"The message received when interacting with an unreinforced block in Information Mode");
		PlayerSettingAPI.registerSetting(ctiNotReinforced, commandSection);

		ctiPercentageHealth = new DecimalFormatSetting(Citadel.getInstance(), new DecimalFormat("#.##"),
				"Reinforcement Percentage Health Format", "citadel_cti_percentage_health",
				new ItemStack(Material.KNOWLEDGE_BOOK),
				"Decimal format used for displaying a percentage value for reinforcement health", 100.0 / 3);
		PlayerSettingAPI.registerSetting(ctiPercentageHealth, commandSection);

		ctiReinforcementHealth = new DecimalFormatSetting(Citadel.getInstance(), new DecimalFormat("0"),
				"Reinforcement Health Format", "citadel_cti_health", new ItemStack(Material.KNOWLEDGE_BOOK),
				"Decimal format used for displaying a reinforcements health", 100.0 / 3);
		PlayerSettingAPI.registerSetting(ctiReinforcementHealth, commandSection);

		ctiDecayMultiplier = new DecimalFormatSetting(Citadel.getInstance(), new DecimalFormat("#.##"),
				"Reinforcement Decay Multiplier Format", "citadel_cti_decay_multiplier",
				new ItemStack(Material.KNOWLEDGE_BOOK),
				"Decimal format used for displaying the decay multiplier of a reinforcement", 100.0 / 3);
		PlayerSettingAPI.registerSetting(ctiDecayMultiplier, commandSection);

		ctiDecay = new StringSetting(Citadel.getInstance(), " (Decayed x%%decay%%)",
				"Reinforcement Decay Format", "citadel_cti_decay", new ItemStack(Material.KNOWLEDGE_BOOK),
				"String format used for displaying the decay of a reinforcement");
		PlayerSettingAPI.registerSetting(ctiDecay, commandSection);

		ctiEnemy = new CommandReplySetting(Citadel.getInstance(),
				ChatColor.RED + "Reinforced at %%health_color%%%%perc_health%%% (%%health%%/%%max_health%%)"
						+ ChatColor.RED + " health with " + ChatColor.AQUA + "%%type%%" + ChatColor.LIGHT_PURPLE + "%%decay_string%%",
				"CTI Message Enemy", "citadel_cti_enemy", new ItemStack(Material.RED_TERRACOTTA),
				"The message received when interacting with enemy reinforcements");
		ctiEnemy.registerArgument("perc_health", "33.33", "the percentage health of the reinforcement");
		ctiEnemy.registerArgument("max_health", "50", "the maximum health of the reinforcement");
		ctiEnemy.registerArgument("health", "25", "the current health of the reinforcement");
		ctiEnemy.registerArgument("type", "Stone", "the type of the reinforcement");
		ctiEnemy.registerArgument("health_color", ModeListener.getDamageColor(0.5).toString(),
				"a color representing the reinforcement health");
		ctiEnemy.registerArgument("decay_string", "", "the decay of the reinforcement");
		PlayerSettingAPI.registerSetting(ctiEnemy, commandSection);
	}

	public void sendCtiEnemyMessage(Player player, Reinforcement reinforcement) {
		Map<String, String> args = new TreeMap<>();
		ReinforcementType type = reinforcement.getType();
		String percFormat = ctiPercentageHealth.getValue(player)
				.format(reinforcement.getHealth() / type.getHealth() * 100);
		args.put("perc_health", percFormat);
		DecimalFormat reinHealthFormatter = ctiReinforcementHealth.getValue(player);
		args.put("health", reinHealthFormatter.format(reinforcement.getHealth()));
		args.put("max_health", reinHealthFormatter.format(type.getHealth()));
		args.put("type", type.getName());
		args.put("health_color",
				ModeListener.getDamageColor(reinforcement.getHealth() / type.getHealth()).toString());
		if (ReinforcementLogic.getDecayDamage(reinforcement) != 1) {
			String ctiDecayAmountFormat = ctiDecayMultiplier.getValue(player).format(ReinforcementLogic.getDecayDamage(reinforcement));
			String ctiDecayFormat = ctiDecay.getValue(player).replaceAll("%%decay%%", ctiDecayAmountFormat);
			args.put("decay_string", ctiDecayFormat);
		} else {
			args.put("decay_string","");
		}
		CitadelUtility.sendAndLog(player, ChatColor.RESET, ctiEnemy.formatReply(player.getUniqueId(), args));
	}
}
