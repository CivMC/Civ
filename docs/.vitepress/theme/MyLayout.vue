<script setup>
import DefaultTheme from 'vitepress/theme'

const { Layout } = DefaultTheme
</script>

<!-- TODO: Add cover images and such to match original civmc.net -->
<template>
    <Layout>
        <template #home-hero-image>
            <img src="./logo.png"  alt="Logo" width="50%"/>
        </template>

        <template #layout-bottom >
            <div class="layout-bottom">
                <h1 class="copyright">Copyright © 2025 - CivMC</h1>
            </div>
        </template>
    </Layout>
</template>

<style>
.layout-bottom {
    width: 100%;
    text-align: center;
    padding-bottom: 1em;
}

.copyright {
    line-height: 24px;
    font-size: 18px;
    font-weight: 400;
    color: var(--vp-c-text-2);
}
</style>
