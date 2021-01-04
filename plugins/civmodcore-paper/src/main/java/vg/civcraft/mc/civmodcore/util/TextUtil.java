package vg.civcraft.mc.civmodcore.util;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import vg.civcraft.mc.civmodcore.chat.ChatUtils;

public class TextUtil {

	public static String formatDuration(long time) {
		return formatDuration(time, TimeUnit.MILLISECONDS);
	}

	public static String formatDuration(long time, TimeUnit unit) {
		long totalSeconds = TimeUnit.SECONDS.convert(time, unit);
		long seconds = totalSeconds % 60;
		long totalMinutes = totalSeconds / 60;
		long minutes = totalMinutes % 60;
		long totalHours = totalMinutes / 60;
		StringBuilder sb = new StringBuilder();
		if (totalHours > 0) {
			sb.append(totalHours);
			sb.append(" h ");
		}
		if (minutes > 0) {
			sb.append(minutes);
			sb.append(" min ");
		}
		if (seconds > 0) {
			sb.append(seconds);
			sb.append(" sec");
		}
		return sb.toString().trim();
	}

	// -------------------------------------------- //
	// Top-level parsing functions.
	// -------------------------------------------- //

	public static String parse(String str, Object... args) {
		return String.format(parse(str), args);
	}

	public static String parse(String str) {
		return parseColor(str);
	}

	// -------------------------------------------- //
	// Color parsing
	// -------------------------------------------- //

	/**
	 * @deprecated Use {@link ChatUtils#parseColor(String)} instead.
	 */
	@Deprecated
	public static String parseColor(String string) {
		string = parseColorAmp(string);
		string = parseColorAcc(string);
		string = parseColorTags(string);
		return string;
	}

	/**
	 * @deprecated Use {@link ChatUtils#parseColorAmp(String)} instead.
	 */
	@Deprecated
	public static String parseColorAmp(String string) {
		string = string.replace("&&", "&");
		string = string.replaceAll("&([a-zA-Z0-9])", "§$1");
		return string;
	}

	/**
	 * @deprecated Use {@link ChatUtils#parseColorAcc(String)} instead.
	 */
	@Deprecated
	public static String parseColorAcc(String string) {
		return string.
				replace("`0", ChatColor.BLACK.toString()).
				replace("`1", ChatColor.DARK_BLUE.toString()).
				replace("`2", ChatColor.DARK_GREEN.toString()).
				replace("`3", ChatColor.DARK_AQUA.toString()).
				replace("`4", ChatColor.DARK_RED.toString()).
				replace("`5", ChatColor.DARK_PURPLE.toString()).
				replace("`6", ChatColor.GOLD.toString()).
				replace("`7", ChatColor.GRAY.toString()).
				replace("`8", ChatColor.DARK_GRAY.toString()).
				replace("`9", ChatColor.BLUE.toString()).
				replace("`A", ChatColor.GREEN.toString()).
				replace("`a", ChatColor.GREEN.toString()).
				replace("`B", ChatColor.AQUA.toString()).
				replace("`b", ChatColor.AQUA.toString()).
				replace("`C", ChatColor.RED.toString()).
				replace("`c", ChatColor.RED.toString()).
				replace("`D", ChatColor.LIGHT_PURPLE.toString()).
				replace("`d", ChatColor.LIGHT_PURPLE.toString()).
				replace("`E", ChatColor.YELLOW.toString()).
				replace("`e", ChatColor.YELLOW.toString()).
				replace("`F", ChatColor.WHITE.toString()).
				replace("`f", ChatColor.WHITE.toString()).
				replace("`L", ChatColor.BOLD.toString()).
				replace("`l", ChatColor.BOLD.toString()).
				replace("`M", ChatColor.STRIKETHROUGH.toString()).
				replace("`m", ChatColor.STRIKETHROUGH.toString()).
				replace("`N", ChatColor.UNDERLINE.toString()).
				replace("`n", ChatColor.UNDERLINE.toString()).
				replace("`O", ChatColor.ITALIC.toString()).
				replace("`o", ChatColor.ITALIC.toString()).
				replace("`R", ChatColor.RESET.toString()).
				replace("`r", ChatColor.RESET.toString());
	}

