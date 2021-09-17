package com.devotedmc.ExilePearl.core;

import com.destroystokyo.paper.MaterialTags;
import com.devotedmc.ExilePearl.ExilePearl;
import com.devotedmc.ExilePearl.ExilePearlPlugin;
import com.devotedmc.ExilePearl.LoreProvider;
import com.devotedmc.ExilePearl.PearlType;
import com.devotedmc.ExilePearl.RepairMaterial;
import com.devotedmc.ExilePearl.command.CmdExilePearl;
import com.devotedmc.ExilePearl.config.PearlConfig;
import com.devotedmc.ExilePearl.holder.PearlHolder;
import com.google.common.base.Preconditions;
import com.programmerdan.minecraft.banstick.handler.BanHandler;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import vg.civcraft.mc.civmodcore.util.TextUtil;

/**
 * Implementation class for generating and parsing exile pearl lore
 * 
 * @author Gordon
 */
final class CoreLoreGenerator implements LoreProvider {

	// These need to match!
	private static String PlayerNameStringFormat = "<a>Player: <n>%s <gray>#%s";
	private static String PlayerNameStringFormatRegex = "<a>Player: <n>.+ <gray>#(.+)";

	private final PearlConfig config;
	private final SimpleDateFormat dateFormat;
	private final NamespacedKey exilePearlid;

	public CoreLoreGenerator(PearlConfig config, NamespacedKey exilePearLid) {
		Preconditions.checkNotNull(config, "config");

		this.config = config;
		this.dateFormat = new SimpleDateFormat("MM/dd/yyyy");
		this.exilePearlid = exilePearLid;
	}

	/**
	 * Generates the lore for the pearl
	 * @return The pearl lore
	 */
	public List<String> generateLore(ExilePearl pearl) {
		return generateLoreInternal(pearl, pearl.getHealth(), true);
	}

	@Override
	public List<String> generateLoreWithModifiedHealth(ExilePearl pearl, int healthValue) {
		return generateLoreInternal(pearl, healthValue, true);
	}

	@Override
	public List<String> generateLoreWithModifiedType(ExilePearl pearl, PearlType type) {
		List<String> lore = generateLoreInternal(pearl, pearl.getHealth(), true);
		lore.set(0, parse("<l>%s", type.getTitle()));
		return lore;
	}

	private List<String> generateLoreInternal(ExilePearl pearl, int health, boolean addCommandHelp) {
		List<String> lore = new ArrayList<String>();

		Integer healthPercent = Math.min(100, Math.max(0, (int)Math.round(((double)health / config.getPearlHealthMaxValue()) * 100)));

		lore.add(parse("<l>%s", pearl.getItemName()));
		lore.add(parse(PlayerNameStringFormat, pearl.getPlayerName(), Integer.toString(pearl.getPearlId(), 36).toUpperCase()));
		lore.add(parse("<a>Health: <n>%s%%", healthPercent.toString()));
		lore.add(parse("<a>Exiled on: <n>%s", dateFormat.format(pearl.getPearledOn())));
		lore.add(parse("<a>Killed by: <n>%s", pearl.getKillerName()));
		if (ExilePearlPlugin.getApi().isBanStickEnabled() && BanHandler.isPlayerBanned(pearl.getPlayerId())) {
			lore.add(parse("<b>Player is banned."));
		}
		Set<RepairMaterial> repair = config.getRepairMaterials(pearl.getPearlType());
		if (repair != null) {
			for (RepairMaterial rep : repair) {
				int amountPerItem = rep.getRepairAmount();
				String item = rep.getStack().getType().toString();
				if(rep.getStack().hasItemMeta() && rep.getStack().getItemMeta().hasDisplayName()){
					item = rep.getStack().getItemMeta().getDisplayName();
				}
				int damagesPerHumanInterval = (config.getPearlHealthDecayHumanIntervalMin() / config.getPearlHealthDecayIntervalMin()) * config.getPearlHealthDecayAmount(); // intervals in a human interval * damage per
				int repairsPerHumanInterval = damagesPerHumanInterval / amountPerItem;
				lore.add(parse("<a>Cost per %s using %s:<n> %s", config.getPearlHealthDecayHumanInterval(), item, Integer.toString(repairsPerHumanInterval)));
			}
		}

		if(pearl.getPearlType() == PearlType.EXILE) {
			Set<RepairMaterial> upgrade = config.getUpgradeMaterials();
			if(upgrade != null) {
				for(RepairMaterial up : upgrade) {
					int amount = up.getRepairAmount();
					String item = up.getStack().getType().toString();
					if(up.getStack().hasItemMeta() && up.getStack().getItemMeta().hasDisplayName()){
						item = up.getStack().getItemMeta().getDisplayName();
					}
					lore.add(parse("<a>Upgrade cost:<n> %d %s", amount, item));
				}
			}
		}

		int decayTimeout = config.getPearlHealthDecayTimeout();
		if ( (new Date()).getTime() - pearl.getLastOnline().getTime() >= (decayTimeout * 60 * 1000)) {
			lore.add(parse("<h>Health Decay suspended due to Inactivity"));
		}

		if (addCommandHelp) {
			// Generate some helpful commands
			lore.add(parse(""));
			CmdExilePearl cmd = CmdExilePearl.instance();
			if (cmd != null) {
				lore.add(parse("<l>Commands:"));
				lore.add(parse(CmdExilePearl.instance().cmdFree.getUsageTemplate(true)));
				if(pearl.getPearlType() == PearlType.PRISON) {
					lore.add(parse(CmdExilePearl.instance().cmdDowngrade.getUsageTemplate(true)));
				}
			}
		}
		return lore;
	}

