package com.programmerdan.minecraft.banstick.handler;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.programmerdan.minecraft.banstick.BanStick;
import com.programmerdan.minecraft.banstick.data.BSBan;
import com.programmerdan.minecraft.banstick.data.BSIP;
import com.programmerdan.minecraft.banstick.data.BSIPData;
import com.programmerdan.minecraft.banstick.data.BSSession;

import inet.ipaddr.IPAddress;
import inet.ipaddr.IPAddressString;

/**
 * This class deals with scheduling a constrained lookup / update of data from IP proxy data key-locked services.
 * 
 * Initially supports iphub.info. Can be used for paid services.
 * 
 * Configurable, slightly.
 * 
 * @author ProgrammerDan
 *
 */
public class BanStickIPHubHandler extends BukkitRunnable {
	private BukkitTask selfTask;
	private ConcurrentLinkedQueue<WeakReference<BSIP>> toCheck = null;
	private boolean enabled = false;
	
	private long period;
	private int currentFailures;
	private int disableOnFailures;
	private long cooldownToReenable;
	
	private String key;
	
	private final String target = "http://v2.api.iphub.info/ip/";
	
	public BanStickIPHubHandler(FileConfiguration config) {
		if (!configure(config.getConfigurationSection("iphub"))) {
			BanStick.getPlugin().warning("IP Hub Proxy (iphub.info) lookup is disabled. This will reduce the quality of information on player's connections.");
			return;
		}
		
		begin();
	}
	private boolean configure(ConfigurationSection config) {
		if (config != null && config.getBoolean("enable", false)) {
			enabled = true;
		} else {
			return false;
		}
		
		this.toCheck = new ConcurrentLinkedQueue<WeakReference<BSIP>>();
		
		this.key = config.getString("key");
		this.period = config.getLong("period", 40);
		this.disableOnFailures = config.getInt("failureCap", 5);
		this.cooldownToReenable = config.getLong("cooldownTicks", 72000l);
		this.currentFailures = 0;
		
		return true;
	}
	
	private void begin() {
		if (enabled) {
			currentFailures = 0;
			selfTask = this.runTaskTimerAsynchronously(BanStick.getPlugin(), period, period);
			BanStick.getPlugin().warning("Dynamic IP Hub lookup task started.");
		}
	}
	
	public void end() {
		this.enabled = false;
		if (this.selfTask == null) return;
		this.selfTask.cancel();
	}
	
	public void offer(BSIP toCheck) {
		if (enabled) {
			this.toCheck.offer(new WeakReference<BSIP>(toCheck));
		}
	}
	
