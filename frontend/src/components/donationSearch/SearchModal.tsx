// 기부처 페이지에서 검색창을 누르면 보일 검색 모달창
import { useState } from "react"
import DonateSearchBar from "../donate/SearchBar"
import { getSearchDonations } from "@/apis/donation"
getSearchDonations
import BackIcon from '@/assets/images/donate/Ic_back.svg'
import { useNavigate } from "react-router-dom"

interface SearchModalProps {
  onClose: () => void
}
export default function SearchModal({onClose}: SearchModalProps){

  const [keyword, setKeyword] = useState("")
  const [donations, setDonations] = useState([])
  const navigate = useNavigate()

  // 돋보기 버튼을 클릭했을 때 기부처 검색 결과 페이지로 이동
  const handleSearchClick = () => {
    const finalkeyword = keyword.trim() === "" ? "donations" : encodeURIComponent(keyword)
    onClose() 
    navigate(`/donate/research/${finalkeyword}`)
  }

  // 뒤로가기 버튼 클릭했을 때 검색창 초기화하기
  const handleClose = () => {
    setKeyword("")
    setDonations([])
    onClose()
  }

  return (
    <div className="absolute top-0 left-0 w-full h-full z-40 px-6 py-6 bg-amber-200">
      <div className="flex justify-start items-center mb-6 h-6">
        <img 
          src={BackIcon} 
          alt="BackIcon" 
          className="w-5 mr-2"
          onClick={handleClose}
        />
      </div>
      <DonateSearchBar 
        keyword={keyword}
        setKeyword={setKeyword}
        onSearchClick={handleSearchClick}
        autoFocus={true}
      />
    </div>
  )
}