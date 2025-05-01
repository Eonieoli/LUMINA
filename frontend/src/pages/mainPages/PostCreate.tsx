import { createPost } from "@/apis/board";
import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";

export default function PostCreate() {
  const [image, setImage] = useState<File | null>(null);
  const [previewUrl, setPreviewUrl] = useState<string | null>(null);
  const [content, setContent] = useState("");
  const [category, setCategory] = useState("환경");
  const [tagInput, setTagInput] = useState("");
  const [tags, setTags] = useState<string[]>([]);
  const navigate = useNavigate();


  const postUpload = async () => {
    if (!content.trim()) {
      alert("내용을 입력해주세요.");
      return;
    }
  
    try {
      await createPost({
        postImageFile: image,
        categoryName: category,
        hashtag: tags,
        postContent: content,
      });
  
      navigate('/');
    } catch (error) {
      console.error("게시물 업로드 실패:", error);
      alert("게시물 업로드에 실패했습니다.");
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
    if (e.key === "Enter" && tagInput.trim()) {
      e.preventDefault();
      if (!tags.includes(tagInput.trim())) {
        setTags([...tags, tagInput.trim()]);
      }
      setTagInput("");
    }
  };

  const removeTag = (tag: string) => {
    setTags(tags.filter((t) => t !== tag));
  };

  return (
    <div className="w-full h-full p-4 flex flex-col gap-4">
      {/* 헤더 */}
      <div className="flex justify-between items-center">
        <Link to="/">
            x
        </Link>
        <h1 className="text-lg font-semibold">새 게시물</h1>
        <button onClick={postUpload} className="text-blue-500 font-semibold">저장</button>
      </div>

      {/* 이미지 업로드 */}
      <div className={`w-full bg-gray-100 flex justify-center items-center relative overflow-hidden ${previewUrl ? null : "aspect-square"}`}>
        {previewUrl ? (
          <img src={previewUrl} alt="preview" className="object-cover w-full h-full" />
        ) : (
          <label className="w-full h-full flex justify-center items-center text-sm text-gray-400 cursor-pointer">
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
          className="w-full border rounded px-3 py-2 mt-1"
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
          className="w-full border rounded px-3 py-2 mt-1 resize-none"
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
          className="w-full border rounded px-3 py-2 mt-1"
          value={tagInput}
          onChange={(e) => setTagInput(e.target.value)}
          onKeyDown={handleTagKeyDown}
          placeholder="해시태그를 입력 후 Enter를 누르세요"
        />
        <div className="flex flex-wrap mt-2 gap-2">
          {tags.map((tag) => (
            <div key={tag} className="flex items-center bg-blue-100 text-blue-700 px-2 py-1 rounded-full text-sm">
              # {tag}
              <button onClick={() => removeTag(tag)} className="ml-1 text-xs">✕</button>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
