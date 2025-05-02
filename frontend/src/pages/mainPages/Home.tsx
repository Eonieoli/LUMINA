import { useEffect, useRef, useState, useCallback } from 'react';
import { getPosts } from '@/apis/board';
import { Board, Header } from '@/components';

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
    const fetchedOnce = useRef(false);

    const handleDelete = (postId: number) => {
        setPosts((prevPosts) =>
            prevPosts.filter((post) => post.postId !== postId)
        );
    };

    useEffect(() => {
        const fetchPosts = async () => {
            if (isLoading || !hasMore || fetchedOnce.current) return;

            fetchedOnce.current = true;
            setIsLoading(true);
            try {
                const data = await getPosts(pageNum);
                if (data.data.posts.length < 10) {
                    setHasMore(false);
                }
                setPosts((prev) => [...prev, ...data.data.posts]);
            } catch (error) {
                console.error('게시물 불러오기 실패:', error);
            }
            setIsLoading(false);
            fetchedOnce.current = false;
        };

        fetchPosts();
    }, [pageNum]);

    const lastPostRef = useCallback(
        (node: HTMLDivElement) => {
            if (isLoading) return;
            if (observer.current) observer.current.disconnect();

            observer.current = new IntersectionObserver((entries) => {
                if (entries[0].isIntersecting && hasMore) {
                    setPageNum((prev) => prev + 1);
                }
            });

            if (node) observer.current.observe(node);
        },
        [isLoading, hasMore]
    );

    return (
        <>
            <Header />
            {posts.map((post, index) => {
                const isLast = index === posts.length - 1;

                return (
                    <div
                        key={post.postId}
                        ref={isLast ? lastPostRef : undefined}
                    >
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
                            onDelete={handleDelete}
                        />
                    </div>
                );
            })}
            {isLoading && (
                <div className="py-4 text-center text-gray-400">
                    불러오는 중...
                </div>
            )}
        </>
    );
}
