import { createPost } from '@/apis/board';
import { elizaBoard } from '@/apis/eliza';
import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
// import { Toaster, toast } from 'sonner';

export default function PostCreate() {
    const [image, setImage] = useState<File | null>(null);
    const [previewUrl, setPreviewUrl] = useState<string | null>(null);
    const [content, setContent] = useState('');
    const [category, setCategory] = useState('환경');
    const [tagInput, setTagInput] = useState('');
    const [tags, setTags] = useState<string[]>([]);
    const navigate = useNavigate();

    const postUpload = async () => {
        if (!content.trim()) {
            alert('내용을 입력해주세요.');
            return;
        }

        try {
            await createPost({
                postImageFile: image,
                categoryName: category,
                hashtag: tags,
                postContent: content,
            }).then(async (res) => {
                await elizaBoard(res.data.postId);
                // toast.promise(elizaBoard(res.data.postId), {
                //     loading: '게시물 생성 중 입니다...',
                //     success: '게시물이 생성되었습니다.',
                //     error: '루나 댓글 생성 과정에서 오류가 발생했습니다.'
                // })
            });

            navigate('/');
        } catch (error) {
            console.error('게시물 업로드 실패:', error);
            alert('게시물 업로드에 실패했습니다.');
        }
    };

    const handleImageChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const file = e.target.files?.[0];
        if (file) {
            setImage(file);
            setPreviewUrl(URL.createObjectURL(file));
        }
    };

    const handleTagKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
        if (e.key === 'Enter' && tagInput.trim()) {
            e.preventDefault();
            if (!tags.includes(tagInput.trim())) {
                setTags([...tags, tagInput.trim()]);
            }
            setTagInput('');
        }
    };

    const removeTag = (tag: string) => {
        setTags(tags.filter((t) => t !== tag));
    };

    return (
        <div className="flex h-full w-full flex-col gap-4 p-4">
            {/* <Toaster /> */}
            {/* 헤더 */}
            <div className="flex items-center justify-between">
                <Link to="/">
                    <div className='flex relative w-4 h-full'>
                        <div className='absolute top-0 w-4 h-[1.5px] bg-black rotate-45'></div>
                        <div className='absolute top-0 w-4 h-[1.5px] bg-black -rotate-45'></div>
                    </div>
                </Link>
                <h1 className="text-lg font-semibold">새 게시물</h1>
                <button
                    onClick={postUpload}
                    className="font-semibold text-blue-500 cursor-pointer"
                >
                    저장
                </button>
            </div>

            {/* 이미지 업로드 */}
            <div
                className={`relative flex w-full items-center justify-center overflow-hidden bg-gray-100 ${previewUrl ? null : 'aspect-square'}`}
            >
                {previewUrl ? (
                    <img
                        src={previewUrl}
                        alt="preview"
                        className="h-full w-full object-cover"
                    />
                ) : (
                    <label className="flex h-full w-full cursor-pointer items-center justify-center text-sm text-gray-400">
                        이미지 업로드
                        <input
                            type="file"
                            accept="image/*"
                            className="hidden"
                            onChange={handleImageChange}
                        />
                    </label>
                )}
            </div>

            {/* 카테고리 선택 */}
            <div>
                <label className="text-sm font-medium">카테고리</label>
                <select
                    className="mt-1 w-full rounded border px-3 py-2"
                    value={category}
                    onChange={(e) => setCategory(e.target.value)}
                >
                    <option value="환경">환경</option>
                    <option value="다문화">다문화</option>
                    <option value="장애">장애</option>
                    <option value="동물">동물</option>
                    <option value="의료">의료</option>
                    <option value="재난">재난</option>
                    <option value="노인">노인</option>
                    <option value="한부모">한부모</option>
                    <option value="아동">아동</option>
                    <option value="기타">기타</option>
                </select>
            </div>

            {/* 내용 입력 */}
            <div>
                <label className="text-sm font-medium">내용</label>
                <textarea
                    className="mt-1 w-full resize-none rounded border px-3 py-2"
                    rows={3}
                    value={content}
                    onChange={(e) => setContent(e.target.value)}
                    placeholder="내용을 입력하세요..."
                />
            </div>

            {/* 해시태그 입력 */}
            <div>
                <label className="text-sm font-medium">해시태그</label>
                <input
                    type="text"
                    className="mt-1 w-full rounded border px-3 py-2"
                    value={tagInput}
                    onChange={(e) => setTagInput(e.target.value)}
                    onKeyDown={handleTagKeyDown}
                    placeholder="해시태그를 입력 후 Enter를 누르세요"
                />
                <div className="mt-2 flex flex-wrap gap-2">
                    {tags.map((tag) => (
                        <div
                            key={tag}
                            className="flex items-center rounded-full bg-blue-100 px-2 py-1 text-sm text-blue-700"
                        >
                            # {tag}
                            <button
                                onClick={() => removeTag(tag)}
                                className="ml-1 text-xs"
                            >
                                ✕
                            </button>
                        </div>
                    ))}
                </div>
            </div>
        </div>
    );
}
