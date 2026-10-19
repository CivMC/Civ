package net.civmc.zorweth.mechanics;

import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.CraftingRecipe;

public class SeismicScannerListener implements Listener {

    private final List<CraftingRecipe> recipes;
    private final OilMechanics mechanics;

    public SeismicScannerListener(List<CraftingRecipe> recipes, OilMechanics mechanics) {
        this.recipes = recipes;
        this.mechanics = mechanics;
    }

    @EventHandler
    public void on(PlayerJoinEvent event) {
        for (CraftingRecipe recipe : recipes) {
            event.getPlayer().discoverRecipe(recipe.getKey());
        }
    }

    @EventHandler
    public void on(PlayerInteractEvent event) {
        if (!(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
            return;
        }
        if (!SeismicScanner.isSeismicScanner(event.getItem())) {
            return;
        }
        Player player = event.getPlayer();
        if (player.getCooldown(SeismicScanner.COOLDOWN_GROUP) > 0) {
            return;
        }

        OilMechanics.VeinPing ping = mechanics.ping(player.getLocation());

        HoverEvent<Component> hover = HoverEvent.showText(Component.text("Seismic scanners can detect crude oil in stages of: 200 blocks away, 100 blocks away, and on an oil deposit"));

        player.setCooldown(SeismicScanner.COOLDOWN_GROUP, 20 * 5);

        if (ping == null) {
            player.sendMessage(Component.text("You don't seem to be able to detect any seismic anomalies nearby.", NamedTextColor.GRAY, TextDecoration.ITALIC).hoverEvent(hover));
        } else if (ping == OilMechanics.VeinPing.LOW) {
            player.sendMessage(Component.text("Seismic activity seems to be slightly higher than usual nearby.", NamedTextColor.YELLOW, TextDecoration.ITALIC).hoverEvent(hover));
        } else if (ping == OilMechanics.VeinPing.HIGH) {
            player.sendMessage(Component.text("There are strong indications that oil can be found nearby.", TextColor.color(224, 198, 41), TextDecoration.ITALIC).hoverEvent(hover));
        } else if (ping == OilMechanics.VeinPing.VEIN) {
            player.sendMessage(Component.text("This spot seems ideal for oil extraction.", NamedTextColor.GOLD, TextDecoration.ITALIC).hoverEvent(hover));
        }
    }
}
