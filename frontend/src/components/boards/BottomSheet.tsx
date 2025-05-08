import { motion, AnimatePresence } from "framer-motion";
import { useEffect } from 'react';

interface BottomSheetProps {
    onClose: () => void;
    children: React.ReactNode;
    isVisible: boolean;
}

export const BottomSheet = ({ onClose, children, isVisible }: BottomSheetProps) => {
    useEffect(() => {
        document.body.style.overflow = 'hidden';
        return () => {
            document.body.style.overflow = '';
        };
    }, []);

    return (
        <div
            className="bg-opacity-50 fixed inset-0 z-50 flex items-end justify-center bg-[#00000050]"
            onClick={onClose}
        >
            <AnimatePresence>
                {isVisible && (
                    <motion.div
                    initial={{ opacity: 0, y: 100 }} // 처음 상태
                    animate={{ opacity: 1, y: 0 }}  // 등장 시
                    exit={{ opacity: 0, y: 100 }}    // 사라질 때
                    transition={{ duration: 0.5 }}
                    className="your-class"
                    >
                        <div
                            className="max-h-[80%] w-full overflow-y-auto rounded-t-2xl bg-white p-4"
                            onClick={(e) => e.stopPropagation()}
                        >
                            <div className="mx-auto mb-3 h-1.5 w-12 rounded-full bg-gray-300" />
                            {children}
                        </div>
                    </motion.div>
                )}
            </AnimatePresence>
        </div>
    );
};
