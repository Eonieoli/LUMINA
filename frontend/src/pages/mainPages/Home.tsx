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

                if (fetchedPosts.length < 10) {
                    if (category === 'following') {
                        // 다음 요청부터는 'all'로 전환
                        setCategory('all');
                        setPageNum(1); // 페이지 초기화
                        setPosts([]); // 게시물도 초기화하거나 유지하고 싶다면 이 줄 제거
                        setHasMore(true); // 더 불러올 수 있도록 다시 true로 설정
                    } else {
                        setHasMore(false);
                    }
                }
                setPosts((prev) => [...prev, ...data.data.posts]);
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
