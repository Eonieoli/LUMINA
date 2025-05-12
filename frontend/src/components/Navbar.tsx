import { Link, useLocation } from 'react-router-dom';
import {
    HomeIcon,
    SearchIcon,
    DonationIcon,
    RankingIcon,
} from '@/assets/images';
import { useAuthStore} from '@/stores/auth';
import { DefaultProfile } from '@/assets/images';
import { getUserProfile } from "@/apis/auth";
import { useEffect, useState } from 'react';

export default function Navbar() {

    const authData = useAuthStore();
    const location = useLocation()

    const [profileImageFile, setProfileImageFile] = useState<string | null>(null)

    //나의 정보 가져오기
    useEffect(() => {
    const fetchMyData = async () => {
        const response = await getUserProfile(authData.data.userId)
        setProfileImageFile(response.data.profileImage)
    }
    fetchMyData()
    },[location])

    return (
        <nav className="/* 모바일뷰 768px까지 */ /* 웹뷰 768px부터 */ absolute bottom-0 z-40 flex h-20 w-full min-w-80 items-center justify-around border-t border-gray-300 bg-white p-2 md:top-0 md:left-0 md:h-full md:w-20 md:min-w-20 md:flex-col md:items-center md:justify-around md:border-r md:py-10">
            <Link to="/">
                <img
                    src={HomeIcon}
                    alt="Home"
                    className="h-6 w-6 cursor-pointer transition-transform duration-300 hover:scale-120"
                />
            </Link>

            <Link to="/search">
                <img
                    src={SearchIcon}
                    alt="Search"
                    className="h-6 w-6 cursor-pointer transition-transform duration-300 hover:scale-120"
                />
            </Link>

            <Link to="/donate">
                <img
                    src={DonationIcon}
                    alt="Donate"
                    className="h-6 w-8 cursor-pointer transition-transform duration-300 hover:scale-120"
                />
            </Link>

            <Link to="/ranking">
                <img
                    src={RankingIcon}
                    alt="Ranking"
                    className="h-6 w-6 cursor-pointer transition-transform duration-300 hover:scale-120"
                />
            </Link>

            <Link to={`/mypage/${authData.data.userId}`}>
                <img
                    src={profileImageFile || DefaultProfile}
                    alt="Profile"
                    className="object-cover h-8 w-8 cursor-pointer rounded-full transition-transform duration-300 hover:scale-120 border-1 border-[#9C97FA]"
                />
            </Link>
        </nav>
    );
}
