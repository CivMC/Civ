package vg.civcraft.mc.civmodcore.utilities;

import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.experimental.UtilityClass;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import vg.civcraft.mc.civmodcore.chat.ChatUtils;

@UtilityClass
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
	@Deprecated(forRemoval = true)
	public static String parseColor(String string) {
		return ChatUtils.parseColor(string);
	}

	/**
	 * @deprecated Use {@link ChatUtils#parseColorAmp(String)} instead.
	 */
	@Deprecated(forRemoval = true)
	public static String parseColorAmp(String string) {
		return ChatUtils.parseColorAmp(string);
	}

	/**
	 * @deprecated Use {@link ChatUtils#parseColorAcc(String)} instead.
	 */
	@Deprecated(forRemoval = true)
	public static String parseColorAcc(String string) {
		return ChatUtils.parseColorAcc(string);
	}

	/**
	 * @deprecated Use {@link ChatUtils#parseColorTags(String)} instead.
	 */
	@Deprecated(forRemoval = true)
	public static String parseColorTags(String string) {
		return ChatUtils.parseColorTags(string);
	}

	/**
	 * @deprecated Use {@link ChatUtils#parseColorTags(String)} instead.
	 */
	@Deprecated(forRemoval = true)
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
	@Deprecated(forRemoval = true)
	public static String repeat(String string, int times) {
		Preconditions.checkArgument(string != null);
		return StringUtils.repeat(string, times);
	}

	/**
	 * @deprecated Use {@link StringUtils#join(Object[], String)} instead.
	 */
	@Deprecated(forRemoval = true)
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
	@Deprecated(forRemoval = true)
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
	@Deprecated(forRemoval = true)
	public static boolean stringEquals(String former, String latter) {
		return StringUtils.equals(former, latter);
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
	@Deprecated(forRemoval = true)
	public static boolean stringEqualsIgnoreCase(String former, String latter) {
		return StringUtils.equalsIgnoreCase(former, latter);
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
	@Deprecated(forRemoval = true)
	public static boolean startsWith(String container, String contained) {
		return StringUtils.startsWithIgnoreCase(container, contained);
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
	@Deprecated(forRemoval = true)
	public static boolean isNullOrEmpty(BaseComponent component) {
		return ChatUtils.isNullOrEmpty(component);
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
	@Deprecated(forRemoval = true)
	public static TextComponent textComponent(final Object value, final net.md_5.bungee.api.ChatColor... formats) {
		return ChatUtils.textComponent(value, formats);
	}

}
