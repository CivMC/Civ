# SecureBoot
Ensure the server is running with all the necessary plugins to help mitigate a repeat of the [2025 Citadel outage](https://civwiki.org/wiki/2025_Citadel_outage)

## How it works
The SecureBoot plugin checks for the presence of the plugins listed in the configuration by their listed ids. The plugin not only checks if the plugins are loaded upon startup, but that they are also enabled and not ever disabled during it as well. In the event any of the listed plugins are found to be in an invalid state, the plugin will refuse entry to any players not listed as a server operator.
