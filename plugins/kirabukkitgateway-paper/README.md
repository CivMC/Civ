# Exemplum

Dummy plugin for the development of civcraft plugins that takes away some of the most common steps which have to be taken initially every time a new plugin is made

---

##How to use

1. Clone this repository

2. Import into an IDE of your choice

3. Rename (via refactor) the package "PLUGIN_NAME_REPLACE_ME" to something appropriate, usually the plugin name is best here

4. Rename (via refactor) MAIN_PLUGIN_CLASS to something unique, like the name of your plugin or at least something that resembles that this will be the main plugin class

5. Edit artifactId, name and url in the pom.xml. Usually filling in the name of your plugin for all of those should be enough

6. Open up the plugin.yml, which can be found at src/main/resources/plugin.yml, add yourself as author and adjust the name and path of your main plugin class, which you decided in step 2 and 3

7. Optionally rename the CustomCommandHandler in the commands package to something unique to avoid conflicts with other plugins using this template
