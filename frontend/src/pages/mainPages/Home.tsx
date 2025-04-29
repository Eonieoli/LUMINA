import { Board } from "@/components/boards/Board"

const posts = [
    {
        "postId": 11,
        "userId":3,
        "nickname":"박우배",
        "profileImage": "http://dummyimage.com/112x100.png/dddddd/000000",
        "postImage": "http://dummyimage.com/112x100.png/dddddd/000000",
        "postContent": "다같이 선행해요",
        "postViews": 3,
        "categoryName": "사람",
        "hashtagList": ["행복", "사랑"],
        "likeCnt": 4,
        "commentCnt":15,
        "isLike": true
    },
    {
        "postId": 1,
        "userId":2,
        "nickname":"박우배",
        "profileImage": "http://dummyimage.com/112x100.png/dddddd/000000",
        "postImage": "http://dummyimage.com/112x100.png/dddddd/000000",
        "postContent": "다같이 선행해요",
        "postViews": 3,
        "categoryName": "사람",
        "hashtagList": ["행복", "사랑"],
        "likeCnt": 4,
        "commentCnt":15,
        "isLike": true
    },
    {
        "postId": 2,
        "userId":1,
        "nickname":"박우배",
        "profileImage": "http://dummyimage.com/112x100.png/dddddd/000000",
        "postImage": "http://dummyimage.com/225x100.png/dddddd/000000",
        "postContent": "다같이 선행해요",
        "postViews": 3,
        "categoryName": "사람",
        "hashtagList": ["행복", "사랑"],
        "likeCnt": 4,
        "commentCnt":15,
        "isLike": true
    },
]

export default function HomePage() {
    return (
        <div className="w-full">
            {posts.map((post) => (
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
            ))}
        </div>
    );
};


