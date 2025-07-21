package vg.civcraft.mc.civmodcore.chat;

import java.util.Objects;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.kyori.adventure.translation.Translatable;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ChatUtils {
    // -------------------------------------------- //
    // Color parsing
    // -------------------------------------------- //

    /**
     * @deprecated Please use MiniMessage instead.
     * <a href="https://docs.adventure.kyori.net/minimessage.html">Read More</a>.
     */
    @Deprecated
    public static @NotNull String parseColor(
        @NotNull String string
    ) {
        string = parseColorAmp(string);
        string = parseColorAcc(string);
        string = parseColorTags(string);
        return string;
    }

    /**
     * @deprecated Please use MiniMessage instead.
     * <a href="https://docs.adventure.kyori.net/minimessage.html">Read More</a>.
     */
    @Deprecated
    public static @NotNull String parseColorAmp(
        @NotNull String string
    ) {
        string = string.replace("&&", "&");
        return ChatColor.translateAlternateColorCodes('&', string);
    }

    /**
     * @deprecated Please use MiniMessage instead.
     * <a href="https://docs.adventure.kyori.net/minimessage.html">Read More</a>.
     */
    @Deprecated
    public static @NotNull String parseColorAcc(
        final @NotNull String string
    ) {
        return ChatColor.translateAlternateColorCodes('`', string);
    }

    /**
     * @deprecated Please use MiniMessage instead.
     * <a href="https://docs.adventure.kyori.net/minimessage.html">Read More</a>.
     */
    @Deprecated
    public static @NotNull String parseColorTags(
        final @NotNull String string
    ) {
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

    @Contract("null -> true")
    public static boolean isNullOrEmpty(
        final Component component
    ) {
        return component == null
            || component == Component.empty()
            || StringUtils.isEmpty(PlainTextComponentSerializer.plainText().serialize(component));
    }

    @Contract("!null -> !null")
    public static @Nullable String translate(
        final Translatable translatable
    ) {
        return translatable == null ? null : translate(Component.translatable(translatable));
    }

    @Contract("!null -> !null")
    public static @Nullable String translate(
        final TranslatableComponent component
    ) {
        return component == null ? null : PlainTextComponentSerializer.plainText().serialize(component);
    }

    /**
     * Creates a new component builder that is explicitly non-italic, good for item display components like display
     * names and lore.
     */
    public static @NotNull TextComponent.Builder nonItalic() {
        return Component.text()
            .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE);
    }

    /**
     * Creates a new component builder that is explicitly non-italic, good for item display components like display
     * names and lore.
     */
    public static @NotNull TextComponent.Builder nonItalic(
        final @NotNull String content
    ) {
        return nonItalic().content(content);
    }

    /**
     * Creates a new component builder that is explicitly non-italic, good for item display components like display
     * names and lore.
     */
    public static @NotNull TextComponent.Builder nonItalic(
        final @NotNull String content,
        final @NotNull TextColor color
    ) {
        return nonItalic(content).color(color);
    }

    /**
     * Determines whether two given components are equal to each other.
     *
     * @param former The left hand side component.
     * @param latter The right hand side component.
     * @return Returns whether the two given components are equal.
     */
    @Contract("null, null -> true; null, !null -> false; !null, null -> false")
    public static boolean areComponentsEqual(
        final Component former,
        final Component latter
    ) {
        if (Objects.equals(former, latter)) {
            return true;
        }
        if (former == null || latter == null) {
            return false;
        }
        if (StringUtils.equals(
            MiniMessage.miniMessage().serialize(former),
            MiniMessage.miniMessage().serialize(latter)
        )) {
            return true;
        }
        if (StringUtils.equals(
            LegacyComponentSerializer.legacyAmpersand().serialize(former),
            LegacyComponentSerializer.legacyAmpersand().serialize(latter)
        )) {
            return true;
        }
        return false;
    }
}
