# Donum

A plugin which allows delivering items to a player (whether they are online or offline). You /deliver player and it will open a gui for you to place items in and save the items and the player must /present to open the gui and retrieve the items. When the player logs in they are informed they have presents to retrieve. /deliverdeath will let you give the player their entire inventory at time of death. 

Currently has a few bugs, the indicator of whether you have already given their death inventory back does not work. And sometimes the players do not receive their /present. Donum also includes an inventory dupe checker which compares logout + login inventories which also seems buggy and sometimes fails to track properly, creating false positives. 
