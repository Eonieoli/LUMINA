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

interface BoardProps {
    postId: number;
    nickname: string;
    profileImage?: string;
    postImage?: string;
    categoryName: string;
    postContent: string;
    likeCnt: number;
    commentCnt: number;
    isLike: boolean;
    onDelete: (postId: number) => void;
}

export const Board = ({
    postId,
    nickname,
    profileImage,
    postImage,
    categoryName,
    postContent,
    likeCnt: initialLikeCnt,
    commentCnt,
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

    const deleteClick = async (postId: number) => {
        try {
            await deletePost(postId);
            onDelete(postId);
        } catch (error) {
            console.error(error);
        }
    };

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

    return (
        <>
            <div className="flex w-full flex-col gap-y-2 border-y-3 border-gray-200 px-5 py-2">
                {/* 사용자 프로필 */}
                <div className="flex items-center justify-between">
                    <div className="flex items-center gap-x-4">
                        <img
                            src={profileImage ? profileImage : DefaultProfile}
                            alt="프로필 이미지"
                            className="h-7 w-7 rounded-full"
                        />
                        <span className="font-bold">{nickname}</span>
                    </div>
                    {authStore.data.nickname === nickname ? (
                        <div
                            onClick={() => deleteClick(postId)}
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
                {/* 좋아요, 댓글 및 공유 */}
                <div className="flex gap-x-4">
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
                        className="flex gap-x-1"
                    >
                        <img src={ChatIcon} alt="댓글" />
                        <span>{commentCnt}</span>
                    </div>
                </div>
            </div>

            {/* 바텀시트 댓글창 */}
            {showComments && (
                <BottomSheet onClose={() => setShowComments(false)}>
                    <Comments postId={postId} />
                </BottomSheet>
            )}
        </>
    );
};
