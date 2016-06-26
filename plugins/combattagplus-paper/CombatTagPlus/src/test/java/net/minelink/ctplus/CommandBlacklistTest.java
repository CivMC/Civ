package net.minelink.ctplus;

import com.google.common.collect.ImmutableList;

import org.junit.Test;

import static junit.framework.Assert.*;

public class CommandBlacklistTest {
    @Test
    public void testTraditionalBlacklist() {
        CommandBlacklist blacklist = CommandBlacklist.parse(
                ImmutableList.of(
                        "/tpa",
                        "/tp",
                        "/tpaccept",
                        "/gamemode"
                ),
                ImmutableList.of()
        );
        assertTrue("tpa isn't blacklisted!", blacklist.isBlacklisted("tpa"));
        assertTrue("'/tpa' isn't blacklisted, but 'tpa' is", blacklist.isBlacklisted("/tpa"));
        assertTrue("gamemode isn't blacklisted!", blacklist.isBlacklisted("gamemode"));
        assertFalse("Tacos are blacklisted!", blacklist.isBlacklisted("taco"));
        assertFalse("'/taco' is blacklisted, but 'taco' isn't!", blacklist.isBlacklisted("taco"));
    }

    @Test
    public void testWhitelist() {
        CommandBlacklist blacklist = CommandBlacklist.parse(
                ImmutableList.of(
                        "/*"
                ),
                ImmutableList.of(
                        "/taco" // TACOS ARE ALWAYS ALLOWED IN COMBAT
                )
        );
        assertTrue("tpa isn't blacklisted!", blacklist.isBlacklisted("tpa"));
        assertTrue("'/tpa' isn't blacklisted, but 'tpa' is", blacklist.isBlacklisted("/tpa"));
        assertTrue("gamemode isn't blacklisted!", blacklist.isBlacklisted("gamemode"));
        assertFalse("Tacos are blacklisted!", blacklist.isBlacklisted("taco"));
        assertFalse("'/taco' is blacklisted, but 'taco' isn't!", blacklist.isBlacklisted("taco"));
    }

    @Test
    public void testPrioritization() {
        CommandBlacklist blacklist = CommandBlacklist.parse(
                ImmutableList.of(
                        "/tp*", // Teleporting is bad
                        "/eat-people"// Eating people is bad
                ),
                ImmutableList.of(
                        "/eat-*", // Eating is good!
                        "/tp-taco" // Teleporting tacos are good!
                )
        );
        assertTrue("tpa isn't blacklisted!", blacklist.isBlacklisted("tpa"));
        assertTrue("eating people isn't blacklisted", blacklist.isBlacklisted("eat-people"));
        assertFalse("Eating tacos is blacklisted!", blacklist.isBlacklisted("eat-taco"));
        assertFalse("Eating potatoes is blacklisted!", blacklist.isBlacklisted("eat-potato"));
        assertFalse("Telpoerting tacos is blacklisted!", blacklist.isBlacklisted("tp-taco"));

    }
}
