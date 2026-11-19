import react from '@vitejs/plugin-react';
import { defineConfig } from 'vite';

export default defineConfig({
  plugins: [react()],
  server: {
    port: 8080,
    strictPort: true,
    host: true,
    allowedHosts: ['greasily-rockband-attention.ngrok-free.dev'],
  },
  preview: {
    port: 8080,
    strictPort: true,
    host: true,
    allowedHosts: ['greasily-rockband-attention.ngrok-free.dev'],
  },
});