import { useEffect, useState } from "react";
import { DefaultProfile, PencilIcon, CircleXIcon } from "@/assets/images";
import { profileEdit } from "@/apis/auth";
import { useLocation, useNavigate } from "react-router-dom";
import { getUserProfile } from "@/apis/auth";
import { useAuthStore } from "@/stores/auth";
import EditInput from "@/components/profile/editInput";
import EditButton from "@/components/profile/editButton";

export default function ProfileEditPage () {

  const navigate = useNavigate()
  const location = useLocation()
  const authData = useAuthStore()

  //상태 관리
  const [nickname, setNickname] = useState<string>("")
  const [message, setMessage] = useState<string>("")
  const [profileImageFile, setProfileImageFile] = useState<File | string | null>(null)

  const [nicknameError, setNicknameError] = useState<string>("")
  const [messageError, setMessageError] = useState<string>("")

  // 이미지 선택
  const handleImageChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0]
    if(file) {
      setProfileImageFile(file)
    }
  }

  //나의 정보 가져오기
  useEffect(() => {
    const fetchMyData = async () => {
      const response = await getUserProfile(authData.data.userId)
      setNickname(response.data.nickname)
      setMessage(response.data.message)
      setProfileImageFile(response.data.profileImage)
    }
    fetchMyData()
  },[location])

  //프로필 수정 api 부르기
  const handleEditProfile = async () => {

    let changeValid = true

    if(nickname.trim() === "") {
      setNicknameError("닉네임을 입력해주세요.")
      changeValid = false
    } else{
      setNicknameError("")
    }

    if(message.trim() === "") {
      setMessageError("상태 메세지를 입력해주세요.")
      changeValid = false
    } else{
      setMessageError("")
    }

    if (!changeValid) return;

    try {
      const formData = new FormData()
      formData.append("nickname", nickname)
      formData.append("message", message)

      // 사진을 선택했을 경우
      if(profileImageFile instanceof File) {
        formData.append("profileImageFile", profileImageFile)
        formData.append("defaultImage", "false")
      }
      // 이미지를 삭제했을 경우
      else if (profileImageFile === null) {
        formData.append("defaultImage", "true")
      } 
      // 이미지 변경 안했을 경우
      else {
        formData.append("defaultImage", "false")        
      }

      console.log("파일", profileImageFile);
      console.log("파일", typeof profileImageFile);
      await profileEdit(formData)
      console.log("프로필 수정 성공")
      navigate(`/mypage/${authData.data.userId}`)
    } 
    catch (error) {
      console.error(error)
    }
  }

  // 취소하기를 눌렀다면
  const goToMypage = () => {
    navigate(`/mypage/${authData.data.userId}`)
  }

  return (
    <div className="bg-white w-full h-full p-6 relative">
      <div className="w-full flex flex-col justify-center items-center">

          {/* 프로필 이미지 */}
          <div className="relative">
            <label className="m-15 cursor-pointer">
              <div className="relative">
                <img 
                  src={profileImageFile instanceof File ? URL.createObjectURL(profileImageFile) : profileImageFile || DefaultProfile} 
                  alt="프로필 이미지" 
                  className="w-40 bg-white rounded-full object-cover aspect-square border-2 border-gray-200"
                />
                <img src={PencilIcon} alt="수정아이콘" className=" absolute w-5 top-1/2 left-1/2 transform -translate-x-1/2 -translate-y-1/2" />
              </div>
              <input type="file" accept="image/*" onChange={handleImageChange} className="hidden"/>
            </label>
            <img src={CircleXIcon} alt="이미지 삭제" onClick={() => setProfileImageFile(null)} className="w-4 absolute top-5 right-0 z-50 cursor-pointer"/>
          </div>

          <div className="w-2/3">
            {/* 닉네임 */}
            <EditInput title="닉네임" value={nickname} onChange={(e) => setNickname(e.target.value)} />
            {nicknameError && <p className="text-sm text-red-500 ml-1">{nicknameError}</p>}

            {/* 상태메세지 */}
            <EditInput title="상태메세지" value={message} onChange={(e) => setMessage(e.target.value)} />        
            {messageError && <p className="text-sm text-red-500 ml-2">{messageError}</p>}
          </div>

        {/* 버튼 */}
        <div className="flex w-full pl-6 pr-6 justify-between mt-15 absolute bottom-5 bg-white">
          <EditButton text="취소하기" onClick={() => {goToMypage()}} bgColor="bg-white" textColor="text-[#5D56F1]" />
          <EditButton text="수정하기" onClick={() => handleEditProfile()} textColor="text-white" bgColor="bg-[#9C97FA]" />            
        </div>

      </div>
    </div>
  )
}