import { Route, Routes } from "react-router-dom"
import { HomePage, SearchPage, DonatePage, RankingPage, MyPage} from "@/pages/mainPages"
import { Navbar } from "@/components"
import { useEffect } from "react"
import { useAuthStore } from "@/stores/auth"
import { getMyProfile } from "@/apis/auth"
import DonationSearchResultPage from "./mainPages/DonateChild/DonationSearchResult"

export default function Main() {
  const authData = useAuthStore();
  
  useEffect(() => {
    const fetchProfile = async () => {
      if (authData.data.userId === -1) {
        try {
          const response = await getMyProfile(); // 함수 실행
          authData.setData(response.data); // 받아온 데이터로 업데이트
        } catch (error) {
          console.error("프로필 불러오기 실패:", error);
        }
      }
    };

    fetchProfile();
  }, [authData]);


  return (
    <>
      {/* 옆에 네비게이션 있으니까 왼쪽과 하단에 패딩값을 넣음 */}
      <div className="flex justify-center pb-20 md:pb-0 md:pl-20"> 
        <div className="relative h-dvh overflow-y-scroll w-full md:w-[468px]">
          <Routes>
            {/* 경로가 정확히 부모 경로와 일치할 때 홈페이지를 보여줌 */}
            <Route index element={<HomePage/>} />
            <Route path="search" element={<SearchPage/>} />        
            <Route path="donate" element={<DonatePage/>} />        
            <Route path="ranking" element={<RankingPage/>} />        
            <Route path="mypage" element={<MyPage/>} />

            <Route path="donate/research/:keyword" element={<DonationSearchResultPage/>}/>
          </Routes>
        </div>
      </div>
      <Navbar/>
    </>
  )
}