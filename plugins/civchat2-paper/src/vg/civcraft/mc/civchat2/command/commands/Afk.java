package vg.civcraft.mc.civchat2.command.commands;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import vg.civcraft.mc.civchat2.CivChat2;
import vg.civcraft.mc.civchat2.CivChat2Manager;
import vg.civcraft.mc.civchat2.command.ChatCommand;
import vg.civcraft.mc.civchat2.utility.CivChat2Log;
import vg.civcraft.mc.namelayer.NameAPI;

public class Afk extends ChatCommand {
	private CivChat2 plugin = CivChat2.getInstance();
	private CivChat2Manager chatMan;
	private CivChat2Log logger = CivChat2.getCivChat2Log();

	public Afk(String name) {
		super(name);
		setIdentifier("afk");
		setDescription("This command is used to toggle afk status.");
		setUsage("/afk");
		setArguments(0, 0);
		setSenderMustBeConsole(true);
	}

	@Override
	public boolean execute(CommandSender sender, String[] args){
		chatMan = plugin.getCivChat2Manager();

		Player player = (Player) sender;		
		String name = NameAPI.getCurrentName(player.getUniqueId());
		chatMan.toggleAfk(name);
		String debugMessage = "Player toggled AFK state, Player: " + name + " Current State: " + chatMan.isAfk(name);
		logger.debug(debugMessage);

		return true;
	}
}
