package com.programmerdan.minecraft.banstick.proxy;

import com.programmerdan.minecraft.banstick.BanStick;
import com.programmerdan.minecraft.banstick.data.BSBan;
import com.programmerdan.minecraft.banstick.data.BSIP;
import com.programmerdan.minecraft.banstick.data.BSIPData;
import com.programmerdan.minecraft.banstick.handler.ProxyLoader;
import inet.ipaddr.IPAddress;
import inet.ipaddr.IPAddressString;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.util.*;

public final class PROXYListLoader extends ProxyLoader {

    private @NotNull List<String> urls;
    private float proxyScore;
    private boolean autoBan;
    private long banLength;
    private String banMessage;

    public PROXYListLoader(ConfigurationSection config) {
        super(config);
    }

    @Override
    public void run() {
        for (String url : this.urls) {
            try {
                BanStick.getPlugin().debug("Scraping {0}", url);

                URL connection = new URI(url).toURL();
                InputStream readIn = connection.openStream();
                BufferedReader in = new BufferedReader(new InputStreamReader(readIn));
                String line = in.readLine();
                int errors = 0;
                long lines = 0;
                while (line != null) {
                    lines++;
                    if (lines % 50 == 0) {
                        BanStick.getPlugin().info("Checked {0} entries from proxylist so far", lines);
                    }
                    if (errors > 10) {
                        break;
                    }

                    String[] parts = line.split(":");
                    if (parts.length != 2) {
                        errors++;
                        continue;
                    }
                    String ip = parts[0];
                    String port = parts[1];
                    IPAddressString ipAddressString = new IPAddressString(ip);
                    ipAddressString.validate();
                    IPAddress ipAddress = ipAddressString.toAddress();

                    BSIP found = BSIP.byIPAddress(ipAddress);
                    if (found == null) {
                        found = BSIP.create(ipAddress);
                    }

                    BSIPData data = BSIPData.byExactIP(found);
                    if (data == null) {
                        data = BSIPData.create(found, null, null, null, null, null, null, null,
                            null, null, null, null, proxyScore, "proxylist Proxy Loader", "Anon Proxy on Port: " + port);
                    }

                    if (autoBan) {
                        boolean wasmatch = false;

                        List<BSBan> ban = BSBan.byProxy(data, true);
                        if (!(ban == null || ban.isEmpty())) {
                            // look for match; if unexpired, extend.
                            for (int i = ban.size() - 1; i >= 0; i--) {
                                BSBan pickOne = ban.get(i);
                                if (pickOne.isAdminBan()) {
                                    continue; // skip admin entered bans.
                                }
                                if (pickOne.getBanEndTime() != null && pickOne.getBanEndTime().after(new Date())) {
                                    if (this.banLength < 0) { // endless
                                        pickOne.clearBanEndTime();
                                    } else {
                                        pickOne.setBanEndTime(new Date(System.currentTimeMillis()
                                            + this.banLength));
                                    }
                                    wasmatch = true;
                                    break;
                                }
                            }
                        }
                        if (!wasmatch) {
                            BSBan.create(data, this.banMessage,
                                this.banLength < 0 ? null : new Date(System.currentTimeMillis()
                                    + this.banLength), false);
                        }
                    }

                    line = in.readLine();
                }
                if (errors > 10) {
                    BanStick.getPlugin().warning("Cancelled proxylist load due to too many errors.");
                }
                BanStick.getPlugin().info("Finished loading {0} entries from proxylist: {1}", lines, url);
            } catch (Exception e) {
                BanStick.getPlugin().warning("Failed reading from proxylist: " + url);
                BanStick.getPlugin().warning("  Failure message: ", e);
            }
        }
    }

    @Override
    public void setup(ConfigurationSection config) {
        this.proxyScore = (float) config.getDouble("defaultScore", 3.0d);
        this.autoBan = config.isConfigurationSection("ban");
        if (this.autoBan) {
            this.banLength = config.getLong("ban.length", -1L);
            this.banMessage = config.getString("ban.message", null);
        }

        if (config.isList("urls")) {
            this.urls = config.getStringList("urls");
            BanStick.getPlugin().info("Loaded {0} proxylist URLs", this.urls.size());
        } else {
            this.urls = new ArrayList<>();
            BanStick.getPlugin().warning("No proxylist URLs configured, loader will not run.");
        }
    }

    @Override
    public String name() {
        return "proxylist";
    }
}