	// For parsing data out of the pearl lore
	private static Pattern pearlIdPattern = Pattern.compile(parse(PlayerNameStringFormatRegex));



	@Override
	public int getPearlIdFromItemStack(ItemStack is) {
		if (is == null || !is.hasItemMeta()) {
			return 0;
		}
		ItemMeta meta = is.getItemMeta();
		if (meta == null) {
			return 0;
		}

		PersistentDataContainer container = meta.getPersistentDataContainer();
		Integer optionalPearlId = container.get(this.exilePearlid, PersistentDataType.INTEGER);
		if (optionalPearlId != null) {
			return optionalPearlId;
		}

		List<String> lore = getValidLore(is, meta);
		if (lore == null) {
			return 0;
		}

		String idLore  = lore.get(1);
		Matcher match = pearlIdPattern.matcher(idLore);
		if (match.find()) {
			String str = match.group(1);
			int id = Integer.parseInt(str, 36);
			return id;
		}

		return 0;
	}

	@Override
	public List<String> generatePearlInfo(ExilePearl pearl) {
		List<String> info = generateLoreInternal(pearl, pearl.getHealth(), false);
		PearlHolder holder = pearl.getHolder();
		Location l = holder.getLocation();
		info.add(parse("<a>Held by: <n>%s [%d %d %d %s]", holder.getName(), l.getBlockX(), l.getBlockY(), l.getBlockZ(), l.getWorld().getName()));
		return info;
	}

	@Override
	public NamespacedKey getExilePearlIdKey() {
		return this.exilePearlid;
	}


	/**
	 * Parses a player ID from a legacy Prison Pearl
	 * The UUID should be on the 2nd line without any formatting
	 */
	@Override
	public UUID getPlayerIdFromLegacyPearl(ItemStack is) {
		List<String> lore = getValidLore(is);
		if (lore == null) {
			return null;
		}

		return UUID.fromString(lore.get(1));
	}

	private List<String> getValidLore(ItemStack is) {
		if (is == null) {
			return null;
		}
		ItemMeta im = is.getItemMeta();
		if (im == null) {
			return null;
		}
		return getValidLore(is, im);
	}

	/**
	 * Gets whether the pearl lore is valid
	 * @param is The item stack to check
	 * @return true if it's valid
	 */
	private List<String> getValidLore(ItemStack is, ItemMeta im) {
		if (!(is.getType().equals(Material.PLAYER_HEAD) || is.getType().equals(Material.ENDER_PEARL))) {
			return null;
		}

		if (!im.hasEnchants() || !im.hasItemFlag(ItemFlag.HIDE_ENCHANTS)) {
			return null;
		}

		List<String> lore = im.getLore();
		if (lore == null || lore.size() < 5) { // technically six but leaving as five for conversion purposes
			return null;
		}
		return lore;
	}

	protected static String parse(String str, Object... args) {
		return TextUtil.parse(str, args);
	}
}
