package com.devotedmc.ExilePearl.Util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import com.devotedmc.ExilePearl.PearlLogger;

public class MockPearlLogger implements PearlLogger {
	
	private final Logger logger;
	
	public MockPearlLogger(final String name) {
		logger = Logger.getLogger(name);
		
		Formatter formatter = new Formatter() {
			
			private final DateFormat df = new SimpleDateFormat("hh:mm:ss");
			
			@Override
			public String format(LogRecord record) {
				return String.format("[%s %s]: %s\n", df.format(new Date(record.getMillis())), record.getLevel().getLocalizedName(), formatMessage(record));
			}
		};
		
		logger.setUseParentHandlers(false);
		ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(formatter);
        logger.addHandler(handler);
	}
	
	public Logger getLogger() {
		return logger;
	}

	@Override
	public void log(Level level, String msg, Object... args) {
		logger.log(level, String.format(msg, args));
	}

	@Override
	public void log(String msg, Object... args) {
		logger.log(Level.INFO, String.format(msg, args));
	}

}
