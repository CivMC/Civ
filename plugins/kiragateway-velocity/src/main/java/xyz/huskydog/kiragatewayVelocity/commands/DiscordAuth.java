package xyz.huskydog.kiragatewayVelocity.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.minimessage.MiniMessage;
import xyz.huskydog.kiragatewayVelocity.KiragatewayVelocity;
import xyz.huskydog.kiragatewayVelocity.rabbit.RabbitCommands;

public final class DiscordAuth implements SimpleCommand {

    @Override
    public void execute(final Invocation invocation) {
        CommandSource source = invocation.source();
        var mm = MiniMessage.miniMessage();

        if (source instanceof Player player) {

            String code = KiragatewayVelocity.getInstance().getAuthcodeManager().getNewCode();
            if (code == null) {
                player.sendMessage(mm.deserialize("<red>Failed to generate auth code. You should probably tell an admin about this</red>"));
                return;
            }
            // lets make the code upper case to make it easier on people
            code = code.toUpperCase();
            RabbitCommands rabbit = KiragatewayVelocity.getInstance().getRabbit();
            // TODO: handle civ username not just mojang username
            rabbit.sendAuthCode(code, player.getUsername(), player.getUniqueId());
            source.sendMessage(mm.deserialize(String.format(
                "<hover:show_text:'Click to copy the auth code'><click:copy_to_clipboard:'%s'><gold>Your code is '%s'. Execute '/auth %s' in the official discord to authenticate and link your account. Note that upper/lower case does not matter.</gold></click></hover>",
                code, code, code)));
        } else {
            source.sendMessage(mm.deserialize("<red>This command can only be run by a player.</red>"));
        }
    }
}
