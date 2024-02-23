package com.github.maxopoly.KiraBukkitGateway.log;

import com.github.maxopoly.KiraBukkitGateway.KiraBukkitGatewayPlugin;
import com.github.maxopoly.KiraBukkitGateway.KiraUtil;
import java.util.regex.Pattern;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.layout.PatternLayout;

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
			KiraBukkitGatewayPlugin.getInstance().getRabbit().sendConsoleRelay(
					KiraUtil.cleanUp(msg), key);
		}
	}

}
