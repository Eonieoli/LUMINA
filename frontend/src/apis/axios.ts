import axios from "axios";

export const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_URL, // API의 기본 URL을 설정하세요.
  withCredentials: true,
});
