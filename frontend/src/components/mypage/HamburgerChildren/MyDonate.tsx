import { getUserDonations } from "@/apis/donation";
import { HamburgerProps } from "../Hamburger";
import { useEffect, useState } from "react";
import { AnimatePresence, motion } from "framer-motion";
import { BackIcon, CoinIcon } from "@/assets/images";

interface Donations {
    donationId: number;
    donationName: string;
    donationCnt: number;
    donationPoint: number;
    createdAt: string;
}

export default function MyDonate({isVisible, onClose}: HamburgerProps) {
    const [donations, setDonations] = useState<Donations[]>([]);

    useEffect(() => {
        document.body.style.overflow = 'hidden';
        const fetchDonations = async () => {
            const response = await getUserDonations();
            setDonations(response.data);
            console.log(response.data)
        }
        
        fetchDonations();
        return () => {
            document.body.style.overflow = '';
        };
    }, [])
    
    return (
        <AnimatePresence>
            {isVisible && (
                <motion.div
                    initial={{ opacity: 0, x: 100 }} // 처음 상태
                    animate={{ opacity: 1, x: 0 }}  // 등장 시
                    exit={{ opacity: 0, x: 100 }}    // 사라질 때
                    transition={{ duration: 0.4 }}
                    className="fixed md:absolute z-50 w-full h-full bg-[#ffffff]"
                >
                <div className="relative w-full h-full flex flex-col">
                    <div className="relative flex justify-center items-center text-xl text-gray-700 font-semibold p-6">
                        기부 내역
                        <img src={BackIcon} className="absolute w-5 left-6 cursor-pointer" onClick={onClose}/>
                    </div>
                    <div className="flex-1 flex flex-col overflow-y-auto">
                        <div className="flex flex-col gap-y-2 px-4">
                            {donations.map((donation) => (
                                <div key={donation.createdAt} className="items-center p-2 border-b border-gray-300">

                                    {/* 안에 컨텐츠 내용 */}
                                    <div className="flex justify-between w-full">

                                        {/* 댓글이랑 타임 스템프 */}
                                        <div className="flex flex-col gap-2">
                                            <div className="font-bold text-xl">
                                                {donation.donationName}
                                            </div>
                                            <p>{donation.createdAt.split("T")[0]}</p>      
                                        </div>
                                        
                                        <div className="flex flex-col gap-2">
                                            <div className="text-end text-[14px] text-gray-600 ">총 {donation.donationCnt} 회</div>
                                            <div className="flex gap-2 justify-end text-xl font-semibold">
                                                {donation.donationPoint} 
                                                <img src={CoinIcon} alt="코인 아이콘" className="w-5 object-contain" />
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            )
                            )}
                        </div>
                    </div>
                </div>
                    
                </motion.div>
            )}
        </AnimatePresence>
    )
}