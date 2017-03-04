#BanStick

Tired of those pesky VPNers and VPSers?

Me too.

Ban 'em, investigate and ban their providers, breath easy again.

--------

#Main features:
* Supports range-bans: Ban players by name, IP address, and subnet -- also ban a player's entire subnet with a single command.
* Supports auto-VPN/VPS flagging: Growing list of vpn/vps range loading for easy vpn / vps proxy flagging. 
* Supports scraping of anon proxies: Scrape a number of anonymous proxy providers in a sustainable interval.
* Supports grabbing of up-to-date Tor exit nodes: Good Tor ban citizen, only grabs and blocks Tor exit notes, configurably.
* Supports automatic grabbing of IP geodata: Uses a batch-request service to never exceed TOS while still providing quality IP geo data.
* Easy expansion: VPN/VPS/Proxy list, HTML Proxy list Scraping, and Tor exit scrapers are easily expanded to include new sources.
* Automatic multi-account support and banning: Set a valid limit of multi-accounts with auto-banning of new accounts that exceed that limit.
* Easily manage existing bans, exemptions by ban class (IP / Proxy / Share)
* Import data from IP-Check (room for expansion to other tools) including preserving pardons / exclusions
* 

How to
====

**Ban a range** Use /banstick 12.1.0.0/16

**Ban a user and their latest range** Use /banstick xXsickVPNsXx/16 (can also use UUID)

**Ban specific IP** Use c/banstick 111.11.11.1

**Ban specific user** Use /banstick xXsickVPNsXx

**Check for Session and Proxy info on a user** Use /lovetap xXsickVPNsXx (can also use UUID)

**Forgive a user** Use /forgive xXsickVPNsXx BAN PROXY IP SHARED

**Forgive a Subnet** Use /forgive 12.1.0.0/16

**Forgive an IP** Use /forgive 111.11.11.1

**Ban a share** Use /doubletap +XxalterxX +XyMyAltsXy

**Revoke a forgiveness** Use /takeitback xXsickVPNsXx PROXY

**Pardon clear a share ban** Use /forgive XxalterxX XyMyAltsXy BAN

Compiling
====

This uses the IPAddress library: https://seancfoley.github.io/IPAddress/

As of this writing it lacks a Maven resource, so I'm installing it manually like so:

mvn install:install-file -Dfile=IPAddress.jar -Dpackaging=jar -DgroupId=inet.ipaddr -DartifactId=IPAddress -Dversion=1.0.0