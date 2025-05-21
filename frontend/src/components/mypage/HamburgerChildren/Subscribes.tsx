import { AnimatePresence, motion } from "framer-motion";
import { HamburgerProps } from "../Hamburger";
import { useEffect, useState } from "react";
import { getCategories, subscribeCategory } from "@/apis/board";
import { BackIcon, HeartDefaultIcon, HeartFilledIcon } from "@/assets/images";

interface category {
    categoryId: number;
    categoryName: string;
    isSubscribe: boolean;
}

export default function Subscribes({isVisible, onClose}: HamburgerProps) {
    const [categories, setCategories] = useState<category[]>([]);

    const heartClick = async (categoryId: number) => {
        setCategories((prev) =>
            prev.map((cat) =>
                cat.categoryId === categoryId
                    ? { ...cat, isSubscribe: !cat.isSubscribe }
                    : cat
            )
        );

        subscribeCategory(categoryId);
    }
    
    useEffect(() => {
        const fetchCategories = async () => {
            const response = await getCategories();
            setCategories(response.data);
        }

        fetchCategories();
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
                    <div className="relative flex justify-center items-center text-xl font-semibold p-6 text-gray-700">
                        구독 목록
                        <img src={BackIcon} className="absolute w-5 left-6 cursor-pointer" onClick={onClose}/>
                    </div>

                    {/* 카테고리 목록 */}
                    <div className="flex-1 flex flex-col justify-between text-xl font-medium text-gray-700 ">
                        <div className="flex flex-col px-4 py-5 border-2 mx-6 rounded-2xl border-gray-200">
                            {categories.map((category) => (
                                <div key={category.categoryId} className="flex justify-between py-2">
                                    <div>{category.categoryName}</div>
                                    <img
                                        className="h-6 cursor-pointer"
                                        onClick={() => heartClick(category.categoryId)}
                                        src={
                                            category.isSubscribe
                                                ? HeartFilledIcon
                                                : HeartDefaultIcon
                                        }
                                        alt="좋아요"
                                    />
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