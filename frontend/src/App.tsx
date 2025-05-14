import './App.css';
import Main from './pages/Main';
import Auth from './pages/Auth';
import Admin from './pages/Admin';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { ScrollToTop, ProtectedRoute, CheckAdmin } from './components';

function App() {
    return (
        <BrowserRouter>
            <ScrollToTop />
            <Routes>
                <Route path="/login" element={<Auth />} />
                <Route element={<ProtectedRoute />}>
                    <Route path="/*" element={<Main />} />
                </Route>
                <Route element={<CheckAdmin />}>
                    <Route path='/admin' element={<Admin />} />
                </Route>
            </Routes>
        </BrowserRouter>
    );
}

export default App;
