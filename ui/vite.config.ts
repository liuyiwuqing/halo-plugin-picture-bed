import {fileURLToPath, URL} from "url";

import {defineConfig} from "vite";
import Vue from "@vitejs/plugin-vue";
import VueJsx from "@vitejs/plugin-vue-jsx";
import Icons from "unplugin-icons/vite";
import {HaloUIPluginBundlerKit} from "@halo-dev/ui-plugin-bundler-kit";
import IconsResolver from "unplugin-icons/resolver";
import Components from "unplugin-vue-components/vite";
import AutoImport from 'unplugin-auto-import/vite'

export default defineConfig({
  plugins: [
    Vue(),
    VueJsx(),
      Icons({
          compiler: "vue3",
          autoInstall: true,
      }),
      Components({
          resolvers: [
              IconsResolver({
                  prefix: "",
              }),
          ],
      }),
      AutoImport({
          imports: [
              'vue',
              'vue-router',
              '@vueuse/core'
          ],
          dts: './auto-imports.d.ts'
      }),
    HaloUIPluginBundlerKit(),
  ],
  resolve: {
    alias: {
      "@": fileURLToPath(new URL("./src", import.meta.url)),
    },
  },
});