	/**
	 * @deprecated Use {@link ChatUtils#parseColorTags(String)} instead.
	 */
	@Deprecated
	public static String parseColorTags(String string) {
		return string.
				replace("<black>", ChatColor.BLACK.toString()).
				replace("<dblue>", ChatColor.DARK_BLUE.toString()).
				replace("<dgreen>", ChatColor.DARK_GREEN.toString()).
				replace("<daqua>", ChatColor.DARK_AQUA.toString()).
				replace("<dred>", ChatColor.DARK_RED.toString()).
				replace("<dpurple>", ChatColor.DARK_PURPLE.toString()).
				replace("<gold>", ChatColor.GOLD.toString()).
				replace("<lgray>", ChatColor.GRAY.toString()). // This has to be lgray because gray is already claimed.
				replace("<dgray>", ChatColor.DARK_GRAY.toString()).
				replace("<blue>", ChatColor.BLUE.toString()).
				replace("<green>", ChatColor.GREEN.toString()).
				replace("<aqua>", ChatColor.AQUA.toString()).
				replace("<red>", ChatColor.RED.toString()).
				replace("<lpurple>", ChatColor.LIGHT_PURPLE.toString()).
				replace("<yellow>", ChatColor.YELLOW.toString()).
				replace("<white>", ChatColor.WHITE.toString()).
				replace("<s>", ChatColor.STRIKETHROUGH.toString()).
				replace("<u>", ChatColor.UNDERLINE.toString()).
				replace("<ul>", ChatColor.UNDERLINE.toString()).
				replace("<r>", ChatColor.RESET.toString()).
				replace("<strike>", ChatColor.STRIKETHROUGH.toString()).
				replace("<italic>", ChatColor.ITALIC.toString()).
				replace("<bold>", ChatColor.BOLD.toString()).
				replace("<reset>", ChatColor.RESET.toString()).
				// Legacy support
				replace("<empty>", ""). // Just... why?
				replace("<navy>", ChatColor.DARK_BLUE.toString()).
				replace("<teal>", ChatColor.DARK_AQUA.toString()).
				replace("<silver>", ChatColor.GRAY.toString()).
				replace("<gray>", ChatColor.DARK_GRAY.toString()). // REEE why name this gray?
				replace("<lime>", ChatColor.GREEN.toString()).
				replace("<rose>", ChatColor.RED.toString()).
				replace("<pink>", ChatColor.LIGHT_PURPLE.toString()).
				replace("<it>", ChatColor.ITALIC.toString()).
				replace("<g>", ChatColor.GREEN.toString()). // Good
				replace("<b>", ChatColor.RED.toString()). // Bad
				replace("<i>", ChatColor.WHITE.toString()). // Info
				replace("<a>", ChatColor.GOLD.toString()). // Art
				replace("<l>", ChatColor.GREEN.toString()). // Logo
				replace("<n>", ChatColor.GRAY.toString()). // Notice
				replace("<h>", ChatColor.LIGHT_PURPLE.toString()). // Highlight
				replace("<c>", ChatColor.AQUA.toString()). // Parameter
				replace("<p>", ChatColor.DARK_AQUA.toString()). // Parameter
				replace("<w>", ChatColor.WHITE.toString()). // Parameter
				replace("<lp>", ChatColor.LIGHT_PURPLE.toString());
	}

	/**
	 * @deprecated Use {@link ChatUtils#parseColorTags(String)} instead.
	 */
	@Deprecated
	public static String parseTags(String sting) {
		return parseColorTags(sting);
	}

	// -------------------------------------------- //
	// Standard utils like UCFirst, implode and repeat.
	// -------------------------------------------- //

	public static String upperCaseFirst(String string) {
		Preconditions.checkArgument(string != null);
		return string.substring(0, 1).toUpperCase() + string.substring(1);
	}

	/**
	 * @deprecated Use {@link StringUtils#repeat(String, int)} instead.
	 */
	@Deprecated
	public static String repeat(String string, int times) {
		Preconditions.checkArgument(string != null);
		if (times <= 0) {
			return "";
		}
		else {
			return string + repeat(string, times - 1);
		}
	}

