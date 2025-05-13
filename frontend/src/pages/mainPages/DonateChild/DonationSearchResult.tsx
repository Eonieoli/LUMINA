import { useEffect, useState } from 'react';
import SearchModal from '@/components/donationSearch/SearchModal';
import DonateSearchBar from '@/components/donate/SearchBar';
import { useParams, useLocation, useNavigate } from 'react-router-dom';
import { getAllDonations, getSearchDonations } from '@/apis/donation';
import DonationCard from '@/components/donate/DonationCard';
import { DonationProps } from '@/components/donate/DonationCard';
import { DownIcon } from '@/assets/images';
import DonationLayout from './DonationLayout';

export default function DonationSearchResultPage() {

    const navigate = useNavigate()
    const location = useLocation()
    const state = location.state as { type?: 'all' | 'search'}

    const [isSearchOpen, setIsSearchOpen] = useState(false);

    const { keyword } = useParams();
    const decodedKeyword =
        keyword === 'donations' ? 'all' : decodeURIComponent(keyword || '');
    const [donations, setDonations] = useState<DonationProps[]>([]);
    const [pageNum, setPageNum] = useState(1);
    const [totalPages, setTotalPages] = useState(1);

    // 검색어가 변경이 되면 페이지 번호를 1로 초기화
    useEffect(() => {
        console.log("기부 검색 변경됨!!")
        setPageNum(1);
        setDonations([]);
    }, [decodedKeyword]);

    useEffect(() => {
        fetchDonations()
    },[decodedKeyword, pageNum])

    const fetchDonations = async () => {
        try {
            if (state?.type === 'all' || decodedKeyword === 'all') {
                const {donations, totalPages} = await getAllDonations(pageNum)
                console.log("❤️전체 기부처 검색!!!",pageNum,"/", totalPages) 
                setTotalPages(totalPages);
                setPageNum(pageNum)
                setDonations(prev => (pageNum === 1? donations : [...prev, ...donations]) )
            } else {
                console.log("검색 api 호출 시작!!!")
                const {donations: newDonations, totalPages} = await getSearchDonations(decodedKeyword, pageNum)
                setTotalPages(totalPages);
                setPageNum(pageNum)
                setDonations((prev) => {
                    const existingIds = prev.map(
                        (donation) => donation.donationId
                    );
                    // 페이지네이션으로 추가되는 기부처들
                    const uniqueNewDonations = newDonations.filter(
                        (donation: DonationProps) =>
                            !existingIds.includes(donation.donationId)
                    );
                    // 기존 기부처 + 추가된 기부처
                    return [...prev, ...uniqueNewDonations];
                });
            }
            console.log("❣️검색 완료", pageNum, totalPages)
        } catch (error) {
            console.log('기부처 검색 실패', error);
        }
    };

    // 더보기를 클릭했을 때
    const handleLoadMore = () => {
        if (pageNum < totalPages) {
            setPageNum((prev) => prev + 1);
            console.log("더보기 클릭! 다음 페이지로 이동!", pageNum, totalPages)
        }
    };

    return (

        <DonationLayout>

            {/* 검색 모달창 */}
            {isSearchOpen && (
                <SearchModal onClose={() => setIsSearchOpen(false)} />
            )}

            <div className="relative flex w-full flex-col">

                {/* 검색창 */}
                <div className="mb-8 w-full" onClick={() => setIsSearchOpen(true)}>
                    <DonateSearchBar
                        keyword=""
                        setKeyword={() => {}}
                        onSearchClick={() => {
                            if (!decodedKeyword.trim()) return
                            navigate(`/donate/research/${encodeURIComponent(decodedKeyword)}`, {
                                state: {type: 'search'},
                            })
                        }}
                    />
                </div>

                {/* 만약 빈 검색창 or 전체조회라면 */}
                {decodedKeyword.trim() === '' && (
                    <div className="mb-8 text-center text-sm text-gray-500">
                        기부처를 검색해보세요!
                    </div>
                )}

                {/* 결과 */}
                    <div className="grid grid-cols-2 gap-4 pb-8">
                        {donations.length > 0 ? (
                            donations.map((donation) => (
                                <DonationCard
                                    key={donation.donationId}
                                    donationId={donation.donationId}
                                    donationName={donation.donationName}
                                />
                            ))
                        ) : (
                            <p className="col-span-2 flex items-center justify-center text-sm text-gray-500">
                                존재하지 않는 기부처입니다.
                            </p>
                        )}
                    </div>

                {/* 더보기 버튼 */}
                {pageNum < totalPages && (
                    <div className="mt-4 flex items-center justify-center">
                        <button
                            onClick={handleLoadMore}
                            className="flex flex-col items-center justify-center cursor-pointer"
                        >
                            <p className="text-gray-500">더보기</p>
                            <img src={DownIcon} alt="더보기" className="h-5 w-5 mb-10" />
                        </button>
                    </div>
                )} 

            </div> 
        </DonationLayout>

    );
}
