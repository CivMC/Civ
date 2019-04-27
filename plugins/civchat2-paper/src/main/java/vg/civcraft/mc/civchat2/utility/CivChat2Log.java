package vg.civcraft.mc.civchat2.utility;

import java.util.logging.Logger;

import org.bukkit.plugin.java.JavaPlugin;

import vg.civcraft.mc.civchat2.CivChat2;

public class CivChat2Log {
	private Logger log;

	public void initializeLogger(JavaPlugin jp) {

		log = jp.getLogger();
	}

	public void info(String msg) {

		log.info(msg);
	}

	public void warning(String msg) {

		log.warning(msg);
	}

	public void severe(String msg) {

		log.severe(msg);
	}

	public void debug(String msg) {
		if (CivChat2.getInstance().debugEnabled()) {
			log.info("[Debug] " + msg);
		}
	}
}
