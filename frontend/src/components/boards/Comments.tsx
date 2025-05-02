import { commentLike, deleteComment, getComments, postComment } from "@/apis/board";
import { DefaultProfile, HeartDefaultIcon, HeartFilledIcon, SendIcon } from "@/assets/images";
import { useEffect, useRef, useState, useCallback } from "react";
import { Replies } from "./Replies";
import { useAuthStore } from "@/stores/auth";

interface Comment {
  commentId: number,
  userId: number,
  nickname: string,
  profileImage: string | null,
  commentContent: string,
  likeCnt: number,
  childCommentCnt: number,
  isLike: boolean
}

interface CommentsProps {
  postId: number;
}

export const Comments = ({ postId }: CommentsProps) => {
  const [comments, setComments] = useState<Comment[]>([]);
  const [hasMore, setHasMore] = useState(true);
  const [target, setTarget] = useState({ commentId: -1, nickname: '' });
  const [content, setContent] = useState('');
  const [replyRefreshKey, setReplyRefreshKey] = useState<number>(0);
  const observerRef = useRef<HTMLDivElement | null>(null);
  const authStore = useAuthStore();

  const pageNumRef = useRef(1);
  const loadingRef = useRef(false);

  const fetchComments = useCallback(async () => {
    if (loadingRef.current || !hasMore) return;

    loadingRef.current = true;

    try {
      const response = await getComments(pageNumRef.current, postId);

      if (response.data.comments.length < 10) {
        setHasMore(false);
      }

      setComments(prev => {
        const existingIds = new Set(prev.map(comment => comment.commentId));
        const newComments = response.data.comments.filter(
          (comment: { commentId: number }) => !existingIds.has(comment.commentId)
        );
        return [...prev, ...newComments];
      });

      pageNumRef.current += 1;
    } catch (err) {
      console.error("댓글 불러오기 실패", err);
    } finally {
      loadingRef.current = false;
    }
  }, [postId, hasMore]);

  useEffect(() => {
    const observer = new IntersectionObserver(
      (entries) => {
        if (entries[0].isIntersecting && !loadingRef.current && hasMore) {
          fetchComments();
        }
      },
      {
        root: null,
        rootMargin: "0px",
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
                likeCnt: comment.isLike ? comment.likeCnt - 1 : comment.likeCnt + 1,
              }
            : comment
        )
      );
    } catch (error) {
      console.error(error);
    }
  };

  // const onPostComment = async () => {
  //   try {
  //     if (target.commentId !== -1) {
  //       await postComment(postId, content, target.commentId);
  //     } else {
  //       await postComment(postId, content);
  //     }

  //     const newComment = {
  //       commentId: -1,
  //       userId: authStore.data.userId,
  //       nickname: authStore.data.nickname,
  //       profileImage: authStore.data.profileImage,
  //       commentContent: content,
  //       likeCnt: 0,
  //       childCommentCnt: 0,
  //       isLike: false,
  //     };
  //     setComments((prev) => [newComment, ...prev]);
  //     setContent('');
  //     setTarget({ commentId: -1, nickname: '' });
  //   } catch (error) {
  //     console.error(error);
  //   }
  // };
  const onPostComment = async () => {
    try {
      let newComment: Comment;
  
      if (target.commentId !== -1) {
        // 답글인 경우
        await postComment(postId, content, target.commentId);
  
        newComment = {
          commentId: Date.now(), // 일시적으로 고유 ID, 서버에서 받아오면 교체 필요
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
              ? { ...comment, childCommentCnt: comment.childCommentCnt + 1 }
              : comment
          )
        );

        setReplyRefreshKey(prev => prev + 1);
      } else {
        // 일반 댓글인 경우
        await postComment(postId, content);
  
        newComment = {
          commentId: Date.now(), // 임시 ID
          userId: authStore.data.userId,
          nickname: authStore.data.nickname,
          profileImage: authStore.data.profileImage,
          commentContent: content,
          likeCnt: 0,
          childCommentCnt: 0,
          isLike: false,
        };
  
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
      await deleteComment(postId, commentId)
      setComments((prevComments) =>
        prevComments.filter((comment) => comment.commentId !== commentId)
      );
    } catch (error) {
      console.error(error);
    }
  }


  return (
    <div className="flex flex-col gap-y-2">
      <h2 className="text-lg font-semibold flex items-center justify-center">댓글</h2>
      <div className="flex flex-col gap-y-2 overflow-y-auto max-h-90 px-2">
        {comments.map((comment) => (
          <div key={comment.commentId} className="grid grid-cols-[auto_1fr] items-center gap-2 border-b border-gray-200 pb-2">
            <div className="flex w-full h-full items-start rounded-full overflow-hidden">
              <img className="w-12 h-auto" src={comment.profileImage ? comment.profileImage : DefaultProfile} alt="댓글프로필" />
            </div>
            <div className="grid grid-cols-[1fr_auto] justify-between items-center">
              <div>
                <p className="text-sm font-medium">{comment.nickname}</p>
                <p className="text-sm break-all">{comment.commentContent}</p>
                <div className="flex items-center gap-x-2 text-[12px] text-gray-500">
                  <div onClick={() => setTarget({commentId: comment.commentId, nickname: comment.nickname})}>답글 달기</div>
                  {authStore.data.userId == comment.userId ? <div onClick={() => deleteClick(comment.commentId)}>삭제하기</div> : null}
                </div>
              </div>
              <div onClick={() => heartClick(postId, comment.commentId)} className="flex flex-col justify-center items-center">
                <img className="w-6" src={comment.isLike ? HeartFilledIcon : HeartDefaultIcon} alt="좋아요" />
                <span className="text-[10px] text-gray-500">{comment.likeCnt}</span>
              </div>
            </div>
            {comment.childCommentCnt > 0 ? 
              <Replies
                postId={postId}
                commentId={comment.commentId}
                childCommentCnt={comment.childCommentCnt}
                refreshKey={replyRefreshKey}
              />
              :
              null
            }
          </div>
        ))}
        {hasMore && <div ref={observerRef} className="h-6" />}
        {!hasMore && comments.length === 0 && (
          <div className="text-center text-sm text-gray-400 py-4">댓글이 없습니다.</div>
        )}
      </div>

      <div>
        <div className="bg-gray-200 rounded-t-3xl mt-3">
          {target.commentId != -1 ?
          <div className="flex items-center justify-between">
            <span className="px-3 py-2">{target.nickname}님에게 답글 달기</span>
            <span onClick={() => setTarget({commentId: -1, nickname: ''})} className="px-3 py-2">X</span>
          </div>
            :
            null
          }
        </div>
        <div className="flex items-center gap-x-2">
          <div className="rounded-full">
            <img className="w-auto h-12" src={authStore.data.profileImage ? authStore.data.profileImage : DefaultProfile} alt="" />
          </div>
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
            className="w-full h-12 border px-3 py-2 rounded-full text-sm"
          />
          <div onClick={onPostComment}>
            <img className="w-auto h-8" src={SendIcon} alt="" />
          </div>
        </div>
      </div>
    </div>
  );
};
