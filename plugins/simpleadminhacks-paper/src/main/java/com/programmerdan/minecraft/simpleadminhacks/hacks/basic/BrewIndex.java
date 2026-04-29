package com.programmerdan.minecraft.simpleadminhacks.hacks.basic;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import com.destroystokyo.paper.profile.PlayerProfile;
import com.dre.brewery.Brew;
import com.dre.brewery.api.events.brew.BrewDrinkEvent;
import com.dre.brewery.recipe.BRecipe;
import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHack;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHackConfig;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import vg.civcraft.mc.civmodcore.commands.CommandManager;
import vg.civcraft.mc.civmodcore.inventory.gui.Clickable;
import vg.civcraft.mc.civmodcore.inventory.gui.ClickableInventory;
import vg.civcraft.mc.civmodcore.inventory.gui.DecorationStack;
import vg.civcraft.mc.civmodcore.inventory.gui.IClickable;
import vg.civcraft.mc.civmodcore.inventory.gui.MultiPageView;
import vg.civcraft.mc.civmodcore.players.settings.PlayerSettingAPI;
import vg.civcraft.mc.civmodcore.players.settings.impl.IntegerSetting;
import vg.civcraft.mc.namelayer.NameLayerAPI;

public final class BrewIndex extends BasicHack {

    private static final String PDC_SEPARATOR = ";";
    private static final String UNIQUE_BREWS_DRUNK_SETTING = "uniqueBrewsDrunk";
    private static final int[] TOP_BREW_SLOTS = {11, 12, 13, 14, 15, 20, 21, 22, 23, 24};

    private final NamespacedKey drunkBrewsKey;
    private final IntegerSetting uniqueBrewsDrunk;
    private final CommandManager commands;
    private boolean breweryEnabled;

    private PlayerProfile profile;
    private volatile boolean profileLoaded = false;

    public BrewIndex(final SimpleAdminHacks plugin, final BasicHackConfig config) {
        super(plugin, config);
        this.drunkBrewsKey = new NamespacedKey(plugin, "drunk_brews");
        this.uniqueBrewsDrunk = new IntegerSetting(plugin, 0, "Unique brews drunk", UNIQUE_BREWS_DRUNK_SETTING);
        this.commands = new CommandManager(plugin()) {
            @Override
            public void registerCommands() {
                registerCommand(new BrewIndexCommand());
            }
        };
    }

