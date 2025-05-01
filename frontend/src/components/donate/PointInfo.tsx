import { useEffect, useState } from "react";
import { CoinIcon } from "@/assets/images";
import { getPointInfo } from "@/apis/donation";

export default function PointInfo() {
  
  const [point, setPoint] = useState<number>(795)

  useEffect(() => {
    const fetchPointInfo = async () => {
      try{
        const response = await getPointInfo()
        setPoint(response)
      }
      catch(error) {
        console.error(error)
      }
    }
    fetchPointInfo()
  },[])

  return (
    <div className="flex justify-end items-center mb-6 h-6">
      <img src={CoinIcon} alt="coinImg" className="w-5 mr-2"/>
      <p className="text-[15px] font-bold">
        {point}
      </p>
  </div>
  )
}