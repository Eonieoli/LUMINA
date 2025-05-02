import './App.css';
import Main from './pages/Main';
import Auth from './pages/Auth';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { ScrollToTop, ProtectedRoute } from './components';

function App() {
    return (
        <BrowserRouter>
            <ScrollToTop />
            <Routes>
                <Route path="/auth" element={<Auth />} />
                <Route element={<ProtectedRoute />}>
                    <Route path="/*" element={<Main />} />
                </Route>
            </Routes>
        </BrowserRouter>
    );
}

export default App;