	@Override
	public void run() {
		if (!enabled) return;
		if (disableOnFailures <= currentFailures) {
			enabled = false;
			if (this.cooldownToReenable > 0) {
				BanStick.getPlugin().severe("Too many failures; temporarily disabling BanStickIPHub updater.");
				Bukkit.getScheduler().runTaskLater(BanStick.getPlugin(), new Runnable() {
					@Override
					public void run() {
						currentFailures = 0;
						enabled = true;
					}
				}, this.cooldownToReenable);
			} else {
				BanStick.getPlugin().severe("Too many failures; permanently disabling BanStickIPHub updater.");
				selfTask.cancel();
			}
			return;
		}
		if (this.toCheck.isEmpty()) return;
		try {
			WeakReference<BSIP> nextCheck = null;
			BSIP nextIP = null;
			IPAddress address = null;
			while (address == null && !this.toCheck.isEmpty()) {
				nextCheck = this.toCheck.poll();
				if (nextCheck != null) {
					nextIP = nextCheck.get();
				}
				address = nextIP.getIPAddress();
				Integer mask = address.getNetworkPrefixLength();
				if (!(mask == null || mask == (address.isIPv4() ? 32 : 128))) {
					address = null;
					continue; // only cidr-less ips allowed.
				}
				if (mask != null) {
					address = address.getLower(); // strip cidr
				}
			} // found
			
			if (address != null) { // sanity
				URL url = new URL(target + "/" + address.toString());
				HttpURLConnection connection = (HttpURLConnection) url.openConnection();
				
				connection.setRequestMethod("GET");
				connection.setDoOutput(true);
				if (key != null) {
					connection.setRequestProperty("X-Key", key);
				}
				connection.setDoInput(true);
				connection.connect();

				IpData reply = null;
								
				GsonBuilder builder = new com.google.gson.GsonBuilder();
				Gson gson = builder.create();

				try (InputStreamReader dataReply = new InputStreamReader(connection.getInputStream())) {
					reply = gson.fromJson(dataReply, IpData.class);
				}

				if (reply == null) {
					BanStick.getPlugin().warning("IPHub periodic updater failure, no data received");
					currentFailures ++;
					return;
				}

				if (reply.getBlock() == null) {
					BanStick.getPlugin().debug("Failure during IPHub lookup for {0}: {1}", address.toString(), reply.toString());
					return;
				}

				IPAddressString replyAddress = new IPAddressString(reply.getIp());
				IPAddress naddress = replyAddress.getAddress();
				if (naddress == null) {
					BanStick.getPlugin().debug("Failure during IPHub lookup for {0}: failed to parse {1} as IP?", address.toString(), reply.getIp());
					return;
				}
				BSIP ipMatch = BSIP.byIPAddress(naddress);
				if (ipMatch == null) {
					BanStick.getPlugin().debug("Failure during IPHub lookup for {0}: returned {1} instead?", address.toString(), reply.getIp());
					return;
				}
				BSIPData dataMatch = BSIPData.byExactIP(ipMatch);
				String continent = null;
				String domain = null;
				String region = null;
				String city = null;
				String comment = null;
				String postal = null;
				Double lat = null;
				Double lon = null;
				String org = null;
				String sauce = "IPHub.info";
				float proxy = reply.transformBlock();
				if (dataMatch != null) {
					if (reply.hasChanged(dataMatch)) { // is old outdated?
						dataMatch.invalidate(); // mark that record and make a new one.
						continent = dataMatch.getContinent();
						domain = dataMatch.getDomain();
						comment = dataMatch.getComment();
						region = dataMatch.getRegion();
						city = dataMatch.getCity();
						proxy = Math.max(dataMatch.getProxy(), reply.transformBlock()); // prefer highest resolved violation
						postal = dataMatch.getPostal();
						lat = dataMatch.getLat();
						lon = dataMatch.getLon();
						org = dataMatch.getProvider();
						
						if (dataMatch.getSource() != null && dataMatch.getSource().contains(sauce)) {
							sauce = dataMatch.getSource();
						} else {
							sauce = dataMatch.getSource() != null ? dataMatch.getSource() + " aug. by IPHub.info" : "IPHub.info";
						}
					} else {
						return; // just move on, no changes.
					}
				}
				dataMatch = BSIPData.create(ipMatch, continent, reply.getCountryName(), region, city, 
						postal, lat, lon, domain, org, reply.getAsn().toString(), reply.getIsp(),
						proxy, sauce, comment);
				
				if (dataMatch != null) {
					BanStick.getPlugin().getEventHandler().manageDeferredProxyKick(nextIP, dataMatch);
				}

			}
		} catch (MalformedURLException mue) {
			enabled = false;
			BanStick.getPlugin().severe("Failed to connect to malformed IPHub check url", mue);
		} catch (IOException ioe) {
			currentFailures ++;
			BanStick.getPlugin().warning("IO Error on IPHub update: ", ioe);
		} catch (ClassCastException cce) {
			enabled = false;
			BanStick.getPlugin().severe("Failed to identify connection as http; perm failure", cce);
		}
	}
	
	class IpData {
		private String ip;
		private String hostname;
		private String countryCode;
		private String countryName;
		private Integer asn;
		private String isp;
		private Integer block;
		
		public IpData() {}

		public String getIp() {
			return ip;
		}

		public void setStatus(String ip) {
			this.ip = ip;
		}

		public String getHostname() {
			return hostname;
		}

		public void setHostname(String hostname) {
			this.hostname = hostname;
		}

		public String getCountryName() {
			return countryName;
		}

		public void setCountryName(String countryName) {
			this.countryName = countryName;
		}

		public String getCountryCode() {
			return countryCode;
		}

		public void setCountryCode(String countryCode) {
			this.countryCode = countryCode;
		}

		public String getIsp() {
			return isp;
		}

		public void setIsp(String isp) {
			this.isp = isp;
		}

		public Integer getBlock() {
			return block;
		}

		public void setBlock(Integer block) {
			this.block = block;
		}
		
		public float transformBlock() {
			switch (block) {
			case 0:
				return 0.0f; // residential IP // safe
			case 1:
				return 3.0f; // non-residential IP / host / proxy / vpn
			case 2:
				return 1.5f; // non-residential  AND  residential IP
			}
			return 0.0f;
		}

		public Integer getAsn() {
			return asn;
		}

		public void setAsn(Integer asn) {
			this.asn = asn;
		}
		
		public boolean hasChanged(BSIPData data) {
			if (isEqual(data.getCountry(), this.countryName) &&
				isEqual(data.getRegisteredAs(), this.asn.toString()) &&
				isEqual(data.getConnection(), this.isp) &&
				isEqual(data.getProxy(), this.transformBlock())) {
				return false;
			}
			return true;
		}
		
		private boolean isEqual(Object a, Object b) {
			if (a == null && b == null) return true;
			if (a != null) return a.equals(b);
			return b.equals(a);
		}
		
		@Override
		public String toString() {
			StringBuffer sb = new StringBuffer("Reply: ");
			sb.append(ip).append(" ").append(hostname).append(" ").append(countryCode).append(" ").append(countryName).append(" ")
					.append(isp).append("-").append(asn).append(" = ").append(block);
			return sb.toString();
		}
	}
}
