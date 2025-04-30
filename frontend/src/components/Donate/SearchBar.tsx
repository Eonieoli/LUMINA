import { SearchIconGray } from "@/assets/images"

interface DonationSearchProps {
  keyword: string
  setKeyword: (value: string) => void
  onSearchClick: () => void
  autoFocus?: boolean
}

export default function DonateSearchBar({keyword, setKeyword, onSearchClick, autoFocus = false}: DonationSearchProps) {
  return (
    <div className="flex items-center w-full h-7.5 border border-gray-300 rounded-3xl">
      <input 
        type="text"
        placeholder="기부처를 검색해보세요."
        value={keyword}
        onChange={(e) => setKeyword(e.target.value)}  
        className="flex-1 outline-none text-sm ml-4"
        autoFocus={autoFocus}
      />
      <img 
        src={SearchIconGray} 
        alt="searchIcon" 
        className="w-3 mr-4 text-gray-300" 
        onClick={onSearchClick}
      />
    </div>
  )
}