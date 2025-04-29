import { Route, Routes } from "react-router-dom"
import { HomePage, SearchPage, DonatePage, RankingPage, MyPage} from "@/pages/mainPages"
import Navbar from "@/components/Navbar"

export default function Main() {
  return (
    <>
      {/* 옆에 네비게이션 있으니까 왼쪽과 하단에 패딩값을 넣음 */}
      <div className="flex justify-center pb-20 md:pb-0 md:pl-20"> 
        <div className="w-full md:w-[468px]">
          <Routes>
            {/* 경로가 정확히 부모 경로와 일치할 때 홈페이지를 보여줌 */}
            <Route index element={<HomePage/>} />
            <Route path="search" element={<SearchPage/>} />        
            <Route path="donate" element={<DonatePage/>} />        
            <Route path="ranking" element={<RankingPage/>} />        
            <Route path="mypage" element={<MyPage/>} />
          </Routes>
        </div>
      </div>
      <Navbar/>
    </>
  )
}