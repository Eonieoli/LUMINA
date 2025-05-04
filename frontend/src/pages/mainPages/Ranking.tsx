import { RankingInfo } from "@/apis/ranking";
import { useEffect, useState } from "react";

interface rankingList {
  userId: number
  nickname: string
  profileImage: string
  sumPoint: number
  rank: number
}

export default function RankingPage() {

  const [rankings, setRankings] = useState<rankingList[]>([])

  // const myRanking = rankings[0]
  const topTen = rankings.slice(1)

  // 랭킹 정보 가져오기
  useEffect (() => {
    const fetchRankings = async () => {
      try {
        const response = await RankingInfo()
        setRankings(response)
      }
      catch (error) {}
    }
    fetchRankings()
  },[])


  return (
  <div>
    {topTen.map((user) => (
      <div key={user.userId}>
        {user.nickname}
      </div>
    ))}
  </div>

)}
