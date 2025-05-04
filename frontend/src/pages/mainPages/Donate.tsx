import { useState } from 'react';
import DonateSearchBar from '@/components/donate/SearchBar';
import FavoriteDonationList from '@/components/donate/FavoriteDonationList';
import RecommendDonationList from '@/components/donate/RecommendDonationList';
import PointInfo from '@/components/donate/PointInfo';
import SearchModal from '@/components/donationSearch/SearchModal';

export default function DonatePage() {

    // 검색창 버튼 클릭시 보일 검색 모달창 상태 관리
    const [isSearchOpen, setIsSearchOpen] = useState(false);

    return (
        <div className="relative flex w-full flex-col px-6 py-6">
            {/* 검색 모달창 */}
            {isSearchOpen && (
                <SearchModal onClose={() => setIsSearchOpen(false)} />
            )}

            {/* 코인 조회 */}
            <div className='mb-6'>
                <PointInfo/>
            </div>

            {/* 검색창 */}
            <div className="mb-8 w-full" onClick={() => setIsSearchOpen(true)}>
                <DonateSearchBar
                    keyword=""
                    setKeyword={() => {}}
                    onSearchClick={() => {}}
                />
            </div>

            {/* 추천 기부처 */}
            <div className="mb-6 w-full">
                <RecommendDonationList />
            </div>

            {/* 관심 기부처 */}
            <div className="mb-6 w-full">
                <FavoriteDonationList />
            </div>
        </div>
    );
}
