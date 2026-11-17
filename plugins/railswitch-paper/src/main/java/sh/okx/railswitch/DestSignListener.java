package sh.okx.railswitch;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;
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
    private static final Set<String> DEST_STRINGS; static {
        final Set<String> temp = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        temp.add("[SET DEST]");
        temp.add("[SET DESTINATION]");
        DEST_STRINGS = Collections.unmodifiableSet(temp);
    }

    private static final PlainTextComponentSerializer plainSerializer = PlainTextComponentSerializer.plainText();

    @EventHandler
    public void signClick(PlayerInteractEvent event) {
        if (event.getAction() != Action.LEFT_CLICK_BLOCK) return;

        Block block = event.getClickedBlock();
        if (block == null) return;

        if (!(block.getState() instanceof Sign sign)) return;

        SignSide clickedSide = sign.getTargetSide(event.getPlayer());
        var firstLine = plainSerializer.serialize(clickedSide.line(0)).trim();
        if (!DEST_STRINGS.contains(firstLine)) return;

        List<String> destinations = clickedSide.lines()
            .stream()
            .skip(1L) // Skip the [SET DEST] line
            .map(plainSerializer::serialize)
            .map((line) -> line.replaceFirst("^/dest ", "").trim())
            .filter(Predicate.not(String::isEmpty))
            .toList();
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
