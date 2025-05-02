import { apiClient } from './axios';

// 유저 포인트 조회
export const getPointInfo = async () => {
    try {
        const response = await apiClient.get('/user/point');
        console.log(
            '유저 포인트 가져오기 성공! 포인트:',
            response.data.data.point
        );
        return response.data.data.point;
    } catch (error) {
        console.log('유저 포인트 가져오기 실패!', error);
        throw error;
    }
};

//관심 기부처 조회
export const getFavoriteDonations = async () => {
    try {
        const response = await apiClient.get('/donation/me');
        console.log('관심 기부처 가져오기 성공! ', response.data.data);
        return response.data.data;
    } catch (error) {
        console.log('관심 기부처 가져오기 실패! ', error);
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
