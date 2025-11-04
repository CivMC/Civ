package vg.civcraft.mc.namelayer.command.commands;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Syntax;
import java.util.List;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentBuilder;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.command.BaseCommandMiddle;
import vg.civcraft.mc.namelayer.group.Group;

public class ListGroups extends BaseCommandMiddle {

    @CommandAlias("nllg|listgroups|groups")
    @Syntax("[page]")
    @Description("List groups.")
    public void execute(CommandSender sender, @Optional String pageNumber) {
        Player p = null;
        UUID uuid = null;
        boolean autopages = false;

        if ((sender.isOp() || sender.hasPermission("namelayer.admin"))) {
            if (pageNumber == null) {
                uuid = NameAPI.getUUID(sender.getName());
            } else if (pageNumber != null) {
                uuid = NameAPI.getUUID(sender.getName());
            }

            if (uuid == null) {
                sender.sendMessage(ChatColor.RED + "UUID is NULL, OP Usage is /nllg <playername>");
                return;
            }
            autopages = true;
        } else {
            p = (Player) sender;
            uuid = NameAPI.getUUID(p.getName());
        }

        List<String> groups = gm.getAllGroupNames(uuid);

        int pages = (groups.size() / 10);
        if (groups.size() % 10 > 0) {
            pages++;
        }
        if (pages == 0) {
            pages = 1;
        }
        int actualPages = pages;

        int target = 1;
        if (pageNumber != null) {
            try {
                target = Integer.parseInt(pageNumber);
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + pageNumber + " is not a number");
                return;
            }
        }

        if (target >= pages) {
            target = pages;
        }

        if (!autopages) {
            pages = target;
        }
        Component holder = Component.empty();
        for (int page = target; page <= pages; page++) {
            if (autopages) {
                holder.append(Component.newline().append(Component.text("Page " + page + " of " + actualPages + ".", NamedTextColor.GREEN)));
            } else {
                holder.append(Component.text("Page " + page + " of " + actualPages + ".", NamedTextColor.GREEN));
            }
            int first = (page - 1) * 10;
            for (int x = first; x < first + 10 && x < groups.size(); x++) {
                Group g = GroupManager.getGroup(groups.get(x));
                Component content = Component.text()
                        .appendNewline()
                        .append(g.getGroupNameColored())
                        .append(Component.text(" : " + g.getPlayerType(uuid), NamedTextColor.GREEN))
                        .build();
                holder.append(content);
            }
        }
        sender.sendMessage(holder);
    }
}
