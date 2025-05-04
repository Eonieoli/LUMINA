import { CoinIcon, WinkLuna } from "@/assets/images"
import { useEffect, useState } from "react"
import DonationLayout from "./DonationLayout"
import { useNavigate, useParams } from "react-router-dom"
import { getDonationDetail } from "@/apis/donation"
import ConfirmDonationModal from "@/components/donationSearch/ConfirmDonationModal"
import { donate } from "@/apis/donation"

export default function DonationInputPage() {

  const {donationId} = useParams() // string값 반환

  const [donationName, setDonationName] = useState("")
  const [point, setPoint] = useState<string>("") // HTML의 input 요소는 항상 문자열로 값을 반환하기 때문에

  const navigate = useNavigate()

  // 확인버튼 클릭시 확인모달창 나오기
  const [isModalOpen, setIsModalOpen] = useState(false)
  
  // useParams로 가져온 id를 통해 해당 기부처 정보 가져오기
  useEffect(() => {
    const fetchDonationDetail = async() => {
      if(!donationId) return

      try {
        const response = await getDonationDetail(donationId)
        setDonationName(response.donationName)
      }
      catch(error){}
    }
    fetchDonationDetail()
  },[donationId])

  //확인모달창에서 기부하기를 눌렀을 때
  const handdleDonate = async () => {
    if(!donationId || !point) return 

    // api 연결되면 순서 바꾸기
    try {
      setIsModalOpen(false)
      navigate(`/donate/thanks`, {state: {donationId}},)
      await donate(Number(donationId), Number(point))
    }
    catch(error){}
  }

  return (

    <DonationLayout
      bottomButton = {
        <button className="w-full h-full relative" onClick={() => setIsModalOpen(true)}>확인</button>
      }
      isModalOpen={isModalOpen}
    >
      <div className="w-full h-full flex justify-center">
        <div className="flex flex-col justify-center items-center" >

          {/* 안내 멘트 */}
          <div className="text-[20px] font-medium text-gray-600 mt-10">
            <p>
              기부하실
              <span className="font-bold text-[#5D56F1]">포인트</span>
              <span className="font-bold">를 입력</span>
              해주세요!
            </p>
          </div>

          {/* 루미나 이미지 */}
            <img src={WinkLuna} alt="윙크하는루미나" className="w-50 mt-15 mb-15"/>  
          
          {/* 포인트 입력창 */}
          <div className="flex justify-center items-center w-2/5 pb-0.5 text-gray-400">
            <input 
              type="text" 
              value={point}
              onChange={(e) => {setPoint(e.target.value)}}
              placeholder="100"
              className="text-2xl font-semibold text-center outline-none w-full mr-1"
            />
            <img src={CoinIcon} alt="코인" className="w-6" />
          </div>

          {/* 확인 모달창 */}
          {isModalOpen && (
            <ConfirmDonationModal
              donationName = {donationName}
              point = {point}
              onCancel = {() => setIsModalOpen(false)}
              onConfirm = {handdleDonate}
            />
          )}
        </div>
      
      </div>

    </DonationLayout>
  )
}