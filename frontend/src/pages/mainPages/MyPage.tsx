import { getMyProfile } from "@/apis/auth";
import { useEffect, useState } from "react";
import { Hamburger, DefaultProfile } from "@/assets/images";
import UserProfileFourInfo from "@/components/profile/fourInfo";
import ProfileBtn from "@/components/profile/button";

interface MypageProps {
    userId: number
    nickname: string
    profileImage: string
    message: string
    positiveness: number
    grade: number
    rank: number
    postCnt: number
    followCnt: number
    followingCnt: number
}

export default function MyPage() {

    const [userInfo, setUserInfo] = useState<MypageProps>()

    useEffect(() => {
        const fetchMyProfile= async () =>  {
            try {
                const response = await getMyProfile()
                setUserInfo(response.data)
                console.log("나의 정보!" , response.data)
                return response.data
            }
            catch (error) {}
        }
        fetchMyProfile()
    }, [])

    return (
        <div className="p-6 h-full">

            {/* 상단 */}
            <div className="flex items-center justify-between h-8 mb-4">
                <p className="text-xl font-semibold">{userInfo?.nickname}</p>
                <img src={Hamburger} alt="햄버거버튼" className="h-4" />
            </div>

            {/* 프로필, 이름, 상태메세지 */}
            <div className="flex flex-col items-center gap-1 mb-4">
                <img 
                    src={userInfo?.profileImage ?? DefaultProfile} alt="프로필 이미지"
                    className="w-25 bg-white rounded-full"
                />
                <p className="font-semibold text-[17px]">{userInfo?.nickname}</p>
                <p className="text-[14px]">{userInfo?.message}</p>
            </div>

            {/* 게시물, 팔로워, 팔로잉, lu */}
            <div className="flex justify-evenly mb-4">
                <UserProfileFourInfo title="게시물" titleNumber={userInfo?.postCnt ?? 0}/>
                <UserProfileFourInfo title="팔로워" titleNumber={userInfo?.followCnt ?? 0}/>
                <UserProfileFourInfo title="팔로잉" titleNumber={userInfo?.followingCnt ?? 0}/>
                <UserProfileFourInfo title="선행도" titleNumber={userInfo?.positiveness ?? 0}/>
            </div>

            {/* 버튼 */}
            <div className="flex justify-center">
                <ProfileBtn text="프로필 수정" textColor="text-gray-600" bgColor="bg-white" />
            </div>

            {/* 루미나 선행도 그래프 */}

            {/* 나의 게시물 */}
            

        </div>
    );
}
