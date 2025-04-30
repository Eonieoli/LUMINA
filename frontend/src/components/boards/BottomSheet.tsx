import { useEffect } from "react";

interface BottomSheetProps {
  onClose: () => void;
  children: React.ReactNode;
}

export const BottomSheet = ({ onClose, children }: BottomSheetProps) => {
  useEffect(() => {
    document.body.style.overflow = "hidden";
    return () => {
      document.body.style.overflow = "";
    };
  }, []);

  return (
    <div
      className="absolute inset-0 z-50 flex justify-center items-end bg-[#00000050] bg-opacity-50"
      onClick={onClose}
    >
      <div
        className="w-full max-h-[80%] bg-white rounded-t-2xl p-4 overflow-y-auto"
        onClick={(e) => e.stopPropagation()}
      >
        <div className="w-12 h-1.5 bg-gray-300 rounded-full mx-auto mb-3" />
        {children}
      </div>
    </div>
  );
};
