import { AngryLuna, SmileLuna } from "@/assets/images"


interface PositivenessChartProps {
  positiveness?: number
}

const messages = {
  positive: {
    message: "당신의 따뜻한 영향력이 세상에 퍼지고 있어요!",
    character: SmileLuna
  },
  negative: {
    message: "조금 더 따뜻한 행동이 필요한 순간이에요!",
    character: AngryLuna
  }
}
export default function PositivenessChart ({positiveness = 0}: PositivenessChartProps) {

  // 양수인지 음수인지 판단
  const isPositive = positiveness >= 0
  const messageData = isPositive ? messages.positive : messages.negative

  return (
    <div className="text-center p-4 flex flex-col items-center ml-4 mr-4 bg-white">

      {/* 말풍선 */}
      <div
        className={`p-1 pl-5 pr-5 rounded-full text-[14px] text-white text-center ${isPositive ? 'bg-[#9C97FA]' : 'bg-red-300 '} `}
      >
        {messageData.message}
      </div>

      {/* 하단 그래프 */}
      <div className="flex items-center gap-5 w-full mt-2">

        {/* 부정zone */}
        <div className="w-full border-2 border-gray-300 rounded-full h-4 flex justify-end">
          <div
            style={{
              width: positiveness < 0 ? `${Math.min(100, Math.abs(positiveness))}%` : '0%'
            }}
            className={`bg-red-300 h-full rounded-full transition-all duration-500 ease-in-out`}
          />
        </div>

        {/* 캐릭터*/}
        <div className="flex justify-center items-center">
          <img src={messageData.character} alt="Character" className="w-40" />
        </div>

        {/* 긍정zone */}
        <div className="w-full border-2 border-gray-300 rounded-full h-4">
          <div
            style={{
              width: positiveness >= 0 ? `${Math.min(100, Math.abs(positiveness))}%` : '0%'
            }}
            className={`bg-[#9C97FA] h-full rounded-full transition-all duration-500 ease-in-out`}
          />
        </div>
      </div>
    </div>
  )
}