import { commentLike, deleteComment, getReplies } from '@/apis/board';
import {
    DefaultProfile,
    HeartDefaultIcon,
    HeartFilledIcon,
} from '@/assets/images';
import { useAuthStore } from '@/stores/auth';
import { useState, useEffect, useCallback } from 'react';

interface reply {
    commentId: number;
    userId: number;
    nickname: string;
    profileImage: string;
    commentContent: string;
    likeCnt: number;
    isLike: boolean;
}

export const Replies = ({
    postId,
    commentId,
    childCommentCnt,
    refreshKey,
}: {
    postId: number;
    commentId: number;
    childCommentCnt: number;
    refreshKey: number;
}) => {
    const [isRepliesOpened, setIsRepliesOpened] = useState(false);
    const [replies, setReplies] = useState<reply[]>([]);
    const authStore = useAuthStore();

    const fetchReplies = useCallback(async () => {
        try {
            const response = await getReplies(postId, commentId);
            setReplies(response.data);
        } catch (error) {
            console.error(error);
        }
    }, [postId, commentId]);

    const toggleReplies = async () => {
        const nextOpen = !isRepliesOpened;
        setIsRepliesOpened(nextOpen);

        if (nextOpen) {
            await fetchReplies();
        }
    };

    useEffect(() => {
        if (isRepliesOpened) {
            fetchReplies();
        }
    }, [refreshKey, fetchReplies, isRepliesOpened]);

    const heartClick = async (postId: number, commentId: number) => {
        try {
            await commentLike(postId, commentId);
            setReplies((prevComments) =>
                prevComments.map((comment) =>
                    comment.commentId === commentId
                        ? {
                              ...comment,
                              isLike: !comment.isLike,
                              likeCnt: comment.isLike
                                  ? comment.likeCnt - 1
                                  : comment.likeCnt + 1,
                          }
                        : comment
                )
            );
        } catch (error) {
            console.error(error);
        }
    };

    const deleteClick = async (commentId: number) => {
        try {
            await deleteComment(postId, commentId);
            setReplies((prevComments) =>
                prevComments.filter(
                    (comment) => comment.commentId !== commentId
                )
            );
        } catch (error) {
            console.error(error);
        }
    };

    return (
        <div className="col-start-2">
            {isRepliesOpened ? (
                <div className="flex flex-col gap-y-2">
                    <div
                        onClick={toggleReplies}
                        className="cursor-pointer text-[12px] text-gray-500"
                    >
                        --- 답글 숨기기
                    </div>
                    {replies.map((reply) => (
                        <div
                            key={reply.commentId}
                            className="grid grid-cols-[auto_1fr] items-center gap-2 border-b border-gray-200 pb-2"
                        >
                            <div className="h-full items-center">
                                <img
                                    className="w-6 rounded-full"
                                    src={
                                        reply.profileImage
                                            ? reply.profileImage
                                            : DefaultProfile
                                    }
                                    alt="댓글프로필"
                                />
                            </div>
                            <div className="grid grid-cols-[1fr_auto] items-center justify-between">
                                <div>
                                    <p className="text-sm font-medium">
                                        {reply.nickname}
                                    </p>
                                    <p className="text-xs">
                                        {reply.commentContent}
                                    </p>
                                    {authStore.data.userId == reply.userId ? (
                                        <div
                                            className="text-xs text-gray-500"
                                            onClick={() =>
                                                deleteClick(reply.commentId)
                                            }
                                        >
                                            삭제하기
                                        </div>
                                    ) : null}
                                </div>
                                <div
                                    onClick={() =>
                                        heartClick(postId, reply.commentId)
                                    }
                                    className="flex cursor-pointer flex-col items-center justify-center"
                                >
                                    <img
                                        className="w-6"
                                        src={
                                            reply.isLike
                                                ? HeartFilledIcon
                                                : HeartDefaultIcon
                                        }
                                        alt="좋아요"
                                    />
                                    <span className="text-[10px] text-gray-500">
                                        {reply.likeCnt}
                                    </span>
                                </div>
                            </div>
                        </div>
                    ))}
                </div>
            ) : (
                <div className="flex flex-col gap-y-2">
                    <div
                        onClick={toggleReplies}
                        className="cursor-pointer text-[12px] text-gray-500"
                    >
                        --- 답글 {childCommentCnt}개 더보기
                    </div>
                </div>
            )}
        </div>
    );
};
