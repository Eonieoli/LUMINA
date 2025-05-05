interface profileBtnProps {
  text: string
  textColor: string
  bgColor: string
}

export default function ProfileBtn ({text, textColor, bgColor}: profileBtnProps) {
  return (
    <div className={`${textColor} ${bgColor} border-2 w-1/3 rounded-[13px] p-1 cursor-pointer text-center text-[15px]`}>
      <button>{text}</button>
    </div>
  )
}