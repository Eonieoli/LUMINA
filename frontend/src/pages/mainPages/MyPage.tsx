import { useEffect, useRef, useState, useCallback} from "react";
import { Hamburger, DefaultProfile, ComputerTypingLuna } from "@/assets/images";
import UserProfileFourInfo from "@/components/profile/fourInfo";
import ProfileBtn from "@/components/profile/button";
import { Post } from "./Home";
import { getUserPosts } from "@/apis/board";
import { useNavigate, useParams } from "react-router-dom";
import { Board, HamburgerSheet } from "@/components";
import { getUserProfile } from "@/apis/auth";
import { useAuthStore } from '@/stores/auth';
import { followToggle } from "@/apis/follow";

interface MypageProps {
    userId: number
    nickname: string
    profileImage: string
    message: string
    positiveness: number
    grade: number
    rank: number
    postCnt: number
    followerCnt: number
    followingCnt: number
    isFollowing: boolean
}

export default function MyPage() {

    const navigate = useNavigate()

    // 나의 아이디 
    const authData = useAuthStore();
    const myUserId = authData.data.userId
    
    // 내가 조회하려는 프로필 유저의  id
    const { userId } = useParams();
    const profileUserId = Number(userId)
    
    //만약 내가 나의 마이페이지를 조회하는거면 true
    const ismyprofile = myUserId===profileUserId ? true : false

    const [userInfo, setUserInfo] = useState<MypageProps>()
    const [userPosts, setUserposts] = useState<Post[]>([])
    const [pageNum, setPageNum] = useState(1);
    const [hasMore, setHasMore] = useState(true);
    const [isLoading, setIsLoading] = useState(false)
    const observer = useRef<IntersectionObserver | null> (null)
    const fetchedOnce = useRef(false)
    const isFollowing = userInfo?.isFollowing

    // 햄버거 열린 상태
    const [isHamburgerOpened, setIsHamburgerOpened] = useState<boolean>(false);


    // 유저 정보 가져오기
    useEffect(() => {

        if(!profileUserId) return

        const fetchProfile= async () =>  {
            const response = await getUserProfile(profileUserId)
            setUserInfo(response.data)
            console.log("나의 아이디", myUserId)
            return response.data
        }
        fetchProfile()
    }, [userId])

    // 유저 게시물 가져오기
    useEffect (() => {
        if(!userInfo) return 

        const fetchPosts = async () => {
            if(isLoading || !hasMore || fetchedOnce.current) return

            fetchedOnce.current = true
            setIsLoading(true)

            try {
                const response = await getUserPosts(userInfo.userId, pageNum)
                if(response.posts.length < 10) {
                    setHasMore(false)
                    console.log("유저 게시물 가져오는중")
                }
                setUserposts((prev) => [...prev, ...response.posts])
            }
            catch(error) {}
            setIsLoading(false)
            fetchedOnce.current = false
        }
        fetchPosts()
    },[pageNum, userInfo])

    // 무한스크롤 
    const lastPostRef = useCallback(
        (node: HTMLDivElement) => {
            if (isLoading) return;
            if (observer.current) observer.current.disconnect();
    
            observer.current = new IntersectionObserver((entries) => {
                if (entries[0].isIntersecting && hasMore) {
                    setPageNum((prev) => prev + 1);
                }
            });
    
            if (node) observer.current.observe(node);
        },
        [isLoading, hasMore]
    );
    
    // 프로필 수정을 눌렀다면
    const goToProfileEdit = () => {
        console.log("프로필 수정버튼 클릭")
    }

    //팔로우 팔로잉 버튼을 눌렀다면
    const handlefollowToggle = async() => {
        await followToggle(profileUserId)
        const response = await getUserProfile(profileUserId)
        setUserInfo(response.data)
    }

    // 팔로워 팔로잉 페이지 버튼을 눌렀다면 
    const goToFollowInfo = (info:string) => {
        console.log(`${profileUserId}`,"의 팔로워를 조회해볼게")
        navigate(`/mypage/${profileUserId}/follow`, {
            state: {nickname: userInfo?.nickname, followers: userInfo?.followerCnt, followings: userInfo?.followingCnt, info}
        })
    }

    // 햄버거 닫기
    const closeHamburger = () => {
        setIsHamburgerOpened(false);
    }

    return (
        <div className="relative w-full h-full bg-white">
            <HamburgerSheet isVisible={isHamburgerOpened} onClose={closeHamburger} />

            {/* 게시물 상단 */}
            <div className="p-6">

                {/* 이름, 햄버거바*/}
                <div className="flex items-center justify-between h-8 mb-4">
                    <p className="text-xl font-semibold">{userInfo?.nickname}</p>
                    <img onClick={() => setIsHamburgerOpened(!isHamburgerOpened)} src={Hamburger} alt="햄버거버튼" className="h-4" />
                </div>

                {/* 프로필 사진, 이름, 상태메세지 */}
                <div className="flex flex-col items-center gap-1 mb-4">
                    <img 
                        src={userInfo?.profileImage ?? DefaultProfile} alt="프로필 이미지"
                        className="w-25 aspect-square object-cover bg-white rounded-full"
                    />
                    <p className="font-semibold text-[17px]">{userInfo?.nickname}</p>
                    <p className="text-[14px]">{userInfo?.message}</p>
                </div>

                {/* 게시물, 팔로워, 팔로잉, lu */}
                <div className="flex justify-evenly mb-4">
                    <UserProfileFourInfo title="게시물" titleNumber={userInfo?.postCnt ?? 0} isBtn={false}/>
                    <UserProfileFourInfo title="팔로워" titleNumber={userInfo?.followerCnt ?? 0 } isBtn={true} onClick={()=> goToFollowInfo("followers")} />
                    <UserProfileFourInfo title="팔로잉" titleNumber={userInfo?.followingCnt ?? 0} isBtn={true} onClick={() => goToFollowInfo("followings")}/>
                    <UserProfileFourInfo title="선행도" titleNumber={userInfo?.positiveness ?? 0} isBtn={false}/>
                </div>

                {/* 버튼 */}
                {/* 나의 프로필이라면 */}
                {ismyprofile && 
                    <div className="flex justify-center">
                        <ProfileBtn text="프로필 수정" textColor="text-gray-600" bgColor="bg-white" onClick={goToProfileEdit} />
                    </div>
                }
                {/* 다른 사람의 프로필이라면 */}
                {!ismyprofile && isFollowing &&
                    <div className="flex justify-center">
                        <ProfileBtn text="팔로잉" textColor="text-white" bgColor="bg-[#5D56F1]"  onClick={handlefollowToggle}/>
                    </div>
                }
                {!ismyprofile && !isFollowing &&
                    <div className="flex justify-center">
                        <ProfileBtn text="팔로우" textColor="text-[#5D56F1]" bgColor="bg-white" onClick={handlefollowToggle} />
                    </div>
                }

            </div>

            {/* 루미나 선행도 그래프 */}

            {/* 유저 게시물 렌더링*/}
            <div className="bg-white">
                {userPosts.length === 0 ? (
                    <div  className="text-center border-t-3 border-gray-300 pt-10 text-gray-500 flex flex-col items-center gap-5 justify-center">
                            <img src={ComputerTypingLuna} alt="루나이미지" className="w-30" />
                            <p className="text-sm">게시물이 없습니다.</p>
                    </div>
                ) : (
                    userPosts.map((post, index) => {
                        const isLast = index === userPosts.length - 1;
                        return (
                        <div key={post.postId} ref={isLast ? lastPostRef : undefined}>
                            <Board
                            postId={post.postId}
                            userId={post.userId}
                            nickname={post.nickname}
                            profileImage={post.profileImage}
                            postImage={post.postImage}
                            categoryName={post.categoryName}
                            postContent={post.postContent}
                            likeCnt={post.likeCnt}
                            commentCnt={post.commentCnt}
                            isLike={post.isLike}
                            onDelete={(id) =>
                                setUserposts((prev) => prev.filter((p) => p.postId !== id))
                            }
                            />
                        </div>
                        );
                    })
                )}
                {isLoading && (
                    <div className="text-center text-gray-400 py-4">불러오는 중...</div>
                )}
            </div>

        </div>
    );
}
