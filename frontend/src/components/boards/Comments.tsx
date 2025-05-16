import {
    commentLike,
    deleteComment,
    getComments,
    postComment,
} from '@/apis/board';
import {
    DefaultProfile,
    HeartDefaultIcon,
    HeartFilledIcon,
    PokerLuna,
    SendIcon,
} from '@/assets/images';
import { useEffect, useRef, useState, useCallback } from 'react';
import { Replies } from './Replies';
import { useAuthStore } from '@/stores/auth';
import { elizaComment } from '@/apis/eliza';
import { toast } from 'sonner';
import { useNavigate } from 'react-router-dom';
// import { Toaster, toast } from 'sonner';

interface Comment {
    commentId: number;
    userId: number;
    nickname: string;
    profileImage: string | null;
    commentContent: string;
    likeCnt: number;
    childCommentCnt: number;
    isLike: boolean;
}

interface CommentsProps {
    postId: number;
    children?: string;
}

export const Comments = ({ postId, children }: CommentsProps) => {
    const [comments, setComments] = useState<Comment[]>([]);
    const [hasMore, setHasMore] = useState(true);
    const [target, setTarget] = useState({ commentId: -1, nickname: '' });
    const [content, setContent] = useState('');
    const [replyRefreshKey, setReplyRefreshKey] = useState<number>(0);
    const [luna, setLuna] = useState<boolean>(false)
    const observerRef = useRef<HTMLDivElement | null>(null);
    const authStore = useAuthStore();

    const pageNumRef = useRef(1);
    const loadingRef = useRef(false);

    const navigate = useNavigate();

    const fetchComments = useCallback(async () => {
        if (loadingRef.current || !hasMore) return;

        loadingRef.current = true;

        try {
            const response = await getComments(pageNumRef.current, postId);

            if (response.data.comments.length < 10) {
                setHasMore(false);
            }

            setComments((prev) => {
                const existingIds = new Set(
                    prev.map((comment) => comment.commentId)
                );
                const newComments = response.data.comments.filter(
                    (comment: { commentId: number }) =>
                        !existingIds.has(comment.commentId)
                );
                return [...prev, ...newComments];
            });

            pageNumRef.current += 1;
        } catch (err) {
            console.error('댓글 불러오기 실패', err);
        } finally {
            loadingRef.current = false;
        }
    }, [postId, hasMore]);

    useEffect(() => {
        const observer = new IntersectionObserver(
            (entries) => {
                if (
                    entries[0].isIntersecting &&
                    !loadingRef.current &&
                    hasMore
                ) {
                    fetchComments();
                }
            },
            {
                root: null,
                rootMargin: '0px',
                threshold: 1.0,
            }
        );

        if (observerRef.current) {
            observer.observe(observerRef.current);
        }

        return () => {
            if (observerRef.current) {
                observer.unobserve(observerRef.current);
            }
        };
    }, [fetchComments, hasMore]);

    const heartClick = async (postId: number, commentId: number) => {
        try {
            await commentLike(postId, commentId);
            setComments((prevComments) =>
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

    const onPostComment = async () => {
        try {
            let newComment: Comment;

            if (target.commentId !== -1) {
                // 답글인 경우
                const response = await postComment(postId, content, target.commentId);

                newComment = {
                    commentId: response.data.commentId, // 일시적으로 고유 ID, 서버에서 받아오면 교체 필요
                    userId: authStore.data.userId,
                    nickname: authStore.data.nickname,
                    profileImage: authStore.data.profileImage,
                    commentContent: content,
                    likeCnt: 0,
                    childCommentCnt: 0,
                    isLike: false,
                };

                setComments((prevComments) =>
                    prevComments.map((comment) =>
                        comment.commentId === target.commentId
                            ? {
                                  ...comment,
                                  childCommentCnt: comment.childCommentCnt + 1,
                              }
                            : comment
                    )
                );
                if (luna) {
                    // await elizaComment(postId, response.data.commentId);
                    toast.promise(elizaComment(postId, response.data.commentId), {
                        loading: '루나가 댓글 작성 중 입니다...',
                        success: '댓글이 생성되었습니다.',
                        error: '루나 댓글 생성 과정에서 오류가 발생했습니다.'
                    })
                    fetchComments();
                }

                setReplyRefreshKey((prev) => prev + 1);
            } else {
                // 일반 댓글인 경우
                const response = await postComment(postId, content);

                newComment = {
                    commentId: response.data.commentId, // 임시 ID
                    userId: authStore.data.userId,
                    nickname: authStore.data.nickname,
                    profileImage: authStore.data.profileImage,
                    commentContent: content,
                    likeCnt: 0,
                    childCommentCnt: 0,
                    isLike: false,
                };

                if (luna) {
                    // await elizaComment(postId, response.data.commentId);
                    toast.promise(elizaComment(postId, response.data.commentId), {
                        loading: '루나가 댓글 작성 중 입니다...',
                        success: '댓글이 생성되었습니다.',
                        error: '루나 댓글 생성 과정에서 오류가 발생했습니다.'
                    })
                    fetchComments();
                }
                setComments((prev) => [newComment, ...prev]);
            }

            setContent('');
            setTarget({ commentId: -1, nickname: '' });
        } catch (error) {
            console.error(error);
        }
    };

    const deleteClick = async (commentId: number) => {
        try {
            await deleteComment(postId, commentId);
            setComments((prevComments) =>
                prevComments.filter(
                    (comment) => comment.commentId !== commentId
                )
            );
        } catch (error) {
            console.error(error);
        }
    };

    const toggleLuna = () => {
        setLuna(!luna);
    }

    const goProfile = (userId: number) => {
        navigate(`/mypage/${userId}`);
    }

    return (
        <div className={`flex h-full w-full flex-col gap-y-2 p-2 ${children}`}>
            <h2 className="flex items-center justify-center text-lg font-semibold">
                댓글
            </h2>
            <div className="flex max-h-100 md:h-100 flex-col gap-y-2 overflow-y-auto px-2">
                {/* <Toaster /> */}
                {comments.map((comment, index) => (
                    <div
                        key={comment.commentId}
                        className={`grid grid-cols-[auto_1fr] items-center gap-2 pb-2
                             ${index == comments.length - 1 ? "" : "border-b border-gray-200 "}
                            `}
                    >
                        <div className="flex h-full items-start overflow-hidden rounded-full">
                            <div className='w-12 h-12 rounded-full overflow-hidden'>
                                <img onClick={() => goProfile(comment.userId)}
                                    className="h-12 w-auto cursor-pointer object-cover"
                                    src={
                                        comment.profileImage
                                            ? comment.profileImage
                                            : DefaultProfile
                                    }
                                    alt="댓글프로필"
                                />
                            </div>
                        </div>
                        <div className="grid grid-cols-[1fr_auto] items-center justify-between">
                            <div>
                                <p onClick={() => goProfile(comment.userId)} className="text-sm font-medium cursor-pointer">
                                    {comment.nickname}
                                </p>
                                <p className="text-sm break-all">
                                    {comment.commentContent}
                                </p>
                                <div className="flex items-center gap-x-2 text-[12px] text-gray-500 cursor-pointer">
                                    <div
                                        onClick={() =>
                                            setTarget({
                                                commentId: comment.commentId,
                                                nickname: comment.nickname,
                                            })
                                        }
                                    >
                                        답글 달기
                                    </div>
                                    {authStore.data.userId == comment.userId ? (
                                        <div
                                            onClick={() =>
                                                deleteClick(comment.commentId)
                                            }
                                        >
                                            삭제하기
                                        </div>
                                    ) : null}
                                </div>
                            </div>
                            <div
                                onClick={() =>
                                    heartClick(postId, comment.commentId)
                                }
                                className="flex flex-col items-center justify-center cursor-pointer"
                            >
                                <img
                                    className="w-6"
                                    src={
                                        comment.isLike
                                            ? HeartFilledIcon
                                            : HeartDefaultIcon
                                    }
                                    alt="좋아요"
                                />
                                <span className="text-[10px] text-gray-500">
                                    {comment.likeCnt}
                                </span>
                            </div>
                        </div>
                        {comment.childCommentCnt > 0 ? (
                            <Replies
                                postId={postId}
                                commentId={comment.commentId}
                                childCommentCnt={comment.childCommentCnt}
                                refreshKey={replyRefreshKey}
                            />
                        ) : null}
                    </div>
                ))}
                {hasMore && <div ref={observerRef} className="h-6" />}
                {!hasMore && comments.length === 0 && (
                    <div className="py-4 text-center text-sm text-gray-400">
                        댓글이 없습니다.
                    </div>
                )}
            </div>

            <div>
                {target.commentId != -1 ? (
                    <div className="flex items-center justify-between">
                        <span className="px-3 py-2">
                            {target.nickname}님에게 답글 달기
                        </span>
                        <span
                            onClick={() =>
                                setTarget({ commentId: -1, nickname: '' })
                            }
                            className="px-3 py-2 cursor-pointer"
                        >
                            X
                        </span>
                    </div>
                ) : null}
                <div className="flex w-full items-center gap-x-2">
                    <div className="aspect-square overflow-hidden rounded-full">
                        <img
                            className="h-12 w-12 object-cover"
                            src={
                                authStore.data.profileImage
                                    ? authStore.data.profileImage
                                    : DefaultProfile
                            }
                            alt=""
                        />
                    </div>
                    <div className='relative w-full'>
                        <input
                            value={content}
                            onChange={(e) => setContent(e.target.value)}
                            onKeyDown={(e) => {
                                if (e.key === 'Enter') {
                                    e.preventDefault();
                                    onPostComment();
                                }
                            }}
                            type="text"
                            placeholder="댓글을 입력하세요"
                            className="h-12 w-full rounded-full border border-gray-400 px-3 py-2 text-sm"
                        />
                        <div className='absolute right-4 top-1/2 -translate-y-1/2 cursor-pointer'>
                            <img onClick={toggleLuna} className={`h-10 w-10 transition duration-300 ${luna ? "opacity-100" : "opacity-50"}`} src={PokerLuna} alt="" />
                        </div>
                    </div>
                    <div onClick={onPostComment}>
                        <img className="w-7 h-auto cursor-pointer object-cover" src={SendIcon} alt="" />
                    </div>
                </div>
            </div>
        </div>
    );
};
