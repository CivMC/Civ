package vg.civcraft.mc.civmodcore.chat;

import com.google.common.base.Strings;
import java.awt.Color;
import java.util.List;
import java.util.Objects;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainComponentSerializer;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

public final class ChatUtils {

	/**
	 * This is necessary as {@link ChatColor#values()} has all colours <i>and</i> all formats.
	 */
	@SuppressWarnings("deprecation")
	public static final List<ChatColor> COLOURS = List.of(
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
			ChatColor.YELLOW);

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

	// -------------------------------------------- //
	// Color parsing
	// -------------------------------------------- //

	public static String parseColor(String string) {
		string = parseColorAmp(string);
		string = parseColorAcc(string);
		string = parseColorTags(string);
		return string;
	}

	public static String parseColorAmp(String string) {
		string = string.replace("&&", "&");
		return ChatColor.translateAlternateColorCodes('&', string);
	}

	public static String parseColorAcc(String string) {
		return ChatColor.translateAlternateColorCodes('`', string);
	}

	public static String parseColorTags(String string) {
		return string
				.replace("<black>", ChatColor.BLACK.toString())
				.replace("<dblue>", ChatColor.DARK_BLUE.toString())
				.replace("<dgreen>", ChatColor.DARK_GREEN.toString())
				.replace("<daqua>", ChatColor.DARK_AQUA.toString())
				.replace("<dred>", ChatColor.DARK_RED.toString())
				.replace("<dpurple>", ChatColor.DARK_PURPLE.toString())
				.replace("<gold>", ChatColor.GOLD.toString())
				.replace("<lgray>", ChatColor.GRAY.toString()) // This has to be lgray because gray is already claimed.
				.replace("<dgray>", ChatColor.DARK_GRAY.toString())
				.replace("<blue>", ChatColor.BLUE.toString())
				.replace("<green>", ChatColor.GREEN.toString())
				.replace("<aqua>", ChatColor.AQUA.toString())
				.replace("<red>", ChatColor.RED.toString())
				.replace("<lpurple>", ChatColor.LIGHT_PURPLE.toString())
				.replace("<yellow>", ChatColor.YELLOW.toString())
				.replace("<white>", ChatColor.WHITE.toString())
				.replace("<s>", ChatColor.STRIKETHROUGH.toString())
				.replace("<u>", ChatColor.UNDERLINE.toString())
				.replace("<ul>", ChatColor.UNDERLINE.toString())
				.replace("<r>", ChatColor.RESET.toString())
				.replace("<strike>", ChatColor.STRIKETHROUGH.toString())
				.replace("<italic>", ChatColor.ITALIC.toString())
				.replace("<bold>", ChatColor.BOLD.toString())
				.replace("<reset>", ChatColor.RESET.toString())
				// Legacy support
				.replace("<empty>", "") // Just... why?
				.replace("<navy>", ChatColor.DARK_BLUE.toString())
				.replace("<teal>", ChatColor.DARK_AQUA.toString())
				.replace("<silver>", ChatColor.GRAY.toString())
				.replace("<gray>", ChatColor.DARK_GRAY.toString()) // REEE why name this gray?
				.replace("<lime>", ChatColor.GREEN.toString())
				.replace("<rose>", ChatColor.RED.toString())
				.replace("<pink>", ChatColor.LIGHT_PURPLE.toString())
				.replace("<it>", ChatColor.ITALIC.toString())
				.replace("<g>", ChatColor.GREEN.toString()) // Good
				.replace("<b>", ChatColor.RED.toString()) // Bad
				.replace("<i>", ChatColor.WHITE.toString()) // Info
				.replace("<a>", ChatColor.GOLD.toString()) // Art
				.replace("<l>", ChatColor.GREEN.toString()) // Logo
				.replace("<n>", ChatColor.GRAY.toString()) // Notice
				.replace("<h>", ChatColor.LIGHT_PURPLE.toString()) // Highlight
				.replace("<c>", ChatColor.AQUA.toString()) // Parameter
				.replace("<p>", ChatColor.DARK_AQUA.toString()) // Parameter
				.replace("<w>", ChatColor.WHITE.toString()) // Parameter
				.replace("<lp>", ChatColor.LIGHT_PURPLE.toString());
	}

	// -------------------------------------------- //
	// Component Stuff
	// -------------------------------------------- //

	/**
	 * <p>Determines whether a given base component is null or empty.</p>
	 *
	 * <p>This is determined by converting the component into plain text, so a non-null component filled with
	 * nothing but colour codes and hover text will likely return true.</p>
	 *
	 * @param component The component to test if null or empty.
	 * @return Returns true if the component is null or has no visible content.
	 * 
	 * @deprecated Has been deprecated due to Paper's move to Kyori's Adventure.
	 */
	@Deprecated(forRemoval = true)
	public static boolean isNullOrEmpty(final BaseComponent component) {
		if (component == null) {
			return true;
		}
		return Strings.isNullOrEmpty(component.toPlainText());
	}

