import { SearchIconGray } from "@/assets/images"
import React from "react"

interface DonationSearchProps {
  keyword: string
  setKeyword: (value: string) => void
  onSearchClick: () => void
  autoFocus?: boolean
}

export default function DonateSearchBar({keyword, setKeyword, onSearchClick, autoFocus = false}: DonationSearchProps) {

  // 엔터를 쳐도 검색이 가능하도록
  const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if(e.key === "Enter") {
      onSearchClick()
    }
  }

  return (
    <div className="flex items-center w-full h-9 border border-gray-300 rounded-3xl">
      <input 
        type="text"
        placeholder="기부처를 검색해보세요."
        value={keyword}
        onChange={(e) => setKeyword(e.target.value)}  
        className="flex-1 outline-none text-[16px] ml-4"
        autoFocus={autoFocus}
        onKeyDown={handleKeyDown}
      />
      <img 
        src={SearchIconGray} 
        alt="searchIcon" 
        className="w-3 m-4 text-gray-300" 
        onClick={onSearchClick}
      />
    </div>
  )
}