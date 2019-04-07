package com.github.maxopoly.KiraBukkitGateway;

import java.util.UUID;

import org.bukkit.Bukkit;

public class MessageProcessor {

	private String input;

	public MessageProcessor(String input) {
		this.input = input;
	}

	public void process() {
		UUID uuid = UUID.fromString(input.split(" ")[0]);
		// uuids with dashes are always 36 characters and we also want to exclude the
		// space after the uuid
		String msg = input.substring(37);
		runCommand(uuid, msg);
	}

	private void runCommand(UUID uuid, String msg) {
		try {
			Bukkit.getServer().dispatchCommand(new PseudoPlayer(uuid), msg);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
