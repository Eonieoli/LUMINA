import { useEffect, useRef, useState, useCallback } from "react";
import { getPosts } from "@/apis/board";
import { Board } from "@/components/boards/Board";

interface Post {
  postId: number;
  userId: number;
  nickname: string;
  profileImage?: string;
  postImage?: string;
  postContent: string;
  postViews: number;
  categoryName: string;
  hashtagList: string[];
  likeCnt: number;
  commentCnt: number;
  isLike: boolean;
}

export default function HomePage() {
  const [posts, setPosts] = useState<Post[]>([]);
  const [pageNum, setPageNum] = useState(1);
  const [hasMore, setHasMore] = useState(true);
  const [isLoading, setIsLoading] = useState(false);
  const observer = useRef<IntersectionObserver | null>(null);

  useEffect(() => {
    console.log(posts);
  }, [posts])
  
  useEffect(() => {
    const fetchPosts = async () => {
      if (isLoading || !hasMore) return;

      setIsLoading(true);
      try {
        const data = await getPosts(pageNum);
        if (data.data.posts.length < 10) {
          setHasMore(false); // 10개 미만이면 마지막 페이지로 간주
        }
        setPosts(prev => [...prev, ...data.data.posts]);
      } catch (error) {
        console.error("게시물 불러오기 실패:", error);
      }
      setIsLoading(false);
    };

    fetchPosts();
  }, [pageNum]);

  const lastPostRef = useCallback(
    (node: HTMLDivElement) => {
      if (isLoading) return;
      if (observer.current) observer.current.disconnect();

      observer.current = new IntersectionObserver(entries => {
        if (entries[0].isIntersecting && hasMore) {
          setPageNum(prev => prev + 1);
        }
      });

      if (node) observer.current.observe(node);
    },
    [isLoading, hasMore]
  );

  return (
    <>
      {posts.map((post, index) => (
        <div key={post.postId} ref={index === posts.length - 1 ? lastPostRef : undefined}>
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
          />
        </div>
      ))}
      {isLoading && (
        <div className="text-center py-4 text-gray-400">불러오는 중...</div>
      )}
    </>
  );
}
