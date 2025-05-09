import { useAuthStore } from "@/stores/auth"
import { useState } from "react";
import { DefaultProfile } from "@/assets/images";
import { profileEdit } from "@/apis/auth";
import { useNavigate } from "react-router-dom";
import EditInput from "@/components/profile/editInput";
import EditButton from "@/components/profile/editButton";

export default function ProfileEditPage () {

  const navigate = useNavigate()
  const authData = useAuthStore();

  //상태 관리
  const [nickname, setNickname] = useState<string>(authData.data.nickname)
  const [message, setMessage] = useState<string>(authData.data.message)
  const [profileImageFile, setProfileImageFile] = useState<File | string | null>(authData.data.profileImage)

  // 이미지 선택
  const handleImageChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0]
    if(file) {
      setProfileImageFile(file)
    }
  }

  //프로필 수정 api 부르기
  const handleEditProfile = async () => {
    console.log("프로필 수정해볼게!!")
    try {
      const formData = new FormData()
      formData.append("nickname", nickname)
      formData.append("message", message)
      if(profileImageFile instanceof File) {
        formData.append("profileImageFile", profileImageFile)
      }
      console.log("파일", profileImageFile);
      console.log("파일 타입", profileImageFile instanceof File);
      await profileEdit(formData)
      console.log("프로필 수정 성공")
      navigate(-1)
    } 
    catch (error) {
      console.error(error)
    }
  }

  return (
    <div className="bg-white w-full h-full p-6 relative">
      <div className="w-full flex flex-col justify-center items-center">

          {/* 프로필 이미지 */}
          <label className="m-15">
            <img 
              src={profileImageFile instanceof File ? URL.createObjectURL(profileImageFile) : authData.data.profileImage || DefaultProfile} 
              alt="프로필 이미지" 
              className="w-40 bg-white rounded-full object-cover aspect-square border-2 border-gray-200"
            />
            <input type="file" accept="image/*" onChange={handleImageChange} className="hidden"/>
          </label>

          {/* 닉네임 */}
          <EditInput title="닉네임" value={nickname} onChange={(e) => setNickname(e.target.value)} />

          {/* 상태메세지 */}
          <EditInput title="상태메세지" value={message} onChange={(e) => setMessage(e.target.value)} />        


        {/* 버튼 */}
        <div className="flex w-full pl-6 pr-6 justify-between mt-15 absolute bottom-5 bg-white">
          <EditButton text="취소하기" onClick={() => {navigate(-1)}} bgColor="bg-white" textColor="text-[#5D56F1]" />
          <EditButton text="수정하기" onClick={() => handleEditProfile()} textColor="text-white" bgColor="bg-[#9C97FA]" />            
        </div>

      </div>
    </div>
  )
}