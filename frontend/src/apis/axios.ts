import axios from 'axios';

// export const apiClient = axios.create({
//   baseURL: import.meta.env.VITE_API_URL, // API의 기본 URL을 설정하세요.
//   withCredentials: true,
// });

// 개발 전용 깃에 절대 올리지 말기
const token =
    'eyJhbGciOiJIUzI1NiJ9.eyJjYXRlZ29yeSI6ImFjY2VzcyIsIm5pY2tuYW1lIjoi7ZmN7ISd7KeEX2tha2FvNyIsInJvbGUiOiJST0xFX1VTRVIiLCJpYXQiOjE3NDYxNDQ5MDQsImV4cCI6MTc0NjIzMTMwNH0.3hsqj0eJP2JcA7WdYtkh109aPNBgAeXtK5H2Pob5wYo';

export const apiClient = axios.create({
    baseURL: import.meta.env.VITE_API_URL, // API의 기본 URL을 설정하세요.
    withCredentials: true,
    headers: {
        Authorization: `Bearer ${token}`,
    },
});
