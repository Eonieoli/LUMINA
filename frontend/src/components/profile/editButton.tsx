interface EditButtonProps {
  text: string
  bgColor: string
  textColor: string
  onClick: () => void
}

export default function EditButton ({text, bgColor, textColor, onClick}: EditButtonProps) {
  return (
      <button 
        className={`${bgColor} ${textColor} p-2 rounded-[10px] w-7/15 cursor-pointer border-2 border-[#9C97FA]`} 
        onClick={onClick}
      >{text}</button>
  )
}