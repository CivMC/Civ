package vg.civcraft.mc.civchat2.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Syntax;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import vg.civcraft.mc.civchat2.ChatStrings;
import vg.civcraft.mc.civchat2.CivChat2;

public class Afk extends BaseCommand {

    @CommandAlias("afk")
    @Description("Toggle afk status")
    @Syntax("[message]")
    public void execute(Player player, @Optional String afkMessageRaw) {
        Component afkMessage;
        if (afkMessageRaw == null) {
            afkMessage = MiniMessage.miniMessage().deserialize(ChatStrings.chatPlayerAfk);
        } else {
            afkMessage = Component.text(afkMessageRaw, NamedTextColor.AQUA);
        }

        boolean isAfk = CivChat2.getInstance().getCivChat2Manager().togglePlayerAfk(player, afkMessage);
        if (!isAfk) {
            player.sendRichMessage(ChatStrings.chatNotAfk);
            return;
        }

        player.sendRichMessage(ChatStrings.chatAfk);
    }
}