	/**
	 * <p>Determines whether a given base component is null or empty.</p>
	 *
	 * <p>This is determined by converting the component into plain text, so a non-null component filled with
	 * nothing but colour codes and hover text will likely return true.</p>
	 *
	 * @param component The component to test if null or empty.
	 * @return Returns true if the component is null or has no visible content.
	 */
	public static boolean isNullOrEmpty(final Component component) {
		if (component == null) {
			return true;
		}
		return StringUtils.isBlank(PlainComponentSerializer.plain().serialize(component));
	}

	/**
	 * Determines whether a given component is a {@code {"text":"","extra":[...]}} component.
	 *
	 * @param component The component to test.
	 * @return Returns whether the given component is a base / container component.
	 */
	public static boolean isBaseComponent(final Component component) {
		if (component == null) {
			return false;
		}
		final var content = component instanceof net.kyori.adventure.text.TextComponent ?
				((net.kyori.adventure.text.TextComponent) component).content() : null;
		return StringUtils.isEmpty(content)
				&& !component.children().isEmpty()
				&& component.clickEvent() == null
				&& component.hoverEvent() == null
				&& !component.hasStyling();
	}

	/**
	 * @return Generates a new text component that's specifically <i>NOT</i> italicised. Use this for item names and
	 *         lore.
	 */
	public static net.kyori.adventure.text.TextComponent newComponent() {
		return newComponent("");
	}

	/**
	 * Generates a new text component that's specifically <i>NOT</i> italicised. Use this for item names and lore.
	 *
	 * @param content The text content for the component.
	 * @return Returns the generated text component.
	 */
	public static net.kyori.adventure.text.TextComponent newComponent(final String content) {
		return Component.text(Objects.requireNonNull(content))
				.decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE);
	}

	/**
	 * Clones a component.
	 *
	 * @param component The component to clone.
	 * @return Returns a clone of the given component.
	 */
	public static Component cloneComponent(final Component component) {
		if (component == null) {
			return null;
		}
		final var raw = GsonComponentSerializer.gson().serialize(component);
		return GsonComponentSerializer.gson().deserialize(raw);
	}

	/**
	 * This is an easy way to create a text component when all you want to do is colour it.
	 *
	 * @param value The value of the text. (Objects will be stringified)
	 * @param formats The colour formats.
	 * @return Returns the created component, so you <i>can</i> do more stuff to it.
	 *
	 * @deprecated Has been deprecated due to Paper's move to Kyori's Adventure. Use {@link Component#text()} instead.
	 */
	@Deprecated(forRemoval = true)
	public static TextComponent textComponent(final Object value, final ChatColor... formats) {
		final TextComponent component = new TextComponent(value == null ? "<null>" : value.toString());
		if (!ArrayUtils.isEmpty(formats)) {
			for (final ChatColor format : formats) {
				if (format == null) {
					//continue;
				}
				else if (format.getColor() != null) {
					component.setColor(format);
				}
				else if (format == ChatColor.RESET) {
					component.setColor(format);
					component.setBold(false);
					component.setItalic(false);
					component.setUnderlined(false);
					component.setStrikethrough(false);
					component.setObfuscated(false);
				}
				else if (format == ChatColor.BOLD) {
					component.setBold(true);
				}
				else if (format == ChatColor.ITALIC) {
					component.setItalic(true);
				}
				else if (format == ChatColor.UNDERLINE) {
					component.setUnderlined(true);
				}
				else if (format == ChatColor.STRIKETHROUGH) {
					component.setStrikethrough(true);
				}
				else if (format == ChatColor.MAGIC) {
					component.setObfuscated(true);
				}
			}
		}
		return component;
	}

	/**
	 * Determines whether two given components are equal to each other.
	 *
	 * @param former The left hand side component.
	 * @param latter The right hand side component.
	 * @return Returns whether the two given components are equal.
	 */
	public static boolean areComponentsEqual(final Component former, final Component latter) {
		if (Objects.equals(former, latter)) {
			return true;
		}
		if (former == null || latter == null) {
			return false;
		}
		if (StringUtils.equals(
				MiniMessage.get().serialize(former),
				MiniMessage.get().serialize(latter))) {
			return true;
		}
		if (StringUtils.equals(
				LegacyComponentSerializer.legacyAmpersand().serialize(former),
				LegacyComponentSerializer.legacyAmpersand().serialize(latter))) {
			return true;
		}
		return false;
	}

}
