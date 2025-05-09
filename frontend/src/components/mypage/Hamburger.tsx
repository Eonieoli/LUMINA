import { motion, AnimatePresence } from "framer-motion";
import { useState } from "react";
import { signOut } from "@/apis/auth";
import { Rewards, Subscribes } from './HamburgerChildren'

export interface HamburgerProps {
    isVisible: boolean;
    onClose: () => void;
}

export default function Hamburger({isVisible, onClose}: HamburgerProps) {
    const [isSubscribeOpened, setIsSubscribeOpened] = useState(false);
    const [isRewardsOpened, setIsRewardsOpened] = useState(false);
    const [isSubscribeAnimation, setIsSubscribeAnimation] = useState(false);
    const [isRewardsAnimation, setIsRewardsAnimation] = useState(false);

    const handleSignOut = async () => {
        await signOut();
    }

    const handleClose = (name: string) => {
        setIsSubscribeOpened(false);
        setIsRewardsOpened(false);
        setTimeout(() => {
            if (name == 'sub') {
                setIsSubscribeAnimation(false);
            } else if (name == 're') {
                setIsRewardsAnimation(false);
            }
        }, 400);
    }
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
                                <div className="p-4 px-10 cursor-pointer">내 기부 내역</div>
                                <div onClick={() => {
                                    setIsRewardsOpened(true)
                                    setIsRewardsAnimation(true)}}
                                    className="p-4 px-10 cursor-pointer"
                                >
                                    리워드 내역
                                    </div>
                                <div onClick={() => {
                                    setIsSubscribeOpened(true)
                                    setIsSubscribeAnimation(true)}}
                                    className="p-4 px-10 cursor-pointer"
                                >
                                    구독 카테고리
                                </div>
                            </div>
                            <div onClick={handleSignOut} className="p-4 px-10 text-right text-xs text-gray-400 cursor-pointer">로그아웃</div>
                        </div>
                        {isRewardsAnimation && (<Rewards isVisible={isRewardsOpened} onClose={() => handleClose('re')} />)}
                        {isSubscribeAnimation && (<Subscribes isVisible={isSubscribeOpened} onClose={() => handleClose('sub')} />)}
                    </div>
                </motion.div>
            )}
        </AnimatePresence>
    )
}