    @Override
    public void onEnable() {
        super.onEnable();
        this.breweryEnabled = Bukkit.getPluginManager().isPluginEnabled("BreweryX");
        if (!this.breweryEnabled) {
            plugin().warning("BrewIndex: BreweryX not found, /brews command will be unavailable");
        }
        if (PlayerSettingAPI.getSetting(UNIQUE_BREWS_DRUNK_SETTING) == null) {
            PlayerSettingAPI.registerSetting(this.uniqueBrewsDrunk, null);
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            this.profile = Bukkit.getServer().createProfile(UUID.fromString("82569b12-c44c-4864-8a73-85a9192ee8f9"), "RedDevel");
            this.profile.complete();
            this.profileLoaded = true;
        });
        this.commands.init();
    }

    @Override
    public void onDisable() {
        this.commands.reset();
        super.onDisable();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBrewDrink(final BrewDrinkEvent event) {
        final Brew brew = event.getBrew();
        final BRecipe recipe = brew.getCurrentRecipe();
        if (recipe == null) {
            return;
        }
        final String recipeName = recipe.getRecipeName();
        if (recipeName == null || recipeName.isEmpty()) {
            return;
        }
        recordDrunkBrew(event.getPlayer(), recipeName, event.getQuality());
        syncUniqueBrewCount(event.getPlayer());
    }

    /**
     * Reads the player's PDC to get a map of recipe name -> best quality drunk.
     * Format stored in PDC: "name:quality;name:quality;..."
     */
    private Map<String, Integer> getDrunkBrews(final Player player) {
        final PersistentDataContainer pdc = player.getPersistentDataContainer();
        final String raw = pdc.get(this.drunkBrewsKey, PersistentDataType.STRING);
        final Map<String, Integer> brews = new HashMap<>();
        if (raw != null && !raw.isEmpty()) {
            for (final String entry : raw.split(PDC_SEPARATOR, -1)) {
                if (entry.isEmpty()) {
                    continue;
                }
                final int colonIndex = entry.lastIndexOf(':');
                final String name = entry.substring(0, colonIndex);
                try {
                    final int quality = Integer.parseInt(entry.substring(colonIndex + 1));
                    brews.put(name, quality);
                } catch (final NumberFormatException ignored) {
                    brews.put(entry, 0);
                }
            }
        }
        return brews;
    }

    private void saveDrunkBrews(final Player player, final Map<String, Integer> brews) {
        final PersistentDataContainer pdc = player.getPersistentDataContainer();
        final StringBuilder builder = new StringBuilder();
        boolean first = true;
        for (final Map.Entry<String, Integer> entry : brews.entrySet()) {
            if (!first) {
                builder.append(PDC_SEPARATOR);
            }
            builder.append(entry.getKey()).append(':').append(entry.getValue());
            first = false;
        }
        pdc.set(this.drunkBrewsKey, PersistentDataType.STRING, builder.toString());
    }

    private void recordDrunkBrew(final Player player, final String recipeName, final int quality) {
        final Map<String, Integer> brews = getDrunkBrews(player);
        final Integer existing = brews.get(recipeName);
        if (existing == null || quality > existing) {
            brews.put(recipeName, quality);
            saveDrunkBrews(player, brews);
        }
    }

    private void syncUniqueBrewCount(final Player player) {
        final int uniqueBrewCount = getDrunkBrews(player).size();
        this.uniqueBrewsDrunk.setValue(player.getUniqueId(), uniqueBrewCount);
    }

    private void openBrewGui(final Player player) {
        if (!this.breweryEnabled) {
            player.sendMessage(Component.text("BreweryX is not installed on this server.")
                .color(NamedTextColor.RED));
            return;
        }

        final List<BRecipe> allRecipes = BRecipe.getAllRecipes();
        if (allRecipes == null || allRecipes.isEmpty()) {
            player.sendMessage(Component.text("No brews are configured on this server.")
                .color(NamedTextColor.RED));
            return;
        }

        final Map<String, Integer> drunkBrews = getDrunkBrews(player);
        final List<BRecipe> sortedRecipes = new ArrayList<>(allRecipes);
        sortedRecipes.sort(Comparator
            .comparing((BRecipe recipe) -> !drunkBrews.containsKey(recipe.getRecipeName()))
            .thenComparing(BRecipe::getRecipeName, String.CASE_INSENSITIVE_ORDER));

        final int totalBrews = sortedRecipes.size();
        final int discoveredBrews = (int) sortedRecipes.stream()
            .filter(recipe -> drunkBrews.containsKey(recipe.getRecipeName()))
            .count();

        final List<IClickable> clickables = new ArrayList<>();
        for (final BRecipe recipe : sortedRecipes) {
            final String recipeName = recipe.getRecipeName();
            final Integer bestQuality = drunkBrews.get(recipeName);
            final boolean hasDrunk = bestQuality != null;

            ItemStack displayItem;
            try {
                final Brew brew = recipe.createBrew(10);
                displayItem = brew.createItem(recipe);
                brew.seal(displayItem, null);
            } catch (final Exception exception) {
                displayItem = new ItemStack(Material.POTION);
            }

            final List<Component> lore = new ArrayList<>();
            final ItemMeta meta = displayItem.getItemMeta();
            meta.displayName(Component.text(recipeName)
                .color(hasDrunk ? NamedTextColor.GREEN : NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
            if (hasDrunk) {
                lore.add(Component.text("✔ Discovered")
                    .color(NamedTextColor.GREEN)
                    .decoration(TextDecoration.ITALIC, false));
                final int clamped = Math.clamp(bestQuality, 0, 10);
                final int filledStars = clamped / 2;
                final int unfilledStars = clamped % 2;
                lore.add(Component.text("★".repeat(filledStars) + "☆".repeat(unfilledStars) + " Best quality")
                    .color(NamedTextColor.GOLD)
                    .decoration(TextDecoration.ITALIC, false));
                meta.setEnchantmentGlintOverride(true);
            } else {
                lore.add(Component.text("✘ Not yet discovered")
                    .color(NamedTextColor.RED)
                    .decoration(TextDecoration.ITALIC, false));
            }
            meta.lore(lore);
            displayItem.setItemMeta(meta);

            clickables.add(new DecorationStack(displayItem));
        }

        final String title = "Brew Log (" + discoveredBrews + "/" + totalBrews + " discovered)";
        final MultiPageView view = new MultiPageView(player, clickables, title, true);
        final ItemStack topBrewers = new ItemStack(Material.PLAYER_HEAD);
        final SkullMeta topBrewersMeta = (SkullMeta) topBrewers.getItemMeta();
        if (profileLoaded) {
            topBrewersMeta.setPlayerProfile(profile);
        }
        topBrewersMeta.displayName(Component.text("Top Brewers")
            .color(NamedTextColor.GOLD)
            .decoration(TextDecoration.ITALIC, false));
        topBrewersMeta.lore(List.of(Component.text("View the top 10 by unique brews drunk")
            .color(NamedTextColor.GRAY)
            .decoration(TextDecoration.ITALIC, false)));
        topBrewers.setItemMeta(topBrewersMeta);
        view.setMenuSlot(new Clickable(topBrewers) {
            @Override
            protected void clicked(final Player clicker) {
                openTopBrewsGui(clicker);
            }
        }, 2);
        view.showScreen();
    }

    private void openTopBrewsGui(final Player player) {
        final List<BrewLeaderboardEntry> topPlayers = getTopBrewers();
        if (topPlayers.isEmpty()) {
            player.sendMessage(Component.text("No one has discovered any brews yet.")
                .color(NamedTextColor.RED));
            return;
        }

        final ClickableInventory inventory = new ClickableInventory(36, "Top Brewer");
        for (int index = 0; index < topPlayers.size(); index++) {
            final BrewLeaderboardEntry entry = topPlayers.get(index);
            inventory.setSlot(new DecorationStack(createLeaderboardHead(entry, index + 1)), TOP_BREW_SLOTS[index]);
        }
        inventory.showInventory(player);
    }

    private List<BrewLeaderboardEntry> getTopBrewers() {
        final List<BrewLeaderboardEntry> entries = new ArrayList<>();
        for (final Map.Entry<UUID, Integer> entry : this.uniqueBrewsDrunk.getValues().entrySet()) {
            final int uniqueBrewCount = entry.getValue();
            if (uniqueBrewCount <= 0) {
                continue;
            }
            entries.add(new BrewLeaderboardEntry(entry.getKey(), uniqueBrewCount));
        }
        entries.sort(Comparator.comparingInt(BrewLeaderboardEntry::uniqueBrewCount).reversed());
        return entries.size() > TOP_BREW_SLOTS.length ? entries.subList(0, TOP_BREW_SLOTS.length) : entries;
    }

    private ItemStack createLeaderboardHead(final BrewLeaderboardEntry entry, final int rank) {
        final ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        final SkullMeta meta = (SkullMeta) head.getItemMeta();
        String playerName = NameLayerAPI.getCurrentName(entry.playerId());
        if (playerName == null) {
            playerName = "unknown";
        }
        meta.setPlayerProfile(Bukkit.getServer().createProfile(entry.playerId(), playerName));
        meta.displayName(Component.empty().decoration(TextDecoration.ITALIC, false)
            .append(Component.text("#" + rank + " ").color(NamedTextColor.GOLD))
            .append(Component.text(playerName).color(NamedTextColor.YELLOW)));
        meta.lore(List.of(Component.text(entry.uniqueBrewCount() + " unique brews drunk")
            .color(NamedTextColor.GREEN)
            .decoration(TextDecoration.ITALIC, false)));
        head.setItemMeta(meta);
        return head;
    }

    private record BrewLeaderboardEntry(UUID playerId, int uniqueBrewCount) {
    }

    @CommandAlias("brews")
    public class BrewIndexCommand extends BaseCommand {

        @Default
        @CommandCompletion("top")
        @Description("Opens the brew log showing all brews and which ones you have drunk")
        public void onBrews(final Player sender) {
            openBrewGui(sender);
        }

        @Subcommand("top")
        @Description("Opens the leaderboard for unique brews drunk")
        public void onBrewsTop(final Player sender) {
            openTopBrewsGui(sender);
        }
    }
}
