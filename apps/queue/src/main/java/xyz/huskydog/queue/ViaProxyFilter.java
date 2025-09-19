package xyz.huskydog.queue;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;
import net.minestom.server.MinecraftServer;

public class ViaProxyFilter extends Filter<ILoggingEvent> {
    @Override
    public FilterReply decide(ILoggingEvent event) {
        String logger = event.getLoggerName();
        if (!logger.contains("Via")) {
            return FilterReply.NEUTRAL;
        } else if (!logger.equals("ViaProxy")) {
            MinecraftServer.LOGGER.info("[ViaMinestom] (" + logger + ") " + event.getMessage());
        }
        return FilterReply.DENY;
    }
}
