# CivModCore

CivModCore is derived from Humbug.

----

## Versions

* 2.0.0 - Paper 1.18.1 - This version onwards requires gradle

* 1.9.0 - Paper 1.17.1

* 1.8.4 - Paper 1.16.5

* [1.8.0](https://github.com/CivClassic/CivModCore/tree/08ad95297eb041cf99bd0eb0aaffc70ca87af4f2) - Paper 1.16.1

* [1.7.9](https://github.com/CivClassic/CivModCore/tree/306b4f7268a3c5d3bd551fe66992f2a4335e86f7) - Spigot 1.14.4

* [1.6.1](https://github.com/CivClassic/CivModCore/tree/8d1043b7ad4bcf3ffe30d87ee5e974f1dd111113) - Spigot 1.12.2 (Mercury Removed -- incompatible with plugins that rely on Mercury hooks)

* [1.5.11](https://github.com/CivClassic/CivModCore/tree/d88d6bbcf231616dc1c7bc08a3fabc0f57911613) - Spigot 1.11.x

* [1.5.9](https://github.com/CivClassic/CivModCore/tree/a55880dd11bee3612f5aa842412119775b3bcb91) - Spigot 1.10.x

No explicit backwards support is offered to any previous version, whether it be major, minor, or patch.

----

##How to compile with Gradle

To compile CivModCore, you need JDK 17.

Clone this repo, run `./gradlew reobfJar` to create your jar, You can find the compiled jar in the paper `build/libs` directory.

To get a full list of tasks, run ./gradlew tasks.

----

## Usage

To take full advantage of CivModCore, you should have your plugin class extend `ACivMod`, like such:

    public class MyNewPlugin extends ACivMod {
    
        @Override
        public void onEnable() {
            // Always have this at the top of the function
            super.onEnable();
            
            // Then do your stuff here that you need to do.
        }
    
        @Override
        public void onDisable() {
            // Do whatever you need to do here
        
            // Try to keep this at the bottom of the function
            super.onDisable();
        }
    
    }

For more information, look through the [CivTemplate plugin](https://github.com/CivClassic/CivTemplate/blob/master/src/main/java/io/protonull/template/TemplatePlugin.java).

----

## Dependency

Include the following in your dependency list in your plugin's POM file:

    <dependency>
        <groupId>net.civmc</groupId>
        <artifactId>CivModCore</artifactId>
        <version>2.0.0-SNAPSHOT</version>
        <scope>provided</scope>
    </dependency>

For Gradle include the following line inside your dependencies code block:

	implementation("net.civmc:civmodcore:2.0.0-SNAPSHOT:dev-all")
