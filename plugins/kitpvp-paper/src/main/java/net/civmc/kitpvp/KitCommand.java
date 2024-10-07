package net.civmc.kitpvp;

import net.civmc.kitpvp.dao.KitPvpDao;
import net.civmc.kitpvp.gui.KitListGui;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class KitCommand implements CommandExecutor {

    private final KitPvpDao dao;

    public KitCommand(KitPvpDao dao) {
        this.dao = dao;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            return false;
        }

        new KitListGui(dao, player);
        return true;
    }
}
