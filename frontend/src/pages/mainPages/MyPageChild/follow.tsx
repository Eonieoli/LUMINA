import { getFollowers, getFollowings, followToggle, deleteFollwer} from "@/apis/follow"
import { useEffect, useState} from "react"
import { useLocation, useNavigate, useParams } from "react-router-dom"
import { BackIcon, XIcon } from "@/assets/images"
import ProfileBtn from "@/components/profile/button"
import { useAuthStore } from "@/stores/auth"
import { DefaultProfile } from "@/assets/images"

export interface followProps {
  isFollowing: boolean
  nickname: string
  userId: number
  profileImage: string
}

export default function FollowPage () {

  // 나의 아이디 
  const authData = useAuthStore();
  const myUserId = authData.data.userId

  const location = useLocation()
  const navigate = useNavigate()
  
  // 내가 팔로워 목록을 보려는 유저 정보들
  const {profileUserId} = useParams()
  const userId = Number(profileUserId)
  const profileNickname = (location.state as {nickname?: string})?.nickname ?? ""
  const profileFollowCnt = (location.state as {followers?: number})?.followers ?? 0
  const profileFollowingCnt = (location.state as {followings?: number})?.followings ?? 0
  const isfollowfollowing = (location.state as {info?: string})?.info ?? ""

  const [followers, setFollowers] = useState<followProps[]>([])
  const [followings, setFollowings] = useState<followProps[]>([])
  const [tab, setTab] = useState<string>(isfollowfollowing)
  

  // 팔로워 목록 조회
  useEffect(() => {
    if (!userId || tab !== 'followers') return

    const fetchGetFollowers = async () => {
      try {
        const response = await getFollowers(userId)
        setFollowers(response)
      }  catch(error){
        console.log("팔로워 목록 조회 실패", error)
      }
    }
    fetchGetFollowers()
  },[tab])

  // 팔로잉 목록 조회
  useEffect(() => {
    if (!userId || tab !== 'followings') return
    const fetchGetFollowings = async () => {
      try {
        const response = await getFollowings(userId)
        setFollowings(response)
      }  catch(error){
        console.log("팔로잉 목록 조회 실패", error)
      }
    }
    fetchGetFollowings()
  },[tab])

  //뒤로가기를 눌렀을 때
  const goToBack = () => {
    navigate(-1)
  }

  // 팔로우 팔로잉 버튼을 눌렀다면
  const handlefollowToggle = async(followingId:number) => {

    setFollowers((prevFollowers) =>
      prevFollowers.map((f) =>
        f.userId === followingId ? { ...f, isFollowing: !f.isFollowing } : f
      )
    )
    setFollowings((prevFollowings) => 
      prevFollowings.map((f) =>
      f.userId === followingId ? {...f, isFollowing: !f.isFollowing} : f
      )
    )

    try {
      await followToggle(followingId)
    }
    catch (error){
      console.log("팔로우 팔로잉 조회에서 토글 실패")
    }
  }
  
  // 내가 나의 팔로워 목록을 조회했을 때 특정 팔로워를 삭제
  const DeleteUser = (deleteUserId:number) => {
    deleteFollwer(deleteUserId)
    setFollowers((prev) => 
      prev.filter((follower) => follower.userId !== deleteUserId)
    )
  }


  return (
    <div className="w-full flex flex-col relative">

      {/* 목록 상단 */}
      <div className="fixed top-0 w-full md:[width:388px] pt-6 pb-2 bg-white z-50 ">
        {/* 상단바 */}
        <div className="flex items-center h-6 relative ml-6 mr-6">
          <img 
            src={BackIcon} 
            alt="뒤로가기" 
            className="absolute left-0 w-5"
            onClick={goToBack}  
          />
          <p 
            className=" absolute left-1/2 -translate-x-1/2 font-normal text-[22px]">{profileNickname}</p>
        </div>

        {/* 탭 버튼 */}
        <div className="flex mt-4">
          <button
            className={` w-1/2 p-1 ${tab === 'followers' ? 'text-gray-700 border-b-2 border-b-gray-700 font-semibold' :  'text-gray-500'}`}
            onClick={() => setTab('followers')}
          >
            {profileFollowCnt} 팔로워 
          </button>
          <button
            className={` w-1/2 p-1 ${tab === 'followings' ? 'text-gray-700 border-b-2 border-b-gray-700 font-semibold' :  'text-gray-500'}`}
            onClick={() => setTab('followings')}
          >
            {profileFollowingCnt} 팔로잉
          </button>
        </div>
      </div>

      {/* 팔로워 팔로잉 목록 */}
      <div className="pl-4 pr-4 w-full pt-28 md:pb-0">
        {tab === 'followers' ? (
          <div className="flex flex-col h-full">
            {followers.length === 0 ? (
            <p className="flex items-center justify-center w-full flex-1 text-center ">팔로워가 없습니다.</p>
            ) : (
              followers.map((follower) => 
                {
                  return (
                    // 유저들 정보 조회
                    <div 
                      key={follower.userId} 
                      className="flex items-center mb-4 justify-between relative">
                      {/* 프로필 사진, 닉네임 */}
                      <div className="flex items-center">
                        <img 
                          src={follower.profileImage || DefaultProfile} alt="프로필이미지" 
                          className="w-10 h-10 object-cover rounded-full mr-5 bg-white"
                        />
                        <p>{follower.nickname}</p>
                      </div>
                      {/* 버튼과 삭제버튼 */}
                      <div className="flex items-center absolute right-0 w-full justify-end gap-2">
                        {follower.isFollowing && follower.userId !== myUserId &&
                          <ProfileBtn 
                            text="팔로잉" textColor="text-white" bgColor="bg-[#9C97FA]"
                            onClick={()=> handlefollowToggle(follower.userId)}/>
                        }
                        {follower.isFollowing === false && follower.userId !== myUserId &&
                          <ProfileBtn 
                            text="팔로우" textColor="text-[#5D56F1]" bgColor="bg-white"
                            onClick={() => handlefollowToggle(follower.userId)}/> 
                        }
                        { myUserId === userId && (
                          <div className=" h-5 flex items-center justify-center" onClick={() => DeleteUser(follower.userId)}>
                            <img src={XIcon} alt="취소" className="w-2" />
                          </div>
                        )}
                      </div>
                    </div>
                  )
                }                  
              ))
            }
          </div>
        ) : (
          <div className="flex flex-col h-full">
            {followings.length === 0 ? (
            <p className="flex items-center justify-center w-full flex-1 text-center ">팔로잉이 없습니다.</p>
            ) : (
              followings.map((following) => 
                {
                  return (
                    // 유저들 정보 조회
                    <div 
                      key={following.userId} 
                      className="flex items-center mb-4 justify-between">
                      <div className="flex items-center">
                        <img 
                          src={following.profileImage || DefaultProfile} alt="프로필이미지" 
                          className="w-10 h-10 object-cover rounded-full mr-5 bg-white"
                        />
                        <p>{following.nickname}</p>
                      </div>
                      {following.isFollowing && following.userId !== myUserId &&
                        <ProfileBtn 
                          text="팔로잉" textColor="text-white" bgColor="bg-[#9C97FA]"
                          onClick={()=> handlefollowToggle(following.userId)}/>
                      }
                      {following.isFollowing === false && following.userId !== myUserId &&
                        <ProfileBtn 
                          text="팔로우" textColor="text-[#5D56F1]" bgColor="bg-white"
                          onClick={() => handlefollowToggle(following.userId)}/> 
                      }
                    </div>
                  )
                }                  
              ))
            }
          </div>          
        )}
      </div>

    </div>
  )
}