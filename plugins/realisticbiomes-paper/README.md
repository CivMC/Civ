RealisticBiomes
===============

![Illustration](http://i.imgur.com/sInZHEN.jpg)

## Changes with 1.16.5
RealisticBiomes underwent a refactor and partial conversion to 1.16.5. It does not currently support controlling animal reproduction and no longer supports altering fishing loot tables. 

Cactus, melons, pumpkins, sugarcane were previously non-persistent crops (you must load their chunk for them to grow). The update to 1.16.5 made them persistent crops so that when you leave and re-enter a chunk, they will match the growth that would have occurred in that time. However this introduced an issue with cactus causing immense server-crippling lag. Therefore it is currently recommended to not enable persistent Cactus growth (AKA leave cactus growth totally vanilla). 

Partially supports greenhouse growth but only with sources of light directly adjacent. 

## Description

Fully configurable plant growth based on biome or biome category. Disallows plant growth without skylight, but allows configuring greenhouse growth for configured light sources. Allows configuring of increased growth rates based on blocks placed below the crop soil. 

Limits plant growth and animal reproduction to their naturally occuring biomes, or reasonable ones for plants that do not occur naturally. Growth is persistent, so plants grow even when their chunk is not loaded. 

See [this spreadsheet](https://devotedmc.github.io/RealisticBiomes/spreadsheet/) for the default configuration of the different plants, animals and actions. 

See [the wiki](https://github.com/DevotedMC/RealisticBiomes/wiki) for in depth explanation of the mechanics.
