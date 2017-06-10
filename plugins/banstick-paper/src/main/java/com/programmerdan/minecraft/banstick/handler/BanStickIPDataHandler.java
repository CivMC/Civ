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
import com.programmerdan.minecraft.banstick.data.BSIP;
import com.programmerdan.minecraft.banstick.data.BSIPData;

import inet.ipaddr.IPAddress;
import inet.ipaddr.IPAddressString;

/**
 * This class deals with scheduling a constrained lookup / update of data from IP data reporting service(s).
 * 
 * Initially supports ip-api.com tl;dr free and decent request limits.
 * 
 * Configurable, slightly.
 * 
 * @author ProgrammerDan
 *
 */
public class BanStickIPDataHandler extends BukkitRunnable {
	private BukkitTask selfTask;
	private ConcurrentLinkedQueue<WeakReference<BSIP>> toCheck = null;
	private boolean enabled = false;
	
	private int maxBatch;
	private long period;
	private int currentFailures;
	private int disableOnFailures;
	private long cooldownToReenable;
	
	private final String target = "http://ip-api.com/batch";
	
	public BanStickIPDataHandler(FileConfiguration config) {
		if (!configure(config.getConfigurationSection("iplookup"))) {
			BanStick.getPlugin().warning("IP Data lookup is disabled. This will reduce the quality of information on player's connections.");
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
		
		this.maxBatch = config.getInt("maxBatch", 50);
		this.period = config.getLong("period", 20);
		this.disableOnFailures = config.getInt("failureCap", 10);
		this.cooldownToReenable = config.getLong("cooldownTicks", 72000l);
		this.currentFailures = 0;
		
		return true;
	}
	
	private void begin() {
		if (enabled) {
			currentFailures = 0;
			selfTask = this.runTaskTimerAsynchronously(BanStick.getPlugin(), period, period);
			BanStick.getPlugin().warning("Dynamic IP Data lookup task started.");
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
				BanStick.getPlugin().severe("Too many failures; temporarily disabling BanStickIPData updater.");
				Bukkit.getScheduler().runTaskLater(BanStick.getPlugin(), new Runnable() {
					@Override
					public void run() {
						currentFailures = 0;
						enabled = true;
					}
				}, this.cooldownToReenable);
			} else {
				BanStick.getPlugin().severe("Too many failures; permanently disabling BanStickIPData updater.");
				selfTask.cancel();
			}
			return;
		}
		if (this.toCheck.isEmpty()) return;
		try {
			Set<Long> hardStaged = new HashSet<Long>();
			List<Map<String, String>> source = new ArrayList<Map<String, String>>();
			
			int cBatch = 0;
			while (cBatch < this.maxBatch && !this.toCheck.isEmpty()) {
				WeakReference<BSIP> nextCheck = this.toCheck.poll();
				if (nextCheck == null) break; // we're somehow empty already
				BSIP nextIP = nextCheck.get();
				if (nextIP == null) continue; // it's not available anymore.
				if (hardStaged.contains(nextIP.getId())) continue; // we've already staged it.
				
				IPAddress address = nextIP.getIPAddress();
				Integer mask = address.getNetworkPrefixLength();
				if (!(mask == null || mask == (address.isIPv4() ? 32 : 128))) continue; // only cidr-less ips allowed.
				if (mask != null) {
					address = address.getLower(); // strip cidr
				}
				
				Map<String, String> newEntry = new HashMap<String, String>();
				newEntry.put("query", address.toString());
				source.add(newEntry);
				cBatch ++;
			}
			
			if (source.size() == 0) return;
			
			IpData[] replies = null;
			
			GsonBuilder builder = new com.google.gson.GsonBuilder();
			Gson gson = builder.create();
			String data = gson.toJson(source);
			BanStick.getPlugin().debug("Requesting data from ip-data: {0}", data);
			byte[] dataPrep = data.getBytes(StandardCharsets.UTF_8);
			
			URL url = new URL(target);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			
			connection.setRequestMethod("POST");
			connection.setDoOutput(true);
			connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
			connection.setRequestProperty("Content-Length", String.valueOf(dataPrep.length));
			connection.setDoInput(true);
			connection.connect();
			try (OutputStream dataSender = connection.getOutputStream()) {
				dataSender.write(dataPrep);
			}
			try (InputStreamReader dataReply = new InputStreamReader(connection.getInputStream())) {
				replies = gson.fromJson(dataReply, IpData[].class);
			}
			if (replies == null || replies.length == 0) {
				BanStick.getPlugin().warning("IPData periodic batch updater failure, no data received");
				currentFailures ++;
				return;
			}
			
			for (IpData reply : replies) {
				if (reply.getMessage() != null) {
					BanStick.getPlugin().debug("Failure during IPData lookup for {0}: {1}", reply.getQuery(), reply.getMessage());
					continue;
				}
				IPAddressString replyAddress = new IPAddressString(reply.getQuery());
				IPAddress address = replyAddress.getAddress();
				if (address == null) continue;
				BSIP ipMatch = BSIP.byIPAddress(address);
				if (ipMatch == null) continue;
				BSIPData dataMatch = BSIPData.byExactIP(ipMatch);
				String continent = null;
				String domain = null;
				String comment = null;
				String sauce = "IP-API batch";
				float proxy = 0.0f;
				if (dataMatch != null) {
					if (reply.hasChanged(dataMatch)) { // is old outdated?
						dataMatch.invalidate(); // mark that record and make a new one.
						continent = dataMatch.getContinent();
						domain = dataMatch.getDomain();
						comment = dataMatch.getComment();
						proxy = dataMatch.getProxy();
						if (dataMatch.getSource() != null && dataMatch.getSource().contains(sauce)) {
							sauce = dataMatch.getSource();
						} else {
							sauce = dataMatch.getSource() != null ? dataMatch.getSource() + " aug. by IP-API batch" : "IP-API batch";
						}
					} else {
						continue; // just move on, no changes.
					}
				}
				dataMatch = BSIPData.create(ipMatch, continent, reply.getCountry(), reply.getRegionName(), reply.getCity(), 
						reply.getZip(), reply.getLat(), reply.getLon(), domain, reply.getOrg(), reply.getAs(), reply.getIsp(),
						proxy, sauce, comment);
			}
		} catch (MalformedURLException mue) {
			enabled = false;
			BanStick.getPlugin().severe("Failed to connect to malformed IPData check url", mue);
		} catch (IOException ioe) {
			currentFailures ++;
			BanStick.getPlugin().warning("IO Error on IPData update: ", ioe);
		} catch (ClassCastException cce) {
			enabled = false;
			BanStick.getPlugin().severe("Failed to identify connection as http; perm failure", cce);
		}
	}
	
