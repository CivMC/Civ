package vg.civcraft.mc.civmodcore.api;

import java.awt.Color;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import net.md_5.bungee.api.ChatColor;

public class ColourAPI {

	/**
	 * This is necessary as {@link ChatColor#values()} has all colours <i>and</i> all formats.
	 */
	public static final List<ChatColor> COLOURS = Collections.unmodifiableList(Arrays.asList(
			ChatColor.BLACK,
			ChatColor.DARK_BLUE,
			ChatColor.DARK_GREEN,
			ChatColor.DARK_AQUA,
			ChatColor.DARK_RED,
			ChatColor.DARK_PURPLE,
			ChatColor.GOLD,
			ChatColor.GRAY,
			ChatColor.DARK_GRAY,
			ChatColor.BLUE,
			ChatColor.GREEN,
			ChatColor.AQUA,
			ChatColor.RED,
			ChatColor.LIGHT_PURPLE,
			ChatColor.YELLOW
	));

	/**
	 * Converts an RGB value into a Bungee ChatColor.
	 *
	 * @param r The red value.
	 * @param g The green value.
	 * @param b The blue value.
	 * @return Returns a valid Bungee ChatColor.
	 */
	public static ChatColor fromRGB(final byte r, final byte g, final byte b) {
		return ChatColor.of(new Color(r, g, b));
	}

	/**
	 * Converts a Bukkit ChatColor to a Bungee ChatColor.
	 *
	 * @param colour The Bukkit ChatColour to convert.
	 * @return The matching Bungee ChatColour, or null.
	 */
	public static ChatColor fromBukkit(final org.bukkit.ChatColor colour) {
		if (colour == null) {
			return null;
		}
		switch (colour) {
			case BLACK:
				return ChatColor.BLACK;
			case DARK_BLUE:
				return ChatColor.DARK_BLUE;
			case DARK_GREEN:
				return ChatColor.DARK_GREEN;
			case DARK_AQUA:
				return ChatColor.DARK_AQUA;
			case DARK_RED:
				return ChatColor.DARK_RED;
			case DARK_PURPLE:
				return ChatColor.DARK_PURPLE;
			case GOLD:
				return ChatColor.GOLD;
			case GRAY:
				return ChatColor.GRAY;
			case DARK_GRAY:
				return ChatColor.DARK_GRAY;
			case BLUE:
				return ChatColor.BLUE;
			case GREEN:
				return ChatColor.GREEN;
			case AQUA:
				return ChatColor.AQUA;
			case RED:
				return ChatColor.RED;
			case LIGHT_PURPLE:
				return ChatColor.LIGHT_PURPLE;
			case YELLOW:
				return ChatColor.YELLOW;
			case WHITE:
				return ChatColor.WHITE;
			case MAGIC:
				return ChatColor.MAGIC;
			case BOLD:
				return ChatColor.BOLD;
			case STRIKETHROUGH:
				return ChatColor.STRIKETHROUGH;
			case UNDERLINE:
				return ChatColor.UNDERLINE;
			case ITALIC:
				return ChatColor.ITALIC;
			case RESET:
				return ChatColor.RESET;
			default:
				// TODO: Add log out to say the colour hasn't been added
				return ChatColor.RESET;
		}
	}

	/**
	 * Attempts to collapse an RGB colour to established Minecraft colours.
	 *
	 * @param colour The given RGB colour.
	 * @return Returns the closest Minecraft match, or null.
	 */
	public static ChatColor collapseColour(final ChatColor colour) {
		if (colour == null) {
			return null;
		}
		final Color color = colour.getColor();
		ChatColor nearestColour = null;
		double nearestDistance = Double.MAX_VALUE;
		for (final ChatColor currentColour : COLOURS) {
			final Color currentColor = currentColour.getColor();
			final double distance = Math.sqrt(
					Math.pow(color.getRed() - currentColor.getRed(), 2)
					- Math.pow(color.getGreen() - currentColor.getGreen(), 2)
					- Math.pow(color.getBlue() - currentColor.getBlue(), 2));
			if (nearestDistance > distance) {
				nearestDistance = distance;
				nearestColour = currentColour;
			}
		}
		return nearestColour;
	}

}
