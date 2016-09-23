package com.devotedmc.ExilePearl.util;

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
import com.devotedmc.ExilePearl.command.CmdExilePearl;

public class PearlLoreUtil {
	
	// These need to match!
	private static String UidStringFormat = "<a>UUID: <n>%s";
	private static String UidStringFormatRegex = "<a>UUID: <n>(.+)";

	/**
	 * Generates the lore for the pearl
	 * @return The pearl lore
	 */
	public static List<String> generateLore(ExilePearl pearl) {
		List<String> lore = new ArrayList<String>();
		lore.add(parse("<l>%s", pearl.getItemName()));
		lore.add(parse("<a>Player: <n>%s", pearl.getPlayerName()));
		lore.add(parse(UidStringFormat, pearl.getUniqueId().toString()));
		lore.add(parse("<a>Health: <n>%f", pearl.getHealth()));
		lore.add(parse("<a>Imprisoned on: <n>%s", pearl.getKilledByName()));
		lore.add(parse("<a>Killed by: <n>%s", new SimpleDateFormat("yyyy-MM-dd").format(pearl.getPearledOn())));
		lore.add(parse(""));
		lore.add(parse("<l>Commands:"));
		lore.add(parse(CmdExilePearl.instance().cmdFree.getUsageTemplate(true)));
		return lore;
	}

	// For parsing the UUID out of the pearl lore
	private static Pattern idPattern = Pattern.compile(parse(UidStringFormatRegex));

	/**
	 * Gets the UUID from a prison pearl
	 * @param is The item stack
	 * @return The player UUID, or null if it can't parse
	 */
	public static UUID getIDFromItemStack(ItemStack is) {
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
		Matcher match = idPattern.matcher(idLore);
		if (match.find()) {
			UUID id = UUID.fromString(match.group(1));
			return id;
		}

		return null;
	}


	protected static String parse(String str) {
		return TextUtil.instance().parse(str);
	}

	protected static String parse(String str, Object... args) {
		return String.format(parse(str), args);
	}
}
