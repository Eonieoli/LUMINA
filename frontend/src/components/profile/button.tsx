interface profileBtnProps {
  text: string
  textColor: string
  bgColor: string
  onClick?: () => void
}

export default function ProfileBtn ({text, textColor, bgColor, onClick}: profileBtnProps) {
  return (
    <div 
      onClick={onClick}
      className={`${textColor} ${bgColor} border-2 w-1/3 rounded-[13px] p-1 cursor-pointer text-center text-[15px]`}
    >
      <button className="cursor-pointer">{text}</button>
    </div>
  )
}