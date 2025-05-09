import DonationCard from './DonationCard';
import { SmileLuna } from '@/assets/images';
import { useState, useEffect } from 'react';
import { getFavoriteDonations } from '@/apis/donation';

// 슬라이드 swiper 관련
import { Swiper, SwiperSlide } from 'swiper/react';
import 'swiper/css';
import 'swiper/css/pagination';
import { Pagination } from 'swiper/modules';

interface FavoriteDonationList {
    donationId: number;
    donationName: string;
}

export default function FavoriteDonationList() {
    const [donations, setDonations] = useState<FavoriteDonationList[]>([]);

    useEffect(() => {
        const fetchFavoriteDonations = async () => {
            const response = await getFavoriteDonations();
            console.log("관심 기부처 가져오기 성공!", response.user)
            setDonations(response.user);
        };
        fetchFavoriteDonations();
    }, []);

    // 4개 카드를 슬라이드 하나에 넣기
    const slides = [];
    for (let i = 0; i < donations.length; i += 4) {
        slides.push(donations.slice(i, i + 4));
    }

    return (
        <div className="flex flex-col gap-4">
            {/* 관심기부처 텍스트 영역 */}
            <div className="flex flex-col items-start gap-2">
                <div className="flex items-center gap-3">
                    <img
                        src={SmileLuna}
                        alt="관심있는 기부처"
                        className="justufy-center w-[40px]"
                    />
                    <p className="text-[20px] font-bold">관심있는 기부처</p>
                </div>
                <p className="text-[15px] text-gray-600">
                    여러분의 따뜻한 선행이 세상을 바꿔요!
                </p>
            </div>

            {/* 슬라이더 */}
            {slides.length === 0 ? (
                <div className="w-full h-20 flex items-center justify-center text-gray-400 text-[15px]">
                기부처를 구독해보세요!
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
