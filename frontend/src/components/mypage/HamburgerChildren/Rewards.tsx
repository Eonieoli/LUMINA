import { AnimatePresence, motion } from "framer-motion";
import { HamburgerProps } from "../Hamburger";
import { useEffect, useState } from "react";
import { getUserReward } from "@/apis/donation";

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
        const fetchRewards = async () => {
            const response = await getUserReward();
            setRewards(response.data);
        }

        fetchRewards();
    }, [])

    return (
        <AnimatePresence>
            {isVisible && (
                <motion.div
                    initial={{ opacity: 0, x: 100 }} // 처음 상태
                    animate={{ opacity: 1, x: 0 }}  // 등장 시
                    exit={{ opacity: 0, x: 100 }}    // 사라질 때
                    transition={{ duration: 0.4 }}
                    className="fixed md:absolute z-50 w-full h-full bg-[#eeeeee]"
                >
                <div className="relative w-full h-full flex flex-col">
                    <div className="relative flex justify-center items-center text-2xl font-bold p-4">
                        리워드 내역
                        <div className="absolute left-4 cursor-pointer" onClick={onClose}>↩</div>
                    </div>
                    <div className="flex-1 flex flex-col justify-between text-lg font-semibold my-4 overflow-y-auto">
                        <div className="flex flex-col gap-y-2 px-4">
                            {rewards.map((reward) => (
                                <div key={reward.createdAt} className={`flex justify-between items-center p-2 border rounded-lg ${reward.commentId ? 'bg-pink-100' : 'bg-yellow-100'}`}>
                                    <div className="w-3/4">
                                        <div>{reward.commentId ? <div>댓글</div> : <div>게시글</div>}</div>
                                        <div className="truncate">{reward.content}</div>
                                    </div>
                                    {reward.point > 0 && <div>+{reward.point}</div>}
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