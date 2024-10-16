package com.programmerdan.minecraft.banstick.scraper;

import com.programmerdan.minecraft.banstick.BanStick;
import com.programmerdan.minecraft.banstick.data.BSBan;
import com.programmerdan.minecraft.banstick.data.BSIP;
import com.programmerdan.minecraft.banstick.data.BSIPData;
import com.programmerdan.minecraft.banstick.handler.ScraperWorker;
import inet.ipaddr.IPAddress;
import inet.ipaddr.IPAddressString;
import inet.ipaddr.IPAddressStringException;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import org.bukkit.configuration.ConfigurationSection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * There are a number of free proxy lists we'd found. Some may no longer
 * be valid. This scrapes those.
 * 
 * @author <a href="mailto:programmerdan@gmail.com">ProgrammerDan</a>
 *
 */
public class ScrapeFreeProxyList extends ScraperWorker {

	private String banMessage;
	private long banLength;
	
	private String[] urls = new String[] {
			"https://free-proxy-list.net",
			"http://www.sslproxies.org/",
			"http://www.us-proxy.org/",
			"http://free-proxy-list.net/uk-proxy.html",
			"http://www.socks-proxy.net/",
			"http://free-proxy-list.net/anonymous-proxy.html"
	};
	
	public ScrapeFreeProxyList(ConfigurationSection config) {
		super(config);
	}

	@Override
	public void setup(ConfigurationSection config) {
		banMessage = config.getString("ban.message");
		banLength = config.getLong("ban.length");
	}

	@Override
	public String name() {
		return "freeproxylist";
	}

	@Override
	public void scrape() {
		for (String url : urls) {
			scrapeOne(url);
		}
	}
	
	/**
	 * Does the work of "scraping" a single passed url.
	 * @param url
	 */
	public void scrapeOne(String url) {
		try {
			BanStick.getPlugin().debug("Scraping {0}", url);
			Document doc = Jsoup.connect(url).userAgent(
					"Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:24.0) Gecko/20100101 Firefox/24.0").get();
			Elements iptable = doc.select("table#proxylisttable");
			
			Elements body = iptable.select("tbody");
			Elements trs = body.select("tr");
			BanStick.getPlugin().debug("Found {0} proxy IPs to scrape", trs.size());
			for (Element tr : trs) {
				Elements tds = tr.select("td");
				String ip = null;
				try {
					ip = tds.first().text();
				} catch (Exception e) {
					continue;
				}
				String country = null;
				try {
					country = tds.get(3).text();
				} catch (Exception e) {
					// intentionally ignore errors.
				}
				String port = null;
				try {
					port = tds.get(1).text();
				} catch (Exception e) {
					// intentionally ignore errors.
				}
				try {
					IPAddressString addressS = new IPAddressString(ip);
					addressS.validate();
					IPAddress address = addressS.toAddress();
					BSIP found = BSIP.byIPAddress(address);
					if (found == null) {
						found = BSIP.create(address);
					}
					BSIPData dataMatch = BSIPData.byExactIP(found);
					if (dataMatch == null) {
						dataMatch = BSIPData.create(found, null, country, null, null, 
								null, null, null, null, null, null, null,
								3.0f, name() + " " + url, "Anon Proxy on Port: " + port);
					}
					// autoban.
					if (banLength > -1) {
						boolean wasmatch = false;

						List<BSBan> ban = BSBan.byProxy(dataMatch, true);
						if (!(ban == null || ban.isEmpty())) {
							// look for match; if unexpired, extend.
							for (int i = ban.size() - 1 ; i >= 0; i--) {
								BSBan pickOne = ban.get(i);
								if (pickOne.isAdminBan()) {
									continue; // skip admin entered bans.
								}
								if (pickOne.getBanEndTime() != null && pickOne.getBanEndTime().after(new Date())) {
									if (this.banLength < 0) {
										pickOne.clearBanEndTime();
									} else {
										pickOne.setBanEndTime(new Date(System.currentTimeMillis() + this.banLength));
									}
									wasmatch = true;
									break;
								}
							}
						}
						if (!wasmatch) {
							BSBan.create(dataMatch, this.banMessage, this.banLength < 0 ? null 
									: new Date(System.currentTimeMillis() + this.banLength), false);
						}
					}
				} catch (IPAddressStringException iase) {
					continue;
				}
			}
		} catch (IOException ioe) {
			BanStick.getPlugin().warning("Failure during scrape of " + url, ioe);
			this.registerError();
		}
	}

}
