package com.devotedmc.ExilePearl.command;

import com.devotedmc.ExilePearl.ExilePearlApi;

public class CmdExilePearl extends PearlCommand {

	private static CmdExilePearl instance;

	public static CmdExilePearl instance() {
		return instance;
	}

	public final PearlCommand cmdLocate;
	public final PearlCommand cmdFree;
	public final PearlCommand cmdBcast;
	public final PearlCommand cmdBcastConfirm;
	public final PearlCommand cmdBcastSilence;
	public final PearlCommand cmdDowngrade;

	public CmdExilePearl(ExilePearlApi pearlApi) {
		super(pearlApi);
		this.aliases.add("ep");

		this.setHelpShort("The ExilePearl command");
		this.getLongHelp().add("<n>Use <c>/ep help <n>for command help.");

		cmdLocate = new CmdPearlLocate(plugin);
		cmdFree = new CmdPearlFree(plugin);
		cmdBcast = new CmdPearlBroadcast(plugin);
		cmdBcastConfirm = new CmdPearlBroadcastAccept(plugin);
		cmdBcastSilence = new CmdPearlBroadcastSilence(plugin);
		cmdDowngrade = new CmdDowngrade(plugin);

		addSubCommand(plugin.getAutoHelp());

		addSubCommand(cmdLocate);
		addSubCommand(cmdFree);
		addSubCommand(cmdBcast);
		addSubCommand(cmdBcastConfirm);
		addSubCommand(cmdBcastSilence);
		addSubCommand(cmdDowngrade);
		addSubCommand(new CmdSummon(plugin));
		addSubCommand(new CmdReturn(plugin));
		addSubCommand(new CmdShowAllPearls(plugin));
		addSubCommand(new CmdSummonConfirm(plugin));
		addSubCommand(new CmdUpgrade(plugin));

		// Admin commands
		addSubCommand(new CmdConfig(plugin));
		addSubCommand(new CmdAdminDecay(plugin));
		addSubCommand(new CmdAdminExileAny(plugin));
		addSubCommand(new CmdAdminSetDate(plugin));
		addSubCommand(new CmdAdminFreeAny(plugin));
		addSubCommand(new CmdAdminSetHealth(plugin));
		addSubCommand(new CmdAdminSetType(plugin));
		addSubCommand(new CmdAdminSetKiller(plugin));
		addSubCommand(new CmdAdminCheckExiled(plugin));
		addSubCommand(new CmdAdminListExiled(plugin));
		addSubCommand(new CmdAdminReload(plugin));

		instance = this;
	}

	@Override
	public void perform() {
		this.commandChain.add(this);
		plugin.getAutoHelp().execute(this.sender, this.args, this.commandChain);
	}
}
