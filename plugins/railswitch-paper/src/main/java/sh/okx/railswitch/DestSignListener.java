package sh.okx.railswitch;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.SignSide;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import sh.okx.railswitch.settings.SettingsManager;

public class DestSignListener implements Listener {

    private final List<String> destStrings = Arrays.asList("[DEST]",
        "[DESTINATION]");
    private static final PlainTextComponentSerializer plainSerializer = PlainTextComponentSerializer.plainText();

    @EventHandler
    public void signClick(PlayerInteractEvent event) {
        if (event.getAction() != Action.LEFT_CLICK_BLOCK) return;

        Block block = event.getClickedBlock();
        if (block == null) return;

        if (!(block.getState() instanceof Sign sign)) return;

        SignSide clickedSide = sign.getTargetSide(event.getPlayer());
        var firstLine = plainSerializer.serialize(clickedSide.line(0)).trim();
        if (!destStrings.contains(firstLine)) return;

        List<String> destinations = new ArrayList<>();
        for (int i = 1; i < 4; i++) {
            var line = plainSerializer.serialize(clickedSide.line(i));
            // remove "/dest " from start if it exists as some players include it
            line = line.replaceFirst("^/dest ", "").trim();
            if (line.isEmpty()) continue;
            destinations.add(line);
        }
        if (destinations.isEmpty()) return;

        String currentDest = SettingsManager.getDestination(event.getPlayer());
        int currentIndex = -1;
        for (int i = 0; i < destinations.size(); i++) {
            if (destinations.get(i).equalsIgnoreCase(currentDest)) {
                currentIndex = i;
                break;
            }
        }

        int nextIndex = currentIndex + 1;
        if (nextIndex >= destinations.size()) {
            nextIndex = 0;
        }
        SettingsManager.setDestination(event.getPlayer(), destinations.get(nextIndex));
    }
}
