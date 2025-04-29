import { postLike } from "@/apis/board";
import { ChatIcon, HeartDefaultIcon, HeartFilledIcon } from "@/assets/images";
import { useEffect, useRef, useState } from "react";
import { Comments } from "./Comments";

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
}

export const Board = ({postId, nickname, profileImage, postImage, categoryName, postContent, likeCnt, commentCnt, isLike}: BoardProps) => {
    const [isExpanded, setIsExpanded] = useState<boolean>(false);
    const [isOverflowing, setIsOverflowing] = useState(false);
    const contentRef = useRef<HTMLDivElement>(null);
    const [showComments, setShowComments] = useState(false);
  
    const openComments = () => setShowComments(true);
    const closeComments = () => setShowComments(false);

    const toggleContent = () => {
        setIsExpanded(!isExpanded);
    }

    const heartClick = async (postId: number) => {
        try {
            const response = await postLike(postId);
    
            console.log(response);
            return;
        } catch (error) {
            console.log(error);
        }
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

    return (
        <>
            <div className="w-full flex flex-col gap-y-2 px-5 py-2 border-y-3 border-gray-200">
                {/* 사용자 프로필 */}
                <div className="flex items-center gap-x-4">
                    <img src={profileImage} alt="프로필 이미지" className="w-7 h-7 rounded-full"/>
                    <span className="font-bold">{nickname}</span>
                </div>
                {/* 게시물 카테고리 */}
                <div>
                    <span className="bg-[#9C97FA] text-white px-2 py-0.5 text-xs rounded-full">{categoryName}</span>
                </div>
                {/* 게시물 사진 (없을 수도 있음) */}
                {postImage ? <div className="flex justify-center items-center"><img src={postImage} alt="게시물" className="w-full h-full"/></div> : null}
                {/* 게시물 내용 */}
                <div
                    ref={contentRef}
                    className={`break-keep ${isExpanded ? "" : "line-clamp-3"} text-sm`}>
                    {postContent}
                </div>

                {/* 더보기 버튼 (3줄 이상일 때만) */}
                {isOverflowing && (
                    <button
                    onClick={toggleContent}
                    className="text-blue-500 text-xs self-start cursor-pointer"
                    >
                    {isExpanded ? "접기" : "더보기"}
                    </button>
                )}
                {/* 좋아요, 댓글 및 공유 */}
                <div className="flex gap-x-4">
                    <div onClick={() => heartClick(postId)} className="flex gap-x-1 cursor-pointer">
                        <img src={isLike ? HeartFilledIcon : HeartDefaultIcon} alt="좋아요" />
                        <span>{likeCnt}</span>
                    </div>
                    <div onClick={openComments} className="flex gap-x-1">
                        <img src={ChatIcon} alt="댓글" />
                        <span>{commentCnt}</span>
                    </div>
                </div>
            </div>

            {/* 액션시트 댓글창 */}
            {showComments && (
                <div className="fixed inset-0 z-50 flex justify-center items-end bg-black/30" onClick={closeComments}>
                {/* 클릭해도 모달 닫히지 않게 이벤트 버블링 막기 */}
                <div
                    className="bg-white w-full rounded-t-2xl p-4 max-h-[80%] overflow-y-auto transition-transform duration-300"
                    onClick={(e) => e.stopPropagation()}
                >
                    <Comments />
                </div>
                </div>
            )}
        </>
    )
}