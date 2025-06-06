// src/components/ProtectedRoute.tsx
import { useEffect, useState } from 'react';
import { Navigate, Outlet, useLocation } from 'react-router-dom';
import { amIAdmin } from '@/apis/admin';

export default function CheckAdmin() {
    const [isAuthenticated, setIsAuthenticated] = useState<boolean | null>(
        null
    );
    const location = useLocation();

    useEffect(() => {
        const fetchIsAdmin = async () => {
            try {
                const response = await amIAdmin();
                if (response.data.role === "ROLE_ADMIN" ) {
                    setIsAuthenticated(true);
                } else {
                    setIsAuthenticated(false);
                }
            } catch {
                setIsAuthenticated(false);
            }
        };

        fetchIsAdmin();
    }, []);

    if (isAuthenticated === null) return <div>관리자 정보 로딩 중...</div>;

    return isAuthenticated ? (
        <Outlet />
    ) : (
        <Navigate to="/login" state={{ from: location }} replace />
    );
}
