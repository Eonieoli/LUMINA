import { useEffect } from 'react';

interface BottomSheetProps {
    onClose: () => void;
    children: React.ReactNode;
}

export const BottomSheet = ({ onClose, children }: BottomSheetProps) => {
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
            <div
                className="max-h-[80%] w-full overflow-y-auto rounded-t-2xl bg-white p-4"
                onClick={(e) => e.stopPropagation()}
            >
                <div className="mx-auto mb-3 h-1.5 w-12 rounded-full bg-gray-300" />
                {children}
            </div>
        </div>
    );
};
