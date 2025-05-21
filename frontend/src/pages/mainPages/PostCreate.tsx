import { createPost } from '@/apis/board';
import { elizaBoard } from '@/apis/eliza';
import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { toast } from 'sonner';

export default function PostCreate() {
    const [image, setImage] = useState<File | null>(null);
    const [previewUrl, setPreviewUrl] = useState<string | null>(null);
    const [content, setContent] = useState('');
    const [tagInput, setTagInput] = useState('');
    const [tags, setTags] = useState<string[]>([]);
    const [isCreating, setIsCreating] = useState<boolean>(false);
    const navigate = useNavigate();
    
    useEffect(() => {
        return () => {
            if (previewUrl) {
                URL.revokeObjectURL(previewUrl);
            }
        };
    }, [previewUrl]);

    const postUpload = async () => {
        if (isCreating) return;

        if (!content.trim()) {
            toast.error('내용을 입력해주세요.');
            return;
        }

        try {
            setIsCreating(true);
            toast.promise(createPost({
                postImageFile: image,
                hashtag: tags.join(","),
                postContent: content,
            }), {
                loading: '카테고리 추출 중...',
                success: (res) => {
                    console.log(res);
                    toast.promise(elizaBoard(res.data.postId), {
                        loading: '루나가 게시글 분석 후 댓글 생성 중...',
                        success: () => {
                            navigate('/');
                            return '리워드 지급 및 댓글 생성 완료!';
                        }
                    })
                    return `업로드 완료!`;
                },
                error: 'Error',
            });


        } catch (error) {
            setIsCreating(false);
            console.error('게시물 업로드 실패:', error);
            toast.error('게시물 업로드 실패');
        }
    };

    const handleImageChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const file = e.target.files?.[0];
        if (file) {
            if (file.size > 5 * 1024 * 1024) { // 5MB 제한
                toast.error('이미지 파일은 5MB 이하만 업로드 가능합니다.');
                return;
            }

            if (previewUrl) {
                URL.revokeObjectURL(previewUrl);
            }

            const newUrl = URL.createObjectURL(file);
            setImage(file);
            setPreviewUrl(newUrl);
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
    
    const handleRemoveImage = () => {
        if (previewUrl) {
            URL.revokeObjectURL(previewUrl);
        }
        setImage(null);
        setPreviewUrl(null);
    };

    return (
        <div className="flex min-h-full w-full flex-col gap-4 p-4 bg-white">
            {/* <Toaster /> */}
            {/* 헤더 */}
            <div className="flex items-center justify-between">
                <div
                    onClick={() => navigate('/')}
                    className="relative flex h-4 w-4 cursor-pointer gap-x-1 py-2"
                >
                    <div className="absolute top-1/2 left-0 h-[2px] w-4 -translate-y-1/2 rotate-45 bg-black"></div>
                    <div className="absolute top-1/2 left-0 h-[2px] w-4 -translate-y-1/2 -rotate-45 bg-black"></div>
                </div>
                <h1 className="text-lg font-semibold">새 게시물</h1>
                <button
                    onClick={postUpload}
                    className="font-semibold text-blue-500 cursor-pointer"
                >
                    저장
                </button>
            </div>

            {/* 이미지 업로드 */}
            <div>
                <label className="text-sm text-gray-600 font-medium">이미지 (선택)</label>
                <div
                    className={`relative flex w-full items-center justify-center overflow-hidden bg-gray-100 ${previewUrl ? null : 'aspect-square'}`}
                >
                    {previewUrl ? (
                        <div className='w-full flex justify-center items-center aspect-square'>
                            <img
                                src={previewUrl}
                                alt="preview"
                                className="h-full object-contain"
                            />
                            <button
                                type="button"
                                onClick={handleRemoveImage}
                                className="absolute top-2 right-2 rounded-full bg-black bg-opacity-60 px-2 py-1 text-xs text-white"
                            >
                                삭제
                            </button>
                        </div>
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
            </div>

            {/* 내용 입력 */}
            <div>
                <label className="text-sm text-gray-600 font-medium">내용</label>
                <textarea
                    className="mt-1 w-full resize-none rounded border-2 border-gray-400 px-3 py-2"
                    rows={3}
                    value={content}
                    onChange={(e) => setContent(e.target.value)}
                    placeholder="내용을 입력하세요."
                />
            </div>

            {/* 해시태그 입력 */}
            <div>
                <label className="text-gray-600 text-sm font-medium">해시태그 (선택)</label>
                <input
                    type="text"
                    className="mt-1 w-full rounded border-2 border-gray-400 px-3 py-2"
                    value={tagInput}
                    onChange={(e) => setTagInput(e.target.value)}
                    onKeyDown={handleTagKeyDown}
                    placeholder="해시태그를 입력 후 Enter를 누르세요."
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
