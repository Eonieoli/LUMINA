import { apiClient } from "./axios";

// 팔로잉 추가, 삭제
export const followToggle = async (followingId:number) => {
  try {
    const response = await apiClient.post('/following', {
      followingId:followingId
    })
    console.log(response.data.message)
    return response.data
  }
  catch(error) {
    console.log("팔로우 토글 실패!", error)
    throw error
  }
}