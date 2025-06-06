import { motion, AnimatePresence } from "framer-motion";
import { useEffect, useState } from "react";
import { signOut } from "@/apis/auth";
import { Rewards, Subscribes } from './HamburgerChildren'
import { useLocation, useNavigate } from "react-router-dom";
import { XIcon } from "@/assets/images";
import MyDonate from "./HamburgerChildren/MyDonate";

export interface HamburgerProps {
    isVisible: boolean;
    onClose: () => void;
}

export default function Hamburger({isVisible, onClose}: HamburgerProps) {
    const [isSubscribeOpened, setIsSubscribeOpened] = useState(false);
    const [isRewardsOpened, setIsRewardsOpened] = useState(false);
    const [isMyDonateOpened, setIsMyDonateOpened] = useState(false);
    const [isSubscribeAnimation, setIsSubscribeAnimation] = useState(false);
    const [isRewardsAnimation, setIsRewardsAnimation] = useState(false);
    const [isMyDonateAnimation, setIsMyDonateAnimation] = useState(false);
    const location = useLocation();
    const navigate = useNavigate();
    const state = location.state;

    const handleSignOut = async () => {
        await signOut();
        navigate('/login');
        
    }

    const handleClose = (name: string) => {
        setIsSubscribeOpened(false);
        setIsRewardsOpened(false);
        setIsMyDonateOpened(false);
        setTimeout(() => {
            if (name == 'sub') {
                setIsSubscribeAnimation(false);
            } else if (name == 're') {
                setIsRewardsAnimation(false);
            } else if (name == 'don') {
                setIsMyDonateAnimation(false);
            }
        }, 400);
    }

    useEffect(() => {
        if (state?.from === 'search') {
            setIsSubscribeAnimation(true);
            setIsSubscribeOpened(true);
        }
    }, [])

    useEffect(() => {
        setTimeout(() => {
            const scrollContainer = document.getElementById('scrollable-container');
    
            if (!scrollContainer) return;
    
            if (isVisible) {
                scrollContainer.style.overflow = 'hidden';
            } else {
                scrollContainer.style.overflow = 'auto';
            }

            return () => {
                scrollContainer.style.overflow = 'auto';
            };
        }, 500);
    }, [isVisible]);
    
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
                            설정
                            <img  src={XIcon}className=" w-4 absolute left-6 cursor-pointer" onClick={onClose}/>
                        </div>
                        <div className="flex-1 flex flex-col justify-between text-[18px] font-medium">
                            <div className="flex flex-col gap-2 text-gray-600">
                                <div onClick={() => {
                                    setIsMyDonateOpened(true)
                                    setIsMyDonateAnimation(true)}}
                                    className="p-2 px-10 cursor-pointer"
                                >
                                    내 기부 내역
                                </div>
                                <div onClick={() => {
                                    setIsRewardsOpened(true)
                                    setIsRewardsAnimation(true)}}
                                    className="p-2 px-10 cursor-pointer"
                                >
                                    리워드 내역
                                </div>
                                <div onClick={() => {
                                    setIsSubscribeOpened(true)
                                    setIsSubscribeAnimation(true)}}
                                    className="p-2 px-10 cursor-pointer"
                                >
                                    구독 카테고리
                                </div>
                            </div>
                            <div onClick={handleSignOut} className="p-4 px-10 text-right text-xs text-gray-400 cursor-pointer">로그아웃</div>
                        </div>
                        {isRewardsAnimation && (<Rewards isVisible={isRewardsOpened} onClose={() => handleClose('re')} />)}
                        {isSubscribeAnimation && (<Subscribes isVisible={isSubscribeOpened} onClose={() => handleClose('sub')} />)}
                        {isMyDonateAnimation && (<MyDonate isVisible={isMyDonateOpened} onClose={() => handleClose('don')} />)}
                    </div>
                </motion.div>
            )}
        </AnimatePresence>
    )
}