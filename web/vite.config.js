import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';
import { fileURLToPath, URL } from 'node:url';

// https://vitejs.dev/config/
export default defineConfig({
    plugins: [vue()],
    // 静态资源输出目录，与旧 vue.config.js 的 assetsDir: 'static' 保持一致
    build: {
        assetsDir: 'static',
        sourcemap: false,
        // 构建时不清空输出目录（避免在受限环境下批量删除 dist 触发保护机制）。
        // 首次构建或需要干净产物时，可手动删除 dist 目录后再构建。
        emptyOutDir: false
    },
    // 相对路径，与旧 publicPath: './' 保持一致
    base: './',
    resolve: {
        alias: {
            '@': fileURLToPath(new URL('./src', import.meta.url))
        }
    },
    server: {
        host: '127.0.0.1',
        port: 8080,
        open: false,
        proxy: {
            '/api': {
                target: 'http://127.0.0.1:8081',
                changeOrigin: true,
                ws: true,
                rewrite: path => path.replace(/^\/api/, '')
            }
        }
    }
});
