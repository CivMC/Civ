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
                text: 'Wiki',
                items: [
                    {text: 'Index', link: '/wiki/'},
                ]
            }
        ],
        '/dev/': [
            {
                text: 'Developer Docs',
                items: [
                    {text: 'Index', link: '/dev/'},
                ]
            }
        ],
    },

    socialLinks: [
      { icon: 'github', link: 'https://github.com/CivMC' }
    ]
  }
})
