import { apiClient } from "./axios";
import { logApiEvent } from '@/utils/analytics';

// 랭킹 정보 가져오기
export const RankingInfo = async () => {
  try {
    const response = await apiClient.get('/board/rank')
    console.log("랭킹조회 가져오기 성공!", response.data.data)
    logApiEvent("RankingInfo", "success");
    return response.data.data
  }
  catch(error) {
    console.log("랭킹조회 가져오기 실패!", error)
    logApiEvent("RankingInfo", "error");
    throw error
  }
}