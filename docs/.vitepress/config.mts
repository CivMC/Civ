import { defineConfig } from 'vitepress'

// https://vitepress.dev/reference/site-config
export default defineConfig({
  title: "CivMC",
  description: "CivMC",
  themeConfig: {
      search: {
          provider: 'local'
      },

    nav: [
      { text: 'Home', link: '/' },
      { text: 'Wiki', link: '/wiki/' },
      { text: 'Developer Docs', link: '/dev/' },
    ],

    sidebar: {
        '/wiki/': [
            {
                text: "Wiki",
                    items: [
                        { text: "New Player Guide", link: "/wiki/new-player-guide" },
                        { text: "Server Overview", link: "/wiki/index" },
                        { text: "Change Log", link: "/wiki/changelog" },
                        {
                            text: "Plugins",
                            items: [
                                {
                                    text: "Essential",
                                    link: "wiki/plugins/essential/",
                                    collapsed: false,
                                    items: [
                                        { text: "NameLayer", link: "wiki/plugins/essential/namelayer" },
                                        { text: "Citadel", link: "wiki/plugins/essential/citadel" },
                                        {
                                            text: "ExilePearl",
                                            link: "wiki/plugins/essential/exilepearl",
                                        },
                                    ],
                                },
                                {
                                    text: "Unique",
                                    link: "pages/plugins/unique/",
                                    collapsed: false,
                                    items: [
                                        { text: "Hiddenore", link: "wiki/plugins/unique/hiddenore" },
                                        { text: "Factorymod", link: "wiki/plugins/unique/factorymod" },
                                        { text: "Finale", link: "wiki/plugins/unique/finale" },
                                        { text: "PvP Server", link: "wiki/plugins/unique/pvpserver" },
                                        { text: "JukeAlert", link: "wiki/plugins/unique/jukealert" },
                                        { text: "Bastions", link: "wiki/plugins/unique/bastions" },
                                        { text: "Realistic Biomes", link: "wiki/plugins/unique/rb" },
                                        { text: "Heliodor", link: "wiki/plugins/unique/heliodor" },
                                        { text: "Transport Changes", link: "wiki//plugins/unique/Transport" },
                                        { text: "Chunk Limits",link: "wiki/plugins/unique/chunklimits" },
                                    ],
                                },
                                {
                                    text: "Fun",
                                    link: "wiki/plugins/fun/",
                                    collapsed: true,
                                    items: [
                                        { text: "Item Exchange", link: "wiki/plugins/fun/itemexchange" },
                                        { text: "Brewery", link: "wiki/plugins/fun/brewery" },
                                        { text: "EvenMoreFish", link: "wiki/plugins/fun/evenmorefish" },
                                        { text: "Wordbank", link: "wiki/plugins/fun/wordbank" },
                                        { text: "Railswitch", link: "wiki/plugins/fun/railswitch" },
                                        { text: "Elevators", link: "wiki/plugins/fun/elevators" },
                                        { text: "Castlegate", link: "wiki/plugins/fun/castlegate" },
                                        { text: "Arthropod Egg", link: "wiki/plugins/fun/arthropodegg" },
                                    ],
                                },
                            ],
                        },
                    ]
            }
        ],
        '/dev/': [
            {
                text: 'Developer Docs',
                items: [
                    {
                        text: "Getting Started",
                        link: '/dev/getting-started/',
                        items: [
                            { text: 'Project Overview', link: '/dev/getting-started/projects' },
                            { text: 'Building with Gradle', link: '/dev/getting-started/building-with-gradle' },
                        ]
                    },
                    {
                        text: "Code Style",
                        link: '/dev/code-style/',
                        items: [
                            { text: 'Java Code Style', link: '/dev/code-style/java-code-style' },
                        ]
                    },
                    {
                        text: "Plugin Standards",
                        link: '/dev/plugin-standards/',
                        items: [
                            { text: 'Event Handling', link: '/dev/plugin-standards/event-handling' },
                            { text: 'Permissions', link: '/dev/plugin-standards/permissions' },
                        ],
                    },
                ]
            }
        ],
    },

    socialLinks: [
      { icon: 'github', link: 'https://github.com/CivMC' }
    ]
  }
})
