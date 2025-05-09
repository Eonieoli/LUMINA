import { apiClient } from "./axios";

// 랭킹 정보 가져오기
export const RankingInfo = async () => {
  try {
    const response = await apiClient.get('/board/rank')
    console.log("랭킹조회 가져오기 성공!", response.data.data)
    return response.data.data
  }
  catch(error) {
    console.log("랭킹조회 가져오기 실패!", error)
    throw error
  }
}