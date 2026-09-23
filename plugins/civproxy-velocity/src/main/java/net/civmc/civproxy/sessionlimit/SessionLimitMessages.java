package net.civmc.civproxy.sessionlimit;

import java.time.Duration;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;

final class SessionLimitMessages {

    private final String server;
    private final String limit;
    private final String breakReset;
    private final String grace;

    SessionLimitMessages(final SessionLimitConfig config) {
        this.server = config.server();
        this.limit = DurationFormat.words(config.limit());
        this.breakReset = DurationFormat.words(config.breakReset());
        this.grace = DurationFormat.words(config.queueGrace());
    }

    Component countdownBar(final Duration remaining) {
        return Component.text("Session limit: " + DurationFormat.clock(remaining) + " left: type /logout to leave safely");
    }

    Component graceBar(final Duration remaining) {
        return Component.text("Queue waiting: moving you to the lobby in " + DurationFormat.clock(remaining)
            + " · type /logout to leave safely");
    }

    Component countdownWarning(final Duration remaining) {
        return Component.text()
            .append(Component.text("You have " + DurationFormat.words(remaining) + " left of your " + this.limit
                + " on " + this.server + ". If players are waiting in the queue when it runs out, you'll be moved to"
                + " the lobby and the back of the queue. Type or click ", NamedTextColor.YELLOW))
            .append(logoutButton())
            .append(Component.text(" to leave safely. A break of " + this.breakReset + " or more resets your timer.",
                NamedTextColor.YELLOW))
            .build();
    }

    Component graceStarted() {
        return Component.text()
            .append(Component.text("Players are waiting in the queue and you're past your " + this.limit + " on "
                + this.server + ". You'll be moved to the lobby in " + this.grace + ". Type or click ", NamedTextColor.RED))
            .append(logoutButton())
            .append(Component.text(" to leave safely.", NamedTextColor.RED))
            .build();
    }

    Component graceEnding(final Duration remaining) {
        return Component.text()
            .append(Component.text("Moving you to the lobby in " + DurationFormat.words(remaining) + ". Type or click ",
                NamedTextColor.RED))
            .append(logoutButton())
            .append(Component.text(" to leave safely.", NamedTextColor.RED))
            .build();
    }

    Component queueCleared() {
        return Component.text("The queue has cleared, so you can keep playing. If it fills up again, you'll get "
            + this.grace + " of warning.", NamedTextColor.GREEN);
    }

    Component moved() {
        return Component.text("You were moved to the queue after " + this.limit + " on " + this.server + " so players"
            + " waiting in the queue can get in..", NamedTextColor.GOLD);
    }

    Component movedDisconnect() {
        return Component.text("You were moved off " + this.server + " after " + this.limit + " so players waiting in"
            + " the queue can get in. Reconnect to rejoin the queue.", NamedTextColor.GOLD);
    }

    private static Component logoutButton() {
        return Component.text("[/logout]", NamedTextColor.GREEN)
            .clickEvent(ClickEvent.runCommand("/logout"))
            .hoverEvent(HoverEvent.showText(Component.text(
                "Stand still for 10 seconds to log out without leaving a logger NPC behind")));
    }
}
