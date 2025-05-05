import { getMyProfile } from "@/apis/auth";
import { useEffect, useRef, useState, useCallback} from "react";
import { Hamburger, DefaultProfile } from "@/assets/images";
import UserProfileFourInfo from "@/components/profile/fourInfo";
import ProfileBtn from "@/components/profile/button";
import { Post } from "./Home";
import { getUserPosts } from "@/apis/board";
import { Board } from "@/components";

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
    const [userPosts, setUserposts] = useState<Post[]>([])
    const [pageNum, setPageNum] = useState(1);
    const [hasMore, setHasMore] = useState(true);
    const [isLoading, setIsLoading] = useState(false)
    const observer = useRef<IntersectionObserver | null> (null)
    const fetchedOnce = useRef(false)

    // 유저 정보 가져오기
    useEffect(() => {
        const fetchProfile= async () =>  {
            try {
                const response = await getMyProfile()
                setUserInfo(response.data)
                console.log("프로필 정보!" , response.data)
                return response.data
            }
            catch (error) {}
        }
        fetchProfile()
    }, [])

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
    


    return (
        <div className="w-full h-full">


            {/* 게시물 상단 */}
            <div className="p-6">

                {/* 이름, 햄버거바*/}
                <div className="flex items-center justify-between h-8 mb-4">
                    <p className="text-xl font-semibold">{userInfo?.nickname}</p>
                    <img src={Hamburger} alt="햄버거버튼" className="h-4" />
                </div>

                {/* 프로필 사진, 이름, 상태메세지 */}
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
                
            </div>

            {/* 루미나 선행도 그래프 */}

            {/* 유저 게시물 렌더링*/}
            <div>
                {userPosts.map((post, index) => {
                    const isLast = index === userPosts.length - 1;

                    return (
                    <div key={post.postId} ref={isLast ? lastPostRef : undefined}>
                        <Board
                        postId={post.postId}
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
                })}
                {isLoading && (
                    <div className="text-center text-gray-400 py-4">불러오는 중...</div>
                )}
                </div>

        </div>
    );
}
