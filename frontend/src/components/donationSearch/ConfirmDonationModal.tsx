import { CoinIcon } from "@/assets/images"

interface ConfirmDonationModalProps {
  donationName: string
  point: string
  onCancel: () => void
  onConfirm: () => void
}

export default function ConfirmDonationModal({donationName, point, onCancel, onConfirm}: ConfirmDonationModalProps) {
  return (

    <div className="absolute flex items-center justify-center z-40">
      <div className="bg-white p-5 pr-5 pl-5 rounded-2xl">

        <p className="text-[18px] text-center text-gray-600">
          <span className="text-xl font-bold text-[#5D56F1] ">  {donationName}
          </span>
          &nbsp;에 <br/>
          <span className="flex items-center justify-center">
            <span className="font-semibold">
              {point} &nbsp;
            </span>
            <img src={CoinIcon} alt="코인아이콘" className="w-5"/>
            &nbsp;를 기부하시겠습니까?
          </span>
        </p>

        <div className="flex justify-between mt-6">
          <button 
            onClick={onCancel}
            className="w-30 py-2 border-2 border-[#9C97FA] text-[#5D56F1] font-semibold rounded-lg mr-5"
          >취소</button>
          <button 
            onClick={onConfirm}
            className="w-30 py-2 bg-[#9C97FA] text-white font-semibold rounded-lg"
          >기부하기</button>
        </div>

      </div>
    </div>
  )
}