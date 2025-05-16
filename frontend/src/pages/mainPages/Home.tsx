import { useEffect, useRef, useState, useCallback } from 'react';
import { getPosts } from '@/apis/board';
import { Board, Header } from '@/components';

export interface Post {
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
    createdAt: string;
    isLike: boolean;
}

export default function HomePage() {
    const [posts, setPosts] = useState<Post[]>([]);
    // 팔로워들
    const [pageNum, setPageNum] = useState(1);
    const [hasMore, setHasMore] = useState(true);
    const [isLoading, setIsLoading] = useState(false);
    // 이전 게시물 다 본 경우 랜덤게시물?
    const [category, setCategory] = useState<'following' | 'all'>('following');
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
                const data = await getPosts(pageNum, category);
                const fetchedPosts = data.data.posts;

                // 처음 요청인데 게시물이 아예 없음 → 바로 all로 전환
                if (pageNum === 1 && fetchedPosts.length === 0 && category === 'following') {
                    setCategory('all');
                    setPageNum(1);
                    setHasMore(true);
                    setIsLoading(false);
                    fetchedOnce.current = false;
                }

                // 두 번째 이후 요청인데 10개 미만이면 now switch to all
                if (pageNum > 1 && fetchedPosts.length < 10 && category === 'following') {
                    setCategory('all');
                    setPageNum(1);
                    setHasMore(true);
                    setIsLoading(false);
                    fetchedOnce.current = false;
                }

                // all인데도 10개 미만이면 종료
                if (category === 'all' && fetchedPosts.length < 10) {
                    setHasMore(false);
                }

                setPosts((prev) => [...prev, ...fetchedPosts]);
            } catch (error) {
                console.error('게시물 불러오기 실패:', error);
            }
            setIsLoading(false);
            fetchedOnce.current = false;
        };
        fetchPosts();
    }, [pageNum, category]);

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
        <div className='bg-white'>
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
                            userId={post.userId}
                            nickname={post.nickname}
                            profileImage={post.profileImage}
                            postImage={post.postImage}
                            categoryName={post.categoryName}
                            postContent={post.postContent}
                            postViews={post.postViews}
                            likeCnt={post.likeCnt}
                            commentCnt={post.commentCnt}
                            createdAt={post.createdAt}
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
        </div>
    );
}