	/**
	 * @deprecated Use {@link StringUtils#join(Object[], String)} instead.
	 */
	@Deprecated
	public static String implode(List<String> list, String glue) {
		Preconditions.checkArgument(list != null);
		Preconditions.checkArgument(glue != null);
		StringBuilder ret = new StringBuilder();
		for (int i = 0; i < list.size(); i++) {
			if (i != 0) {
				ret.append(glue);
			}
			ret.append(list.get(i));
		}
		return ret.toString();
	}

	public static String implodeCommaAnd(List<String> list, String comma, String and) {
		if (list.size() == 0) {
			return "";
		}
		if (list.size() == 1) {
			return list.get(0);
		}
		String lastItem = list.get(list.size() - 1);
		String nextToLastItem = list.get(list.size() - 2);
		String merge = nextToLastItem + and + lastItem;
		list.set(list.size() - 2, merge);
		list.remove(list.size() - 1);
		return implode(list, comma);
	}

	public static String implodeCommaAnd(List<String> list) {
		return implodeCommaAnd(list, ", ", " and ");
	}

	// -------------------------------------------- //
	// Paging and chrome-tools like titleize
	// -------------------------------------------- //

	private static final String titleizeLine = repeat("_", 52);
	private static final int titleizeBalance = -1;

	public static String titleize(String str) {
		return titleize("<a>", str);
	}

	public static String titleize(String colorCode, String str) {
		String center = ".[ " + parseColorTags("<l>") + str + parseColorTags(colorCode) + " ].";
		int centerlen = ChatColor.stripColor(center).length();
		int pivot = titleizeLine.length() / 2;
		int eatLeft = (centerlen / 2) - titleizeBalance;
		int eatRight = (centerlen - eatLeft) + titleizeBalance;

		if (eatLeft < pivot) {
			return parseColorTags(colorCode) + titleizeLine.substring(0, pivot - eatLeft) + center + titleizeLine.substring(pivot + eatRight);
		}
		else {
			return parseColorTags(colorCode) + center;
		}
	}

	public static ArrayList<String> getPage(List<String> lines, int pageHumanBased, String title) {
		ArrayList<String> ret = new ArrayList<>();
		int pageZeroBased = pageHumanBased - 1;
		int pageheight = 9;
		int pagecount = (lines.size() / pageheight) + 1;

		ret.add(titleize(title + " " + pageHumanBased + "/" + pagecount));

		if (pagecount == 0) {
			ret.add(parseColorTags("<i>Sorry. No Pages available."));
			return ret;
		}
		else if (pageZeroBased < 0 || pageHumanBased > pagecount) {
			ret.add(parseColorTags("<i>Invalid page. Must be between 1 and " + pagecount));
			return ret;
		}

		int from = pageZeroBased * pageheight;
		int to = from + pageheight;
		if (to > lines.size()) {
			to = lines.size();
		}

		ret.addAll(lines.subList(from, to));

		return ret;
	}

	/**
	 * Static utility method for easily sending formatting strings to players.
	 *
	 * @param player The player
	 * @param message The message
	 */
	@Deprecated
	public static void msg(Player player, String message) {
		if (player != null && player.isOnline()) {
			player.sendMessage(parse(message));
		}
	}

	/**
	 * Static utility method for easily sending formatting strings to players.
	 *
	 * @param player The player
	 * @param message The message
	 * @param args Additional arguments which are used later in a String.format()
	 */
	@Deprecated
	public static void msg(Player player, String message, Object... args) {
		if (player != null && player.isOnline()) {
			player.sendMessage(parse(message, args));
		}
	}

	// -------------------------------------------- //
	// Misc
	// -------------------------------------------- //

	/**
	 * Determines if two strings match in value. This has been created to avoid those times when you want to check for
	 * equality without having to do null checks yourself, such as after pulling a value from a map or list.
	 *
	 * @param former The first value.
	 * @param latter The second value.
	 * @return Returns true if the first and second values match.
	 *
	 * @deprecated Use {@link StringUtils#equals(CharSequence, CharSequence)} instead.
	 */
	@Deprecated
	public static boolean stringEquals(String former, String latter) {
		if (former == latter) { // Don't change this to .equals(), this is a null and pointer check
			return true;
		}
		if (former == null || latter == null) {
			return false;
		}
		if (former.equals(latter)) {
			return true;
		}
		return false;
	}

