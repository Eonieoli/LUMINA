import { AnimatePresence, motion } from "framer-motion";
import { HamburgerProps } from "../Hamburger";
import { useEffect, useState } from "react";
import { getUserReward } from "@/apis/donation";
import { BackIcon, CoinIcon } from "@/assets/images";

interface Reward {
    postId: number;
    commentId: number;
    content: string;
    point: number;
    positiveness: null;
    createdAt: string;
}

export default function Rewards({isVisible, onClose}: HamburgerProps) {
    const [rewards, setRewards] = useState<Reward[]>([]);

    useEffect(() => {
        document.body.style.overflow = 'hidden';
        const fetchRewards = async () => {
            const response = await getUserReward();
            setRewards(response.data);
        }
        
        fetchRewards();
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
                        리워드 내역
                        <img src={BackIcon} className="absolute w-5 left-6 cursor-pointer" onClick={onClose}/>
                    </div>

                    <div className="flex-1 flex flex-col overflow-y-auto">
                        <div className="flex flex-col gap-y-2 px-4">
                            {rewards.map((reward) => (
                                <div key={reward.createdAt} className="items-center p-2 border-b border-gray-300">

                                    {/* 안에 컨텐츠 내용 */}
                                    <div className="w-full">

                                        {/* 댓글이랑 타임 스템프 */}
                                        <div className="flex gap-2 items-center mb-2">
                                            <div className="bg-[#9C97FA] p-1 pr-3 pl-3 rounded-2xl">{reward.commentId ? 
                                                <div className="text-white">댓글</div> : 
                                                <div className="text-white">게시글</div>}
                                            </div>
                                            <p>{reward.createdAt.split("T")[0]}</p>      
                                        </div>

                                        <div className="truncate pl-2">{reward.content}</div>
                                        
                                        {reward.positiveness !== 0 && reward.positiveness !== null &&
                                            <div className="text-end text-[14px] text-gray-600 ">선행도 {reward.positiveness}</div>
                                        }

                                        {reward.point !== 0 && reward.point !== null ?  (
                                            <div className="flex gap-2 justify-end text-xl font-semibold">
                                                + {reward.point} 
                                                <img src={CoinIcon} alt="코인 아이콘" className="w-5 object-contain" />
                                            </div>
                                            ) : (
                                            <div className="flex gap-2 justify-end text-xl font-semibold">
                                            + 0 
                                            <img src={CoinIcon} alt="코인 아이콘" className="w-5 object-contain" />
                                            </div>
                                            )
                                        }
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