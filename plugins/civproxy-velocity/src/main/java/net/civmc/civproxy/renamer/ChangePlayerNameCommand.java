package net.civmc.civproxy.renamer;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.ProxyServer;
import net.civmc.nameApi.NameAPI;
import net.kyori.adventure.text.Component;
import java.util.List;
import java.util.UUID;

public class ChangePlayerNameCommand implements SimpleCommand {

    private final ProxyServer server;
    private final NameAPI nameAPI;

    public ChangePlayerNameCommand(ProxyServer server, NameAPI nameAPI) {
        this.server = server;
        this.nameAPI = nameAPI;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();
        if (args.length < 2) {
            source.sendPlainMessage("Usage: /" + invocation.alias() + " <old player name> <new player name>");
            return;
        }

        UUID uuid = nameAPI.getUUID(args[0]);
        if (uuid == null) {
            source.sendPlainMessage("Player not found");
            return;
        }
        String newName = args[1].length() >= 16 ? args[1].substring(0, 16) : args[1];
        nameAPI.changePlayer(newName, uuid);
        source.sendPlainMessage("Changed name of " + args[0] + " to " + newName);

        server.getPlayer(uuid).ifPresent(player ->
            player.disconnect(Component.text("Your name has been changed! Please rejoin.")));
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("civproxy.changeplayername");
    }

    @Override
    public List<String> suggest(final Invocation invocation) {
        List<String> suggestions = new java.util.ArrayList<>();
        if (invocation.arguments().length > 1) {
            return suggestions; // No suggestions for the second argument
        }

        // Suggest online player names for the first argument
        server.getAllPlayers().forEach(player -> suggestions.add(player.getUsername()));
        return suggestions;
    }
}
