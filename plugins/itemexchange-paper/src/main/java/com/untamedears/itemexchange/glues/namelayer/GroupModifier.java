package com.untamedears.itemexchange.glues.namelayer;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Single;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import com.untamedears.itemexchange.ItemExchangeListener;
import com.untamedears.itemexchange.commands.SetCommand;
import com.untamedears.itemexchange.rules.interfaces.Modifier;
import com.untamedears.itemexchange.rules.interfaces.ModifierData;
import com.untamedears.itemexchange.utility.ModifierHandler;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import vg.civcraft.mc.civmodcore.nbt.NbtCompound;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.group.Group;

/**
 * This is where the group limitation is stored. Don't worry if it doesn't seem to do much, it's actually
 * not supposed to. The logic behind group limitations is done within {@link ItemExchangeListener},
 * so take a look at that. This is basically just a place to store the group value.
 */
@CommandAlias(SetCommand.ALIAS) // This is needed to make commands work
@Modifier(slug = "GROUP", order = 2000)
public final class GroupModifier extends ModifierData {

    // Make sure to have a template instance to make registration easier
    public static final GroupModifier TEMPLATE = new GroupModifier();

    private static final String ID_KEY = "id";
    private static final String NAME_KEY = "name";

    private int groupId;
    private String groupName;

    @Override
    public GroupModifier construct(final ItemStack item) {
        return null; // Group modifiers must be specifically added
    }

    @Override
    public boolean conforms(final ItemStack item) {
        return true; // Do not break anything, the check is done elsewhere.
    }

    @Override
    public boolean isBroken() {
        return false;
    }

    @Override
    public void toNBT(final @NotNull NbtCompound nbt) {
        nbt.setInt(ID_KEY, getGroupId());
        nbt.setString(NAME_KEY, getGroupName());
    }

    public static @NotNull GroupModifier fromNBT(final @NotNull NbtCompound nbt) {
        final var modifier = new GroupModifier();
        modifier.setGroupId(nbt.getInt(ID_KEY, 0));
        modifier.setGroupName(nbt.getString(NAME_KEY, null));
        return modifier;
    }

    @Override
    public List<String> getDisplayInfo() {
        return List.of(isBroken() ?
            ChatColor.RED + "BROKEN GROUP MODIFIER" :
            "Group: " + this.groupName);
    }

    @Override
    public String toString() {
        return "%S{%s:%d}".formatted(getSlug(), getGroupName(), getGroupId());
    }

    // ------------------------------------------------------------
    // Commands
    // ------------------------------------------------------------

    @Subcommand("group")
    @Description("Sets (or unsets) the group to limit this trade to.")
    @Syntax("[group name]")
    public void commandSetGroup(final Player player,
                                final @Optional @Single String name) {
        if (!NameLayerGlue.instance.isDependencyEnabled()) {
            player.sendMessage(ChatColor.RED + "Can't do that right now!");
            return;
        }
        try (final var handler = new ModifierHandler<>(player, this)) {
            if (StringUtils.isEmpty(name)) {
                handler.setModifier(null);
                handler.relay(ChatColor.GREEN + "Successfully removed group requirement.");
                return;
            }
            final Group group = GroupManager.getGroup(name);
            if (group == null) {
                handler.relay(ChatColor.RED + "Could not find group \"" + name + "\"");
                return;
            }
            final GroupModifier modifier = handler.ensureModifier();
            modifier.setGroupId(group.getGroupId());
            modifier.setGroupName(group.getName());
            handler.relay(ChatColor.GREEN + "Set trade to group \"" + group.getName() + "\"");
        }
    }

    // ------------------------------------------------------------
    // Getters + Setters
    // ------------------------------------------------------------

    public int getGroupId() {
        return this.groupId;
    }

    public void setGroupId(final int groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return this.groupName;
    }

    public void setGroupName(final String groupName) {
        this.groupName = groupName;
    }

}
