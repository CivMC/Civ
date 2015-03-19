package com.github.igotyou.FactoryMod.persistence;

import java.io.File;
import java.io.IOException;

import com.github.igotyou.FactoryMod.FactoryModPlugin;
import com.google.common.io.Files;

public class FileBackup {
	
	private static String BACKUP_EXT = ".bak";
	
	public static synchronized void backup(File file) {
		
		File backup = new File(file.getAbsolutePath() + BACKUP_EXT);
		
		if(backup.exists()) {
			backup.delete();
		}
		
		try {
			Files.copy(file, backup);
		} catch (IOException e) {
			FactoryModPlugin.sendConsoleMessage("ERROR: Could not copy file to backup!" + file.getName());
		}
	}

}
