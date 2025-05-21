import { deletePost, getUserPosts } from "@/apis/admin";
import { motion, AnimatePresence } from "framer-motion";
import { useEffect, useRef, useState, useCallback } from "react";

interface AdminPostsProps {
  isVisible: boolean;
  onClose: () => void;
  userId: number;
}

interface Post {
  postId: number;
  postImage: string;
  postContent: string;
  postViews: number;
}

export const Posts = ({ isVisible, onClose, userId }: AdminPostsProps) => {
  const [pageNum, setPageNum] = useState<number>(1);
  const [posts, setPosts] = useState<Post[]>([]);
  const [hasMore, setHasMore] = useState<boolean>(true);
  const [loading, setLoading] = useState<boolean>(false);

  const observer = useRef<IntersectionObserver | null>(null);
  const lastPostRef = useCallback(
    (node: HTMLDivElement | null) => {
      if (loading) return;
      if (observer.current) observer.current.disconnect();

      observer.current = new IntersectionObserver((entries) => {
        if (entries[0].isIntersecting && hasMore) {
          setPageNum((prev) => prev + 1);
        }
      });

      if (node) observer.current.observe(node);
    },
    [loading, hasMore]
  );

  const handleDelete = async (postId: number) => {
    await deletePost(postId);
    setPosts((prevPosts) => prevPosts.filter((post) => post.postId !== postId));
  };

  useEffect(() => {
    const fetchUserPosts = async () => {
      setLoading(true);
      try {
        const response = await getUserPosts(userId, pageNum);
        const newPosts: Post[] = response.data.posts;

        if (newPosts.length === 0) {
          setHasMore(false);
        } else {
          setPosts((prev) => {
            const existingIds = new Set(prev.map((post) => post.postId));
            const filteredNewPosts = newPosts.filter(
              (post) => !existingIds.has(post.postId)
            );
            return [...prev, ...filteredNewPosts];
          });
        }
      } finally {
        setLoading(false);
      }
    };

    if (isVisible) {
      fetchUserPosts();
    }
  }, [pageNum, isVisible]);

  useEffect(() => {
    if (!isVisible) {
      // 초기화
      setPosts([]);
      setPageNum(1);
      setHasMore(true);
    }
  }, [isVisible]);

  return (
    <div className="absolute z-20 h-full w-full min-w-80 md:w-[600px] md:h-9/12">
      <AnimatePresence>
        {isVisible && (
          <motion.div
            initial={{ opacity: 0, y: 50 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: 50 }}
            transition={{ duration: 0.4 }}
            className="relative h-full w-full p-4 bg-amber-200"
          >
            {/* ✕ 버튼 - 고정 */}
            <div
              onClick={onClose}
              className="absolute font-black right-5 top-5 cursor-pointer z-10"
            >
              ✕
            </div>

            {/* 콘텐츠 영역 - 스크롤만 이 영역에 적용 */}
            <div className="h-full overflow-y-scroll p-4 pt-16">
              {posts.length === 0 && !loading ? (
                <div className="text-center mt-20">게시물이 없습니다.</div>
              ) : (
                <div className="flex flex-col gap-4">
                  {posts.map((post, index) => {
                    const isLast = index === posts.length - 1;
                    return (
                      <div
                        ref={isLast ? lastPostRef : null}
                        key={post.postId}
                        className="bg-white p-4 rounded shadow"
                      >
                        <div className="flex justify-end">
                          <span onClick={() => handleDelete(post.postId)} className="cursor-pointer">게시물 삭제</span>
                        </div>
                        {post.postImage && (
                          <img
                            src={post.postImage}
                            alt="게시물 이미지"
                            className="w-full h-64 rounded mb-2 object-contain"
                          />
                        )}
                        <p>{post.postContent}</p>
                        <p className="text-sm text-gray-500">
                          조회수: {post.postViews}
                        </p>
                      </div>
                    );
                  })}
                  {loading && <div className="text-center">로딩 중...</div>}
                  {!hasMore && (
                    <div className="text-center text-gray-400">
                      모든 게시물을 불러왔습니다.
                    </div>
                  )}
                </div>
              )}
            </div>
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  );
};
