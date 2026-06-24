package vg.civcraft.mc.citadel.playerstate;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.CitadelUtility;
import vg.civcraft.mc.citadel.ReinforcementLogic;
import vg.civcraft.mc.citadel.reinforcementtypes.ReinforcementType;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.group.Group;

public class FortificationState extends AbstractPlayerState {

    private final ReinforcementType type;
    private final String groupName;

    public FortificationState(Player player, ReinforcementType type, String groupName) {
        super(player);
        this.type = type;
        this.groupName = groupName;
    }

    public Group getGroup() {
        return GroupManager.getGroup(groupName);
    }

    @Override
    public String getName() {
        return "Fortifying mode with " + ChatColor.AQUA + type.getName() + ChatColor.YELLOW + " on "
            + ChatColor.LIGHT_PURPLE + groupName;
    }

    public ReinforcementType getType() {
        return type;
    }

    @Override
    public void handleBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        // Prevent double reinforcement (slabs)
        if (ReinforcementLogic.getReinforcementAt(block.getLocation()) != null) {
            return;
        }
        if (CitadelUtility.attemptReinforcementCreation(block, type, getGroup(), groupName, player)) { // true == fail
            Citadel.getInstance().getStateManager().setState(player, null);
            event.setCancelled(true);
            //return;
        }
    }

    @Override
    public void handleInteractBlock(PlayerInteractEvent event) {
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof FortificationState fort)) {
            return false;
        }
        return fort.type == this.type && fort.groupName.equals(this.groupName);
    }

    @Override
    public String getOverlayText() {
        return String.format("%sCTF %s%s %s%s", ChatColor.GOLD, ChatColor.LIGHT_PURPLE, groupName, ChatColor.AQUA,
            type.getName());
    }
}
