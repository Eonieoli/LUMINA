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

// 특정 유저 팔로워 조회 
export const getFollowers = async (profileUserId: number) => {
  try {
    const response = await apiClient.get(`/follower`, {
      params : {
        userId: profileUserId,
    }})
    console.log(`${profileUserId}`,"님의 팔로워 리스트 가져오기 성공",response.data.data)
    return response.data.data
  }
  catch(error){
    console.log("팔로워 조회 실패!", error)
    throw error
  }
}

// 특정 유저 팔로잉 조회
export const getFollowings = async (profileUserId: number) => {
  try {
    const response = await apiClient.get(`/following`, {
      params: {
        userId: profileUserId,
      }
    })
    console.log(`${profileUserId}`,"님의 팔로잉 리스트 가져오기 성공",response.data.data)
    return response.data.data
  }
  catch(error){
    console.log("팔로잉 조회 실패!", error)
    throw error
  }
}

// 내가 나의 팔로워를 조회했을 때 팔로워 삭제하기
export const deleteFollwer = async (userId: number) => {
  try {
    await apiClient.delete(`/follower/${userId}`)
    console.log(`${userId}님 팔로워에서 삭제`)
  }
  catch (error) {
    console.log("내 팔로워 삭제 실패", error)
    throw error
  }
}