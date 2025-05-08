import { motion, AnimatePresence } from "framer-motion";
import Subscribes from "./HamburgerChildren/Subscribes";
import { useState } from "react";

export interface HamburgerProps {
    isVisible: boolean;
    onClose: () => void;
}

export default function Hamburger({isVisible, onClose}: HamburgerProps) {
    const [isSubscribeOpened, setIsSubscribeOpened] = useState(false);
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
                            설정
                            <div className="absolute left-4 cursor-pointer" onClick={onClose}>✕</div>
                        </div>
                        <div className="flex-1 flex flex-col justify-between text-xl font-semibold my-4">
                            <div className="flex flex-col gap-y-2">
                                <div className="p-4 px-10">내 기부 내역</div>
                                <div className="p-4 px-10">리워드 보상 내역</div>
                                <div onClick={() => setIsSubscribeOpened(true)} className="p-4 px-10">구독 카테고리</div>
                            </div>
                            <div className="p-4 px-10 text-right text-xs text-gray-400">로그아웃</div>
                        </div>
                        {isSubscribeOpened && (<Subscribes isVisible={isSubscribeOpened} onClose={() => setIsSubscribeOpened(!isSubscribeOpened)} />)}
                    </div>
                </motion.div>
            )}
        </AnimatePresence>
    )
}