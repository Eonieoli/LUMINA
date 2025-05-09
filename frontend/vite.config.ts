import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import tailwindcss from "@tailwindcss/vite";
import compression from 'vite-plugin-compression';
import { fileURLToPath } from 'url';


// https://vite.dev/config/
export default defineConfig({
  plugins: [react(),
    compression({
      algorithm: 'gzip',
      ext: '.gz',
      threshold: 10240, // 최소 압축 크기 (bytes), 기본 10KB
      deleteOriginFile: false, // true로 설정하면 원본 파일 삭제됨
    }),
    tailwindcss()
  ],
  build: {
    rollupOptions: {
      output: {
        manualChunks: {
          react: ['react', 'react-dom'],
          router: ['react-router-dom'],
          animation: ['framer-motion', 'react-transition-group'],
          utils: ['zustand', 'axios'],
          ui: ['swiper', 'sonner'],
        },
      },
    },
    commonjsOptions: {
      ignoreTryCatch: [
        // Ignore Tailwind's optional native module loading
        '@tailwindcss/oxide',
      ],
    },
  },
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },

});
