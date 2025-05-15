import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import './index.css';
import App from './App.tsx';
import { Toaster } from 'sonner';
import { initGA } from './utils/analytics';
import { BrowserRouter } from 'react-router-dom';

initGA();

createRoot(document.getElementById('root')!).render(
    <StrictMode>
        <Toaster richColors position='top-right' />
        <BrowserRouter>
            <App />
        </BrowserRouter>
    </StrictMode>
);
