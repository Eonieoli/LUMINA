import DonationCard from './DonationCard';
import { PokerLuna, ComputerHandsupLuna } from '@/assets/images';
// 슬라이드 swiper 관련
import { Swiper, SwiperSlide } from 'swiper/react';
import 'swiper/css';
import 'swiper/css/pagination';
import { Pagination } from 'swiper/modules';
import { useEffect, useState } from 'react';
import { getFavoriteDonations } from '@/apis/donation';
import { useNavigate } from 'react-router-dom';

interface RecommendDonationListProps {
    donationId: number;
    donationName: string;
}

export default function RecommendDonationList() {

    const [donations, setDonations] = useState<RecommendDonationListProps[]>([])
    const navigate = useNavigate()

    useEffect(() => {
        const fetchRecommendDonations = async () => {
            const response = await getFavoriteDonations()
            console.log("추천 기부처 가져오기 성공!", response.ai)
            setDonations(response.ai)
        }
        fetchRecommendDonations()
    },[])

    // 2개 카드를 슬라이드 하나에 넣기
    const slides = [];
    for (let i = 0; i < donations.length; i += 2) {
        slides.push(donations.slice(i, i + 2));
    }

    // 메인페이지로 이동
    const goToMainPage = () => { 
        navigate('/')
    }

    return (
        <div className="flex flex-col gap-4">
            {/* 추천 기부처 텍스트 영역 */}
            <div className="flex flex-col items-start gap-2">
                <div className="flex items-center gap-3">
                    <img
                        src={PokerLuna}
                        alt="추천 기부처"
                        className="justufy-center w-[40px]"
                    />
                    <p className="text-[20px] font-bold">이런 곳은 어때요?</p>
                </div>
                <p className="text-[15px] text-gray-600">
                    AI가 여러분의 관심에 따라 기부처를 추천해드릴게요!
                </p>
            </div>

            {/* 추천 기부처 목록 */}
            {slides.length === 0 ? (
                <div className='flex flex-col items-center cursor-pointer' onClick={goToMainPage} >
                    <img src={ComputerHandsupLuna} alt="기부처 루나" className='w-2/4' />
                    <div className="w-full h-20 flex items-center justify-center text-gray-400 text-[15px]">
                    게시글에 좋아요를 눌러보세요!
                    
                    </div>

                </div>                
            ) : (
                <Swiper
                    pagination={{ clickable: true }}
                    modules={[Pagination]}
                    className="mySwiper w-full"
                >
                    {slides.map((group, index) => (
                        <SwiperSlide key={index}>
                            <div className="grid grid-cols-2 gap-4 pb-10">
                                {group.map((donation) => (
                                    <DonationCard
                                        key={donation.donationId}
                                        donationId={donation.donationId}
                                        donationName={donation.donationName}
                                    />
                                ))}
                            </div>
                        </SwiperSlide>
                    ))}
                </Swiper>
            )}
        </div>
    );
}
