import { AnimatePresence, motion } from "framer-motion";
import { HamburgerProps } from "../Hamburger";
import { useEffect, useState } from "react";
import { getCategories, subscribeCategory } from "@/apis/board";
import { HeartDefaultIcon, HeartFilledIcon } from "@/assets/images";

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

        const response = await subscribeCategory(categoryId);
        console.log(response);
        console.log(categories);
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
                    className="fixed md:absolute z-50 w-full h-full bg-[#eeeeee]"
                >
                <div className="relative w-full h-full flex flex-col">
                    <div className="relative flex justify-center items-center text-2xl font-bold p-4">
                        구독 목록
                        <div className="absolute left-4 cursor-pointer" onClick={onClose}>↩</div>
                    </div>
                    <div className="flex-1 flex flex-col justify-between text-xl font-semibold my-4">
                        <div className="flex flex-col gap-y-2 px-10">
                            {categories.map((category) => (
                                <div key={category.categoryId} className="flex justify-between py-2">
                                    <div>{category.categoryName}</div>
                                    <img
                                        className="w-6 cursor-pointer"
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