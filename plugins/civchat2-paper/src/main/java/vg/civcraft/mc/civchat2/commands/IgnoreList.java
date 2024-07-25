package vg.civcraft.mc.civchat2.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Stream;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import vg.civcraft.mc.civchat2.CivChat2;
import vg.civcraft.mc.civchat2.database.CivChatDAO;
import vg.civcraft.mc.namelayer.NameAPI;

public final class IgnoreList extends BaseCommand {

    @CommandAlias("ignorelist")
    @Description("Lists the players and groups you are ignoring")
    private void execute(final @NotNull CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("This command can only be run by players", NamedTextColor.RED));
            return;
        }
        final CivChatDAO db = CivChat2.getInstance().getDatabaseManager();

        // Ignored players
        sender.sendMessage(
            Component.text()
                .content("Ignored players: [")
                .append(
                    db.getIgnoredPlayers(player.getUniqueId())
                        .stream()
                        .map(NameAPI::getCurrentName)
                        .filter(Objects::nonNull)
                        .flatMap(commaSeparatedClickableNames("/ignore %s"))
                        .toList()
                )
                .append(Component.text("]"))
        );

        // Ignored groups
        sender.sendMessage(
            Component.text()
                .content("Ignored groups: [")
                .append(
                    db.getIgnoredGroups(player.getUniqueId())
                        .stream()
                        .flatMap(commaSeparatedClickableNames("/ignoregroup %s"))
                        .toList()
                )
                .append(Component.text("]"))
        );
    }

    private static @NotNull Function<String, Stream<Component>> commaSeparatedClickableNames(
        final @NotNull String formattedCommand
    ) {
        final var hasPreviousName = new AtomicBoolean(false);
        return (name) -> {
            final Component clickableName = Component.text()
                .content(name)
                .color(NamedTextColor.YELLOW)
                .decoration(TextDecoration.UNDERLINED, TextDecoration.State.TRUE)
                .clickEvent(ClickEvent.clickEvent(
                    ClickEvent.Action.COPY_TO_CLIPBOARD,
                    formattedCommand.formatted(name)
                ))
                .hoverEvent(HoverEvent.showText(Component.text("Click to copy un-ignore command")))
                .build();
            if (hasPreviousName.get()) {
                return Stream.of(
                    Component.text(", "),
                    clickableName
                );
            } else {
                hasPreviousName.set(true);
                return Stream.of(clickableName);
            }
        };
    }
}
