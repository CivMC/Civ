package com.programmerdan.minecraft.banstick.commands;

import com.programmerdan.minecraft.banstick.BanStick;
import com.programmerdan.minecraft.banstick.containers.BanResult;
import com.programmerdan.minecraft.banstick.handler.BanHandler;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.input.TextDialogInput;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import vg.civcraft.mc.civmodcore.dialog.DialogManager;
import vg.civcraft.mc.namelayer.NameLayerAPI;

@SuppressWarnings("UnstableApiUsage")
public final class BanPlayerCommand implements CommandExecutor {

    public record Rule(@NotNull String rule, @NotNull String hint) {

        public @NotNull Component label() {
            final Component label = Component.text(rule);
            return hint.isBlank() ? label
                : label.append(Component.newline()).append(MiniMessage.miniMessage().deserialize(hint));
        }
    }

    public static @NotNull List<Rule> loadRules(final @NotNull ConfigurationSection config) {
        final List<Rule> rules = new ArrayList<>();
        for (final Map<?, ?> entry : config.getMapList("banplayer.rules")) {
            if (!(entry.get("rule") instanceof final String rule) || rule.isBlank()) {
                throw new IllegalArgumentException("Each banplayer.rules entry must have a non-empty rule.");
            }
            final String hint = entry.get("hint") instanceof final String text ? text : "";
            rules.add(new Rule(rule, hint));
        }
        return List.copyOf(rules);
    }

    @Override
    public boolean onCommand(final @NotNull CommandSender sender, final @NotNull Command command,
                             final @NotNull String label, final @NotNull String[] arguments) {
        if (!(sender instanceof final Player moderator)) {
            sender.sendMessage(Component.text("This command must be run by a player.", NamedTextColor.RED));
            return true;
        }
        if (!moderator.hasPermission("banstick.ban")) {
            return true;
        }
        if (arguments.length != 1) {
            return false;
        }
        final String target = arguments[0];
        final UUID targetId = resolvePlayer(target);
        if (targetId == null) {
            moderator.sendMessage(Component.text("Unable to find player " + target, NamedTextColor.RED));
            return true;
        }
        final List<Rule> rules = loadRules(BanStick.getPlugin().getConfig());
        showDialog(moderator, targetId, target, rules, "", "", List.of(), null);
        return true;
    }

    private static @Nullable UUID resolvePlayer(final @NotNull String target) {
        if (target.length() == 36) {
            try {
                return UUID.fromString(target);
            } catch (final IllegalArgumentException exception) {
                return null;
            }
        }
        final Player online = Bukkit.getPlayerExact(target);
        if (online != null) {
            return online.getUniqueId();
        }
        if (Bukkit.getPluginManager().isPluginEnabled("NameLayer")) {
            return NameLayerAPI.getUUID(target);
        }
        return null;
    }

    private static void showDialog(final @NotNull Player moderator, final @NotNull UUID targetId,
                                   final @NotNull String target, final @NotNull List<Rule> rules,
                                   final @NotNull String expiration, final @NotNull String note,
                                   final @NotNull List<Integer> selected, final @Nullable String error) {
        final List<DialogBody> body = new ArrayList<>();
        if (error != null) {
            body.add(DialogBody.plainMessage(Component.text(error, NamedTextColor.RED)));
        }
        final List<DialogInput> inputs = new ArrayList<>();
        inputs.add(DialogInput.text("expiration", Component.text("Expiration (YYYY-MM-DD, leave empty for permanent)"))
            .initial(expiration).maxLength(10).width(300).build());
        inputs.add(DialogInput.text("note", Component.text("Message (optional)"))
            .initial(note).maxLength(2000).width(300)
            .multiline(TextDialogInput.MultilineOptions.create(null, 80)).build());
        for (int index = 0; index < rules.size(); index++) {
            inputs.add(DialogInput.bool("rule_" + index, rules.get(index).label())
                .initial(selected.contains(index)).build());
        }
        DialogManager.showDialog(moderator, Key.key("banstick", "banplayer"),
            Component.text("Ban " + target), body, inputs, view -> {
                if (!moderator.hasPermission("banstick.ban")) {
                    moderator.sendMessage(Component.text("You no longer have permission to ban players.", NamedTextColor.RED));
                    return;
                }
                final String submittedExpiration = Objects.requireNonNullElse(view.getText("expiration"), "").trim();
                final String submittedNote = Objects.requireNonNullElse(view.getText("note"), "");
                final List<Integer> submittedRules = new ArrayList<>();
                for (int index = 0; index < rules.size(); index++) {
                    if (Boolean.TRUE.equals(view.getBoolean("rule_" + index))) {
                        submittedRules.add(index);
                    }
                }
                final Date banEnd;
                try {
                    banEnd = parseExpiration(submittedExpiration);
                } catch (final IllegalArgumentException exception) {
                    showDialog(moderator, targetId, target, rules, submittedExpiration, submittedNote,
                        submittedRules, exception.getMessage());
                    return;
                }
                if (submittedRules.isEmpty()) {
                    showDialog(moderator, targetId, target, rules, submittedExpiration, submittedNote,
                        submittedRules, "Select at least one rule.");
                    return;
                }
                final List<String> reasons = new ArrayList<>();
                for (final int index : submittedRules) {
                    reasons.add(rules.get(index).rule());
                }
                final String message = "Rules violated: " + String.join("; ", reasons)
                    + (submittedNote.isBlank() ? "" : "\n" + submittedNote.trim());
                Bukkit.getScheduler().runTaskAsynchronously(BanStick.getPlugin(), () -> {
                    final BanResult result = BanHandler.doUUIDBan(targetId, message, banEnd, true);
                    Bukkit.getScheduler().runTask(BanStick.getPlugin(), () -> result.informCommandSender(moderator));
                });
            });
    }

    public static @Nullable Date parseExpiration(final @NotNull String expiration) {
        if (expiration.isEmpty()) {
            return null;
        }
        try {
            if (!expiration.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}")) {
                throw new IllegalArgumentException("Enter a valid expiration date in YYYY-mm-dd format.");
            }
            final Date end = Date.from(LocalDate.parse(expiration, DateTimeFormatter.ISO_LOCAL_DATE)
                .atStartOfDay(ZoneId.systemDefault()).toInstant());
            if (!end.after(new Date())) {
                throw new IllegalArgumentException("Expiration must be in the future.");
            }
            return end;
        } catch (final DateTimeParseException exception) {
            throw new IllegalArgumentException("Enter a valid expiration date in YYYY-mm-dd format.", exception);
        }
    }
}
