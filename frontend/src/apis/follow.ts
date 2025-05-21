import { apiClient } from "./axios";
import { logApiEvent } from '@/utils/analytics';

// 팔로잉 추가, 삭제
export const followToggle = async (followingId:number) => {
  try {
    const response = await apiClient.post('/following', {
      followingId:followingId
    })
    logApiEvent("followToggle", "success");
    return response.data
  }
  catch(error) {
    logApiEvent("followToggle", "error");
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
    logApiEvent("getFollowers", "success");
    return response.data.data
  }
  catch(error){
    logApiEvent("getFollowers", "error");
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
    logApiEvent("getFollowings", "success");
    return response.data.data
  }
  catch(error){
    logApiEvent("getFollowings", "error");
    throw error
  }
}

// 내가 나의 팔로워를 조회했을 때 팔로워 삭제하기
export const deleteFollwer = async (userId: number) => {
  try {
    await apiClient.delete(`/follower/${userId}`)
    logApiEvent("deleteFollwer", "success");
  }
  catch (error) {
    logApiEvent("deleteFollwer", "error");
    throw error
  }
}