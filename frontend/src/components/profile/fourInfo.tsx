interface userProfileFourInfoProps {
  title:string
  titleNumber:number
  onClick?: () => void
  isBtn?: boolean
}

export default function UserProfileFourInfo ({title, titleNumber, onClick, isBtn}: userProfileFourInfoProps) {
  return (
    <div className={`flex flex-col items-center {${isBtn} ? 'cursor-pointer' : "" }`} onClick={onClick}>
      <p className="text-[15px] font-semibold">{titleNumber}</p>
      <p className="text-[14px]">{title}</p>
    </div>
  )
}