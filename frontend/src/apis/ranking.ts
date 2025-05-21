import { apiClient } from "./axios";
import { logApiEvent } from '@/utils/analytics';

// 랭킹 정보 가져오기
export const RankingInfo = async () => {
  try {
    const response = await apiClient.get('/board/rank')
    logApiEvent("RankingInfo", "success");
    return response.data.data
  }
  catch(error) {
    logApiEvent("RankingInfo", "error");
    throw error
  }
}