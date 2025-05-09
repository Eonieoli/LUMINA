import { apiClient } from './axios';

// 유저 포인트, 닉네임, id 조회
export const getPointInfo = async () => {
    try {
        const response = await apiClient.get('/user/point');
        console.log(
            response.data.data.nickname,
            '유저 포인트 가져오기 성공! 포인트:',
            response.data.data.point, 
        );
        return response.data.data;
    } catch (error) {
        console.log('유저 포인트 가져오기 실패!', error);
        throw error;
    }
};

//추천 기부처, 관심 기부처 조회
export const getFavoriteDonations = async () => {
    try {
        const response = await apiClient.get('/donation/me');
        return response.data.data;
    } catch (error) {
        console.log("추천, 관심 기부처 가져오기 실패!", error)
        throw error;
    }
};

// 기부처 검색하기 
export const getSearchDonations = async(keyword: string, pageNum: number) => {
  try {
    const response = await apiClient.get("/donation/search", {params: {keyword, pageNum}})
    console.log("기부처 검색하기 성공!", response.data.data.donations)
    const {donations, totalPages} = response.data.data
    return { donations, totalPages}
  }
  catch (error) {
    console.log("기부처 검색 실패!", error)
    throw error
  }
}

// 기부처 상세페이지
export const getDonationDetail = async(donationId:string) => {
  try {
    const response = await apiClient.get(`/donation/${donationId}`)
    console.log("기부처 상세 정보 가져오기 성공!", response.data.data)
    return response.data.data
  }
  catch(error) {
    console.log("기부처 상세 정보 가져오기 실패!", error)
    throw error
  }
}

// 기부처 구독 및 구독 취소하기
export const toggleDonationSubscribe = async(donationId: number) => {
  try {
    const response = await apiClient.post(`/donation/${donationId}`)
    console.log("구독 토글 성공!", response.data.message)
  }
  catch(error){
    console.log("구독 토글 실패!", error)
    throw error
  }
}

// 기부하기
export const postDonatePoint = async(donationId: number, point: number) => {
  try {
    const response = await apiClient.post('/donation', {donationId:donationId, point:point})
    console.log("기부하기 성공!!", response.data.message)
    return response.data
  }
  catch(error){
    console.log("기부하기 실패", error)
    throw error
  }
}
