import { apiClient } from "./axios";

// 유저 포인트 조회
export const getPointInfo = async() => {
  try{
    const response = await apiClient.get("/user/point")
    console.log("유저 포인트 가져오기 성공!", response)
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
    console.log("관심 기부처 가져오기 성공! ", response)
    return response.data
  }
  catch(error){
    console.log("관심 기부처 가져오기 실패! ", error)
    throw error
  }
}

// 기부처 검색하기 
export const getSearchDonations = async(keyword: string, pageNum: number) => {
  try {
    const response = await apiClient.get("/donation/search", {params: {keyword, pageNum}})
    console.log("기부처 검색하기 성공!", response.data.data.donations)
    return response.data.data.donations
  }
  catch (error) {
    console.log("기부처 검색 실패!", error)
    throw error
  }
}
