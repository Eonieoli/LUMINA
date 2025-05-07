interface userProfileFourInfoProps {
  title:string
  titleNumber:number
}

export default function UserProfileFourInfo ({title, titleNumber}: userProfileFourInfoProps) {
  return (
    <div className="flex flex-col items-center">
      <p className="text-[15px] font-semibold">{titleNumber}</p>
      <p className="text-[14px]">{title}</p>
    </div>
  )
}