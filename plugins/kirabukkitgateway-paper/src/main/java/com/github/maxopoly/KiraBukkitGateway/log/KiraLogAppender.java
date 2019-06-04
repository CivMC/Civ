package com.github.maxopoly.KiraBukkitGateway.log;

import java.util.regex.Pattern;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.layout.PatternLayout;

import com.github.maxopoly.KiraBukkitGateway.KiraBukkitGatewayPlugin;

public class KiraLogAppender extends AbstractAppender {

	private Pattern pattern;
	private String key;

	public KiraLogAppender(String key, String regex) {
		super("Kira Appender regex " + regex, null, PatternLayout.createDefaultLayout());
		this.pattern = Pattern.compile(regex);
		this.key = key;
	}

	@Override
	public void append(LogEvent event) {
		String msg = event.getMessage().getFormattedMessage();
		if (pattern.matcher(msg).matches()) {
			KiraBukkitGatewayPlugin.getInstance().getRabbit().sendConsoleRelay(msg, key);
		}
	}

}
