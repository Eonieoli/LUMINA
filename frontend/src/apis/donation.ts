import { apiClient } from './axios';
import { logApiEvent } from '@/utils/analytics';

// 유저 포인트, 닉네임, id 조회
export const getPointInfo = async () => {
    try {
        const response = await apiClient.get('/user/point');
        logApiEvent("getPointInfo", "success");
        return response.data.data;
    } catch (error) {
        logApiEvent("getPointInfo", "error");
        throw error;
    }
};

//추천 기부처, 관심 기부처 조회
export const getFavoriteDonations = async () => {
    try {
        const response = await apiClient.get('/donation/me');
        logApiEvent("getFavoriteDonations", "success");
        return response.data.data;
    } catch (error) {
        logApiEvent("getFavoriteDonations", "error");
        throw error;
    }
};

// 전체 기부처 조회
export const getAllDonations = async (pageNum: number) => {
  try{
    const response = await apiClient.get("/donation", {params:{pageNum}})
    const { donations, totalPages } = response.data.data
    logApiEvent("getAllDonations", "success");
    return { donations, totalPages}
  }
  catch (error) {
    logApiEvent("getAllDonations", "error");
    throw error
  }
}

// 기부처 검색하기 
export const getSearchDonations = async(keyword: string, pageNum: number) => {
  try {
    const response = await apiClient.get("/donation/search", {params: {keyword, pageNum}})
    const {donations, totalPages} = response.data.data
    logApiEvent("getSearchDonations", "success");
    return { donations, totalPages}
  }
  catch (error) {
    logApiEvent("getSearchDonations", "error");
    throw error
  }
}

// 기부처 상세페이지
export const getDonationDetail = async(donationId:string) => {
  try {
    const response = await apiClient.get(`/donation/${donationId}`)
    logApiEvent("getDonationDetail", "success");
    return response.data.data
  }
  catch(error) {
    logApiEvent("getDonationDetail", "error");
    throw error
  }
}

// 기부처 구독 및 구독 취소하기
export const toggleDonationSubscribe = async(donationId: number) => {
  try {
    await apiClient.post(`/donation/${donationId}`)
    logApiEvent("toggleDonationSubscribe", "success");
  }
  catch(error){
    logApiEvent("toggleDonationSubscribe", "error");
    throw error
  }
}

// 기부하기
export const postDonatePoint = async(donationId: number, point: number) => {
  try {
    const response = await apiClient.post('/donation', {donationId:donationId, point:point})
    logApiEvent("postDonatePoint", "success");
    return response.data
  }
  catch(error){
    logApiEvent("postDonatePoint", "error");
    throw error
  }
}

// 유저 리워드 조회
export const getUserReward = async () => {
  try {
    const response = await apiClient.get('/user/reward');
    logApiEvent("getUserReward", "success");
    return response.data
  } catch (error) {
    logApiEvent("getUserReward", "error");
    throw error
  }
}

// 유저 기부 내역 조회
export const getUserDonations = async () => {
  try {
    const response = await apiClient.get('/user/donation');
    logApiEvent("getUserDonations", "success");
    return response.data
  } catch (error) {
    logApiEvent("getUserDonations", "error");
    throw error
  }
}