// import { useEffect, useState } from "react"
// import SearchModal from "@/components/donationSearch/SearchModal"
// import PointInfo from "@/components/donate/PointInfo"
// import DonateSearchBar from "@/components/donate/SearchBar"
// import { useParams } from "react-router-dom"
// import { getSearchDonations } from "@/apis/donation"

export default function DonationSearchResultPage() {

  // const [isSearchOpen, setIsSearchOpen] = useState(false)

  // const {keyword} = useParams()
  // const [inputKeyword, setInputKeyword] = useState(keyword === "donations" ? "" : decodeURIComponent(keyword || "" ))
  // const [donations, setDonations] = useState([])
  // const [pageNum, setPageNum] = useState(1)

  // // API 호출하기
  // useEffect(() => {
  //   const fetchDonationResult = async() => {

  //     // 검색어가 없다면 전체 기부처 조회
  //     if(!keyword || keyword === "donations") {
  //       // const response = await getAllDonations(pageNum)
  //       // setDonations(response)
  //     }
  //     // 검색어가 있다면 해당 기부처 조회
  //     else{
  //       const response = await getSearchDonations(decodeURIComponent(keyword),pageNum )
  //       setDonations(response)
  //     }
  //   }
  //   fetchDonationResult()
  // }, [keyword, pageNum])

  // const handleSearch = () => {
  //   const encoded = inputKeyword.trim() === "" ? "donations" : encodeURIComponent(inputKeyword)
  //   window.location.href = `/donate/research/${encoded}`
  // }

  return(
    <div className="relative flex flex-col w-full px-6 py-6 bg-amber-100">

      {/* 검색 모달창 */}
      {/* {isSearchOpen && <SearchModal onClose={() => setIsSearchOpen(false)} /> } */}

      {/* 코인 조회 */}
      {/* <PointInfo /> */}

      {/* 검색창 */}
      {/* <div className="w-full mb-8" onClick={() => setIsSearchOpen(true)}>
        <DonateSearchBar
          keyword=""
          setKeyword={() => {}}
          onSearchClick={()=>{}}
        />
      </div> */}

      {/* 결과 */}
        {/* <p>결과 페이지</p> */}

    </div>
  )
}