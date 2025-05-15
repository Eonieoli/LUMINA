import { postLike, deletePost } from '@/apis/board';
import {
    ChatIcon,
    DefaultProfile,
    HeartDefaultIcon,
    HeartFilledIcon,
} from '@/assets/images';
import { useEffect, useRef, useState } from 'react';
import { BottomSheet, Comments } from '@/components';
import { useAuthStore } from '@/stores/auth';
import { useNavigate } from 'react-router-dom';

interface BoardProps {
    postId: number;
    userId: number;
    nickname: string;
    profileImage?: string;
    postImage?: string;
    categoryName: string;
    postContent: string;
    postViews: number;
    likeCnt: number;
    commentCnt: number;
    createdAt: string;
    isLike: boolean;
    onDelete: (postId: number) => void;
}

export const Board = ({
    postId,
    userId,
    nickname,
    profileImage,
    postImage,
    categoryName,
    postContent,
    postViews,
    likeCnt: initialLikeCnt,
    commentCnt,
    createdAt,
    isLike: initialIsLike,
    onDelete,
}: BoardProps) => {
    const authStore = useAuthStore();
    const [isExpanded, setIsExpanded] = useState<boolean>(false);
    const [isOverflowing, setIsOverflowing] = useState(false);
    const [isLiked, setIsLiked] = useState(initialIsLike);
    const [likes, setLikes] = useState(initialLikeCnt);
    const contentRef = useRef<HTMLDivElement>(null);
    const [showComments, setShowComments] = useState(false);
    const [isDesktop, setIsDesktop] = useState(false);
    const [isModalOpened, setIsModalOpened] = useState<boolean>(false);

    const navigate = useNavigate();

    const toggleContent = () => setIsExpanded(!isExpanded);

    const heartClick = async (postId: number) => {
        try {
            await postLike(postId);

            setIsLiked((prev) => !prev);
            setLikes((prev) => prev + (isLiked ? -1 : 1));
            return;
        } catch (error) {
            console.error(error);
        }
    };

    const deleteClick = () => {
        setIsModalOpened(true);
    };

    const deleteConfirm = async (postId: number) => {
        try {
            await deletePost(postId);
            onDelete(postId);
            setIsModalOpened(false);
        } catch (error) {
            console.error(error);
        }
    }

    const profileClick = (userId: number) => {
        navigate(`/mypage/${userId}`);
    }

    useEffect(() => {
        if (contentRef.current) {
            const el = contentRef.current;
            const lineHeight = parseInt(window.getComputedStyle(el).lineHeight);
            const maxHeight = lineHeight * 3; // 3줄 높이
            if (el.scrollHeight > maxHeight) {
                setIsOverflowing(true);
            }
        }
    }, [postContent]);

    useEffect(() => {
        const scrollContainer = document.getElementById('scrollable-container');

        if (!scrollContainer) return;

        if (showComments) {
            scrollContainer.style.overflow = 'hidden';
        } else {
            scrollContainer.style.overflow = 'auto';
        }
        

        return () => {
            scrollContainer.style.overflow = 'auto';
        };
    }, [showComments]);

    useEffect(() => {
        const mediaQuery = window.matchMedia("(min-width: 768px)"); // md 기준
        setIsDesktop(mediaQuery.matches);
    
        const handler = (e: MediaQueryListEvent) => setIsDesktop(e.matches);
        mediaQuery.addEventListener("change", handler);
    
        return () => mediaQuery.removeEventListener("change", handler);
      }, []);

      function formatCreatedAt(createdAt: string): string {
        const createdDate = new Date(createdAt);
        const now = new Date();
      
        // 오늘 00:00 기준으로 비교하기 위해 시간 제거
        const startOfToday = new Date(now.getFullYear(), now.getMonth(), now.getDate());
        const startOfCreated = new Date(createdDate.getFullYear(), createdDate.getMonth(), createdDate.getDate());
      
        const msInDay = 1000 * 60 * 60 * 24;
        const diffInMs = startOfToday.getTime() - startOfCreated.getTime();
        const diffInDays = Math.floor(diffInMs / msInDay);
      
        if (diffInDays < 0) {
          return '미래 날짜'; // 예외 처리
        } else if (diffInDays === 0) {
          return '오늘';
        } else if (diffInDays <= 7) {
          return `${diffInDays}일 전`;
        } else {
          const yy = String(createdDate.getFullYear()).slice(2);
          const mm = String(createdDate.getMonth() + 1).padStart(2, '0');
          const dd = String(createdDate.getDate()).padStart(2, '0');
          return `${yy}.${mm}.${dd}`;
        }
      }

    return (
        <>
            {/* 삭제 확인 모달 */}
            <div onClick={() => setIsModalOpened(false)} className={`fixed flex justify-center items-center z-50 left-0 top-0 w-full h-full bg-[#00000050] ${isModalOpened ? 'block' : 'hidden'}`}>
                <div
                    onClick={(e) => e.stopPropagation()}
                    className='relative overflow-hidden flex flex-col justify-center items-center rounded-2xl bg-white w-4/5 aspect-square max-w-96 md:ml-20 p-10'
                >
                    <p>게시물을 삭제하시겠습니까?</p>
                    <p>게시물을 삭제하시는 경우 복구가 불가능합니다.</p>
                    <div className='absolute grid grid-cols-1 bottom-0 w-full h-1/3 text-lg font-semibold'>
                        <div onClick={() => deleteConfirm(postId)} className='flex justify-center items-center border-t border-gray-300 cursor-pointer hover:bg-gray-100 transition duration-300 text-red-500'>삭제</div>
                        <div onClick={() => setIsModalOpened(false)} className='flex justify-center items-center border-t border-gray-300 cursor-pointer hover:bg-gray-100 transition duration-300'>취소</div>
                    </div>
                </div>
            </div>
            <div className="flex w-full flex-col gap-y-2 border-b-3 border-gray-200 px-5 py-2">
                {/* 사용자 프로필 */}
                <div className="flex items-center justify-between">
                    <div className="flex items-center gap-x-4" onClick={() => profileClick(userId)}>
                        <img
                            src={profileImage ? profileImage : DefaultProfile}
                            alt="프로필 이미지"
                            className="h-7 w-7 rounded-full object-cover"
                        />
                        <span className="font-bold">{nickname}</span>
                    </div>
                    {authStore.data.nickname === nickname ? (
                        <div
                            onClick={() => deleteClick()}
                            className="relative flex h-4 w-4 cursor-pointer gap-x-1 py-2"
                        >
                            <div className="absolute top-1/2 left-0 h-[2px] w-4 -translate-y-1/2 rotate-45 bg-black"></div>
                            <div className="absolute top-1/2 left-0 h-[2px] w-4 -translate-y-1/2 -rotate-45 bg-black"></div>
                        </div>
                    ) : null}
                </div>
                {/* 게시물 카테고리 */}
                <div>
                    <span className="rounded-full bg-[#9C97FA] px-2 py-0.5 text-xs text-white">
                        {categoryName}
                    </span>
                </div>
                {/* 게시물 사진 (없을 수도 있음) */}
                {postImage ? (
                    <div className="flex items-center justify-center">
                        <img
                            src={postImage}
                            alt="게시물"
                            className="h-full w-full"
                        />
                    </div>
                ) : null}
                {/* 게시물 내용 */}
                <div
                    ref={contentRef}
                    className={`break-keep ${isExpanded ? '' : 'line-clamp-3'} text-sm`}
                >
                    {postContent}
                </div>

                {/* 더보기 버튼 (3줄 이상일 때만) */}
                {isOverflowing && (
                    <button
                        onClick={toggleContent}
                        className="cursor-pointer self-start text-xs text-blue-500"
                    >
                        {isExpanded ? '접기' : '더보기'}
                    </button>
                )}
                <div className='text-sm text-gray-500 flex items-center'>{formatCreatedAt(createdAt)}</div>
                {/* 좋아요, 댓글 및 공유 */}
                <div className="flex justify-between items-center">
                    <div className='flex gap-x-4'>
                        <div
                            onClick={() => heartClick(postId)}
                            className="flex cursor-pointer gap-x-1"
                        >
                            <img
                                src={isLiked ? HeartFilledIcon : HeartDefaultIcon}
                                alt="좋아요"
                            />
                            <span>{likes}</span>
                        </div>
                        <div
                            onClick={() => setShowComments(true)}
                            className="flex cursor-pointer gap-x-1"
                        >
                            <img src={ChatIcon} alt="댓글" />
                            <span>{commentCnt}</span>
                        </div>
                    </div>
                    <div className='text-xs text-gray-500'>조회수 {postViews}</div>
                </div>
            </div>

            {/* 바텀시트 댓글창 */}
            {/* {showComments && (
                <BottomSheet onClose={() => setShowComments(false)}>
                    <Comments postId={postId} />
                </BottomSheet>
            )} */}
            {showComments && (
                isDesktop ? (
                <div onClick={() => setShowComments(false)} className="fixed flex justify-center items-center right-0 top-0 h-full w-full bg-[#00000050] shadow-lg z-50">
                    <div onClick={(e) => e.stopPropagation()} className='grid grid-cols-5 ml-20 w-2/3 h-3/4 min-w-[688px]'>
                        <div className='flex justify-center items-center col-span-3 rounded-l-md bg-black'>
                            <img src={postImage} className='w-full h-auto' alt="" />
                        </div>
                        <Comments postId={postId} children='col-span-2 bg-white rounded-r-md justify-between' />
                    </div>
                </div>
                ) : (
                <BottomSheet isVisible={showComments} onClose={() => setShowComments(false)}>
                    <Comments postId={postId} />
                </BottomSheet>
                )
            )}
        </>
    );
};
