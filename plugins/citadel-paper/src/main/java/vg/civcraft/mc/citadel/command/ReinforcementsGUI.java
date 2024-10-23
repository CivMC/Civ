package vg.civcraft.mc.citadel.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.acidtypes.AcidType;
import vg.civcraft.mc.citadel.reinforcementtypes.ReinforcementType;
import vg.civcraft.mc.civmodcore.inventory.gui.DecorationStack;
import vg.civcraft.mc.civmodcore.inventory.gui.IClickable;
import vg.civcraft.mc.civmodcore.inventory.gui.MultiPageView;
import vg.civcraft.mc.civmodcore.inventory.items.ItemUtils;
import vg.civcraft.mc.civmodcore.utilities.TextUtil;

import static java.util.stream.Collectors.partitioningBy;

public class ReinforcementsGUI extends BaseCommand {

    private DecimalFormat format = new DecimalFormat("##.##");

    @CommandAlias("ctdl|reinforcements")
    @Description("Opens a GUI displaying all reinforcement materials")
    public void execute(Player sender) {

        Map<Boolean, List<ReinforcementType>> allowedOrNot = Citadel.getInstance().getReinforcementTypeManager().getAllTypes()
            .stream().collect(partitioningBy(i ->
                i.isAllowedInWorld(sender.getWorld().getName())));

        List<ReinforcementType> allowedTypes = allowedOrNot.get(true);
        List<ReinforcementType> disallowedTypes = allowedOrNot.get(false);

        // sort ascending by health
        Collections.sort(allowedTypes, (o1, o2) -> Double.compare(o1.getHealth(), o2.getHealth()));
        Collections.sort(disallowedTypes, (o1, o2) -> Double.compare(o1.getHealth(), o2.getHealth()));
        List<IClickable> clicks = new LinkedList<>();

        clicks.addAll(getMaterialClicks(allowedTypes, true));

        if (disallowedTypes.size() > 0) {
            for (int i = 0; i < 18 - (allowedTypes.size() % 9); i++) {
                clicks.add(new DecorationStack(Material.AIR));
            }
        }
        clicks.addAll(getMaterialClicks(disallowedTypes, false));

        List<AcidType> acidTypes = Citadel.getInstance().getAcidManager().getAcidTypes();
        // check if dimensional reinforcements
        int size = disallowedTypes.size() > 0 ? disallowedTypes.size() : allowedTypes.size();
        for (int i = 0; i < 18 - (size % 9); i++) {
            clicks.add(new DecorationStack(Material.AIR));
        }
        clicks.addAll(getAcidTypeClicks(acidTypes));

        MultiPageView pageView = new MultiPageView(sender, clicks, ChatColor.BLUE + "Reinforcements", true);
        pageView.showScreen();
    }

    private List<IClickable> getMaterialClicks(List<ReinforcementType> types, boolean allowed) {
        List<IClickable> clickables = new LinkedList<>();

        for (ReinforcementType type : types) {
            ItemStack is = type.getItem().clone();
            ItemUtils.setComponentDisplayName(is, Component.text(ChatColor.AQUA + type.getName() + (allowed ? ChatColor.GREEN + " (Allowed in current dimension)" : ChatColor.RED + " (Not allowed in current dimension)")));
            ItemUtils.addLore(is, ChatColor.GREEN + "Health: " + format.format(type.getHealth()));
            ItemUtils.addLore(is, ChatColor.GOLD + "Maturation time: "
                + TextUtil.formatDuration(type.getMaturationTime(), TimeUnit.MILLISECONDS));
            if (type.getAcidTime() > 0) {
                ItemUtils.addLore(is, ChatColor.GOLD + "Acid maturation time: "
                    + TextUtil.formatDuration(type.getAcidTime(), TimeUnit.MILLISECONDS));
            } else {
                ItemUtils.addLore(is, ChatColor.GOLD + "Can not be used for acid");
            }
            ItemUtils.addLore(is,
                ChatColor.WHITE + "Return chance: " + format.format(type.getReturnChance() * 100.0) + " %");

            ItemUtils.addComponentLore(is, Component.text(ChatColor.GOLD + "Allowed dimensions:"));
            List<Component> allowedDimensionComponents = type.getAllowedWorlds().stream().map(e ->
                Component.text(ChatColor.GREEN + " - " + e)).collect(Collectors.toList());

            if (allowedDimensionComponents.isEmpty()) {
                ItemUtils.addComponentLore(is, Component.text(ChatColor.DARK_GREEN + " * Everywhere"));
            } else {
                ItemUtils.addComponentLore(is, allowedDimensionComponents);
            }
            IClickable click = new DecorationStack(is);
            clickables.add(click);
        }

        return clickables;
    }

    private List<IClickable> getAcidTypeClicks(List<AcidType> acidTypes) {
        List<IClickable> clickables = new ArrayList<>();

        for (AcidType acidType : acidTypes) {
            ItemStack is = new ItemStack(acidType.material());
            String blockName = ItemUtils.getItemName(acidType.material());
            ItemUtils.setComponentDisplayName(is, Component.text(ChatColor.RED + blockName));
            ItemUtils.addLore(is, ChatColor.GOLD + "Acid faces: " + (acidType.blockFaces().stream().map(BlockFace::toString).collect(Collectors.joining(", "))));
            ItemUtils.addLore(is, ChatColor.GOLD + "Maturation modifier: " + format.format(acidType.modifier()) + "x");

            IClickable click = new DecorationStack(is);
            clickables.add(click);
        }

        return clickables;
    }
}
