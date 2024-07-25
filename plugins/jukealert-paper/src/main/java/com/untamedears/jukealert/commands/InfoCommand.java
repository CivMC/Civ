package com.untamedears.jukealert.commands;

import static com.untamedears.jukealert.util.JAUtility.findLookingAtOrClosestSnitch;


import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Syntax;
import com.untamedears.jukealert.JukeAlert;
import com.untamedears.jukealert.model.Snitch;
import com.untamedears.jukealert.model.actions.abstr.LoggableAction;
import com.untamedears.jukealert.model.actions.abstr.PlayerAction;
import com.untamedears.jukealert.model.actions.abstr.SnitchAction;
import com.untamedears.jukealert.model.appender.SnitchLogAppender;
import com.untamedears.jukealert.util.JAUtility;
import com.untamedears.jukealert.util.JukeAlertPermissionHandler;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class InfoCommand extends BaseCommand {

    private class FilterOptions {

        public Integer snitchId;
        public Integer pageNumber;
        public String actionType;
        public String playerName;
        public boolean censor;
    }

    private static final Map<UUID, FilterOptions> _history = new ConcurrentHashMap<>();

    @CommandAlias("jainfo")
    @Description("Display information from a snitch")
    @Syntax("[next | ([page_number] [censor] [action=action_type] [player=player_name])]")
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("This command can only be run by players", NamedTextColor.RED));
            return;
        }
        executeCommand(player, new FilterOptions());
    }

    @CommandAlias("jainfo")
    @Description("Display information from a snitch")
    @Syntax("[next | ([page_number] [censor] [action=action_type] [player=player_name])]")
    public void execute(CommandSender sender,
                        String option1,
                        @Optional String option2,
                        @Optional String option3,
                        @Optional String option4) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("This command can only be run by players", NamedTextColor.RED));
            return;
        }
        if ((option2 == null || option2.length() == 0) && option1.equalsIgnoreCase("next")) {
            executeNextCommand(player);
            return;
        }

        var options = new FilterOptions();

        if (parseOption(player, option1, options, true)
            && parseOption(player, option2, options, false)
            && parseOption(player, option3, options, false)
            && parseOption(player, option4, options, false)) {
            executeCommand(player, options);
        }
    }

    private static boolean parseOption(
        Player player,
        String optionText,
        FilterOptions options,
        boolean allowPageNumber) {
        if (optionText == null || optionText.length() == 0) {
            return true;
        }

        if (allowPageNumber && parsePageNumber(optionText, options)) {
            return true;
        }

        String loweredOptionText = optionText.toLowerCase();

        if (loweredOptionText.equals("censor")) {
            if (options.censor) {
                player.sendMessage(Component.text("The option 'censor' is declared more than once", NamedTextColor.RED));
                return false;
            }
            options.censor = true;
        } else if (loweredOptionText.startsWith("action=")) {
            if (options.actionType != null) {
                player.sendMessage(Component.text("The option 'action' is declared more than once", NamedTextColor.RED));
                return false;
            }
            options.actionType = optionText.substring("action=".length());
        } else if (loweredOptionText.startsWith("player=")) {
            if (options.playerName != null) {
                player.sendMessage(Component.text("The option 'player' is declared more than once", NamedTextColor.RED));
                return false;
            }
            options.playerName = optionText.substring("player=".length());
        } else if (loweredOptionText.equals("next")) {
            player.sendMessage(Component.text("The keyword 'next' cannot be used in this context", NamedTextColor.RED));
            return false;
        } else {
            player.sendMessage(Component.text("The unknown option: " + optionText, NamedTextColor.RED));
            return false;
        }

        return true;
    }

    private static boolean parsePageNumber(String optionText, FilterOptions options) {
        try {
            int pageNumber = Integer.parseInt(optionText);
            options.pageNumber = pageNumber;
        } catch (NumberFormatException e) {
            return false;
        }

        return true;
    }

    private void executeNextCommand(Player player) {
        FilterOptions options = _history.get(player.getUniqueId());

        if (options != null) {
            options.pageNumber++;
        } else {
            options = new FilterOptions();
        }

        executeCommand(player, options);
    }

    private void executeCommand(Player player, FilterOptions options) {
        Snitch snitch = findLookingAtOrClosestSnitch(player, JukeAlertPermissionHandler.getReadLogs());
        if (snitch == null) {
            player.sendMessage(Component.text("You do not own any snitches nearby or lack permission to view their logs!",
                NamedTextColor.RED));
            return;
        }
        if (!snitch.hasAppender(SnitchLogAppender.class)) {
            player.sendMessage(Component.text("This " + snitch.getType().getName() + " named " + snitch.getName()
                    + " can not save logs",
                NamedTextColor.RED));
            return;
        }

        if (options.snitchId != null && options.snitchId != snitch.getId()) {
            options = new FilterOptions();
        }

        options.snitchId = snitch.getId();

        if (options.pageNumber == null) {
            options.pageNumber = 0;
        } else if (options.pageNumber < 0) {
            player.sendMessage(Component.text("You cannot input a negative number here").color(NamedTextColor.RED));
            return;
        }

        _history.put(player.getUniqueId(), options);

        int pageLength = JukeAlert.getInstance().getSettingsManager().getJaInfoLength(player.getUniqueId());

        sendSnitchLog(player, snitch, options.pageNumber, pageLength, options.actionType, options.playerName, options.censor);
    }

    public void sendSnitchLog(
        Player player,
        Snitch snitch,
        int offset,
        int pageLength,
        String actionType,
        String playerName,
        boolean censor) {
        SnitchLogAppender logAppender = snitch.getAppender(SnitchLogAppender.class);
        List<LoggableAction> logs = new ArrayList<>(logAppender.getFullLogs());
        if (playerName != null) {
            String playerNameLowered = playerName.toLowerCase();
            List<LoggableAction> logCopy = new LinkedList<>();
            for (LoggableAction log : logs) {
                if (!((SnitchAction) log).hasPlayer()) {
                    continue;
                }
                PlayerAction playerAc = (PlayerAction) log;
                if (playerAc.getPlayerName().toLowerCase().contains(playerNameLowered)) {
                    logCopy.add(log);
                }
            }
            logs = logCopy;
        }
        if (actionType != null) {
            String actionTypeLowered = actionType.toLowerCase();
            List<LoggableAction> logCopy = new LinkedList<>();
            for (LoggableAction log : logs) {
                if (log.getChatRepresentationIdentifier().toLowerCase().contains(actionTypeLowered)) {
                    logCopy.add(log);
                }
            }
            logs = logCopy;
        }
        int initialOffset = pageLength * offset;
        if (initialOffset >= logs.size()) {
            TextComponent reply = JAUtility.genTextComponent(snitch);
            reply.addExtra(ChatColor.GOLD + " has only " + logs.size() + " logs fitting your criteria");
            player.spigot().sendMessage(reply);
            return;
        }
        int currentPageSize = Math.min(pageLength, logs.size() - initialOffset);
        ListIterator<LoggableAction> iter = logs.listIterator(initialOffset);
        int currentSlot = 0;
        TextComponent reply = new TextComponent(ChatColor.GOLD + "--- Page " + offset + " for ");
        reply.addExtra(JAUtility.genTextComponent(snitch));
        player.spigot().sendMessage(reply);
        while (currentSlot++ < currentPageSize) {
            player.spigot().sendMessage(iter.next().getChatRepresentation(player.getLocation(), false, censor));
        }
    }
}
