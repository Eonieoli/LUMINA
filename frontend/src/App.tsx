import './App.css';
import Main from './pages/Main';
import Auth from './pages/Auth';
import Admin from './pages/Admin';
import { Routes, Route, useLocation } from 'react-router-dom';
import { ScrollToTop, CheckAdmin, GAListener } from './components';
import { useEffect } from 'react';
import { pageView } from './utils/analytics';

function App() {
    const location = useLocation();

    const routeTitleMap: Record<string, string> = {
    '/': '홈',
    '/login': '로그인',
    '/admin': '관리자',
    '/search' : '탐색',
    '/donate' : '기부',
    '/ranking' : '랭킹',
    '/mypage' : '마이페이지'
    };

    const getBasePath = (path: string): string => {
        const segments = path.split('/').filter(Boolean); // ['', 'mypage', '23'] → ['mypage', '23']
        return '/' + (segments[0] ?? ''); // → '/mypage'
    };
    
    useEffect(() => {
        const path = location.pathname + location.search
        const basePath = getBasePath(location.pathname);
        const title = routeTitleMap[basePath] ?? document.title;
        
      pageView(path, title);
    }, [location]);
    
    return (
            <GAListener>
                <ScrollToTop />
                <Routes>
                    <Route path="/login" element={<Auth />} />
                        <Route path="/*" element={<Main />} />
                    <Route element={<CheckAdmin />}>
                        <Route path='/admin' element={<Admin />} />
                    </Route>
                </Routes>
            </GAListener>
    );
}

export default App;
