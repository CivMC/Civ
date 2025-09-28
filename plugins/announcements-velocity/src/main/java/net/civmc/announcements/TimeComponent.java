package net.civmc.announcements;

import java.time.Duration;
import net.kyori.adventure.text.Component;

public class TimeComponent {

    public static Component replace(Component component, Duration duration) {
        long minutes = Math.ceilDiv(duration.getSeconds(), 60);
        return component
            .replaceText(b -> b.matchLiteral("%").replacement(Long.toString(minutes)))
            .replaceText(b -> b.matchLiteral("$").replacement(minutes == 1 ? "" : "s"));
    }

}
