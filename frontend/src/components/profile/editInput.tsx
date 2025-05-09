interface EditInputProps {
  title: string 
  value: string
  onChange: (e: React.ChangeEvent<HTMLInputElement>) => void
}

export default function EditInput ({title, value, onChange}:EditInputProps) {
  return (
    <div className="w-2/3">
        <div className="flex flex-col mb-5">
          <p className="mb-2 ml-2 text-gray-800">{title}</p>
          <div className="w-full h-9 items-center rounded-[10px] border border-gray-300 flex">
            <input type="text" value={value} onChange={onChange}
              className="border-none ml-4 text-gray-600"
            />
          </div>
        </div>
    </div>
  )
}