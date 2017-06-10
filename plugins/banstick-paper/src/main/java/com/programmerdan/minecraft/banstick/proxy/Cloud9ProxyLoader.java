package com.programmerdan.minecraft.banstick.proxy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.bukkit.configuration.ConfigurationSection;

import com.programmerdan.minecraft.banstick.BanStick;
import com.programmerdan.minecraft.banstick.data.BSBan;
import com.programmerdan.minecraft.banstick.data.BSIP;
import com.programmerdan.minecraft.banstick.data.BSIPData;
import com.programmerdan.minecraft.banstick.handler.ProxyLoader;

import com.google.common.math.BigIntegerMath;

import inet.ipaddr.IPAddress;
import inet.ipaddr.IPAddressString;

public final class Cloud9ProxyLoader extends ProxyLoader {

	private String loadUrl;
	private float proxyScore;
	private boolean autoBan;
	private long banLength;
	private String banMessage;
	
	public Cloud9ProxyLoader(ConfigurationSection config) {
		super(config);
	}
	
	private BigInteger addressToLong(IPAddress convert) {
		BigInteger output = BigInteger.ZERO;
		int offset = 0;
		String[] segments = convert.getSegmentStrings();
		for (int a = segments.length - 1; a >= 0; a --) {
			BigInteger inject = BigInteger.valueOf(Long.valueOf(segments[a]));
			output = output.add(inject.shiftLeft(offset));
			offset += convert.getBitsPerSegment();
		}
		return output;
	}

	@Override
	public void run() {
		try {
			URL connection = new URL(this.loadUrl);
			InputStream readIn = connection.openStream();
			BufferedReader in = new BufferedReader(new InputStreamReader(readIn));
			String line = in.readLine();
			int errors = 0;
			long lines = 0;
			while (line != null) {
				lines ++;
				if (lines % 50 == 0) {
					BanStick.getPlugin().info("Checked {0} entries from Cloud9 so far", lines);
				}
				if (errors > 10) break;
				try {
					ArrayList<IPAddress> cidrFromRange = new ArrayList<IPAddress>();
					String[] fields = line.split(",");
					IPAddressString lowbound = new IPAddressString(fields[0]);
					IPAddressString highbound = new IPAddressString(fields[1]);
					IPAddress lowboundAddr = lowbound.getAddress();
					IPAddress highboundAddr = highbound.getAddress();
					BanStick.getPlugin().debug("Starting with range: {0} vs {1}", lowboundAddr, highboundAddr);
					BigInteger end = addressToLong(highboundAddr);
					BigInteger start = addressToLong(lowboundAddr);
					
					// Borrowed wholesale from http://stackoverflow.com/a/5032908
					// Little hard to explain, but basically, finds the minimal set of CIDRs that fit the range,
					//  using bit math.
					// TODO: Refactor into a utility.
					while (end.compareTo(start) >= 0) {
						try {
							byte[] arr = start.toByteArray();
							if (arr[0] == 0) {
								arr = Arrays.copyOfRange(arr, 1, arr.length);
							}
							lowboundAddr = IPAddress.from(arr);
						} catch (IllegalArgumentException iae) {
							BanStick.getPlugin().debug("Failed on byte array {0}", start.toByteArray());
						}

						int mask = lowboundAddr.isIPv4() ? 32 : 128;
						while (mask > 0) {
							IPAddress maskAddr = lowboundAddr.toSubnet(mask - 1	);
							if ( maskAddr.getLower().compareTo(lowboundAddr) != 0) break;
							mask --;
						}
						int x = BigIntegerMath.log2(end.subtract(start).add(BigInteger.ONE), RoundingMode.FLOOR);
						int maxd = (lowboundAddr.isIPv4() ? 32 : 128) - x;
						if ( mask < maxd) {
							mask = maxd;
						}
						
						BanStick.getPlugin().debug("  Found sub-CIDR: {0}", lowboundAddr.toSubnet(mask).toCanonicalString());
						cidrFromRange.add(lowboundAddr.toSubnet(mask));
						
						BigInteger migrate = BigInteger.valueOf(2).pow((lowboundAddr.isIPv4() ? 32 : 128) - mask);
						start = start.add( migrate );
					}

					String registeredAs = fields[2];
					String domain = (fields.length > 3) ? fields[3] : null;

					for (IPAddress address : cidrFromRange) {
						BSIP found = BSIP.byCIDR(address.getLower().toString(), address.getNetworkPrefixLength());
						if (found == null) {
							found = BSIP.create(address.getLower(), address.getNetworkPrefixLength());
						}
						
						BSIPData data = BSIPData.byExactIP(found);
						if (data == null) {
							data = BSIPData.create(found, null, null, null, null, null, null, null, domain, null, registeredAs, null, proxyScore, "Cloud9 Proxy Loader", null);
						}
						
						if (autoBan) {
							boolean wasmatch = false;
	
							List<BSBan> ban = BSBan.byProxy(data, true);
							if (!(ban == null || ban.size() == 0)) {
								// look for match; if unexpired, extend.
								for (int i = ban.size() - 1 ; i >= 0; i-- ) {
									BSBan pickOne = ban.get(i);
									if (pickOne.isAdminBan()) continue; // skip admin entered bans.
									if (pickOne.getBanEndTime() != null && pickOne.getBanEndTime().after(new Date())) {
										pickOne.setBanEndTime(this.banLength < 0 ? null : new Date(System.currentTimeMillis() + this.banLength));
										wasmatch = true;
										break;
									}
								}
							}
							if (!wasmatch) {
								BSBan.create(data, this.banMessage, 
										this.banLength < 0 ? null : new Date(System.currentTimeMillis() + this.banLength), false);
							}
						}
					}
					
				} catch (Exception e) {
					BanStick.getPlugin().debug("Failed to load: {0} due to {1}", line, e.getMessage());
					BanStick.getPlugin().warning("  Failure:", e);
					e.printStackTrace();
					errors ++;
				}
				line = in.readLine();
			}
			if (errors > 10) {
				BanStick.getPlugin().warning("Cancelled cloud9 load due to too many errors.");
			}
		} catch (IOException  e) {
			BanStick.getPlugin().debug("Failed reading from Cloud9-style list: " + loadUrl);
		}
	}

	@Override
	public void setup(ConfigurationSection config) {
		this.loadUrl = config.getString("url");
		this.proxyScore = (float) config.getDouble("defaultScore", 3.0d);
		this.autoBan = config.isConfigurationSection("ban");
		if (this.autoBan) {
			this.banLength = config.getLong("ban.length", -1l);
			this.banMessage = config.getString("ban.message", null);
		}
	}

	@Override
	public String name() {
		return "cloud9";
	}
}
