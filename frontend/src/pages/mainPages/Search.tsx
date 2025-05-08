import { getCategoryExplore, getHashtagPosts, getUser } from "@/apis/board";
import { DefaultProfile, SearchIcon } from "@/assets/images";
import { Board } from "@/components";
import { ChangeEvent, useCallback, useEffect, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";

interface userDatas {
    userId: number,
    profileImage: string,
    nickname: string
}

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

export default function Search() {
    const [posts, setPosts] = useState<Post[]>([]);
    const [pageNum, setPageNum] = useState(1);
    const [searchInput, setSearchInput] = useState('');
    const [debouncedInput, setDebouncedInput] = useState("");
    const [searchedUsers, setSearchedUsers] = useState<userDatas[]>([]);
    const [hasMore, setHasMore] = useState(true);
    const [isLoading, setIsLoading] = useState(false);
    const observer = useRef<IntersectionObserver | null>(null);
    const fetchedOnce = useRef(false);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const wrapperRef = useRef<HTMLDivElement>(null); // 전체 감싸는 div ref
    const inputRef = useRef<HTMLInputElement>(null);  // 인풋 ref
    const [hashtag, setHashtag] = useState('');
    // IME 상태 관리를 위한 참조
    const isComposing = useRef(false);

    const navigate = useNavigate();

    const handleInput = (e: ChangeEvent<HTMLInputElement>) => {
        setSearchInput(e.target.value);
        // 입력 변경 시에는 사용자 목록을 초기화하지 않습니다.
        // setSearchedUsers([]);
    }

    const handleDelete = (postId: number) => {
        setPosts((prevPosts) =>
            prevPosts.filter((post) => post.postId !== postId)
        );
    };

    // 디바운스 로직 (입력 후 500ms 지나면 업데이트)
    useEffect(() => {
      const timer = setTimeout(() => {
        setDebouncedInput(searchInput); // 실질적인 요청 트리거
      }, 500);
  
      return () => clearTimeout(timer); // 입력이 바뀌면 기존 타이머 제거
    }, [searchInput]);

    useEffect(() => {
        console.log("해시태그 변경:", hashtag);
        if (hashtag !== "") {
            setPosts([]);
            setPageNum(1);
            fetchedOnce.current = false;
        }
    }, [hashtag]);
    
    useEffect(() => {
        console.log('해시택2');
        const fetchPosts = async () => {
            console.log('조건 체크:', { isLoading, hasMore, fetchedOnce: fetchedOnce.current });
            if (isLoading || !hasMore || fetchedOnce.current) return;
    
            fetchedOnce.current = true;
            setIsLoading(true);
            try {
                let data;
                if (hashtag !== "") {
                    console.log('해시택3:', hashtag, pageNum);
                    data = await getHashtagPosts(hashtag, pageNum);
                } else {
                    data = await getCategoryExplore(pageNum);
                }
    
                const newPosts = data.data.posts;
                setPosts((prev) => [...prev, ...newPosts]);
    
                if (newPosts.length < 10) {
                    setHasMore(false);
                }
            } catch (error) {
                console.error('게시물 불러오기 실패:', error);
            }
            setIsLoading(false);
            fetchedOnce.current = false;
        };

        fetchPosts();
    }, [pageNum, hashtag]);

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
  
    // 디바운스된 값이 변경되면 API 호출
    useEffect(() => {
      if (debouncedInput.trim() === "") return;
      console.log("API 호출: ", debouncedInput);
      const fetchUser = async (keyword: string) => {
        try {
            const response = await getUser(keyword);
            
            setSearchedUsers(response.data.users);
        
            setIsModalOpen(true); // 검색 결과 있을 때 모달 열기

        } catch (error) {
            console.error(error);
        }
      }

      fetchUser(searchInput);
    }, [debouncedInput]);

    
    // 외부 클릭 감지
    useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
        if (
        wrapperRef.current &&
        !wrapperRef.current.contains(event.target as Node)
        ) {
        setIsModalOpen(false);
        }
    };
    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
    }, []);

    // IME 이벤트 핸들러
    const handleCompositionStart = () => {
        console.log('IME 입력 시작');
        isComposing.current = true;
    };

    const handleCompositionEnd = () => {
        console.log('IME 입력 완료');
        isComposing.current = false;
    };

    // 사용자 항목 클릭 핸들러 - 별도로 분리하여 IME와 관계없이 동작하도록 함
    const handleUserClick = (userId: number) => {
        console.log('사용자 클릭됨:', userId);
        // 모달 닫기
        setIsModalOpen(false);
        // 페이지 이동
        navigate(`/mypage/${userId}`);
    };

    return (
        <div className="h-full overflow-y-auto">
            <div className="sticky top-0 z-30">
                <div className="relative flex h-20 items-end px-5 pb-3 bg-white" ref={wrapperRef}>
                    <div className="relative w-full">
                        <input
                            type="text"
                            ref={inputRef}
                            value={searchInput}
                            onChange={handleInput}
                            onCompositionStart={handleCompositionStart}
                            onCompositionEnd={handleCompositionEnd}
                            onFocus={() => {
                                if (searchedUsers.length) setIsModalOpen(true);
                            }}
                            className="w-full h-10 z-20 px-4 text-[16px] border-b-2 border-[#9C97FA]"
                        />
                        <img 
                            onClick={() => {
                                setHashtag(searchInput.trim());
                                setHasMore(true);
                                setIsModalOpen(false);
                            }}
                            src={SearchIcon}
                            alt="돋보기"
                            className="absolute right-4 bottom-0 -translate-y-1/2 w-4 h-auto cursor-pointer"
                        />
                    </div>
                    {/* 유저 검색 모달 */}
                    {isModalOpen && searchedUsers.length > 0 && (
                        <div className="absolute left-0 bottom-0 translate-y-full bg-[#000000b0] max-h-80 overflow-y-auto w-full rounded-b-2xl">
                        {searchedUsers.map((user) => (
                            <div
                            key={user.userId}
                            // 클릭 이벤트를 별도의 함수로 처리
                            onClick={() => handleUserClick(user.userId)}
                            className="p-4 flex gap-x-4 text-lg text-white items-center cursor-pointer hover:bg-[#ffffff20]"
                            >
                                <img
                                    src={user.profileImage || DefaultProfile}
                                    alt=""
                                    className="w-10 h-10 bg-white rounded-full"
                                />
                                <span>{user.nickname}</span>
                            </div>
                        ))}
                        </div>
                    )}
                    {/* 유저가 없을 때 해시태그 */}
                    {isModalOpen && debouncedInput && searchedUsers.length === 0 && (
                        <div className="absolute left-0 bottom-0 translate-y-full bg-[#000000b0] w-full rounded-b-2xl">
                            <div
                            onClick={() => {
                                setHashtag(searchInput.trim());
                                setHasMore(true);
                                setIsModalOpen(false);
                            }}
                            className="p-4 text-lg text-white cursor-pointer hover:bg-[#ffffff20]"
                            >
                            #{searchInput}
                            </div>
                        </div>
                    )}
                </div>
            </div>
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