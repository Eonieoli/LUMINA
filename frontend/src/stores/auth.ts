import { create } from 'zustand';


interface authData {
    userId: number;
    nickname: string;
    profileImage: string;
    message: string;
    positiveness: number;
    grade: number;
    rank: number;
    postCnt: number;
    followerCnt: number;
    followingCnt: number;
}

interface auth {
    data: authData;
    setData: (newData: authData) => void;
    resetData: () => void;
}


export const useAuthStore = create<auth>((set) => {
    const initialData: authData = {
        userId: -1,
        nickname: '',
        profileImage: '',
        message: '',
        positiveness: -1,
        grade: -1,
        rank: -1,
        postCnt: -1,
        followerCnt: -1,
        followingCnt: -1
    }
    return {
        data: initialData,
        setData: (data: authData) => set({ data }),
        resetData: () => set(() => ({
            data: initialData
        }))
    }
})