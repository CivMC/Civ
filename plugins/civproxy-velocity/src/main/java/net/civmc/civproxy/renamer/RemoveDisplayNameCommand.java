package net.civmc.civproxy.renamer;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.ProxyServer;
import net.civmc.nameapi.NameAPI;
import net.kyori.adventure.text.Component;
import java.util.List;
import java.util.UUID;

public class RemoveDisplayNameCommand implements SimpleCommand {
    private final ProxyServer server;
    private final NameAPI nameAPI;
    private final PlayerRenamer playerRenamer;

    public RemoveDisplayNameCommand(ProxyServer server, NameAPI nameAPI, PlayerRenamer playerRenamer) {
        this.server = server;
        this.nameAPI = nameAPI;
        this.playerRenamer = playerRenamer;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        if (args.length < 2) {
            source.sendPlainMessage("Usage: /" + invocation.alias() + " <current player name> <name to remove>");
            return;
        }

        UUID uuid = nameAPI.getUUID(args[0]);
        if (uuid == null) {
            source.sendPlainMessage("Player not found");
            return;
        }

        String nameToRemove = args[1];
        boolean removed = playerRenamer.removeDisplayName(uuid, nameToRemove);
        
        if (removed) {
            source.sendPlainMessage("Removed '" + nameToRemove + "' from " + args[0] + "'s name history");
        } else {
            source.sendPlainMessage("Could not find '" + nameToRemove + "' in " + args[0] + "'s name history");
        }
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("civproxy.removeplayername");
    }

    @Override
    public List<String> suggest(final Invocation invocation) {
        List<String> suggestions = new java.util.ArrayList<>();
        if (invocation.arguments().length > 1) {
            return suggestions;
        }
        server.getAllPlayers().forEach(player -> suggestions.add(player.getUsername()));
        return suggestions;
    }
}