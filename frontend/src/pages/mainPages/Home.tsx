import { useEffect, useState } from "react";
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

  useEffect(() => {
    const fetchPosts = async () => {
      try {
        const data = await getPosts(1); // 1페이지부터 시작
        setPosts(data);
      } catch (error) {
        console.error("게시물 불러오기 실패:", error);
      }
    };

    fetchPosts();
  }, []);

  return (
    <div className="w-full">
      {posts.length > 0 ? posts.map((post) => (
        <Board
          key={post.postId}
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
      )) : null}
    </div>
  );
}
