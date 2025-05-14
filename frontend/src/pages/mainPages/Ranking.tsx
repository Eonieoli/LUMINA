import { RankingInfo } from "@/apis/ranking";
import { useEffect, useState } from "react";
import { LuminaLogo } from "@/assets/images";
import { DefaultProfile, SmileLuna, PokerLuna } from '@/assets/images';
import { useNavigate } from "react-router-dom";

interface rankingList {
  userId: number
  nickname: string
  profileImage: string
  sumPoint: number
  rank: number
}

export default function RankingPage() {

  const [rankings, setRankings] = useState<rankingList[]>([])
  const [totalPeople, setTotalPeople] = useState<number>(-1);
  const navigate = useNavigate()

  // const myRanking = rankings[0]
  const topTen = rankings.slice(1)

  // 랭킹 정보 가져오기
  useEffect (() => {
    const fetchRankings = async () => {
      const response = await RankingInfo()
      console.log(response);
      setTotalPeople(response.totalUser)
      setRankings(response.ranks)
    }
    fetchRankings()
  },[])

  // 프로필 페이지로 이동하기
  const goToUserProfile = (userId:number) => {
    navigate(`/mypage/${userId}`)
  }


  return (
  <div className="p-6 h-full overflow-y-auto bg-white">
    {/* 로고 */}
    <img src={LuminaLogo} alt="루미나 로고" className="w-25"/>

    {/* top 3 */}
    <div className="flex w-full justify-between items-end gap-2 mb-5">

      <div className="flex flex-col justify-center items-center cursor-pointer w-full" onClick={() => {goToUserProfile(rankings[2].userId)}}>
        <img 
          src={rankings[2]?.profileImage ?? DefaultProfile} 
          alt="top2 프로필이미지" 
          className="w-8/10 rounded-full object-cover mb-2 aspect-square"
        />
        <p className="text-gray-700 text-[15px] font-semibold text-center w-[80px] truncate">{rankings[2]?.nickname}</p>
        <p className="text-gray-500">{rankings[2]?.sumPoint}</p>
      </div>

      <div className="flex flex-col justify-center items-center cursor-pointer w-full" onClick={() => {goToUserProfile(rankings[1].userId)}}>
        <img 
          src={rankings[1]?.profileImage ?? DefaultProfile} 
          alt="top1 프로필이미지" 
          className="w-10/10 aspect-square rounded-full object-cover mb-2"
        />
        <p className="text-gray-700 text-[15px] font-semibold text-center w-[100px] truncate">{rankings[1]?.nickname}</p>
        <p className="text-gray-500">{rankings[1]?.sumPoint}</p>
      </div>

      <div className="flex flex-col justify-center items-center cursor-pointer w-full" onClick={() => {goToUserProfile(rankings[3].userId)}}>
        <img 
          src={rankings[3]?.profileImage ?? DefaultProfile} 
          alt="top3 프로필이미지" 
          className="w-7/10 aspect-square rounded-full object-cover mb-2"
        />
        <p className="text-gray-700 text-[15px] font-semibold text-center  w-[80px] truncate">{rankings[3]?.nickname}</p>
        <p className="text-gray-500">{rankings[3]?.sumPoint}</p>
      </div>

    </div>

    {/* 나의 정보 */}
    <div className="mb-5">
      <div className="text-[18px] font-semibold text-gray-600">
        <div className="flex items-center gap-2">
          <img src={PokerLuna} alt="루나 이미지" className="w-10" />
          {rankings[0]?.nickname}님의 랭킹
        </div>
        <div className="bg-[#9c97fa] w-full p-5 h-23 sm:h-20 rounded-2xl mt-2d pr-5 pl-5 text-white mt-3">

          <div className="flex sm:flex-row justify-evenly h-full items-center gap-5">

            <img
              src={rankings[0]?.profileImage ?? DefaultProfile}
              alt="나의 프로필 이미지" 
              className="w-18 sm:w-15 aspect-square object-cover bg-white rounded-full"
            />

            <div className="flex flex-col sm:flex-row sm:gap-10 items-center gap-2">
              <p className="sm:text-xl text-[15px]">
                {rankings[0]?.rank <= 3
                  ? `${rankings[0]?.nickname}님! Top3 에요! 👑`
                  : `${rankings[0]?.nickname}님! 조금만 더 힘내요! 💪`
                }
              </p>
              <div>
                <p className="flex justify-center sm:justify-end font-semibold sm:text-[16px] text-[14px]">
                  <span className="font-normal ">
                    {rankings[0]?.sumPoint}점 /&nbsp;
                  </span>
                  {rankings[0]?.rank}위
                </p>
                <p className="sm:text-sm font-light text-[14px]">
                  전체 <span className="font-bold">{totalPeople}명</span> 중 상위 
                  <span className="font-bold"> {Math.round(rankings[0]?.rank / totalPeople * 100)}%</span>
                </p>
              </div>
            
            </div>  
          </div>
        </div>
      </div>
    </div>

    {/* top10 정보 */}
    <div>

      <div className="flex items-center gap-2 text-[18px] font-semibold text-gray-600 mb-3">
        <img src={SmileLuna} alt="루나 이미지" className="w-10" />
        Top 10
      </div>

      <div className="space-y-2">
        {topTen.map((user) => (
          <div
            key={`${user.userId}`}
            className="flex items-center justify-between border-2 border-[#9C97FA] rounded-2xl px-4 py-2 cursor-pointer"
            onClick={()=> goToUserProfile(user.userId)}
          >
            {/* 왼쪽: 순위, 이미지, 닉네임 */}
            <div className="flex items-center gap-3">
              <span className="text-[20px] font-semmibold text-[#5D56F1] w-5 text-center">{user.rank}</span>
              <img
                src={user.profileImage ?? DefaultProfile}
                alt="프로필"
                className="w-10 h-10 rounded-full object-cover bg-white"
              />
              <span className="text-sm font-medium text-gray-600 max-w-[100px] truncate">
                {user.nickname}
              </span>
            </div>

            {/* 오른쪽: 점수 */}
            <div className="text-m font-semibold text-gray-600">{user.sumPoint}점</div>
          </div>
        ))}
      </div>
    </div>
  </div>

)}
