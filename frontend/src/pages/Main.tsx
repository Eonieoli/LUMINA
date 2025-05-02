import { Route, Routes } from "react-router-dom"
import { HomePage, SearchPage, DonatePage, RankingPage, MyPage, PostCreatePage, DonationDetailPage} from "@/pages/mainPages"
import { Navbar } from "@/components"
import DonationSearchResultPage from "./mainPages/DonateChild/DonationSearchResult"

export default function Main() {
  return (
    <>
      {/* 옆에 네비게이션 있으니까 왼쪽과 하단에 패딩값을 넣음 */}
      <div className="flex justify-center pb-20 md:pb-0 md:pl-20"> 
        <div id="scrollable-container" className="relative h-dvh overflow-y-scroll w-full min-w-80 md:w-[468px]">
          <Routes>
            {/* 경로가 정확히 부모 경로와 일치할 때 홈페이지를 보여줌 */}
            <Route index element={<HomePage/>} />
            <Route path="post" element={<PostCreatePage/>} />        
            <Route path="search" element={<SearchPage/>} />        
            <Route path="donate" element={<DonatePage/>} />        
            <Route path="ranking" element={<RankingPage/>} />        
            <Route path="mypage" element={<MyPage/>} />

            <Route path="donate/research/:keyword" element={<DonationSearchResultPage/>}/>
            <Route path="donate/:donationId" element={<DonationDetailPage/>} />
          </Routes>
        </div>
      <Navbar/>
      </div>
    </>
  )
}
