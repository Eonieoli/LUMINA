// src/components/ProtectedRoute.tsx
import { useEffect, useState } from 'react';
import { Navigate, Outlet, useLocation } from 'react-router-dom';
import { useAuthStore } from '@/stores/auth';
import { getMyProfile } from '@/apis/auth';

export default function ProtectedRoute() {
    const [isAuthenticated, setIsAuthenticated] = useState<boolean | null>(
        null
    );
    const location = useLocation();

    const authData = useAuthStore();

    useEffect(() => {
        const fetchProfile = async () => {
            if (authData.data.userId === -1) {
                try {
                    const response = await getMyProfile();
                    authData.setData(response.data);
                    setIsAuthenticated(true);
                } catch (error) {
                    console.error('프로필 불러오기 실패:', error);
                    setIsAuthenticated(false);
                }
            } else {
                setIsAuthenticated(true);
            }
        };

        fetchProfile();
    }, [authData]);

    if (isAuthenticated === null) return <div>사용자 정보 로딩 중...</div>;

    return isAuthenticated ? (
        <Outlet />
    ) : (
        <Navigate to="/login" state={{ from: location }} replace />
    );
}