	class IpData {
		private String status;
		private String message;
		private String query;
		private String country;
		private String countryCode;
		private String region;
		private String regionName;
		private String city;
		private String zip;
		private Double lat;
		private Double lon;
		private String timezone;
		private String isp;
		private String org;
		private String as;
		
		public IpData() {}

		public String getStatus() {
			return status;
		}

		public void setStatus(String status) {
			this.status = status;
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}

		public String getQuery() {
			return query;
		}

		public void setQuery(String query) {
			this.query = query;
		}

		public String getCountry() {
			return country;
		}

		public void setCountry(String country) {
			this.country = country;
		}

		public String getCountryCode() {
			return countryCode;
		}

		public void setCountryCode(String countryCode) {
			this.countryCode = countryCode;
		}

		public String getRegion() {
			return region;
		}

		public void setRegion(String region) {
			this.region = region;
		}

		public String getRegionName() {
			return regionName;
		}

		public void setRegionName(String regionName) {
			this.regionName = regionName;
		}

		public String getCity() {
			return city;
		}

		public void setCity(String city) {
			this.city = city;
		}

		public String getZip() {
			return zip;
		}

		public void setZip(String zip) {
			this.zip = zip;
		}

		public Double getLat() {
			return lat;
		}

		public void setLat(Double lat) {
			this.lat = lat;
		}

		public Double getLon() {
			return lon;
		}

		public void setLon(Double lon) {
			this.lon = lon;
		}

		public String getTimezone() {
			return timezone;
		}

		public void setTimezone(String timezone) {
			this.timezone = timezone;
		}

		public String getIsp() {
			return isp;
		}

		public void setIsp(String isp) {
			this.isp = isp;
		}

		public String getOrg() {
			return org;
		}

		public void setOrg(String org) {
			this.org = org;
		}

		public String getAs() {
			return as;
		}

		public void setAs(String as) {
			this.as = as;
		}
		
		public boolean hasChanged(BSIPData data) {
			if (isEqual(data.getCountry(), this.country) &&
				isEqual(data.getRegion(), this.regionName) &&
				isEqual(data.getCity(), this.city) &&
				isEqual(data.getPostal(), this.zip) &&
				isEqual(data.getLat(), this.lat) &&
				isEqual(data.getLon(), this.lon) &&
				isEqual(data.getConnection(), this.isp) &&
				isEqual(data.getProvider(), this.org) &&
				isEqual(data.getRegisteredAs(), this.as)) {
				return false;
			}
			return true;
		}
		
		private boolean isEqual(Object a, Object b) {
			if (a == null && b == null) return true;
			if (a != null) return a.equals(b);
			return b.equals(a);
		}
	}
}
