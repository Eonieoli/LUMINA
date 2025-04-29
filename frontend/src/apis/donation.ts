import { apiClient } from "./axios";

//관심 기부처 조회
export const getFavoriteDonations = async() => {
  try {
    const response = await apiClient.get("/donation/me")
    return response.data;
  }
  catch(error){
    console.log("관심 기부처 가져오기 실패! ")
    throw error
  }
}