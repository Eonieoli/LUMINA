import { Route, Routes } from "react-router-dom"
import Navbar from "@/components/Navbar"
import HomePage from "./main/Home"
import SearchPage from "./main/Search"
import DonatePage from "./main/Donate"
import RankingPage from "./main/Ranking"
import MyPage from "./main/MyPage"

export default function Main() {
  return (
    <>
      {/* 옆에 네비게이션 있으니까 왼쪽과 하단에 패딩값을 넣음 */}
      <div className="pb-20 md:pl-20"> 
        <Routes>
          {/* 경로가 정확히 부모 경로와 일치할 때 홈페이지를 보여줌 */}
          <Route index element={<HomePage/>} />
          <Route path="search" element={<SearchPage/>} />        
          <Route path="donate" element={<DonatePage/>} />        
          <Route path="ranking" element={<RankingPage/>} />        
          <Route path="mypage" element={<MyPage/>} />
        </Routes>
      </div>
      <div className="min-h-screen relative">
        <Navbar/>
      </div>
    </>
  )
}