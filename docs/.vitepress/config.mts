import { defineConfig } from "vitepress";

// should roughly match the tag line used on the main site page
const DESCRIPTION = "A Minecraft server focused on civilization building";

const SITE_URL = "https://civmc.net";
const SITE_LOGO = SITE_URL + "/logo.png";

const GITHUB_ORG = "https://github.com/CivMC";
const DISCORD_LINK = "https://discord.gg/HkD79GfmQQ";
const REDDIT = "https://www.reddit.com/r/CivMC/";
const PATREON = "https://www.patreon.com/Civ_MC";

// https://vitepress.dev/reference/site-config
export default defineConfig({
    lang: "en-US",
    title: "CivMC",
    description: DESCRIPTION,

    sitemap: {
        hostname: SITE_URL,
        lastmodDateOnly: false,
        xmlns: {
            // trim the xml namespace
            news: false,
            video: false,
            xhtml: true,
            image: false,
        },
    },

    head: [
        // favicon stuff
        [
            "link",
            {
                rel: "apple-touch-icon",
                sizes: "180x180",
                href: "/apple-touch-icon.png",
                type: "image/png",
            },
        ],
        [
            "link",
            {
                rel: "icon",
                type: "image/png",
                sizes: "32x32",
                href: "/favicon-32x32.png",
            },
        ],
        [
            "link",
            {
                rel: "icon",
                type: "image/png",
                sizes: "16x16",
                href: "/favicon-16x16.png",
            },
        ],
        [
            "link",
            {
                rel: "manifest",
                href: "/site.webmanifest",
            },
        ],

        // used by search engines
        [
            "meta",
            {
                name: "keywords",
                content:
                    "Civ, CivCraft, CivMC, Minecraft, Wiki, Roleplay, Role-playing",
            },
        ],

        // open graph
        ["meta", { property: "og:type", content: "website" }],
        ["meta", { property: "og:title", content: DESCRIPTION }],
        ["meta", { property: "og:image", content: SITE_LOGO }],
        [
            "meta",
            {
                property: "og:description",
                content: DESCRIPTION,
            },
        ],
        ["meta", { property: "og:url", content: SITE_URL }],
        // twitter's og because special
        [
            "meta",
            {
                name: "twitter:description",
                content: DESCRIPTION,
            },
        ],
        ["meta", { name: "twitter:title", content: "CivMC" }],
        ["meta", { name: "twitter:card", content: "summary_large_image" }],
        ["meta", { name: "twitter:image", content: SITE_LOGO }],
        ["meta", { name: "twitter:url", content: SITE_URL }],
    ],

    themeConfig: {
        search: {
            provider: "local",
        },

        // enables showing the last time the page was updated
        lastUpdated: {},

        editLink: {
            pattern: GITHUB_ORG + "/Civ/edit/main/docs/:path",
        },

        nav: [
            { text: "Home", link: "/" },
            { text: "Wiki", link: "/wiki/" },
            { text: "Developer Docs", link: "/dev/" },
        ],

        sidebar: {
            "/wiki/": [
                {
                    text: "Wiki",
                    items: [
                        {
                            text: "New Player Guide",
                            link: "/wiki/new-player-guide",
                        },
                        { text: "Server Overview", link: "/wiki/index" },
                        { text: "Change Log", link: "/wiki/changelog" },
                        {
                            text: "Plugins",
                            items: [
                                {
                                    text: "Essential",
                                    link: "wiki/plugins/essential/index",
                                    collapsed: false,
                                    items: [
                                        {
                                            text: "NameLayer",
                                            link: "wiki/plugins/essential/namelayer",
                                        },
                                        {
                                            text: "Citadel",
                                            link: "wiki/plugins/essential/citadel",
                                        },
                                        {
                                            text: "ExilePearl",
                                            link: "wiki/plugins/essential/exilepearl",
                                        },
                                    ],
                                },
                                {
                                    text: "Unique",
                                    link: "pages/plugins/unique/index",
                                    collapsed: false,
                                    items: [
                                        {
                                            text: "Hiddenore",
                                            link: "wiki/plugins/unique/hiddenore",
                                        },
                                        {
                                            text: "Factorymod",
                                            link: "wiki/plugins/unique/factorymod",
                                        },
                                        {
                                            text: "Finale",
                                            link: "wiki/plugins/unique/finale",
                                        },
                                        {
                                            text: "PvP Server",
                                            link: "wiki/plugins/unique/pvpserver",
                                        },
                                        {
                                            text: "JukeAlert",
                                            link: "wiki/plugins/unique/jukealert",
                                        },
                                        {
                                            text: "Bastions",
                                            link: "wiki/plugins/unique/bastions",
                                        },
                                        {
                                            text: "Realistic Biomes",
                                            link: "wiki/plugins/unique/rb",
                                        },
                                        {
                                            text: "Heliodor",
                                            link: "wiki/plugins/unique/heliodor",
                                        },
                                        {
                                            text: "Transport Changes",
                                            link: "wiki//plugins/unique/Transport",
                                        },
                                        {
                                            text: "Chunk Limits",
                                            link: "wiki/plugins/unique/chunklimits",
                                        },
                                    ],
                                },
                                {
                                    text: "Fun",
                                    link: "wiki/plugins/fun/index",
                                    collapsed: true,
                                    items: [
                                        {
                                            text: "Item Exchange",
                                            link: "wiki/plugins/fun/itemexchange",
                                        },
                                        {
                                            text: "Brewery",
                                            link: "wiki/plugins/fun/brewery",
                                        },
                                        {
                                            text: "EvenMoreFish",
                                            link: "wiki/plugins/fun/evenmorefish",
                                        },
                                        {
                                            text: "Wordbank",
                                            link: "wiki/plugins/fun/wordbank",
                                        },
                                        {
                                            text: "Railswitch",
                                            link: "wiki/plugins/fun/railswitch",
                                        },
                                        {
                                            text: "Elevators",
                                            link: "wiki/plugins/fun/elevators",
                                        },
                                        {
                                            text: "Castlegate",
                                            link: "wiki/plugins/fun/castlegate",
                                        },
                                        {
                                            text: "Arthropod Egg",
                                            link: "wiki/plugins/fun/arthropodegg",
                                        },
                                    ],
                                },
                            ],
                        },
                    ],
                },
            ],
            "/dev/": [
                {
                    text: "Developer Docs",
                    items: [
                        {
                            text: "Getting Started",
                            link: "/dev/getting-started/",
                            items: [
                                {
                                    text: "Project Overview",
                                    link: "/dev/getting-started/projects",
                                },
                                {
                                    text: "Building with Gradle",
                                    link: "/dev/getting-started/building-with-gradle",
                                },
                            ],
                        },
                        {
                            text: "Code Style",
                            link: "/dev/code-style/",
                            items: [
                                {
                                    text: "Java Code Style",
                                    link: "/dev/code-style/java-code-style",
                                },
                            ],
                        },
                        {
                            text: "Plugin Standards",
                            link: "/dev/plugin-standards/",
                            items: [
                                {
                                    text: "Event Handling",
                                    link: "/dev/plugin-standards/event-handling",
                                },
                                {
                                    text: "Permissions",
                                    link: "/dev/plugin-standards/permissions",
                                },
                            ],
                        },
                    ],
                },
            ],
        },

        socialLinks: [
            { icon: "discord", link: DISCORD_LINK, ariaLabel: "CivMC Discord" },
            {
                icon: {
                    svg: '<svg width="32" height="32" viewBox="0 0 24 24"><path fill="currentColor" d="M12 0A12 12 0 0 0 0 12a12 12 0 0 0 12 12a12 12 0 0 0 12-12A12 12 0 0 0 12 0zm5.01 4.744c.688 0 1.25.561 1.25 1.249a1.25 1.25 0 0 1-2.498.056l-2.597-.547l-.8 3.747c1.824.07 3.48.632 4.674 1.488c.308-.309.73-.491 1.207-.491c.968 0 1.754.786 1.754 1.754c0 .716-.435 1.333-1.01 1.614a3.111 3.111 0 0 1 .042.52c0 2.694-3.13 4.87-7.004 4.87c-3.874 0-7.004-2.176-7.004-4.87c0-.183.015-.366.043-.534A1.748 1.748 0 0 1 4.028 12c0-.968.786-1.754 1.754-1.754c.463 0 .898.196 1.207.49c1.207-.883 2.878-1.43 4.744-1.487l.885-4.182a.342.342 0 0 1 .14-.197a.35.35 0 0 1 .238-.042l2.906.617a1.214 1.214 0 0 1 1.108-.701zM9.25 12C8.561 12 8 12.562 8 13.25c0 .687.561 1.248 1.25 1.248c.687 0 1.248-.561 1.248-1.249c0-.688-.561-1.249-1.249-1.249zm5.5 0c-.687 0-1.248.561-1.248 1.25c0 .687.561 1.248 1.249 1.248c.688 0 1.249-.561 1.249-1.249c0-.687-.562-1.249-1.25-1.249zm-5.466 3.99a.327.327 0 0 0-.231.094a.33.33 0 0 0 0 .463c.842.842 2.484.913 2.961.913c.477 0 2.105-.056 2.961-.913a.361.361 0 0 0 .029-.463a.33.33 0 0 0-.464 0c-.547.533-1.684.73-2.512.73c-.828 0-1.979-.196-2.512-.73a.326.326 0 0 0-.232-.095z"/></svg>',
                },
                link: REDDIT,
                ariaLabel: "CivMC SubReddit",
            },
            { icon: "github", link: GITHUB_ORG, ariaLabel: "CivMC GitHub" },
            { icon: "patreon", link: PATREON, ariaLabel: "CivMC Patreon" },
        ],

        footer: {
            copyright: "Copyright © 2023-present CivMC",
        },
    },
});
