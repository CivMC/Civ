package net.minelink.ctplus;

import java.util.List;
import java.util.stream.Collectors;

import net.minelink.ctplus.util.SimpleRegexSet;
import net.minelink.ctplus.util.Strings2;

import static com.google.common.base.Preconditions.*;

public class CommandBlacklist {
    private final SimpleRegexSet blacklist;
    private final SimpleRegexSet whitelist;

    public CommandBlacklist(SimpleRegexSet blacklist, SimpleRegexSet whitelist) {
        this.blacklist = checkNotNull(blacklist, "Null blacklist");
        this.whitelist = checkNotNull(whitelist, "Null whitelist");
    }

    public static CommandBlacklist parse(final List<String> blacklist, final List<String> whitelist) {
        SimpleRegexSet blacklistSet = SimpleRegexSet.parsePatterns(
                blacklist.stream()
                        .map((s) -> Strings2.stripLeading(s, '/')) // Remove redundant '/'
                        .map((s) -> s.split(" ")[0]) // Only check the first part of the command
                        .collect(Collectors.toList())
        );
        SimpleRegexSet whitelistSet = SimpleRegexSet.parsePatterns(
                whitelist.stream()
                        .map((s) -> Strings2.stripLeading(s, '/')) // Remove redundant '/'
                        .map((s) -> s.split(" ")[0]) // Only check the first part of the command
                        .collect(Collectors.toList())
        );
        return new CommandBlacklist(blacklistSet, whitelistSet);
    }

    public boolean isBlacklisted(String command) {
        if (checkNotNull(command, "Null command").isEmpty()) return false; // Feel free to execute nothing
        if (command.charAt(0) == '/') command = command.substring(1);
        command = command.toLowerCase();
        command = command.split(" ")[0];
        return !whitelist.containsExactly(command)
                && (
                blacklist.containsExactly(command)
                        || !whitelist.doesAnyPatternMatch(command)
                        && blacklist.doesAnyPatternMatch(command)
        );
    }
}
