import { Route, Routes, useLocation } from "react-router-dom"
import { HomePage, SearchPage, DonatePage, RankingPage, MyPage, PostCreatePage, DonationDetailPage, DonationThanksPage, DonationSearchResultPage, DonationInputPage, FollowPage} from "@/pages/mainPages"
import { Navbar } from "@/components"

export default function Main() {
  const location = useLocation() // 기부처 페이지 컴포넌트 재마운트 용
  
  return (
    <>
      {/* 옆에 네비게이션 있으니까 왼쪽과 하단에 패딩값을 넣음 */}
      <div className="flex justify-center"> 
        <div id="scrollable-container" className="relative h-dvh overflow-y-scroll w-full min-w-80 md:w-[468px] pb-20 md:pb-0 md:pl-20">
          <Routes>
            {/* 경로가 정확히 부모 경로와 일치할 때 홈페이지를 보여줌 */}
            <Route index element={<HomePage/>} />
            <Route path="post" element={<PostCreatePage/>} />        
            <Route path="search" element={<SearchPage/>} />        
            <Route path="donate" element={<DonatePage key={location.key}/>} />        
            <Route path="ranking" element={<RankingPage/>} />        
            <Route path="mypage/:userId?" element={<MyPage/>} />

            <Route path="donate/research/:keyword" element={<DonationSearchResultPage/>}/>
            <Route path="donate/:donationId" element={<DonationDetailPage/>} />
            <Route path="donate/:donationId/point" element={<DonationInputPage/>} />
            <Route path="donate/thanks" element={<DonationThanksPage/>}/>

            <Route path="mypage/:profileUserId/follow" element={<FollowPage/>} /> 
          </Routes>
        </div>
      <Navbar/>
      </div>
    </>
  )
}
