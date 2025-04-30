import { commentLike, getReplies } from "@/apis/board";
import { DefaultProfile, HeartDefaultIcon, HeartFilledIcon } from "@/assets/images";
import { useState } from "react";

interface reply {
    commentId: number,
    userId: number,
    nickname: string,
    profileImage: string,
    commentContent: string,
    likeCnt: number,
    isLike: boolean
}

export const Replies = ({postId, commentId, childCommentCnt}: {postId: number, commentId: number, childCommentCnt: number}) => {
    const [isRepliesOpened, setIsRepliesOpened] = useState(false);
    const [replies, setReplies] = useState<reply[]>([]);

    const toggleReplies = async () => {
        setIsRepliesOpened(!isRepliesOpened);

        try {
            const response = await getReplies(postId, commentId);
            setReplies(response.data);
        } catch (error) {
            console.error(error);
        }
    }

    
    const heartClick = async (postId: number, commentId: number) => {
        try {
            await commentLike(postId, commentId);
            setReplies((prevComments) =>
            prevComments.map((comment) =>
                comment.commentId === commentId
                ? {
                    ...comment,
                    isLike: !comment.isLike,
                    likeCnt: comment.isLike ? comment.likeCnt - 1 : comment.likeCnt + 1,
                    }
                : comment
            )
            );
        } catch (error) {
            console.log(error);
        }
    }

    return (
        <div className="col-start-2">
            {isRepliesOpened ?
                <div className="flex flex-col gap-y-2">
                    <div onClick={toggleReplies} className="text-[12px] text-gray-500">--- 답글 숨기기</div>
                    {replies.map((reply) => (
                        <div key={reply.commentId} className="flex items-center gap-x-2">
                            <div className="rounded-full overflow-hidden">
                                <img className="w-6" src={reply.profileImage ? reply.profileImage : DefaultProfile} alt="댓글프로필필" />
                            </div>
                            <div className="w-full flex justify-between items-center">
                                <div>
                                    <p className="text-sm font-medium">{reply.nickname}</p>
                                    <p className="text-sm">{reply.commentContent}</p>
                                </div>
                                <div onClick={() => heartClick(postId, reply.commentId)} className="flex flex-col justify-center items-center">
                                    <img className="w-6" src={reply.isLike ? HeartFilledIcon : HeartDefaultIcon} alt="좋아요" />
                                    <span className="text-[10px] text-gray-500">{reply.likeCnt}</span>
                                </div>
                            </div>
                        </div>
                    ))}
                </div>
                :
                <div className="flex flex-col gap-y-2">
                    <div onClick={toggleReplies} className="text-[12px] text-gray-500">--- 답글 {childCommentCnt}개 더보기</div>
                </div>
            }
        </div>
    )
}