	/**
	 * Determines if two strings match regardless of case. This has been created to avoid those times when you want to
	 * check for equality without having to do null checks yourself, such as after pulling a value from a map or list.
	 *
	 * @param former The first value.
	 * @param latter The second value.
	 * @return Returns true if the first and second values match regardless of case.
	 *
	 * @deprecated Use {@link StringUtils#equalsIgnoreCase(CharSequence, CharSequence)} instead.
	 */
	@Deprecated
	public static boolean stringEqualsIgnoreCase(String former, String latter) {
		if (former == latter) { // Don't change this to .equals(), this is a null and pointer check
			return true;
		}
		if (former == null || latter == null) {
			return false;
		}
		if (former.equalsIgnoreCase(latter)) {
			return true;
		}
		return false;
	}

	/**
	 * Determins if one string starts with another.
	 *
	 * @param container The string to check the start of.
	 * @param contained The string to search for.
	 * @return Returns true if the contained is contained within the container.
	 *
	 * @deprecated Use {@link StringUtils#startsWithIgnoreCase(CharSequence, CharSequence)} instead.
	 */
	@Deprecated
	public static boolean startsWith(String container, String contained) {
		if (contained == null || contained.isEmpty()) {
			return true;
		}
		if (container == null || container.isEmpty()) {
			return false;
		}
		return container.toLowerCase().startsWith(contained.toLowerCase());
	}

	/**
	 * <p>Determines whether a given base component is null or empty.</p>
	 *
	 * <p>This is determined by converting the component into plain text, so a non-null component filled with
	 * nothing but colour codes and hover text will likely return true.</p>
	 *
	 * @param component The component to test if null or empty.
	 * @return Returns true if the component is null or has no visible content.
	 *
	 * @deprecated Use {@link ChatUtils#isNullOrEmpty(BaseComponent)} instead.
	 */
	@Deprecated
	public static boolean isNullOrEmpty(BaseComponent component) {
		if (component == null) {
			return true;
		}
		return Strings.isNullOrEmpty(component.toPlainText());
	}

	/**
	 * This is an easy way to create a text component when all you want to do is colour it.
	 *
	 * @param value The value of the text. (Objects will be stringified)
	 * @param formats The colour formats.
	 * @return Returns the created component, so you <i>can</i> do more stuff to it.
	 *
	 * @deprecated Use {@link ChatUtils#textComponent(Object, net.md_5.bungee.api.ChatColor...)} instead.
	 */
	@Deprecated
	public static TextComponent textComponent(final Object value, final net.md_5.bungee.api.ChatColor... formats) {
		final TextComponent component = new TextComponent(value == null ? "<null>" : value.toString());
		if (!ArrayUtils.isEmpty(formats)) {
			for (final net.md_5.bungee.api.ChatColor format : formats) {
				if (format == null) {
					//continue;
				}
				else if (format.getColor() != null) {
					component.setColor(format);
				}
				else if (format == net.md_5.bungee.api.ChatColor.RESET) {
					component.setColor(format);
					component.setBold(false);
					component.setItalic(false);
					component.setUnderlined(false);
					component.setStrikethrough(false);
					component.setObfuscated(false);
				}
				else if (format == net.md_5.bungee.api.ChatColor.BOLD) {
					component.setBold(true);
				}
				else if (format == net.md_5.bungee.api.ChatColor.ITALIC) {
					component.setItalic(true);
				}
				else if (format == net.md_5.bungee.api.ChatColor.UNDERLINE) {
					component.setUnderlined(true);
				}
				else if (format == net.md_5.bungee.api.ChatColor.STRIKETHROUGH) {
					component.setStrikethrough(true);
				}
				else if (format == net.md_5.bungee.api.ChatColor.MAGIC) {
					component.setObfuscated(true);
				}
			}
		}
		return component;
	}

}
