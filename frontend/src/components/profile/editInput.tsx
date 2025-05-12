interface EditInputProps {
  title: string 
  value: string
  onChange: (e: React.ChangeEvent<HTMLInputElement>) => void
}

export default function EditInput ({title, value, onChange}:EditInputProps) {
  return (
    <div className="flex flex-col mb-1 mt-5">
      <p className="mb-2 ml-2 text-gray-800">{title}</p>
      <div className="w-full h-9 items-center rounded-[10px] border border-gray-300 flex">
        <input type="text" value={value} onChange={onChange}
          className="border-none  outline-none w-full ml-4 mr-4 text-gray-600"
        />
      </div>
    </div>
  )
}