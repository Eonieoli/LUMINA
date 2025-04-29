import { SearchIconGray } from "@/assets/images"

export default function DonateSearchBar() {
  return (
    <div className="flex items-center w-full h-7.5 border border-gray-300 rounded-3xl">
      <input 
        type="text"
        placeholder="기부처를 검색해보세요."  
        className="flex-1 outline-none text-sm ml-4"
      />
      <img src={SearchIconGray} alt="searchIcon" className="w-3 mr-4 text-gray-300" />
    </div>
  )
}