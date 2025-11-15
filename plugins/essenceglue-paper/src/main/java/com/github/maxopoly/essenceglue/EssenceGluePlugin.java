package com.github.maxopoly.essenceglue;

import com.github.maxopoly.essenceglue.commands.EssenceCommand;
import com.github.maxopoly.essenceglue.commands.StreakCommand;
import com.github.maxopoly.essenceglue.commands.VoteCommand;
import com.github.maxopoly.essenceglue.reward.PhysicalRewarder;
import com.github.maxopoly.essenceglue.reward.Rewarder;
import com.github.maxopoly.essenceglue.reward.VirtualRewarder;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.civmodcore.ACivMod;
import vg.civcraft.mc.civmodcore.commands.CommandManager;
import vg.civcraft.mc.civmodcore.inventory.items.custom.CustomItem;
import vg.civcraft.mc.civmodcore.inventory.items.custom.CustomItemFactory;
import java.util.List;

public final class EssenceGluePlugin extends ACivMod {

    private static EssenceGluePlugin instance;
    private EssenceConfigManager configMan;
    private StreakManager streakMan;
    private RewardManager rewardMan;
    private VotifyManager votifyMan;
    private CommandManager commandManager;

    public static EssenceGluePlugin instance() {
        return instance;
    }

    CustomItemFactory ESSENCE_ITEM = CustomItem.registerCustomItem("player_essence", () -> {
        ItemStack essence = ItemStack.of(Material.ENDER_EYE);
        essence.setData(DataComponentTypes.ITEM_NAME, MiniMessage.miniMessage().deserialize("<i>Player Essence</i>"));
        essence.setData(DataComponentTypes.LORE, ItemLore.lore(List.of(
            Component.text("Activity reward used to fuel pearls")
        )));
        essence.setAmount(1);
        return essence;
    });

    @Override
    public void onEnable() {
        super.onEnable();
        instance = this;

        // ensure custom item is registered by creating one
        ESSENCE_ITEM.createItem();

        configMan = new EssenceConfigManager(this);
        if (!configMan.parse()) {
            getLogger().severe("Failed to read config, disabling");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        commandManager = new CommandManager(this);
        commandManager.init();
        registerCommands();
        streakMan = new StreakManager(this, configMan.getStreakDelay(), configMan.getStreakGracePeriod(),
            configMan.getMaxStreak(), configMan.getOnlineTimeForReward(), configMan.giveRewardToPearled());
        if (configMan.getDatabase() != null) {
            EssenceDAO dao = new EssenceDAO(this, configMan.getDatabase());
            if (!dao.update()) {
                getLogger().severe("Failed to apply database updates, disabling");
                Bukkit.getPluginManager().disablePlugin(this);
                return;
            }
        }
        Rewarder rewarder;
        if (configMan.isPhysical()) {
            rewarder = new PhysicalRewarder(configMan.getRewards());
        } else {
            VirtualEssenceManager essenceManager = new VirtualEssenceManager(this, configMan.getVirtualCap());
            commandManager.registerCommand(new EssenceCommand(essenceManager, configMan.getRewards()));
            rewarder = new VirtualRewarder(essenceManager);
        }

        rewardMan = new RewardManager(rewarder, configMan.getLoginReward(), configMan.getVotingReward());
        if (Bukkit.getPluginManager().isPluginEnabled("Votifier")) {
            votifyMan = new VotifyManager(rewardMan, configMan.getVotingCooldowns());
            Bukkit.getPluginManager().registerEvents(votifyMan, this);
        } else {
            getLogger().info("Votifier is not enabled, no voting support is possible");
        }
        Bukkit.getPluginManager().registerEvents(new ExilePearListener(streakMan, configMan.multiplyPearlCost()), this);
    }

    private void registerCommands() {
        commandManager.registerCommand(new StreakCommand());
        commandManager.registerCommand(new VoteCommand());
    }

    public StreakManager getStreakManager() {
        return streakMan;
    }

    public RewardManager getRewardManager() {
        return rewardMan;
    }

    public EssenceConfigManager getConfigManager() {
        return configMan;
    }

    public VotifyManager getVoteManager() {
        return votifyMan;
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

}
