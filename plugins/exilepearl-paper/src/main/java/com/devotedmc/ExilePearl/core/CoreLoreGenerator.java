package com.devotedmc.ExilePearl.core;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.devotedmc.ExilePearl.ExilePearl;
import com.devotedmc.ExilePearl.PearlLoreGenerator;
import com.devotedmc.ExilePearl.command.CmdExilePearl;
import com.devotedmc.ExilePearl.util.TextUtil;

class CoreLoreGenerator implements PearlLoreGenerator {
	
	// These need to match!
	private static String UidStringFormat = "<a>UUID: <n>%s";
	private static String UidStringFormatRegex = "<a>UUID: <n>(.+)";
	
	// These need to match!
	private static String PlayerNameStringFormat = "<a>Player: <n>%s <gray>#%s";
	private static String PlayerNameStringFormatRegex = "<a>Player: <n>.+ <gray>#(.+)";

	/**
	 * Generates the lore for the pearl
	 * @return The pearl lore
	 */
	public List<String> generateLore(ExilePearl pearl) {
		List<String> lore = new ArrayList<String>();
		lore.add(parse("<l>%s", pearl.getItemName()));
		lore.add(parse(PlayerNameStringFormat, pearl.getPlayerName(), Integer.toString(pearl.getPearlId(), 36).toUpperCase()));
		lore.add(parse("<a>Health: <n>%s%%", pearl.getHealthPercent().toString()));
		lore.add(parse("<a>Imprisoned on: <n>%s", new SimpleDateFormat("yyyy-MM-dd").format(pearl.getPearledOn())));
		lore.add(parse("<a>Killed by: <n>%s", pearl.getKillerName()));
		lore.add(parse(""));
		
		CmdExilePearl cmd = CmdExilePearl.instance();
		if (cmd != null) {
			lore.add(parse("<l>Commands:"));
			lore.add(parse(CmdExilePearl.instance().cmdFree.getUsageTemplate(true)));
		}
		return lore;
	}

	// For parsing the UUID out of the pearl lore
	private static Pattern playerIdPattern = Pattern.compile(parse(UidStringFormatRegex));
	private static Pattern pearlIdPattern = Pattern.compile(parse(PlayerNameStringFormatRegex));

	/**
	 * Gets the UUID from a prison pearl
	 * @param is The item stack
	 * @return The player UUID, or null if it can't parse
	 */
	public UUID getPlayerIdFromItemStack(ItemStack is) {
		if (is == null) {
			return null;
		}

		if (!is.getType().equals(Material.ENDER_PEARL)) {
			return null;
		}

		ItemMeta im = is.getItemMeta();
		if (im == null) {
			return null;
		}

		List<String> lore = im.getLore();
		if (lore == null) {
			return null;
		}

		String idLore  = lore.get(2);
		Matcher match = playerIdPattern.matcher(idLore);
		if (match.find()) {
			UUID id = UUID.fromString(match.group(1));
			return id;
		}

		return null;
	}


	@Override
	public int getPearlIdFromItemStack(ItemStack is) {
		if (is == null) {
			return 0;
		}

		if (!is.getType().equals(Material.ENDER_PEARL)) {
			return 0;
		}

		ItemMeta im = is.getItemMeta();
		if (im == null) {
			return 0;
		}

		List<String> lore = im.getLore();
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


	protected static String parse(String str) {
		return TextUtil.instance().parse(str);
	}

	protected static String parse(String str, Object... args) {
		return String.format(parse(str), args);
	}
}
