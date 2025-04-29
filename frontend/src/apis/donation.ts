import { apiClient } from "./axios";

// 유저 포인트 조회
export const getPointInfo = async() => {
  try{
    const response = await apiClient.get("/user/point")
    return response.data
  }
  catch(error){
    console.log("유저 포인트 가져오기 실패!", error)
    throw error
  }
}

//관심 기부처 조회
export const getFavoriteDonations = async() => {
  try {
    const response = await apiClient.get("/donation/me")
    return response.data;
  }
  catch(error){
    console.log("관심 기부처 가져오기 실패! ", error)
    throw error
  }
}

