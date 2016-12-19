Tired of those pesky VPNers and VPSers?

Me too.

Ban 'em, and share those bans with other participating servers (eventually).

Supports range-bans, auto-VPN flagging, and other forms of IP identification and more.

=== How to

**Ban a range** Use /banstick 12.1.0.0/16

**Ban a user and their range** Use /banstick xXsickVPNsXx /16 (can also use UUID)

**Ban specific IP** Use /banstick 111.11.11.1

**Ban specific user** Use /banstick xXsickVPNsXx

**Check for VPN info on a user** Use /lovetap xXsickVPNsXx (can also use UUID)

**Check for found flags** Use /lovetaps

**View user IP history** Use /taphistory xXsickVPNsXx (can also use UUID)

**Check for users sharing connection** Use /sharedlove xXsickVPNsXx

**Check for users sharing a CIDR range** Use /sharedlove xXsickVPNsXx/16 or 12.1.0.0/16 (or use UUID)

**Forgive a user** Use /forgive xXsickVPNsXx VPN or SHARED

=== Compiling

This uses the IPAddress library: https://seancfoley.github.io/IPAddress/

As of this writing it lacks a Maven resource, so I'm installing it manually like so:

mvn install:install-file -Dfile=IPAddress.jar -Dpackaging=jar -DgroupId=inet.ipaddr -DartifactId=IPAddress -Dversion=1.0.0